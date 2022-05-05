package com.bgsoftware.superiorskyblock.service.message;

import com.bgsoftware.superiorskyblock.api.service.message.IMessageComponent;
import com.bgsoftware.superiorskyblock.api.service.message.MessagesService;
import com.bgsoftware.superiorskyblock.formatting.Formatters;
import com.bgsoftware.superiorskyblock.lang.Message;
import com.bgsoftware.superiorskyblock.lang.component.MultipleComponents;
import com.bgsoftware.superiorskyblock.lang.component.impl.RawMessageComponent;
import org.bukkit.configuration.file.YamlConfiguration;

import javax.annotation.Nullable;
import java.util.Locale;

public final class MessagesServiceImpl implements MessagesService {

    public MessagesServiceImpl() {

    }

    @Nullable
    @Override
    public IMessageComponent parseComponent(YamlConfiguration config, String path) {
        if (config.isConfigurationSection(path)) {
            return MultipleComponents.parseSection(config.getConfigurationSection(path));
        } else {
            return RawMessageComponent.of(Formatters.COLOR_FORMATTER.format(config.getString(path, "")));
        }
    }

    @Nullable
    @Override
    public IMessageComponent getComponent(String messageName, Locale locale) {
        Message message;

        try {
            message = Message.valueOf(messageName.toUpperCase(Locale.ENGLISH));
        } catch (IllegalArgumentException error) {
            // The given name was invalid.
            return null;
        }

        return message.isCustom() ? null : message.getComponent(locale);
    }

}
