package com.bgsoftware.superiorskyblock.hooks;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import com.mojang.authlib.properties.Property;
import net.skinsrestorer.shared.storage.Config;
import net.skinsrestorer.shared.storage.Locale;
import net.skinsrestorer.shared.storage.MySQL;
import org.bukkit.Bukkit;
import skinsrestorer.bukkit.SkinsRestorer;
import skinsrestorer.shared.exception.SkinRequestException;
import skinsrestorer.shared.storage.SkinStorage;

public final class SkinsRestorerHook {

    private static SuperiorSkyblockPlugin plugin;
    private static ISkinsRestorer skinsRestorer = null;

    public static void setSkinTexture(SuperiorPlayer superiorPlayer){
        if(Bukkit.isPrimaryThread()){
            Executor.async(() -> setSkinTexture(superiorPlayer));
            return;
        }

        Property property = skinsRestorer.getSkin(superiorPlayer);
        if(property != null)
            Executor.sync(() -> plugin.getNMSAdapter().setSkinTexture(superiorPlayer, property));
    }

    public static boolean isEnabled() {
        return skinsRestorer != null;
    }

    public static void register(SuperiorSkyblockPlugin plugin){
        SkinsRestorerHook.plugin = plugin;
        try{
            Class.forName("net.skinsrestorer.bukkit.SkinsRestorer");
            skinsRestorer = new SkinsRestorerNew();
        }catch (Exception ex){
            skinsRestorer = new SkinsRestorerOld();
        }
    }

    interface ISkinsRestorer {

        Property getSkin(SuperiorPlayer superiorPlayer);

    }

    private static final class SkinsRestorerOld implements ISkinsRestorer {

        @Override
        public Property getSkin(SuperiorPlayer superiorPlayer) {
            try {
                SkinStorage skinStorage = SkinsRestorer.getInstance().getSkinStorage();
                return (Property) skinStorage.getOrCreateSkinForPlayer(superiorPlayer.getName(), true);
            }catch (SkinRequestException | NullPointerException ex){
                return null;
            }
        }

    }

    private static final class SkinsRestorerNew implements ISkinsRestorer {

        private final net.skinsrestorer.bukkit.SkinsRestorer instance;

        public SkinsRestorerNew(){
            instance = net.skinsrestorer.bukkit.SkinsRestorer.getInstance();
            if(instance.isBungeeEnabled()){
                // We want to adjust config options to also work if bungee mode is enabled

                Config.load(instance.getConfigPath(), instance.getResource("config.yml"));
                Locale.load(instance.getConfigPath());

                if (Config.MYSQL_ENABLED) {
                    try {
                        MySQL mysql = new MySQL(
                                Config.MYSQL_HOST,
                                Config.MYSQL_PORT,
                                Config.MYSQL_DATABASE,
                                Config.MYSQL_USERNAME,
                                Config.MYSQL_PASSWORD,
                                Config.MYSQL_CONNECTIONOPTIONS
                        );
                        mysql.openConnection();
                        mysql.createTable();
                        instance.getSkinStorage().setMysql(mysql);
                    } catch (Exception ignored) { }
                } else {
                    instance.getSkinStorage().loadFolders(instance.getDataFolder());
                }
            }
        }

        @Override
        public Property getSkin(SuperiorPlayer superiorPlayer) {
            try {
                return (Property) instance.getSkinStorage().getOrCreateSkinForPlayer(superiorPlayer.getName(), true);
            }catch (net.skinsrestorer.shared.exception.SkinRequestException ex){
                return null;
            }
        }

    }

}
