package com.bgsoftware.superiorskyblock.core.formatting.impl;

import com.bgsoftware.superiorskyblock.core.Text;
import com.bgsoftware.superiorskyblock.core.formatting.IFormatter;

import java.util.Locale;

public class CapitalizedFormatter implements IFormatter<String> {

    private static final CapitalizedFormatter INSTANCE = new CapitalizedFormatter();

    public static CapitalizedFormatter getInstance() {
        return INSTANCE;
    }

    private CapitalizedFormatter() {

    }

    @Override
    public String format(String value) {
        StringBuilder formattedKey = new StringBuilder();

        try {
            String[] split = value.split(":");
            //Checking if the type is <TYPE>:<INTEGER>
            Integer.parseInt(split[1]);
            value = split[0];
        } catch (Throwable ignored) {
        }

        value = value.replace(":", "_-_");

        for (String subKey : value.split("_")) {
            if (!Text.isBlank(subKey)) {
                formattedKey.append(" ")
                        .append(subKey.substring(0, 1).toUpperCase(Locale.ENGLISH))
                        .append(subKey.substring(1).toLowerCase(Locale.ENGLISH));
            }
        }

        return formattedKey.substring(1);
    }

}
