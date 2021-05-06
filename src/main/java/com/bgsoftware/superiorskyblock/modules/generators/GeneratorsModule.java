package com.bgsoftware.superiorskyblock.modules.generators;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.modules.BuiltinModule;
import com.bgsoftware.superiorskyblock.modules.generators.listeners.GeneratorsListener;
import org.bukkit.Bukkit;

public final class GeneratorsModule extends BuiltinModule {

    public GeneratorsModule(){
        super("Generators");
    }

    @Override
    public void onEnable(SuperiorSkyblockPlugin plugin) {
        if(plugin.getSettings().generators)
            Bukkit.getPluginManager().registerEvents(new GeneratorsListener(plugin), plugin);
    }

    @Override
    public void onDisable() {

    }

}
