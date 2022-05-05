package com.bgsoftware.superiorskyblock.service.message;

import com.bgsoftware.superiorskyblock.api.service.message.IMessageComponent;
import com.bgsoftware.superiorskyblock.api.service.message.MessagesService;
import com.bgsoftware.superiorskyblock.formatting.Formatters;
import com.bgsoftware.superiorskyblock.lang.component.MultipleComponents;
import com.bgsoftware.superiorskyblock.lang.component.impl.RawMessageComponent;
import org.bukkit.configuration.file.YamlConfiguration;

import javax.annotation.Nullable;

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

}
