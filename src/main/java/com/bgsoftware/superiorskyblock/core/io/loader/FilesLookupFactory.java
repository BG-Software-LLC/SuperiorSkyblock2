package com.bgsoftware.superiorskyblock.core.io.loader;

import java.io.File;

public class FilesLookupFactory {

    private static final FilesLookupFactory INSTANCE = new FilesLookupFactory();

    public static FilesLookupFactory getInstance() {
        return INSTANCE;
    }

    private FilesLookupProvider filesLookupProvider = findSuitableFilesLookupProvider();

    private FilesLookupFactory() {

    }

    public void setProvider(FilesLookupProvider filesLookupProvider) {
        this.filesLookupProvider = filesLookupProvider;
    }

    public FilesLookup lookupFolder(File folder) {
        try {
            return this.filesLookupProvider.createFilesLookup(folder);
        } catch (IllegalStateException error) {
            if (this.filesLookupProvider == DefaultFilesLookupProvider.getInstance())
                throw error;

            this.filesLookupProvider = DefaultFilesLookupProvider.getInstance();
            return lookupFolder(folder);
        }
    }

    private static FilesLookupProvider findSuitableFilesLookupProvider() {
        try {
            Class.forName("io.papermc.paper.pluginremap.PluginRemapper");
            return (FilesLookupProvider) Class.forName(
                            "com.bgsoftware.superiorskyblock.external.remapper.PluginRemapperFilesLookupProvider")
                    .newInstance();
        } catch (Throwable ignored) {
        }

        return DefaultFilesLookupProvider.getInstance();
    }

}
