package com.bgsoftware.superiorskyblock.core.logging;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;

import javax.annotation.Nullable;
import java.io.File;
import java.util.EnumSet;
import java.util.logging.Level;

public class Log {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private static final EnumSet<Debug> DEBUG_FILTERS = EnumSet.noneOf(Debug.class);
    private static boolean debugMode = false;

    private Log() {

    }

    public static void info(Object first, Object... parts) {
        logInternal(Level.INFO, first, parts);
    }

    public static void warn(Object first, Object... parts) {
        logInternal(Level.WARNING, first, parts);
    }

    public static void warn(File file, Object first, Object... parts) {
        logInternal(Level.WARNING, file, first, parts);
    }

    public static void error(Object first, Object... parts) {
        logInternal(Level.SEVERE, first, parts);
    }

    public static void error(Throwable error, Object first, Object... parts) {
        error(first, parts);
        error.printStackTrace();
    }

    public static void error(File file, Object first, Object... parts) {
        logInternal(Level.SEVERE, file, first, parts);
    }

    public static void error(Throwable error, File file, Object first, Object... parts) {
        error(file, first, parts);
        error.printStackTrace();
    }

    public static void debug(Debug debug, String clazz, String method, Object... params) {
        if (isDebugged(debug))
            entering(clazz, method, null, params);
    }

    public static void debugResult(Debug debug, String clazz, String method, @Nullable String message, Object result) {
        if (isDebugged(debug))
            entering(clazz, method, message, result);
    }

    public static void debugResult(Debug debug, String clazz, String method, @Nullable String message, Throwable error) {
        if (isDebugged(debug)) {
            enteringInternal(Level.SEVERE, clazz, method, message);
            error.printStackTrace();
        }
    }

    public static void entering(String clazz, String method, @Nullable String message, Object... params) {
        enteringInternal(Level.INFO, clazz, method, message, params);
    }

    public static boolean isDebugMode() {
        return debugMode;
    }

    public static boolean isDebugged(Debug debug) {
        return debugMode && DEBUG_FILTERS.contains(debug);
    }

    public static void toggleDebugMode() {
        debugMode = !debugMode;
    }

    public static void setDebugFilter(@Nullable Debug debugFilter) {
        if (debugFilter == null) {
            DEBUG_FILTERS.clear();
        } else if (DEBUG_FILTERS.contains(debugFilter)) {
            DEBUG_FILTERS.remove(debugFilter);
        } else {
            DEBUG_FILTERS.add(debugFilter);
        }
    }

    private static void enteringInternal(Level level, String clazz, String method, @Nullable String message, Object... params) {
        StringBuilder paramsMessage = new StringBuilder();
        for (Object param : params)
            paramsMessage.append(" {").append(param).append("}");
        logInternal(level, clazz, "::", method, message == null ? "" : " " + message, paramsMessage.toString());
    }

    private static void logInternal(Level level, File file, Object first, Object... parts) {
        plugin.getLogger().log(level, buildFromParts(file, first, parts));
    }

    private static void logInternal(Level level, Object first, Object... parts) {
        plugin.getLogger().log(level, buildFromParts(first, parts));
    }

    private static String buildFromParts(Object first, Object... parts) {
        StringBuilder builder = new StringBuilder().append(first);
        for (Object part : parts)
            builder.append(part);
        return builder.toString();
    }

    private static String buildFromParts(File prefixFile, Object first, Object... parts) {
        StringBuilder builder = new StringBuilder("[").append(prefixFile.getName()).append("] ").append(first);
        for (Object part : parts)
            builder.append(part);
        return builder.toString();
    }

}
