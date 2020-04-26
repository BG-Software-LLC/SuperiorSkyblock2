package com.bgsoftware.superiorskyblock.hooks;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import com.mojang.authlib.properties.Property;
import org.bukkit.Bukkit;
import skinsrestorer.bukkit.SkinsRestorer;
import skinsrestorer.shared.storage.SkinStorage;

public final class SkinsRestorerHook {

    private static SuperiorSkyblockPlugin plugin;
    private static boolean isEnabled = false;

    public static void setSkinTexture(SuperiorPlayer superiorPlayer){
        if(Bukkit.isPrimaryThread()){
            Executor.async(() -> setSkinTexture(superiorPlayer));
            return;
        }
        
        SkinStorage skinStorage = SkinsRestorer.getInstance().getSkinStorage();
        try {
            Property property = (Property) skinStorage.getOrCreateSkinForPlayer(superiorPlayer.getName());
            plugin.getNMSAdapter().setSkinTexture(superiorPlayer, property);
        }catch(Exception ignored){ }
    }

    public static boolean isEnabled() {
        return isEnabled;
    }

    public static void register(SuperiorSkyblockPlugin plugin){
        SkinsRestorerHook.plugin = plugin;
        isEnabled = true;
    }

}
