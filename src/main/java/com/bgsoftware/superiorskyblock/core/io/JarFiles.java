package com.bgsoftware.superiorskyblock.core.io;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class JarFiles {

    private JarFiles() {

    }

    public static List<Class<?>> getClasses(URL jar, Class<?> clazz) {
        return getClasses(jar, clazz, clazz.getClassLoader());
    }

    public static List<Class<?>> getClasses(URL jar, Class<?> clazz, ClassLoader classLoader) {
        List<Class<?>> list = new ArrayList<>();

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
                    list.add(c);
                }
            }
        } catch (Throwable ignored) {
        }

        return list;
    }

}
