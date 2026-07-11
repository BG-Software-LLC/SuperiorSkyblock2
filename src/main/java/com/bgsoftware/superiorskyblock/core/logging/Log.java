package com.bgsoftware.superiorskyblock.core.logging;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.module.logging.ModuleLoggerFileHandler;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.EnumSet;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Log {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
    private static final Logger LOGGER = initializeLogger();

    private static final EnumSet<Debug> DEBUG_FILTERS = EnumSet.noneOf(Debug.class);
    private static boolean debugMode = false;

    private static Logger initializeLogger() {
        Logger logger = Logger.getLogger("SuperiorSkyblock2");

        logger.setParent(plugin.getLogger());
        logger.setUseParentHandlers(true);

        File logsFolder = new File(plugin.getDataFolder(), "logs");
        File archiveLogsFile = new File(logsFolder, "archive");

        ModuleLoggerFileHandler.addToLogger(logsFolder, archiveLogsFile, logger);

        return logger;
    }

    private Log() {

    }

    public static void info(Object first, Object... parts) {
        logInternal(Level.INFO, first, parts);
    }

    public static void warn(Object first, Object... parts) {
        logInternal(Level.WARNING, first, parts);
    }

    public static void warnFromFile(String fileName, Object first, Object... parts) {
        logInternalWithFile(Level.WARNING, fileName, first, parts);
    }

    public static void error(Object first, Object... parts) {
        logInternal(Level.SEVERE, first, parts);
    }

    public static void error(Throwable error, Object first, Object... parts) {
        error(first, parts);
        printStackTrace(error);
    }

    public static void errorFromFile(String fileName, Object first, Object... parts) {
        logInternalWithFile(Level.SEVERE, fileName, first, parts);
    }

    public static void errorFromFile(Throwable error, String fileName, Object first, Object... parts) {
        errorFromFile(fileName, first, parts);
        printStackTrace(error);
    }

    public static void profile(String[] profiledDataLines) {
        if (!isDebugged(Debug.PROFILER))
            return;

        for (String line : profiledDataLines)
            logInternal(Level.INFO, line);
    }

    public static void debug(Debug debug, Object... params) {
        if (isDebugged(debug)) {
            String[] classAndMethod = getClassAndMethodNames();
            enteringInternal(Level.INFO, classAndMethod[0], classAndMethod[1], null, params);
            if (isDebugged(Debug.SHOW_STACKTRACE))
                printStackTrace();
        }
    }

    public static void debugResult(Debug debug, @Nullable String message, Object result) {
        if (isDebugged(debug)) {
            String[] classAndMethod = getClassAndMethodNames();
            enteringInternal(Level.INFO, classAndMethod[0], classAndMethod[1], message, result);
            if (isDebugged(Debug.SHOW_STACKTRACE))
                printStackTrace();
        }
    }

    public static void entering(@Nullable String message, Object... params) {
        String[] classAndMethod = getClassAndMethodNames();
        enteringInternal(Level.INFO, classAndMethod[0], classAndMethod[1], message, params);
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

    private static void logInternalWithFile(Level level, String fileName, Object first, Object... parts) {
        LOGGER.log(level, buildFromPartsWithFile(fileName, first, parts));
    }

    private static void logInternal(Level level, Object first, Object... parts) {
        LOGGER.log(level, buildFromParts(first, parts));
    }

    private static String buildFromParts(Object first, Object... parts) {
        StringBuilder builder = new StringBuilder().append(first);
        for (Object part : parts)
            builder.append(part);
        return builder.toString();
    }

    private static String buildFromPartsWithFile(String prefixFile, Object first, Object... parts) {
        StringBuilder builder = new StringBuilder("[").append(prefixFile).append("] ").append(first);
        for (Object part : parts)
            builder.append(part);
        return builder.toString();
    }

    private static String[] getClassAndMethodNames() {
        StackTraceElement currentElement = Thread.currentThread().getStackTrace()[3];

        String methodName = currentElement.getMethodName();
        if (methodName.contains("lambda")) {
            methodName = methodName.split("\\$")[1];
        }

        String className = currentElement.getClassName();

        return new String[]{className.substring(className.lastIndexOf(".") + 1), methodName};
    }

    private static void printStackTrace() {
        printStackTrace(new Exception("Stack trace"));
    }

    private static void printStackTrace(Throwable error) {
        StringWriter buffer = new StringWriter();
        PrintWriter pw = new PrintWriter(buffer);
        error.printStackTrace(pw);
        LOGGER.log(Level.SEVERE, buffer.toString());
    }

}
