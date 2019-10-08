package com.bgsoftware.superiorskyblock.utils;

import com.bgsoftware.superiorskyblock.Locale;

import java.math.BigDecimal;
import java.text.DecimalFormat;

public final class StringUtils {

    private static DecimalFormat numberFormatter = new DecimalFormat("###,###,###,###,###,###,###,###,###,##0.00");
    private static final double Q = 1000000000000000D, T = 1000000000000D, B = 1000000000D, M = 1000000, K = 1000D;

    public static String format(String type){
        StringBuilder formattedKey = new StringBuilder();

        for(String subKey : type.split("_"))
            formattedKey.append(" ").append(subKey.substring(0, 1).toUpperCase()).append(subKey.substring(1).toLowerCase());

        return formattedKey.toString().substring(1);
    }

    public static String format(double d){
        return format(BigDecimal.valueOf(d));
    }

    public static String format(BigDecimal bigDecimal){
        String s = numberFormatter.format(Double.parseDouble(bigDecimal instanceof BigDecimalFormatted ?
                ((BigDecimalFormatted) bigDecimal).getAsString() : bigDecimal.toString()));
        return s.endsWith(".00") ? s.replace(".00", "") : s;
    }

    public static String fancyFormat(BigDecimal bigDecimal){
        double d = bigDecimal.doubleValue();
        if(d > Q)
            return format((d / Q)) + "Q";
        else if(d > T)
            return format((d / T)) + "T";
        else if(d > B)
            return format((d / B)) + "B";
        else if(d > M)
            return format((d / M)) + "M";
        else if(d > K)
            return format((d / K)) + "K";
        else
            return format(d);
    }

    public static String formatRating(double rating){
        StringBuilder starsString = new StringBuilder();
        if(rating >= 1)
            starsString.append(Locale.ISLAND_INFO_RATE_ONE_COLOR.getMessage()).append(Locale.ISLAND_INFO_RATE_SYMBOL.getMessage());
        if(rating >= 2)
            starsString.append(Locale.ISLAND_INFO_RATE_TWO_COLOR.getMessage()).append(Locale.ISLAND_INFO_RATE_SYMBOL.getMessage());
        if(rating >= 3)
            starsString.append(Locale.ISLAND_INFO_RATE_THREE_COLOR.getMessage()).append(Locale.ISLAND_INFO_RATE_SYMBOL.getMessage());
        if(rating >= 4)
            starsString.append(Locale.ISLAND_INFO_RATE_FOUR_COLOR.getMessage()).append(Locale.ISLAND_INFO_RATE_SYMBOL.getMessage());
        if(rating >= 5)
            starsString.append(Locale.ISLAND_INFO_RATE_FIVE_COLOR.getMessage()).append(Locale.ISLAND_INFO_RATE_SYMBOL.getMessage());
        for(int i = 5; i > rating; i--)
            starsString.append(Locale.ISLAND_INFO_RATE_EMPTY_SYMBOL.getMessage());

        return starsString.toString();
    }

}
