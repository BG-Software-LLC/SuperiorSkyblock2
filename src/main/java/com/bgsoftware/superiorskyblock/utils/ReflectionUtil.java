package com.bgsoftware.superiorskyblock.utils;

import org.bukkit.Bukkit;

public class ReflectionUtil {

    private static String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];

    public static Class getClass(String classPath){
        try {
            return Class.forName(classPath.replace("VERSION", version));
        } catch(ClassNotFoundException ex){
            ex.printStackTrace();
            return null;
        }
    }

}
