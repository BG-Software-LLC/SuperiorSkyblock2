package com.bgsoftware.superiorskyblock.utils;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandFlag;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class StringUtils {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private static final double Q = 1000000000000000D, T = 1000000000000D, B = 1000000000D, M = 1000000D, K = 1000D;
    private static final long D = 86400000L, H = 3600000L, MIN = 60000L, S = 1000L;
    private static final char SPACE_ASCII = 160;

    @SuppressWarnings("all")
    private static final Pattern STRIP_COLOR_PATTERN = Pattern.compile("(?i)(&|" + ChatColor.COLOR_CHAR + ")[0-9A-FK-OR]");
    @SuppressWarnings("all")
    private static final Pattern HEX_COLOR_PATTERN = Pattern.compile("(&|§)(\\{HEX:([0-9A-Fa-f]*)\\})");
    private static final Pattern NUMBER_PATTERN = Pattern.compile("^[a-z]{2}[_|-][A-Z]{2}$");
    private static Pattern DECIMAL_PATTERN;

    private static DecimalFormat numberFormatter;
    private static char decimalSeparator;
    private static SimpleDateFormat dateFormatter;

    private StringUtils(){

    }

    public static void setNumberFormatter(String numberFormat){
        numberFormat = numberFormat.replace("_", "-");

        if(!NUMBER_PATTERN.matcher(numberFormat).matches()){
            SuperiorSkyblockPlugin.log("&cThe number format \"" + numberFormat + "\" is invalid. Using default one: en-US.");
            numberFormat = "en-US";
        }

        String[] numberFormatSections = numberFormat.split("-");
        numberFormatter = (DecimalFormat) NumberFormat.getInstance(new java.util.Locale(numberFormatSections[0], numberFormatSections[1]));
        numberFormatter.setGroupingUsed(true);
        numberFormatter.setMinimumFractionDigits(2);
        numberFormatter.setMaximumFractionDigits(2);
        numberFormatter.setRoundingMode(RoundingMode.FLOOR);

        decimalSeparator = numberFormatter.getDecimalFormatSymbols().getDecimalSeparator();

        DECIMAL_PATTERN = Pattern.compile("(.*)" + Pattern.quote(decimalSeparator + "") + "(\\d)0");
    }

    public static void setDateFormatter(String dateFormat){
        dateFormatter = new SimpleDateFormat(dateFormat);
        try {
            for (Island island : plugin.getGrid().getIslands()) {
                island.updateDatesFormatter();
            }
        }catch (Exception ignored){}
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

    public static String format(BigInteger bigInteger){
        return format(new BigDecimal(bigInteger));
    }

    public static String format(BigDecimal bigDecimal){
        String s = numberFormatter.format(bigDecimal);

        //Because of some issues with formatting, spaces are converted to ascii 160.
        s = s.replace(SPACE_ASCII, ' ');

        Matcher matcher;

        if(s.endsWith(decimalSeparator + "00")){
            return s.replace(decimalSeparator + "00", "");
        }

        else if((matcher = DECIMAL_PATTERN.matcher(s)).matches()){
            return s.replaceAll(Pattern.quote(decimalSeparator + "") + "(\\d)0", decimalSeparator + matcher.group(2));
        }

        return s;
    }

    public static String fancyFormat(BigDecimal bigDecimal, java.util.Locale locale){
        double d = bigDecimal.doubleValue();
        if(d >= Q)
            return format((d / Q)) + Locale.FORMAT_QUAD.getMessage(locale);
        else if(d >= T)
            return format((d / T)) + Locale.FORMAT_TRILLION.getMessage(locale);
        else if(d >= B)
            return format((d / B)) + Locale.FORMAT_BILLION.getMessage(locale);
        else if(d >= M)
            return format((d / M)) + Locale.FORMAT_MILLION.getMessage(locale);
        else if(d >= K)
            return format((d / K)) + Locale.FORMAT_THOUSANDS.getMessage(locale);
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
        for(int i = 5; i > rating && i > 0; i--)
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
                timeBuilder.insert(0, timeUnitValue).insert(0, " ").insert(0, timeUnitValue == 1 ? Locale.FORMAT_DAY_NAME.getMessage(locale) :
                        Locale.FORMAT_DAYS_NAME.getMessage(locale)).insert(0, ", ");
            }
            else {
                timeBuilder.append(timeUnitValue).append(" ").append(timeUnitValue == 1 ? Locale.FORMAT_DAY_NAME.getMessage(locale) :
                        Locale.FORMAT_DAYS_NAME.getMessage(locale)).append(", ");
            }
            time %= D;
        }

        if(time > H){
            timeUnitValue = time / H;
            if(RTL){
                timeBuilder.insert(0, timeUnitValue).insert(0, " ").insert(0, timeUnitValue == 1 ? Locale.FORMAT_HOUR_NAME.getMessage(locale) :
                        Locale.FORMAT_HOURS_NAME.getMessage(locale)).insert(0, ", ");
            }
            else {
                timeBuilder.append(timeUnitValue).append(" ").append(timeUnitValue == 1 ? Locale.FORMAT_HOUR_NAME.getMessage(locale) :
                        Locale.FORMAT_HOURS_NAME.getMessage(locale)).append(", ");
            }
            time %= H;
        }

        if(time > MIN){
            timeUnitValue = time / MIN;
            if(RTL){
                timeBuilder.insert(0, timeUnitValue).insert(0, " ").insert(0, timeUnitValue == 1 ? Locale.FORMAT_MINUTE_NAME.getMessage(locale) :
                        Locale.FORMAT_MINUTES_NAME.getMessage(locale)).insert(0, " ,");
            }
            else {
                timeBuilder.append(timeUnitValue).append(" ").append(timeUnitValue == 1 ? Locale.FORMAT_MINUTE_NAME.getMessage(locale) :
                        Locale.FORMAT_MINUTES_NAME.getMessage(locale)).append(", ");
            }
            time %= MIN;
        }

        if(time > S){
            timeUnitValue = time / S;
            if(RTL){
                timeBuilder.insert(0, timeUnitValue).insert(0, " ").insert(0, timeUnitValue == 1 ? Locale.FORMAT_SECOND_NAME.getMessage(locale) :
                        Locale.FORMAT_SECONDS_NAME.getMessage(locale)).insert(0, " ,");
            }
            else {
                timeBuilder.append(timeUnitValue).append(" ").append(timeUnitValue == 1 ? Locale.FORMAT_SECOND_NAME.getMessage(locale) :
                        Locale.FORMAT_SECONDS_NAME.getMessage(locale)).append(", ");
            }
        }

        if(timeBuilder.length() == 0){
            if(RTL){
                timeBuilder.insert(0, "1 ").append(Locale.FORMAT_SECOND_NAME.getMessage(locale)).insert(0, " ,");
            }
            else {
                timeBuilder.append("1 ").append(Locale.FORMAT_SECOND_NAME.getMessage(locale)).append(", ");
            }
        }

        return RTL ? timeBuilder.substring(2) : timeBuilder.substring(0, timeBuilder.length() - 2);
    }

    public static long parseLong(String str){
        try{
            return Long.parseLong(str);
        }catch(Exception ex){
            return 0;
        }
    }

    public static String format(java.util.Locale locale){
        return locale.getLanguage() + "-" + locale.getCountry();
    }

    public static String translateColors(String string){
        String output = ChatColor.translateAlternateColorCodes('&', string);

        if(ServerVersion.isLessThan(ServerVersion.v1_16))
            return output;

        while(true) {
            Matcher matcher = HEX_COLOR_PATTERN.matcher(output);

            if(!matcher.find())
                break;

            output = matcher.replaceFirst(parseHexColor(matcher.group(3)));
        }

        return output;
    }

    public static List<String> translateColors(List<String> list){
        return list.stream().map(StringUtils::translateColors).collect(Collectors.toList());
    }

    public static String stripColors(String str){
        return str == null ? null : STRIP_COLOR_PATTERN.matcher(str).replaceAll("");
    }

    public static boolean isValidName(SuperiorPlayer superiorPlayer, Island currentIsland, String islandName){
        return isValidName(superiorPlayer.asPlayer(), currentIsland, islandName);
    }

    public static boolean isValidName(CommandSender sender, Island currentIsland, String islandName){
        String coloredName = plugin.getSettings().islandNamesColorSupport ?
                StringUtils.translateColors(islandName) : islandName;
        String strippedName = plugin.getSettings().islandNamesColorSupport ?
                StringUtils.stripColors(coloredName) : islandName;

        if(strippedName.length() > plugin.getSettings().islandNamesMaxLength){
            Locale.NAME_TOO_LONG.send(sender);
            return false;
        }

        if(strippedName.length() < plugin.getSettings().islandNamesMinLength){
            Locale.NAME_TOO_SHORT.send(sender);
            return false;
        }

        if(plugin.getSettings().islandNamesPreventPlayerNames && plugin.getPlayers().getSuperiorPlayer(strippedName) != null){
            Locale.NAME_SAME_AS_PLAYER.send(sender);
            return false;
        }

        if(plugin.getSettings().filteredIslandNames.stream().anyMatch(name -> islandName.toLowerCase().contains(name.toLowerCase()))){
            Locale.NAME_BLACKLISTED.send(sender);
            return false;
        }

        if(currentIsland != null && currentIsland.getName().equalsIgnoreCase(islandName)){
            Locale.SAME_NAME_CHANGE.send(sender);
            return false;
        }

        if(plugin.getGrid().getIsland(islandName) != null){
            Locale.ISLAND_ALREADY_EXIST.send(sender);
            return false;
        }

        return true;
    }

    public static String formatDate(long time){
        return dateFormatter.format(new Date(time));
    }

    public static String removeNonAlphabet(String text, List<Character> allowedChars){
        StringBuilder newText = new StringBuilder();

        for(char ch : text.toCharArray()){
            if((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') ||
                    (ch >= '1' && ch <= '9') || allowedChars.contains(ch))
                newText.append(ch);
        }

        return newText.toString();
    }

    private static String parseHexColor(String hexColor){
        if(hexColor.length() != 6 && hexColor.length() != 3)
            return hexColor;

        StringBuilder magic = new StringBuilder(ChatColor.COLOR_CHAR + "x");
        int multiplier = hexColor.length() == 3 ? 2 : 1;

        for(char ch : hexColor.toCharArray()) {
            for(int i = 0; i < multiplier; i++)
                magic.append(ChatColor.COLOR_CHAR).append(ch);
        }

        return magic.toString();
    }

}
