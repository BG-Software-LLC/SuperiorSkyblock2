package com.bgsoftware.superiorskyblock.core.io.loader;

import java.io.File;

public class DefaultFilesLookupProvider implements FilesLookupProvider {

    private static final DefaultFilesLookupProvider INSTANCE = new DefaultFilesLookupProvider();

    public static DefaultFilesLookupProvider getInstance() {
        return INSTANCE;
    }

    private DefaultFilesLookupProvider() {

    }

    @Override
    public FilesLookup createFilesLookup(File folder) {
        return new DefaultFilesLookup(folder);
    }

    private static class DefaultFilesLookup implements FilesLookup {

        private final File folder;

        DefaultFilesLookup(File folder) {
            this.folder = folder;
        }

        @Override
        public File getFile(String name) {
            return new File(this.folder, name);
        }

        @Override
        public void close() {
            // Do nothing
        }
    }

}
