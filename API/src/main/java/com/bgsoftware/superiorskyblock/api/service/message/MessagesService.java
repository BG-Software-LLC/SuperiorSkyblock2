package com.bgsoftware.superiorskyblock.api.service.message;

import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.configuration.file.YamlConfiguration;

import javax.annotation.Nullable;
import java.util.Locale;

public interface MessagesService {

    /**
     * Parse a message from config file into a message component.
     *
     * @param config The configuration file.
     * @param path   The path of the message.
     * @return the parsed component message.
     */
    IMessageComponent parseComponent(YamlConfiguration config, String path);

    /**
     * Get a component of a built-in message of the plugin.
     *
     * @param message The name of the message, similar to its name in the lang file of the plugin.
     * @param locale  The locale to retrieve the message for.
     *                For player's locale, use {@link SuperiorPlayer#getUserLocale()}
     */
    @Nullable
    IMessageComponent getComponent(String message, Locale locale);

}
