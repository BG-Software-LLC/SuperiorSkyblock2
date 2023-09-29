package com.bgsoftware.superiorskyblock.commands.arguments;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CommandArguments {

    private CommandArguments() {

    }

    public static Map<String, String> parseArguments(String[] args) {
        Map<String, String> parsedArgs = new HashMap<>();
        String currentKey = null;
        StringBuilder stringBuilder = new StringBuilder();

        for (String arg : args) {
            if (arg.startsWith("-")) {
                if (currentKey != null && stringBuilder.length() > 0) {
                    parsedArgs.put(currentKey, stringBuilder.substring(1));
                }

                currentKey = arg.substring(1).toLowerCase(Locale.ENGLISH);
                stringBuilder = new StringBuilder();
            } else if (currentKey != null) {
                stringBuilder.append(" ").append(arg);
            }
        }

        if (currentKey != null && stringBuilder.length() > 0) {
            parsedArgs.put(currentKey, stringBuilder.substring(1));
        }

        return parsedArgs;
    }

}
