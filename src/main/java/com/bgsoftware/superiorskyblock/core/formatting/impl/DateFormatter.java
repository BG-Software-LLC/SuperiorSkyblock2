package com.bgsoftware.superiorskyblock.core.formatting.impl;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.core.formatting.IFormatter;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateFormatter implements IFormatter<Date> {

    private static final DateFormatter INSTANCE = new DateFormatter();

    // SimpleDateFormat is not thread-safe and format() is called from async (placeholder)
    // threads, so each thread gets its own instance via a ThreadLocal. The ThreadLocal is
    // swapped out when the format changes so threads lazily rebuild with the new pattern.
    private static volatile ThreadLocal<SimpleDateFormat> dateFormatter;

    public static void setDateFormatter(SuperiorSkyblockPlugin plugin, String dateFormat) {
        dateFormatter = ThreadLocal.withInitial(() -> new SimpleDateFormat(dateFormat));
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
