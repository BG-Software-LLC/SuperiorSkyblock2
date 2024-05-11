package com.bgsoftware.superiorskyblock.core.io.loader;

import java.io.File;

public interface FilesLookup extends AutoCloseable {

    File getFile(String name);

    @Override
    void close();

}
