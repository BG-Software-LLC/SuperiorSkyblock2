package com.bgsoftware.superiorskyblock.formatting.impl;

import com.bgsoftware.superiorskyblock.formatting.IFormatter;

import java.util.Locale;

public final class LocaleFormatter implements IFormatter<Locale> {

    private static final LocaleFormatter INSTANCE = new LocaleFormatter();

    public static LocaleFormatter getInstance() {
        return INSTANCE;
    }

    private LocaleFormatter() {

    }

    @Override
    public String format(Locale value) {
        return value.getLanguage() + "-" + value.getCountry();
    }

}
