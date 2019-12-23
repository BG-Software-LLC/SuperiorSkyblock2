package com.bgsoftware.superiorskyblock.utils;

import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Locale;
import java.util.regex.Pattern;

public final class LocaleUtils {

    private static final Locale englishLocale = new Locale("en", "US");
    private static final Pattern RTLLocalePattern = Pattern.compile(
            "^(ar|dv|he|iw|fa|nqo|ps|sd|ug|ur|yi|.*[-_](Arab|Hebr|Thaa|Nkoo|Tfng))(?!.*[-_](Latn|Cyrl)($|-|_))($|-|_)");

    public static Locale getLocale(CommandSender sender){
        return sender instanceof Player ? SSuperiorPlayer.of(sender).getUserLocale() : englishLocale;
    }

    public static Locale getLocale(SuperiorPlayer superiorPlayer){
        return superiorPlayer.getUserLocale();
    }

    public static java.util.Locale getLocale(String str) throws IllegalArgumentException{
        str = str.replace("_", "-");

        if(!Pattern.compile("^[a-z]{2}[_|-][A-Z]{2}$").matcher(str).matches())
            throw new IllegalArgumentException("String " + str + " is not a valid language.");

        String[] numberFormatSections = str.split("-");

        return new java.util.Locale(numberFormatSections[0], numberFormatSections[1]);
    }

    public static boolean isRightToLeft(Locale locale){
        return RTLLocalePattern.matcher(locale.getLanguage()).matches();
    }

    public static Locale getDefault(){
        return com.bgsoftware.superiorskyblock.Locale.getDefaultLocale();
    }

}
