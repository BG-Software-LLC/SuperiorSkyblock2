package com.bgsoftware.superiorskyblock.core.formatting.impl;

import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.player.PlayerLocales;
import com.bgsoftware.superiorskyblock.core.formatting.ILocaleFormatter;

import java.time.Duration;
import java.util.Locale;

public class TimeFormatter implements ILocaleFormatter<Duration> {

    private static final TimeFormatter INSTANCE = new TimeFormatter();

    public static TimeFormatter getInstance() {
        return INSTANCE;
    }

    private TimeFormatter() {

    }

    @Override
    public String format(Duration value, Locale locale) {
        Locale formatLocale = locale == null ? PlayerLocales.getDefaultLocale() : locale;

        StringBuilder timeBuilder = new StringBuilder();
        boolean RTL = PlayerLocales.isRightToLeft(formatLocale);

        {
            long days = value.toDays();
            if (days > 0) {
                formatTimeSection(timeBuilder, RTL, days, days == 1 ? Message.FORMAT_DAY_NAME : Message.FORMAT_DAYS_NAME, formatLocale);
                value = value.minusDays(days);
            }
        }

        {
            long hours = value.toHours();
            if (hours > 0) {
                formatTimeSection(timeBuilder, RTL, hours, hours == 1 ? Message.FORMAT_HOUR_NAME : Message.FORMAT_HOURS_NAME, formatLocale);
                value = value.minusHours(hours);
            }
        }

        {
            long minutes = value.toMinutes();
            if (minutes > 0) {
                formatTimeSection(timeBuilder, RTL, minutes, minutes == 1 ? Message.FORMAT_MINUTE_NAME : Message.FORMAT_MINUTES_NAME, formatLocale);
                value = value.minusMinutes(minutes);
            }
        }

        {
            long seconds = value.getSeconds();
            if (seconds > 0)
                formatTimeSection(timeBuilder, RTL, seconds, seconds == 1 ? Message.FORMAT_SECOND_NAME : Message.FORMAT_SECONDS_NAME, formatLocale);
        }

        if (timeBuilder.length() == 0) {
            if (RTL) {
                timeBuilder.insert(0, "1 ").append(Message.FORMAT_SECOND_NAME.getMessage(formatLocale)).insert(0, " ,");
            } else {
                timeBuilder.append("1 ").append(Message.FORMAT_SECOND_NAME.getMessage(formatLocale)).append(", ");
            }
        }

        return RTL ? timeBuilder.substring(2) : timeBuilder.substring(0, timeBuilder.length() - 2);
    }

    private static void formatTimeSection(StringBuilder stringBuilder, boolean RTL, long value,
                                          Message timeFormatMessage, Locale locale) {
        if (RTL) {
            stringBuilder.insert(0, value)
                    .insert(0, " ")
                    .insert(0, timeFormatMessage.getMessage(locale))
                    .insert(0, ", ");
        } else {
            stringBuilder.append(value)
                    .append(" ")
                    .append(timeFormatMessage.getMessage(locale))
                    .append(", ");
        }
    }

}
