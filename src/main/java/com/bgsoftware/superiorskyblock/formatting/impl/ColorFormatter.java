package com.bgsoftware.superiorskyblock.formatting.impl;

import com.bgsoftware.superiorskyblock.formatting.IFormatter;
import com.bgsoftware.superiorskyblock.utils.ServerVersion;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import org.bukkit.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ColorFormatter implements IFormatter<String> {

    private static final ColorFormatter INSTANCE = new ColorFormatter();

    private static final Pattern HEX_COLOR_PATTERN = Pattern.compile("([&§])(\\{HEX:([0-9A-Fa-f]*)})");

    public static ColorFormatter getInstance() {
        return INSTANCE;
    }

    private ColorFormatter() {

    }

    @Override
    public String format(String value) {
        if (StringUtils.isBlank(value))
            return "";

        String output = ChatColor.translateAlternateColorCodes('&', value);

        if (ServerVersion.isLessThan(ServerVersion.v1_16))
            return output;

        while (true) {
            Matcher matcher = HEX_COLOR_PATTERN.matcher(output);

            if (!matcher.find())
                break;

            output = matcher.replaceFirst(parseHexColor(matcher.group(3)));
        }

        return output;
    }

    private static String parseHexColor(String hexColor) {
        if (hexColor.length() != 6 && hexColor.length() != 3)
            return hexColor;

        StringBuilder magic = new StringBuilder(ChatColor.COLOR_CHAR + "x");
        int multiplier = hexColor.length() == 3 ? 2 : 1;

        for (char ch : hexColor.toCharArray()) {
            for (int i = 0; i < multiplier; i++)
                magic.append(ChatColor.COLOR_CHAR).append(ch);
        }

        return magic.toString();
    }

}
