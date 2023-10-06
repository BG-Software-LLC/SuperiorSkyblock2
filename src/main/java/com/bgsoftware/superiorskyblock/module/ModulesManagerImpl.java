package com.bgsoftware.superiorskyblock.module;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.SuperiorCommand;
import com.bgsoftware.superiorskyblock.api.handlers.ModulesManager;
import com.bgsoftware.superiorskyblock.api.modules.ModuleLoadTime;
import com.bgsoftware.superiorskyblock.api.modules.PluginModule;
import com.bgsoftware.superiorskyblock.core.Either;
import com.bgsoftware.superiorskyblock.core.Manager;
import com.bgsoftware.superiorskyblock.core.io.FileClassLoader;
import com.bgsoftware.superiorskyblock.core.io.JarFiles;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.module.container.ModulesContainer;
import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Stream;

public class ModulesManagerImpl extends Manager implements ModulesManager {

    private final ModulesContainer modulesContainer;
    private final File modulesFolder;
    private final File dataFolder;

    public ModulesManagerImpl(SuperiorSkyblockPlugin plugin, ModulesContainer modulesContainer) {
        super(plugin);
        this.modulesContainer = modulesContainer;
        this.modulesFolder = new File(plugin.getDataFolder(), "modules");
        this.dataFolder = new File(plugin.getDataFolder(), "datastore/modules");
    }

    @Override
    public void loadData() {
        if (!modulesFolder.exists())
            //noinspection ResultOfMethodCallIgnored
            modulesFolder.mkdirs();

        registerModule(BuiltinModules.GENERATORS);
        registerModule(BuiltinModules.MISSIONS);
        registerModule(BuiltinModules.BANK);
        registerModule(BuiltinModules.UPGRADES);
        registerExternalModules();
    }

    @Override
    public void registerModule(PluginModule pluginModule) {
        Preconditions.checkNotNull(pluginModule, "pluginModule parameter cannot be null.");
        this.modulesContainer.registerModule(pluginModule, modulesFolder, dataFolder);
    }

    @Override
    public PluginModule registerModule(File moduleFile) throws IOException, ReflectiveOperationException {
        Preconditions.checkArgument(moduleFile.exists(), "The file " + moduleFile.getName() + " does not exist.");
        Preconditions.checkArgument(moduleFile.getName().endsWith(".jar"), "The file " + moduleFile.getName() + " is not a valid jar file.");

        FileClassLoader moduleClassLoader = new FileClassLoader(moduleFile, plugin.getPluginClassLoader());

        Either<Class<?>, Throwable> moduleClassLookup = JarFiles.getClass(moduleFile.toURL(), PluginModule.class, moduleClassLoader);

        if (moduleClassLookup.getLeft() != null)
            throw new RuntimeException("An error occurred while reading " + moduleFile.getName(), moduleClassLookup.getLeft());

        Class<?> moduleClass = moduleClassLookup.getRight();

        if (moduleClass == null)
            throw new RuntimeException("The module file " + moduleFile.getName() + " is not valid.");

        PluginModule pluginModule = createInstance(moduleClass);
        pluginModule.initModuleLoader(moduleFile, moduleClassLoader);

        registerModule(pluginModule);

        return pluginModule;
    }

    @Override
    public void unregisterModule(PluginModule pluginModule) {
        Preconditions.checkNotNull(pluginModule, "pluginModule parameter cannot be null.");
        Preconditions.checkState(getModule(pluginModule.getName()) != null, "PluginModule with the name " + pluginModule.getName() + " is not registered in the plugin anymore.");

        Log.info("Disabling the module ", pluginModule.getName(), "...");

        try {
            pluginModule.onDisable(plugin);
        } catch (Throwable error) {
            Log.error("An unexpected error occurred while disabling the module ", pluginModule.getName(), ".");
            Log.error(error, "Contact ", pluginModule.getAuthor(), " regarding this, this has nothing to do with the plugin.");
        }

        this.modulesContainer.unregisterModule(pluginModule);

        // We now want to unload the ClassLoader and free the held handles for the file.
        ClassLoader classLoader = pluginModule.getClassLoader();
        if (classLoader instanceof URLClassLoader) {
            try {
                ((URLClassLoader) classLoader).close();
                // This is an attempt to force Windows to free the handles of the file.
                System.gc();
            } catch (IOException ignored) {
            }
        }

    }

    @Override
    @Nullable
    public PluginModule getModule(String name) {
        Preconditions.checkNotNull(name, "name parameter cannot be null.");
        return this.modulesContainer.getModule(name);
    }

    @Override
    public Collection<PluginModule> getModules() {
        return this.modulesContainer.getModules();
    }

    @Override
    public void enableModule(PluginModule pluginModule) {
        Preconditions.checkNotNull(pluginModule, "pluginModule parameter cannot be null.");

        long startTime = System.currentTimeMillis();

        Log.info("Enabling the module ", pluginModule.getName(), "...");

        try {
            pluginModule.onEnable(plugin);
        } catch (Exception error) {
            Log.error("An unexpected error occurred while disabling the module ", pluginModule.getName(), ".");
            Log.error(error, "Contact ", pluginModule.getAuthor(), " regarding this, this has nothing to do with the plugin.");

            try {
                // Unregistering the module.
                unregisterModule(pluginModule);
            } catch (Throwable error2) {
                Log.error("An unexpected error occurred while disabling the module ", pluginModule.getName(), ".");
                Log.error(error2, "Contact ", pluginModule.getAuthor(), " regarding this, this has nothing to do with the plugin.");
            }

            return;
        }

        Listener[] listeners = pluginModule.getModuleListeners(plugin);
        SuperiorCommand[] commands = pluginModule.getSuperiorCommands(plugin);
        SuperiorCommand[] adminCommands = pluginModule.getSuperiorAdminCommands(plugin);

        if (listeners != null || commands != null || adminCommands != null)
            this.modulesContainer.addModuleData(pluginModule, new ModuleData(listeners, commands, adminCommands));

        if (listeners != null)
            Arrays.stream(listeners).forEach(listener -> Bukkit.getPluginManager().registerEvents(listener, plugin));

        if (commands != null)
            Arrays.stream(commands).forEach(plugin.getCommands()::registerCommand);

        if (adminCommands != null)
            Arrays.stream(adminCommands).forEach(plugin.getCommands()::registerAdminCommand);

        Log.info("Finished enabling the module ", pluginModule.getName(), " (Took ",
                System.currentTimeMillis() - startTime, "ms)");
    }

    @Override
    public void enableModules(ModuleLoadTime moduleLoadTime) {
        Preconditions.checkNotNull(moduleLoadTime, "moduleLoadTime parameter cannot be null.");
    }

    public void runModuleLifecycle(ModuleLoadTime moduleLoadTime, boolean isReload) {
        if (isReload) {
            reloadModulesInternal(moduleLoadTime);
        } else {
            enableModulesInternal(moduleLoadTime);
        }
    }

    private void reloadModulesInternal(ModuleLoadTime moduleLoadTime) {
        filterModules(moduleLoadTime).forEach(this::reloadModuleInternal);
    }

    private void enableModulesInternal(ModuleLoadTime moduleLoadTime) {
        filterModules(moduleLoadTime).forEach(this::enableModule);
    }

    private void reloadModuleInternal(PluginModule pluginModule) {
        try {
            pluginModule.onReload(plugin);
        } catch (Throwable error) {
            Log.error("An unexpected error occurred while reloading the module ", pluginModule.getName(), ".");
            Log.error(error, "Contact ", pluginModule.getAuthor(), " regarding this, this has nothing to do with the plugin.");
        }
    }

    public void loadModulesData(SuperiorSkyblockPlugin plugin) {
        getModules().forEach(pluginModule -> {
            try {
                pluginModule.loadData(plugin);
            } catch (Throwable error) {
                Log.error("An unexpected error occurred while loading data for the module ", pluginModule.getName(), ".");
                Log.error(error, "Contact ", pluginModule.getAuthor(), " regarding this, this has nothing to do with the plugin.");
            }
        });
    }

    private void registerExternalModules() {
        File[] folderFiles = modulesFolder.listFiles();

        if (folderFiles != null) {
            for (File file : folderFiles) {
                if (!file.isDirectory() && file.getName().endsWith(".jar")) {
                    try {
                        registerModule(file);
                    } catch (Exception error) {
                        Log.error(error, "An unexpected error occurred while registering module ", file.getName(), ":");
                    }
                }
            }
        }
    }

    private Stream<PluginModule> filterModules(ModuleLoadTime moduleLoadTime) {
        return this.modulesContainer.getModules().stream()
                .filter(pluginModule -> pluginModule.getLoadTime() == moduleLoadTime);
    }

    private PluginModule createInstance(Class<?> clazz) throws ReflectiveOperationException {
        Preconditions.checkArgument(PluginModule.class.isAssignableFrom(clazz), "Class " + clazz + " is not a PluginModule.");

        for (Constructor<?> constructor : clazz.getConstructors()) {
            if (constructor.getParameterCount() == 0) {
                if (!constructor.isAccessible())
                    constructor.setAccessible(true);

                return (PluginModule) constructor.newInstance();
            }
        }

        throw new IllegalArgumentException("Class " + clazz + " has no valid constructors.");
    }

}
