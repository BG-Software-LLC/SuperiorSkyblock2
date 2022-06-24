package com.bgsoftware.superiorskyblock.core.io;

import javax.annotation.Nullable;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class JarFiles {

    private JarFiles() {

    }

    @Nullable
    public static Class<?> getClass(URL jar, Class<?> clazz) {
        return getClass(jar, clazz, clazz.getClassLoader());
    }

    @Nullable
    public static Class<?> getClass(URL jar, Class<?> clazz, ClassLoader classLoader) {
        try (URLClassLoader cl = new URLClassLoader(new URL[]{jar}, classLoader); JarInputStream jis = new JarInputStream(jar.openStream())) {
            JarEntry jarEntry;
            while ((jarEntry = jis.getNextJarEntry()) != null) {
                String name = jarEntry.getName();

                if (!name.endsWith(".class")) {
                    continue;
                }

                name = name.replace("/", ".");
                String clazzName = name.substring(0, name.lastIndexOf(".class"));

                Class<?> c = cl.loadClass(clazzName);

                if (clazz.isAssignableFrom(c)) {
                    return c;
                }
            }
        } catch (Throwable ignored) {
        }

        return null;
    }

}
