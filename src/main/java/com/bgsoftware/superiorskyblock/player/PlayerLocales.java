package com.bgsoftware.superiorskyblock.player;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.google.common.base.Preconditions;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

public class PlayerLocales {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private static final Pattern RTL_LOCALE_PATTERN = Pattern.compile(
            "^(ar|dv|he|iw|fa|nqo|ps|sd|ug|ur|yi|.*[-_](Arab|Hebr|Thaa|Nkoo|Tfng))(?!.*[-_](Latn|Cyrl)($|-|_))($|-|_)");
    private static final Pattern LOCALE_PATTERN = Pattern.compile("^[a-zA-Z]{2}[_|-][a-zA-Z]{2}$");

    private static final Set<Locale> locales = new HashSet<>();
    private static java.util.Locale defaultLocale = null;

    private PlayerLocales() {

    }

    public static Locale getLocale(CommandSender sender) {
        return sender instanceof Player ? plugin.getPlayers().getSuperiorPlayer(sender).getUserLocale() : defaultLocale;
    }

    public static java.util.Locale getLocale(String str) throws IllegalArgumentException {
        str = str.replace("_", "-");

        Preconditions.checkArgument(LOCALE_PATTERN.matcher(str).matches(), "String " + str + " is not a valid language.");

        String[] numberFormatSections = str.split("-");

        return new java.util.Locale(numberFormatSections[0], numberFormatSections[1]);
    }

    public static boolean isRightToLeft(Locale locale) {
        return RTL_LOCALE_PATTERN.matcher(locale.getLanguage()).matches();
    }

    public static void clearLocales() {
        locales.clear();
    }

    public static void registerLocale(java.util.Locale locale) {
        if (locales.isEmpty())
            defaultLocale = locale;
        locales.add(locale);
    }

    public static java.util.Locale getDefaultLocale() {
        return defaultLocale;
    }

    public static void setDefaultLocale(Locale defaultLocale) {
        PlayerLocales.defaultLocale = defaultLocale;
    }

    public static boolean isValidLocale(java.util.Locale locale) {
        return locales.contains(locale);
    }

}
