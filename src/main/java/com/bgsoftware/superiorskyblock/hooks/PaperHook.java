package com.bgsoftware.superiorskyblock.hooks;

public final class PaperHook {

    private static boolean usingPaper = false;

    static {
        try {
            Class.forName("com.destroystokyo.paper.VersionHistoryManager$VersionData");
            usingPaper = true;
        } catch (Throwable ignored) {}
    }

    public static boolean isUsingPaper() {
        return usingPaper;
    }
}
