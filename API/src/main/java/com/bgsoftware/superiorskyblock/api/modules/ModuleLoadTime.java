package com.bgsoftware.superiorskyblock.api.modules;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;

public enum ModuleLoadTime {

    /**
     * When used, the module will be loaded before the plugin is initialized.
     * This makes it possible to listen to the PluginInitializeEvent and alter it.
     */
    PLUGIN_INITIALIZE,

    /**
     * When used, the module will be enabled before the plugin loads the worlds for the islands.
     */
    BEFORE_WORLD_CREATION,

    /**
     * When used, the module will be enabled after the worlds are loaded into the game.
     * Please note that all the managers of the plugin are not loaded at this time, and you cannot use them
     * inside your {@link PluginModule#onEnable(SuperiorSkyblock)} method. Furthermore, the data of your module
     * was not yet loaded at this time. If you want to use your data in the {@link PluginModule#onEnable(SuperiorSkyblock)}
     * method, check out {@link #AFTER_MODULE_DATA_LOAD}.
     */
    NORMAL,

    /**
     * When used, the module will be enabled after its data was loaded by calling the
     * {@link PluginModule#loadData(SuperiorSkyblock)} method. Please note that not all the managers of the plugin
     * are loaded at this time, and using them inside your {@link PluginModule#onEnable(SuperiorSkyblock)} may
     * lead to undefined behavior; access them at your own risk.
     */
    AFTER_MODULE_DATA_LOAD,

    /**
     * When used, the module will be enabled after all the managers were completely loaded.
     */
    AFTER_HANDLERS_LOADING

}
