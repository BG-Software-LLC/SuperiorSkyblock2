package com.bgsoftware.superiorskyblock.core.formatting.impl;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.core.formatting.IFormatter;
import com.bgsoftware.superiorskyblock.core.debug.PluginDebugger;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateFormatter implements IFormatter<Date> {

    private static final DateFormatter INSTANCE = new DateFormatter();

    private static SimpleDateFormat dateFormatter;

    public static void setDateFormatter(SuperiorSkyblockPlugin plugin, String dateFormat) {
        dateFormatter = new SimpleDateFormat(dateFormat);
        try {
            for (Island island : plugin.getGrid().getIslands()) {
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
