package com.bgsoftware.superiorskyblock.core.formatting.impl;

import com.bgsoftware.superiorskyblock.core.formatting.IFormatter;

import java.util.Locale;

public class LocaleFormatter implements IFormatter<Locale> {

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
