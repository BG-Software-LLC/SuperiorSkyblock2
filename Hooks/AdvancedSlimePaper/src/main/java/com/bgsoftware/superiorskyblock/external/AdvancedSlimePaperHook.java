package com.bgsoftware.superiorskyblock.external;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.infernalsuite.aswm.api.SlimePlugin;
import com.infernalsuite.aswm.api.loaders.SlimeLoader;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

@SuppressWarnings("unused")
public class AdvancedSlimePaperHook {

    private static final String[] LOADERS = new String[]{"api", "mysql", "mongodb", "file"};

    private static Plugin slimeWorldPlugin = null;

    public static boolean isCompatible() {
        try {
            Class.forName("com.infernalsuite.aswm.api.loaders.SlimeLoader");
        } catch (ClassNotFoundException error) {
            return false;
        }

        // We don't support API v3

        try {
            Class.forName("com.infernalsuite.aswm.api.AdvancedSlimePaperAPI");
            return false;
        } catch (ClassNotFoundException error) {
            return true;
        }
    }

    public static void register(SuperiorSkyblockPlugin plugin) {
        Bukkit.getLogger().info("AdvancedSlimePaperHook::register");
        slimeWorldPlugin = Bukkit.getPluginManager().getPlugin("SlimeWorldManager");
        plugin.getProviders().registerWorldsListener(AdvancedSlimePaperHook::loadWorld);
    }

    private static void loadWorld(String worldName) {
        SlimePlugin slimePlugin = (SlimePlugin) slimeWorldPlugin;

        for (String loaderName : LOADERS) {
            try {
                SlimeLoader slimeLoader = slimePlugin.getLoader(loaderName);
                if (slimeLoader != null && slimeLoader.worldExists(worldName)) {
                    slimeLoader.loadWorld(worldName);
                    break;
                }
            } catch (Exception ignored) {
            }
        }
    }

}
