package com.bgsoftware.superiorskyblock.utils;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public final class StringUtil {

    private static NumberFormat numberFormatter = new DecimalFormat("###,###,###,###,###,###,###,###,###,##0.00");

    public static String format(String type){
        StringBuilder formattedKey = new StringBuilder();

        for(String subKey : type.split("_"))
            formattedKey.append(" ").append(subKey.substring(0, 1).toUpperCase()).append(subKey.substring(1).toLowerCase());

        return formattedKey.toString().substring(1);
    }

    public static String format(double d){
        String s = numberFormatter.format(d);
        return s.endsWith(".00") ? s.replace(".00", "") : s;
    }

}
