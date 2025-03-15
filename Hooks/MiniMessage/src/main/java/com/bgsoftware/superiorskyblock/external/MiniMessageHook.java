package com.bgsoftware.superiorskyblock.external;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.core.formatting.impl.ColorFormatter;
import com.bgsoftware.superiorskyblock.external.text.ITextFormatter;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MiniMessageHook {

    private static final Map<String, String> COLORS_MAPPINGS = new LinkedHashMap<>();

    private static boolean registered = false;

    static {
        COLORS_MAPPINGS.put("0", "black");
        COLORS_MAPPINGS.put("1", "dark_blue");
        COLORS_MAPPINGS.put("2", "dark_green");
        COLORS_MAPPINGS.put("3", "dark_aqua");
        COLORS_MAPPINGS.put("4", "dark_red");
        COLORS_MAPPINGS.put("5", "dark_purple");
        COLORS_MAPPINGS.put("6", "gold");
        COLORS_MAPPINGS.put("7", "gray");
        COLORS_MAPPINGS.put("8", "dark_gray");
        COLORS_MAPPINGS.put("9", "blue");
        COLORS_MAPPINGS.put("a", "green");
        COLORS_MAPPINGS.put("A", "green");
        COLORS_MAPPINGS.put("b", "aqua");
        COLORS_MAPPINGS.put("B", "aqua");
        COLORS_MAPPINGS.put("c", "red");
        COLORS_MAPPINGS.put("C", "red");
        COLORS_MAPPINGS.put("d", "light_purple");
        COLORS_MAPPINGS.put("D", "light_purple");
        COLORS_MAPPINGS.put("e", "yellow");
        COLORS_MAPPINGS.put("E", "yellow");
        COLORS_MAPPINGS.put("f", "white");
        COLORS_MAPPINGS.put("F", "white");
        COLORS_MAPPINGS.put("k", "obfuscated");
        COLORS_MAPPINGS.put("K", "obfuscated");
        COLORS_MAPPINGS.put("l", "bold");
        COLORS_MAPPINGS.put("L", "bold");
        COLORS_MAPPINGS.put("m", "strikethrough");
        COLORS_MAPPINGS.put("M", "strikethrough");
        COLORS_MAPPINGS.put("n", "underlined");
        COLORS_MAPPINGS.put("N", "underlined");
        COLORS_MAPPINGS.put("o", "italic");
        COLORS_MAPPINGS.put("O", "italic");
        COLORS_MAPPINGS.put("r", "reset");
        COLORS_MAPPINGS.put("R", "reset");
    }

    public static void register(SuperiorSkyblockPlugin plugin) {
        if (!registered) {
            ColorFormatter.addTextFormatter(MiniMessageTextFormatter.INSTANCE);
            registered = true;
        }
    }

    private static class MiniMessageTextFormatter implements ITextFormatter {

        private static final Pattern HEX_PATTERN = Pattern.compile("(?<!<)(?<!:)#([a-fA-F0-9]{6})");

        private static final MiniMessageTextFormatter INSTANCE = new MiniMessageTextFormatter();

        private static final MiniMessage miniMessage = MiniMessage.builder().tags(StandardTags.defaults()).build();

        @Override
        public String format(String value) {
            return LegacyComponentSerializer.legacySection().serialize(miniMessage.deserialize(colorMiniMessage(value)));
        }

        private static boolean isBold(String value) {
            value = value.toLowerCase(Locale.ENGLISH);
            return value.contains("&l") || value.contains(ChatColor.BOLD.toString());
        }

        private static boolean isColorized(String value) {
            return value.indexOf('&') >= 0 || value.indexOf(ChatColor.COLOR_CHAR) >= 0;
        }

        private String colorMiniMessage(String message) {
            StringBuilder stringBuilder = new StringBuilder();

            Matcher matcher = HEX_PATTERN.matcher(message);

            while (matcher.find()) {
                matcher.appendReplacement(stringBuilder, "<$0>");
            }
            matcher.appendTail(stringBuilder);

            message = stringBuilder.toString();

            if (isColorized(message)) {
                StringBuilder newMessage = new StringBuilder();

                boolean lastWordIsBold = false;

                for (String word : message.split(" ")) {
                    if (isColorized(word)) {
                        boolean isBold = isBold(word);
                        for (Map.Entry<String, String> entry : COLORS_MAPPINGS.entrySet()) {
                            String key = entry.getKey();
                            String value = "<" + entry.getValue() + ">";
                            word = word.replace("&" + key, value).replace(ChatColor.COLOR_CHAR + key, value);
                        }

                        if (lastWordIsBold && !isBold)
                            newMessage.append("</bold>");

                        lastWordIsBold = isBold;
                    }

                    newMessage.append(" ").append(word);
                }

                message = newMessage.substring(1);
            }

            return message;
        }

    }

}
