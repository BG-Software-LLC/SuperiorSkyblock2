package com.bgsoftware.superiorskyblock.hooks.support;

import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import com.mojang.authlib.properties.Property;
import net.skinsrestorer.api.SkinsRestorerAPI;
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
            Executor.sync(() -> plugin.getNMSPlayers().setSkinTexture(superiorPlayer, property));
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

        private static final ReflectMethod<Object> SKINS_RESTORER_GET_SKIN = new ReflectMethod<>(SkinsRestorerAPI.class, "getSkinData", String.class);

        @Override
        public Property getSkin(SuperiorPlayer superiorPlayer) {
            try {
                return (Property) SkinsRestorerAPI.getApi().getSkinData(superiorPlayer.getName());
            }catch(Throwable ex){
                return (Property) SKINS_RESTORER_GET_SKIN.invoke(SkinsRestorerAPI.getApi(), superiorPlayer.getName());
            }
        }

    }

}
