package com.bgsoftware.superiorskyblock.api.service.message;

import org.bukkit.configuration.file.YamlConfiguration;

public interface MessagesService {

    /**
     * Parse a message from config file into a message component.
     *
     * @param config The configuration file.
     * @param path   The path of the message.
     * @return the parsed component message.
     */
    IMessageComponent parseComponent(YamlConfiguration config, String path);

}
