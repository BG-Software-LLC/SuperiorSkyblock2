package com.bgsoftware.superiorskyblock.utils;

import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import com.google.common.base.Preconditions;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Locale;
import java.util.regex.Pattern;

public final class LocaleUtils {

    private static final Pattern RTL_LOCALE_PATTERN = Pattern.compile(
            "^(ar|dv|he|iw|fa|nqo|ps|sd|ug|ur|yi|.*[-_](Arab|Hebr|Thaa|Nkoo|Tfng))(?!.*[-_](Latn|Cyrl)($|-|_))($|-|_)");
    private static final Pattern LOCALE_PATTERN = Pattern.compile("^[a-z]{2}[_|-][A-Z]{2}$");

    public static Locale getLocale(CommandSender sender){
        return sender instanceof Player ? SSuperiorPlayer.of(sender).getUserLocale() : com.bgsoftware.superiorskyblock.Locale.getDefaultLocale();
    }

    public static Locale getLocale(SuperiorPlayer superiorPlayer){
        return superiorPlayer.getUserLocale();
    }

    public static java.util.Locale getLocale(String str) throws IllegalArgumentException{
        str = str.replace("_", "-");

        Preconditions.checkArgument(LOCALE_PATTERN.matcher(str).matches(), "String " + str + " is not a valid language.");

        String[] numberFormatSections = str.split("-");

        return new java.util.Locale(numberFormatSections[0], numberFormatSections[1]);
    }

    public static boolean isRightToLeft(Locale locale){
        return RTL_LOCALE_PATTERN.matcher(locale.getLanguage()).matches();
    }

    public static Locale getDefault(){
        return com.bgsoftware.superiorskyblock.Locale.getDefaultLocale();
    }

}
