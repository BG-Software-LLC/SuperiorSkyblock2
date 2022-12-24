package com.bgsoftware.superiorskyblock.core.io;

import com.bgsoftware.superiorskyblock.core.Either;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class JarFiles {

    private JarFiles() {

    }

    public static Either<Class<?>, Throwable> getClass(URL jar, Class<?> clazz, ClassLoader classLoader) {
        try (URLClassLoader cl = new URLClassLoader(new URL[]{jar}, classLoader); JarInputStream jis = new JarInputStream(jar.openStream())) {
            JarEntry jarEntry;
            while ((jarEntry = jis.getNextJarEntry()) != null) {
                String name = jarEntry.getName();

                if (!name.endsWith(".class")) {
                    continue;
                }

                name = name.replace("/", ".");
                String clazzName = name.substring(0, name.lastIndexOf(".class"));

                try {
                    Class<?> c = cl.loadClass(clazzName);
                    if (clazz.isAssignableFrom(c)) {
                        return Either.right(c);
                    }
                } catch (NoClassDefFoundError ignored) {
                    // If we can't find the class, can just be ignored.
                }
            }
        } catch (Throwable error) {
            return Either.left(error);
        }

        return Either.right(null);
    }

}
