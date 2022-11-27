package com.bgsoftware.superiorskyblock.core.menu.converter;

import com.bgsoftware.superiorskyblock.core.menu.layout.RegularMenuLayoutImpl;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

public class MenuConverter {

    private MenuConverter() {

    }

    public static int convertFillItems(ConfigurationSection fillItemsSection, int charCounter, char[] patternChars, ConfigurationSection itemsSection, ConfigurationSection commandsSection, ConfigurationSection soundsSection) {
        for (String itemFill : fillItemsSection.getKeys(false)) {
            ConfigurationSection section = fillItemsSection.getConfigurationSection(itemFill);
            String[] slots = section.getString("slots").split(",");
            section.set("slots", null);

            char itemChar = RegularMenuLayoutImpl.BUTTON_SYMBOLS[charCounter++];
            for (String slot : slots) {
                patternChars[Integer.parseInt(slot)] = itemChar;
            }

            convertItem(section, patternChars, itemChar, itemsSection, commandsSection, soundsSection);
        }

        return charCounter;
    }

    public static void convertItem(ConfigurationSection section, char[] patternChars, char itemChar, ConfigurationSection itemsSection, ConfigurationSection commandsSection, ConfigurationSection soundsSection) {
        if (section.contains("slot")) {
            patternChars[section.getInt("slot")] = itemChar;
            section.set("slot", null);
        }

        soundsSection.set(itemChar + "", section.get("sound"));
        section.set("sound", null);

        commandsSection.set(itemChar + "", section.get("commands"));
        section.set("commands", null);

        itemsSection.set(itemChar + "", section);
    }

    public static void convertItemAccess(ConfigurationSection section, char[] patternChars, char itemChar, ConfigurationSection itemsSection, ConfigurationSection commandsSection, ConfigurationSection soundsSection) {
        patternChars[section.getInt("slot")] = itemChar;
        section.set("slot", null);

        section.set("access", section.getConfigurationSection("has-access-item"));
        soundsSection.set(itemChar + ".access", section.get("has-access-item.sound"));
        commandsSection.set(itemChar + ".access", section.get("has-access-item.commands"));
        section.set("has-access-item.sound", null);
        section.set("has-access-item.commands", null);
        section.set("has-access-item", null);

        section.set("no-access", section.getConfigurationSection("no-access-item"));
        soundsSection.set(itemChar + ".no-access", section.get("no-access-item.sound"));
        commandsSection.set(itemChar + ".no-access", section.get("no-access-item.commands"));
        section.set("no-access-item.sound", null);
        section.set("no-access-item.commands", null);
        section.set("no-access-item", null);

        itemsSection.set(itemChar + "", section);
    }

    public static void convertPagedButtons(ConfigurationSection section, ConfigurationSection newMenu, char[] patternChars, char slotsChar, char previousChar, char currentChar, char nextChar, ConfigurationSection itemsSection, ConfigurationSection commandsSection, ConfigurationSection soundsSection) {
        convertPagedButtons(section, null, newMenu, patternChars, slotsChar, previousChar, currentChar, nextChar, itemsSection, commandsSection, soundsSection);
    }

    public static void convertPagedButtons(ConfigurationSection section, ConfigurationSection itemSection, ConfigurationSection newMenu, char[] patternChars, char slotsChar, char previousChar, char currentChar, char nextChar, ConfigurationSection itemsSection, ConfigurationSection commandsSection, ConfigurationSection soundsSection) {
        String slots = itemSection != null ? itemSection.getString("slots") : section.getString("slots");

        if (slots != null) {
            for (String slot : slots.split(","))
                patternChars[Integer.parseInt(slot)] = slotsChar;
            if (itemSection != null)
                itemSection.set("slots", null);
            else
                section.set("slots", null);
        }

        if (itemSection != null) {
            MenuConverter.convertItem(itemSection, patternChars, slotsChar, itemsSection, commandsSection, soundsSection);
        }

        MenuConverter.convertItem(section.getConfigurationSection("previous-page"), patternChars, previousChar,
                itemsSection, commandsSection, soundsSection);
        MenuConverter.convertItem(section.getConfigurationSection("current-page"), patternChars, currentChar,
                itemsSection, commandsSection, soundsSection);
        MenuConverter.convertItem(section.getConfigurationSection("next-page"), patternChars, nextChar,
                itemsSection, commandsSection, soundsSection);

        newMenu.set("slots", slotsChar + "");
        newMenu.set("previous-page", previousChar + "");
        newMenu.set("current-page", currentChar + "");
        newMenu.set("next-page", nextChar + "");
    }

    public static List<String> buildPattern(int size, char[] patternChars, char replaceChar) {
        int lineLength = patternChars.length == 5 ? 5 : 9;
        int charCount = 0;

        List<String> pattern = new ArrayList<>(size);
        StringBuilder line = new StringBuilder();

        for (char ch : patternChars) {
            charCount++;
            line.append(" ").append(ch);
            if (charCount == lineLength) {
                charCount = 0;
                pattern.add(line.substring(1).replace('\n', replaceChar));
                line = new StringBuilder();
            }
        }

        return pattern;
    }

}
