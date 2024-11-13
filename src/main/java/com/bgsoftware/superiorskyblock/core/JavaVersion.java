package com.bgsoftware.superiorskyblock.core;

public class JavaVersion {

    private static final int currentJavaVersion = detectJavaVersion();

    private static int detectJavaVersion() {
        String[] javaVersionSections = System.getProperty("java.version").split("\\.");
        if(javaVersionSections[0].equals("1"))
            return Integer.parseInt(javaVersionSections[1]);

        return Integer.parseInt(javaVersionSections[0]);
    }

    public static boolean isAtLeast(int version) {
        return currentJavaVersion >= version;
    }

    private JavaVersion() {

    }

}
