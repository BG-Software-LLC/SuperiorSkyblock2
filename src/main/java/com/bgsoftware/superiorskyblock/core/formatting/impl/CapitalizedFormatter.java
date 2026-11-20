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

        //Checking if the type is <TYPE>:<INTEGER>
        int separatorIndex = value.indexOf(':');
        if (separatorIndex != -1) {
            int nextSeparatorIndex = value.indexOf(':', separatorIndex + 1);
            String data = nextSeparatorIndex == -1 ? value.substring(separatorIndex + 1) :
                    value.substring(separatorIndex + 1, nextSeparatorIndex);
            if (Text.isInt(data))
                value = value.substring(0, separatorIndex);
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
