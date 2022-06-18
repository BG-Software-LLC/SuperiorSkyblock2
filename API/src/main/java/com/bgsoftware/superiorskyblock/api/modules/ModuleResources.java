package com.bgsoftware.superiorskyblock.api.modules;

import com.google.common.base.Preconditions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Class to handle resources for a module.
 */
public class ModuleResources {

    private final File moduleFile;
    private final File moduleFolder;
    private final ClassLoader classLoader;

    /**
     * Constructor for the class.
     *
     * @param moduleFile   The file of the module.
     * @param moduleFolder The data folder of the module.
     * @param classLoader  The class loader of the module.
     */
    public ModuleResources(File moduleFile, File moduleFolder, ClassLoader classLoader) {
        this.moduleFile = moduleFile;
        this.moduleFolder = moduleFolder;
        this.classLoader = classLoader;
    }

    /**
     * Saves the raw contents of an embedded resource within the module.
     *
     * @param resourcePath Path to the resource to save.
     */
    public void saveResource(String resourcePath) {
        Preconditions.checkNotNull(resourcePath, "resourcePath cannot be null.");
        Preconditions.checkArgument(!resourcePath.isEmpty(), "resourcePath cannot be empty.");

        resourcePath = resourcePath.replace('\\', '/');

        try (InputStream resourceInput = getResource(resourcePath)) {
            File outFile = new File(this.moduleFolder, resourcePath);
            int lastIndex = resourcePath.lastIndexOf(47);
            File outDir = new File(this.moduleFolder, resourcePath.substring(0, Math.max(lastIndex, 0)));

            if (!outDir.exists()) {
                outDir.mkdirs();
            }

            try (OutputStream out = new FileOutputStream(outFile)) {
                byte[] buf = new byte[1024];

                int len;
                while ((len = resourceInput.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            }
        } catch (IOException ex) {
            throw new RuntimeException("Could not save " + resourcePath, ex);
        }
    }

    /**
     * Get the raw contents of an embedded resource within the module.
     *
     * @param fileName The name of the resource to get contents of.
     */
    public InputStream getResource(String fileName) {
        Preconditions.checkNotNull(fileName, "fileName cannot be null.");

        try {
            URL url = this.classLoader.getResource(fileName);
            if (url != null) {
                URLConnection connection = url.openConnection();
                connection.setUseCaches(false);
                return connection.getInputStream();
            }
        } catch (IOException ignored) {
        }

        throw new IllegalArgumentException("The embedded resource '" + fileName + "' cannot be found in " + this.moduleFile);
    }

}
