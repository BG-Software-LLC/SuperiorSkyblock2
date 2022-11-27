package com.bgsoftware.superiorskyblock.api.menu.parser;

import org.bukkit.configuration.file.YamlConfiguration;

/**
 * In case when calling {@link MenuParser#parseMenu(YamlConfiguration)} and it failed to parse the menu,
 * this exception will be thrown with an appropriate message.
 */
public class MenuParseException extends Exception {

    public MenuParseException(String message) {
        super(message);
    }

}
