package com.bgsoftware.superiorskyblock.core.io.loader;

import java.io.File;

public interface FilesLookupProvider {

    FilesLookup createFilesLookup(File folder) throws IllegalStateException;

}
