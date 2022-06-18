package com.bgsoftware.superiorskyblock.core.formatting.impl;

import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.formatting.ILocaleFormatter;

import java.util.Locale;

public class FancyNumberFormatter implements ILocaleFormatter<Number> {

    private static final FancyNumberFormatter INSTANCE = new FancyNumberFormatter();

    private static final double Q = 1000000000000000D;
    private static final double T = 1000000000000D;
    private static final double B = 1000000000D;
    private static final double M = 1000000D;
    private static final double K = 1000D;

    public static FancyNumberFormatter getInstance() {
        return INSTANCE;
    }

    private FancyNumberFormatter() {

    }

    @Override
    public String format(Number value, Locale locale) {
        double doubleValue = value.doubleValue();
        if (doubleValue >= Q)
            return Formatters.NUMBER_FORMATTER.format(doubleValue / Q) + Message.FORMAT_QUAD.getMessage(locale);
        else if (doubleValue >= T)
            return Formatters.NUMBER_FORMATTER.format(doubleValue / T) + Message.FORMAT_TRILLION.getMessage(locale);
        else if (doubleValue >= B)
            return Formatters.NUMBER_FORMATTER.format(doubleValue / B) + Message.FORMAT_BILLION.getMessage(locale);
        else if (doubleValue >= M)
            return Formatters.NUMBER_FORMATTER.format(doubleValue / M) + Message.FORMAT_MILLION.getMessage(locale);
        else if (doubleValue >= K)
            return Formatters.NUMBER_FORMATTER.format(doubleValue / K) + Message.FORMAT_THOUSANDS.getMessage(locale);
        else
            return Formatters.NUMBER_FORMATTER.format(value);
    }

}
