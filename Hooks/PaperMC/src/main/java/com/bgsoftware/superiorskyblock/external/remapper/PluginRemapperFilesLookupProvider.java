package com.bgsoftware.superiorskyblock.external.remapper;

import com.bgsoftware.common.reflection.ClassInfo;
import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.superiorskyblock.core.io.Files;
import com.bgsoftware.superiorskyblock.core.io.loader.FilesLookup;
import com.bgsoftware.superiorskyblock.core.io.loader.FilesLookupProvider;

import java.io.File;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

public class PluginRemapperFilesLookupProvider implements FilesLookupProvider {

    private static final ReflectMethod<Object> CREATE_PLUGIN_REMAPPER;
    private static final ReflectMethod<Void> PLUGIN_REMAPPER_REWRITE_PLUGIN_DIRECTORY;
    private static final ReflectMethod<Path> PLUGIN_REMAPPER_REWRITE_PLUGIN;
    private static final ReflectMethod<Void> PLUGIN_REMAPPER_LOADING_PLUGINS;
    private static final ReflectMethod<Void> PLUGIN_REMAPPER_PLUGINS_ENABLED;
    private static final ReflectMethod<Void> PLUGIN_REMAPPER_SHUTDOWN;

    static {
        ClassInfo pluginRemapperClassInfo = new ClassInfo("io.papermc.paper.pluginremap.PluginRemapper",
                ClassInfo.PackageType.UNKNOWN);
        CREATE_PLUGIN_REMAPPER = new ReflectMethod<>(pluginRemapperClassInfo, "create", Path.class);
        PLUGIN_REMAPPER_REWRITE_PLUGIN_DIRECTORY = new ReflectMethod<>(pluginRemapperClassInfo, "rewritePluginDirectory", List.class);
        PLUGIN_REMAPPER_REWRITE_PLUGIN = new ReflectMethod<>(pluginRemapperClassInfo, "rewritePlugin", Path.class);
        PLUGIN_REMAPPER_LOADING_PLUGINS = new ReflectMethod<>(pluginRemapperClassInfo, "loadingPlugins", new Class[0]);
        PLUGIN_REMAPPER_PLUGINS_ENABLED = new ReflectMethod<>(pluginRemapperClassInfo, "pluginsEnabled", new Class[0]);
        PLUGIN_REMAPPER_SHUTDOWN = new ReflectMethod<>(pluginRemapperClassInfo, "shutdown", new Class[0]);
    }

    @Override
    public FilesLookup createFilesLookup(File folder) {
        Object pluginRemapper = CREATE_PLUGIN_REMAPPER.invoke(null, folder.toPath());
        if (pluginRemapper == null)
            throw new IllegalStateException("Cannot create PluginRemapper");

        PLUGIN_REMAPPER_LOADING_PLUGINS.invoke(pluginRemapper);

        List<Path> folderFiles = new LinkedList<>();
        for (File file : Files.listFolderFiles(folder, false, file -> file.getName().endsWith(".jar"))) {
            folderFiles.add(file.toPath());
        }

        PLUGIN_REMAPPER_REWRITE_PLUGIN_DIRECTORY.invoke(pluginRemapper, folderFiles);

        File remappedFolder = new File(folder, ".paper-remapped");
        if (!remappedFolder.isDirectory())
            throw new IllegalStateException("Cannot find remapped folder: " + remappedFolder.getAbsolutePath());

        return new PluginRemapperFilesLookup(remappedFolder, pluginRemapper);
    }

    private static class PluginRemapperFilesLookup implements FilesLookup {

        private final File remappedFolder;
        private Object pluginRemapper;

        public PluginRemapperFilesLookup(File remappedFolder, Object pluginRemapper) {
            this.remappedFolder = remappedFolder;
            this.pluginRemapper = pluginRemapper;
        }

        @Override
        public File getFile(String name) {
            File file = new File(this.remappedFolder, name);
            return PLUGIN_REMAPPER_REWRITE_PLUGIN.invoke(this.pluginRemapper, file.toPath()).toFile();
        }

        @Override
        public void close() {
            PLUGIN_REMAPPER_PLUGINS_ENABLED.invoke(this.pluginRemapper);
            PLUGIN_REMAPPER_SHUTDOWN.invoke(this.pluginRemapper);
            this.pluginRemapper = null;
        }

    }

}
