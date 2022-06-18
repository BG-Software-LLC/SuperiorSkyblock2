package com.bgsoftware.superiorskyblock.core.formatting.impl;

import com.bgsoftware.superiorskyblock.api.enums.BorderColor;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.core.formatting.ILocaleFormatter;

import java.util.EnumMap;
import java.util.Locale;

public class BorderColorFormatter implements ILocaleFormatter<BorderColor> {

    private static final BorderColorFormatter INSTANCE = new BorderColorFormatter();

    private static final EnumMap<BorderColor, Message> BORDER_COLOR_MESSAGES = new EnumMap<>(BorderColor.class);

    static {
        BORDER_COLOR_MESSAGES.put(BorderColor.RED, Message.BORDER_PLAYER_COLOR_NAME_RED);
        BORDER_COLOR_MESSAGES.put(BorderColor.BLUE, Message.BORDER_PLAYER_COLOR_NAME_BLUE);
        BORDER_COLOR_MESSAGES.put(BorderColor.GREEN, Message.BORDER_PLAYER_COLOR_NAME_GREEN);
    }

    public static BorderColorFormatter getInstance() {
        return INSTANCE;
    }

    private BorderColorFormatter() {

    }

    @Override
    public String format(BorderColor value, Locale locale) {
        return BORDER_COLOR_MESSAGES.get(value).getMessage(locale);
    }

}
