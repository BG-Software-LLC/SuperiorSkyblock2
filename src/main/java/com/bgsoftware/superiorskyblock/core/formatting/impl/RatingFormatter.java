package com.bgsoftware.superiorskyblock.core.formatting.impl;

import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.core.formatting.ILocaleFormatter;

import java.util.Locale;

public class RatingFormatter implements ILocaleFormatter<Number> {

    private static final RatingFormatter INSTANCE = new RatingFormatter();

    public static RatingFormatter getInstance() {
        return INSTANCE;
    }

    private RatingFormatter() {

    }

    @Override
    public String format(Number value, Locale locale) {
        StringBuilder starsString = new StringBuilder();
        double rating = value.doubleValue();

        if (rating >= 1)
            starsString.append(Message.ISLAND_INFO_RATE_ONE_COLOR.getMessage(locale)).append(Message.ISLAND_INFO_RATE_SYMBOL.getMessage(locale));
        if (rating >= 2)
            starsString.append(Message.ISLAND_INFO_RATE_TWO_COLOR.getMessage(locale)).append(Message.ISLAND_INFO_RATE_SYMBOL.getMessage(locale));
        if (rating >= 3)
            starsString.append(Message.ISLAND_INFO_RATE_THREE_COLOR.getMessage(locale)).append(Message.ISLAND_INFO_RATE_SYMBOL.getMessage(locale));
        if (rating >= 4)
            starsString.append(Message.ISLAND_INFO_RATE_FOUR_COLOR.getMessage(locale)).append(Message.ISLAND_INFO_RATE_SYMBOL.getMessage(locale));
        if (rating >= 5)
            starsString.append(Message.ISLAND_INFO_RATE_FIVE_COLOR.getMessage(locale)).append(Message.ISLAND_INFO_RATE_SYMBOL.getMessage(locale));

        for (int i = 5; i > rating && i > 0; i--)
            starsString.append(Message.ISLAND_INFO_RATE_EMPTY_SYMBOL.getMessage(locale));

        return starsString.toString();
    }

}
