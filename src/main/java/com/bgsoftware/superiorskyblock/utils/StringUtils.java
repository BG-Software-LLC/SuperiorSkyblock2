package com.bgsoftware.superiorskyblock.utils;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.IslandFlag;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import org.bukkit.ChatColor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class StringUtils {

    private static final double Q = 1000000000000000D, T = 1000000000000D, B = 1000000000D, M = 1000000D, K = 1000D;
    private static final long D = 86400000L, H = 3600000L, MIN = 60000L, S = 1000L;
    private static final char SPACE_ASCII = 160;

    private static final Pattern STRIP_COLOR_PATTERN = Pattern.compile("(?i)(&|" + ChatColor.COLOR_CHAR + ")[0-9A-FK-OR]");

    private static NumberFormat numberFormatter;

    public static void setNumberFormatter(String numberFormat){
        numberFormat = numberFormat.replace("_", "-");

        if(!Pattern.compile("^[a-z]{2}[_|-][A-Z]{2}$").matcher(numberFormat).matches()){
            SuperiorSkyblockPlugin.log("&cThe number format \"" + numberFormat + "\" is invalid. Using default one: en-US.");
            numberFormat = "en-US";
        }

        String[] numberFormatSections = numberFormat.split("-");
        numberFormatter = NumberFormat.getInstance(new java.util.Locale(numberFormatSections[0], numberFormatSections[1]));
        numberFormatter.setGroupingUsed(true);
        numberFormatter.setMinimumFractionDigits(2);
        numberFormatter.setMaximumFractionDigits(2);
        numberFormatter.setRoundingMode(RoundingMode.FLOOR);
    }

    public static String format(String type){
        StringBuilder formattedKey = new StringBuilder();

        try{
            String[] split = type.split(":");
            //Checking if the type is <TYPE>:<INTEGER>
            Integer.parseInt(split[1]);
            type = split[0];
        }catch(Throwable ignored){}

        type = type.replace(":", "_-_");

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

        //Because of some issues with formatting, spaces are converted to ascii 160.
        s = s.replace(SPACE_ASCII, ' ');

        Matcher matcher;

        if(s.endsWith(".00")){
            return s.replace(".00", "");
        }

        else if((matcher = Pattern.compile("(.*)\\.(\\d)0").matcher(s)).matches()){
            return s.replaceAll("\\.(\\d)0", "." + matcher.group(2));
        }

        return s;
    }

    public static String fancyFormat(double d){
        return fancyFormat(BigDecimal.valueOf(d));
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

    public static String formatRating(java.util.Locale locale, double rating){
        StringBuilder starsString = new StringBuilder();
        if(rating >= 1)
            starsString.append(Locale.ISLAND_INFO_RATE_ONE_COLOR.getMessage(locale)).append(Locale.ISLAND_INFO_RATE_SYMBOL.getMessage(locale));
        if(rating >= 2)
            starsString.append(Locale.ISLAND_INFO_RATE_TWO_COLOR.getMessage(locale)).append(Locale.ISLAND_INFO_RATE_SYMBOL.getMessage(locale));
        if(rating >= 3)
            starsString.append(Locale.ISLAND_INFO_RATE_THREE_COLOR.getMessage(locale)).append(Locale.ISLAND_INFO_RATE_SYMBOL.getMessage(locale));
        if(rating >= 4)
            starsString.append(Locale.ISLAND_INFO_RATE_FOUR_COLOR.getMessage(locale)).append(Locale.ISLAND_INFO_RATE_SYMBOL.getMessage(locale));
        if(rating >= 5)
            starsString.append(Locale.ISLAND_INFO_RATE_FIVE_COLOR.getMessage(locale)).append(Locale.ISLAND_INFO_RATE_SYMBOL.getMessage(locale));
        for(int i = 5; i > rating; i--)
            starsString.append(Locale.ISLAND_INFO_RATE_EMPTY_SYMBOL.getMessage(locale));

        return starsString.toString();
    }

    public static String getPermissionsString(){
        StringBuilder stringBuilder = new StringBuilder();
        IslandPrivilege.values().stream().sorted(Comparator.comparing(IslandPrivilege::getName)).forEach(islandPermission -> stringBuilder.append(", ").append(islandPermission.toString().toLowerCase()));
        return stringBuilder.toString().substring(2);
    }

    public static String getSettingsString(){
        StringBuilder stringBuilder = new StringBuilder();
        IslandFlag.values().stream().sorted(Comparator.comparing(IslandFlag::getName)).forEach(islandFlag -> stringBuilder.append(", ").append(islandFlag.getName().toLowerCase()));
        return stringBuilder.toString().substring(2);
    }

    public static String getUpgradesString(SuperiorSkyblockPlugin plugin){
        StringBuilder stringBuilder = new StringBuilder();
        plugin.getUpgrades().getUpgrades().forEach(upgrade -> stringBuilder.append(", ").append(upgrade.getName()));
        return stringBuilder.toString().substring(2);
    }

    public static String formatTime(java.util.Locale locale, long time){
        StringBuilder timeBuilder = new StringBuilder();
        long timeUnitValue;
        boolean RTL = LocaleUtils.isRightToLeft(locale);

        if(time > D){
            timeUnitValue = time / D;
            if(RTL){
                timeBuilder.insert(0, timeUnitValue).insert(0, " ").insert(0, timeUnitValue == 1 ? Locale.COMMAND_COOLDOWN_DAY_NAME.getMessage(locale) :
                        Locale.COMMAND_COOLDOWN_DAYS_NAME.getMessage(locale)).insert(0, ", ");
            }
            else {
                timeBuilder.append(timeUnitValue).append(" ").append(timeUnitValue == 1 ? Locale.COMMAND_COOLDOWN_DAY_NAME.getMessage(locale) :
                        Locale.COMMAND_COOLDOWN_DAYS_NAME.getMessage(locale)).append(", ");
            }
            time %= D;
        }

        if(time > H){
            timeUnitValue = time / H;
            if(RTL){
                timeBuilder.insert(0, timeUnitValue).insert(0, " ").insert(0, timeUnitValue == 1 ? Locale.COMMAND_COOLDOWN_HOUR_NAME.getMessage(locale) :
                        Locale.COMMAND_COOLDOWN_HOURS_NAME.getMessage(locale)).insert(0, ", ");
            }
            else {
                timeBuilder.append(timeUnitValue).append(" ").append(timeUnitValue == 1 ? Locale.COMMAND_COOLDOWN_HOUR_NAME.getMessage(locale) :
                        Locale.COMMAND_COOLDOWN_HOURS_NAME.getMessage(locale)).append(", ");
            }
            time %= H;
        }

        if(time > MIN){
            timeUnitValue = time / MIN;
            if(RTL){
                timeBuilder.insert(0, timeUnitValue).insert(0, " ").insert(0, timeUnitValue == 1 ? Locale.COMMAND_COOLDOWN_MINUTE_NAME.getMessage(locale) :
                        Locale.COMMAND_COOLDOWN_MINUTES_NAME.getMessage(locale)).insert(0, " ,");
            }
            else {
                timeBuilder.append(timeUnitValue).append(" ").append(timeUnitValue == 1 ? Locale.COMMAND_COOLDOWN_MINUTE_NAME.getMessage(locale) :
                        Locale.COMMAND_COOLDOWN_MINUTES_NAME.getMessage(locale)).append(", ");
            }
            time %= MIN;
        }

        if(time > S){
            timeUnitValue = time / S;
            if(RTL){
                timeBuilder.insert(0, timeUnitValue).insert(0, " ").insert(0, timeUnitValue == 1 ? Locale.COMMAND_COOLDOWN_SECOND_NAME.getMessage(locale) :
                        Locale.COMMAND_COOLDOWN_SECONDS_NAME.getMessage(locale)).insert(0, " ,");
            }
            else {
                timeBuilder.append(timeUnitValue).append(" ").append(timeUnitValue == 1 ? Locale.COMMAND_COOLDOWN_SECOND_NAME.getMessage(locale) :
                        Locale.COMMAND_COOLDOWN_SECONDS_NAME.getMessage(locale)).append(", ");
            }
        }

        if(timeBuilder.length() == 0){
            if(RTL){
                timeBuilder.insert(0, "1 ").append(Locale.COMMAND_COOLDOWN_SECOND_NAME.getMessage(locale)).insert(0, " ,");
            }
            else {
                timeBuilder.append("1 ").append(Locale.COMMAND_COOLDOWN_SECOND_NAME.getMessage(locale)).append(", ");
            }
        }

        return RTL ? timeBuilder.substring(2) : timeBuilder.substring(0, timeBuilder.length() - 2);
    }

    public static String stripColors(String str){
        return str == null ? null : STRIP_COLOR_PATTERN.matcher(str).replaceAll("");
    }

}
