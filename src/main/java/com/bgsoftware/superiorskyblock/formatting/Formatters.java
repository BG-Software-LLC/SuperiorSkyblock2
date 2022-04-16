package com.bgsoftware.superiorskyblock.formatting;

import com.bgsoftware.superiorskyblock.api.enums.BorderColor;
import com.bgsoftware.superiorskyblock.formatting.impl.BorderColorFormatter;
import com.bgsoftware.superiorskyblock.formatting.impl.CapitalizedFormatter;
import com.bgsoftware.superiorskyblock.formatting.impl.ColorFormatter;
import com.bgsoftware.superiorskyblock.formatting.impl.CommaFormatter;
import com.bgsoftware.superiorskyblock.formatting.impl.DateFormatter;
import com.bgsoftware.superiorskyblock.formatting.impl.FancyNumberFormatter;
import com.bgsoftware.superiorskyblock.formatting.impl.LocaleFormatter;
import com.bgsoftware.superiorskyblock.formatting.impl.NumberFormatter;
import com.bgsoftware.superiorskyblock.formatting.impl.RatingFormatter;
import com.bgsoftware.superiorskyblock.formatting.impl.StripColorFormatter;
import com.bgsoftware.superiorskyblock.formatting.impl.TimeFormatter;

import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Formatters {

    public static final ILocaleFormatter<BorderColor> BORDER_COLOR_FORMATTER = BorderColorFormatter.getInstance();
    public static final IFormatter<String> CAPITALIZED_FORMATTER = CapitalizedFormatter.getInstance();
    public static final IFormatter<String> COLOR_FORMATTER = ColorFormatter.getInstance();
    public static final IFormatter<Stream<String>> COMMA_FORMATTER = CommaFormatter.getInstance();
    public static final IFormatter<Date> DATE_FORMATTER = DateFormatter.getInstance();
    public static final ILocaleFormatter<Number> FANCY_NUMBER_FORMATTER = FancyNumberFormatter.getInstance();
    public static final IFormatter<Locale> LOCALE_FORMATTER = LocaleFormatter.getInstance();
    public static final IFormatter<Number> NUMBER_FORMATTER = NumberFormatter.getInstance();
    public static final ILocaleFormatter<Number> RATING_FORMATTER = RatingFormatter.getInstance();
    public static final IFormatter<String> STRIP_COLOR_FORMATTER = StripColorFormatter.getInstance();
    public static final ILocaleFormatter<Duration> TIME_FORMATTER = TimeFormatter.getInstance();

    private Formatters() {

    }

    public static <T> List<String> formatList(List<T> list, IFormatter<T> formatter) {
        return list.stream().map(formatter::format).collect(Collectors.toList());
    }

}
