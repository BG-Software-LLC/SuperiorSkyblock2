package com.bgsoftware.superiorskyblock.core.formatting.impl;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.core.formatting.IFormatter;
import com.bgsoftware.superiorskyblock.core.logging.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateFormatter implements IFormatter<Date> {

    private static final DateFormatter INSTANCE = new DateFormatter();
    private static final String DEFAULT_DATE_FORMAT = "dd/MM/yyyy HH:mm:ss";

    // SimpleDateFormat is not thread-safe and format() is called from async (placeholder)
    // threads, so each thread gets its own instance via a ThreadLocal. The ThreadLocal is
    // swapped out when the format changes so threads lazily rebuild with the new pattern.
    private static volatile ThreadLocal<SimpleDateFormat> dateFormatter;

    public static void setDateFormatter(SuperiorSkyblockPlugin plugin, String dateFormat) {
        SimpleDateFormat newDateFormatter;
        try {
            newDateFormatter = new SimpleDateFormat(dateFormat);
        } catch (IllegalArgumentException error) {
            Log.warn("The date format \"", dateFormat, "\" is invalid (", error.getMessage(), "). Using default one: ", DEFAULT_DATE_FORMAT, ".");
            newDateFormatter = new SimpleDateFormat(DEFAULT_DATE_FORMAT);
        }

        SimpleDateFormat dateFormatterTemplate = newDateFormatter;
        dateFormatter = ThreadLocal.withInitial(() -> (SimpleDateFormat) dateFormatterTemplate.clone());

        try {
            for (Island island : plugin.getGrid().getIslands()) {
                island.updateDatesFormatter();
            }
        } catch (Exception ignored) {
        }
    }

    public static DateFormatter getInstance() {
        return INSTANCE;
    }

    private DateFormatter() {

    }

    @Override
    public String format(Date value) {
        return dateFormatter.get().format(value);
    }

}
