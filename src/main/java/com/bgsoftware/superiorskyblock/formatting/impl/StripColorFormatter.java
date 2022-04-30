package com.bgsoftware.superiorskyblock.formatting.impl;

import com.bgsoftware.superiorskyblock.formatting.IFormatter;
import com.bgsoftware.superiorskyblock.utils.StringUtils;

import java.util.regex.Pattern;

public final class StripColorFormatter implements IFormatter<String> {

    private static final StripColorFormatter INSTANCE = new StripColorFormatter();

    private static final Pattern STRIP_COLOR_PATTERN = Pattern.compile("(?i)([&§])[0-9A-FK-OR]");

    public static StripColorFormatter getInstance() {
        return INSTANCE;
    }

    private StripColorFormatter() {

    }

    @Override
    public String format(String value) {
        return StringUtils.isBlank(value) ? "" : STRIP_COLOR_PATTERN.matcher(value).replaceAll("");
    }

}
