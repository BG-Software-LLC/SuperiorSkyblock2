package com.bgsoftware.superiorskyblock.core.formatting.impl;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.IslandBase;
import com.bgsoftware.superiorskyblock.core.debug.PluginDebugger;
import com.bgsoftware.superiorskyblock.core.formatting.IFormatter;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateFormatter implements IFormatter<Date> {

    private static final DateFormatter INSTANCE = new DateFormatter();

    private static SimpleDateFormat dateFormatter;

    public static void setDateFormatter(SuperiorSkyblockPlugin plugin, String dateFormat) {
        dateFormatter = new SimpleDateFormat(dateFormat);
        try {
            for (IslandBase island : plugin.getGrid().getBaseIslands()) {
                island.updateDatesFormatter();
            }
        } catch (Exception error) {
            PluginDebugger.debug(error);
        }
    }

    public static DateFormatter getInstance() {
        return INSTANCE;
    }

    private DateFormatter() {

    }

    @Override
    public String format(Date value) {
        return dateFormatter.format(value);
    }

}
