package com.bgsoftware.superiorskyblock.core.zmenu.loader;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import fr.maxlego08.menu.api.loader.ButtonLoader;
import org.bukkit.plugin.Plugin;

public abstract class SuperiorButtonLoader implements ButtonLoader {

    protected final SuperiorSkyblockPlugin plugin;
    private final String buttonName;

    public SuperiorButtonLoader(SuperiorSkyblockPlugin plugin, String buttonName) {
        this.plugin = plugin;
        this.buttonName = buttonName;
    }

    @Override
    public Plugin getPlugin() {
        return this.plugin;
    }

    @Override
    public String getName() {
        return "SUPERIORSKYBLOCK_" + buttonName;
    }
}
