package com.bgsoftware.superiorskyblock.modules;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.api.modules.PluginModule;

public abstract class BuiltinModule extends PluginModule {

    public BuiltinModule(String moduleName){
        super(moduleName, "Ome_R");
    }

    @Override
    public final void onEnable(SuperiorSkyblock plugin) {
        onEnable((SuperiorSkyblockPlugin) plugin);
    }

    public abstract void onEnable(SuperiorSkyblockPlugin plugin);

}
