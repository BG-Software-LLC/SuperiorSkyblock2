package com.bgsoftware.superiorskyblock.formatting.impl;

import com.bgsoftware.superiorskyblock.formatting.ILocaleFormatter;
import com.bgsoftware.superiorskyblock.lang.Message;

import java.util.Locale;

public final class BooleanFormatter implements ILocaleFormatter<Boolean> {

    private static final BooleanFormatter INSTANCE = new BooleanFormatter();

    public static BooleanFormatter getInstance() {
        return INSTANCE;
    }

    private BooleanFormatter() {

    }

    @Override
    public String format(Boolean value, Locale locale) {
        return (value ? Message.PLACEHOLDER_YES : Message.PLACEHOLDER_NO).getMessage(locale);
    }

}
