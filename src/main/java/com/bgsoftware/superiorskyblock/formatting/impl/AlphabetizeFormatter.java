package com.bgsoftware.superiorskyblock.formatting.impl;

import com.bgsoftware.superiorskyblock.formatting.IFormatter;
import com.bgsoftware.superiorskyblock.utils.StringUtils;

import java.util.regex.Pattern;

public final class AlphabetizeFormatter implements IFormatter<String> {

    private static final AlphabetizeFormatter INSTANCE = new AlphabetizeFormatter();

    private static final Pattern ALPHABETIC_REPLACE_PATTERN = Pattern.compile("[^\\w]");

    public static AlphabetizeFormatter getInstance() {
        return INSTANCE;
    }

    private AlphabetizeFormatter() {

    }

    @Override
    public String format(String value) {
        if (StringUtils.isBlank(value))
            return "";

        return ALPHABETIC_REPLACE_PATTERN.matcher(value).replaceAll("");
    }

}
