package com.bgsoftware.superiorskyblock.service.message;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.service.bossbar.BossBar;
import com.bgsoftware.superiorskyblock.api.service.message.IMessageComponent;
import com.bgsoftware.superiorskyblock.api.service.message.MessagesService;
import com.bgsoftware.superiorskyblock.api.world.GameSound;
import com.bgsoftware.superiorskyblock.core.EnumHelper;
import com.bgsoftware.superiorskyblock.core.GameSoundImpl;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.core.messages.component.BossBarComponent;
import com.bgsoftware.superiorskyblock.core.messages.component.MultipleComponents;
import com.bgsoftware.superiorskyblock.core.messages.component.SoundComponent;
import com.bgsoftware.superiorskyblock.service.IService;
import com.google.common.base.Preconditions;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class MessagesServiceImpl implements MessagesService, IService {

    private final SuperiorSkyblockPlugin plugin;

    public MessagesServiceImpl(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public Class<?> getAPIClass() {
        return MessagesService.class;
    }

    @Nullable
    @Override
    public IMessageComponent parseComponent(YamlConfiguration config, String path) {
        if (config.isConfigurationSection(path)) {
            return MultipleComponents.parseSection(config.getConfigurationSection(path));
        } else {
            return plugin.getProviders().getUIProvider()
                    .createRawMessageComponent(Formatters.COLOR_FORMATTER.format(config.getString(path)));
        }
    }

    @Nullable
    @Override
    public IMessageComponent getComponent(String messageName, Locale locale) {
        Message message = EnumHelper.getEnum(Message.class, messageName.toUpperCase(Locale.ENGLISH));
        return message == null || message.isCustom() ? null : message.getComponent(locale);
    }

    @Override
    public Builder newBuilder() {
        return new BuilderImpl();
    }

    private static class BuilderImpl implements Builder {

        private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
        private final List<IMessageComponent> messageComponents = new LinkedList<>();

        @Override
        public boolean addActionBar(@Nullable String message) {
            return addMessageComponent(plugin.getProviders().getUIProvider()
                    .createActionBarComponent(message));
        }

        @Override
        public boolean addBossBar(@Nullable String message, BossBar.Color color, int duration) {
            return addBossBar(message, color, BossBar.Style.SOLID, duration);
        }

        @Override
        public boolean addBossBar(@Nullable String message, BossBar.Color color, BossBar.Style style, int duration) {
            return addMessageComponent(BossBarComponent.of(message, color, style, duration));
        }

        @Override
        public boolean addComplexMessage(@Nullable TextComponent textComponent) {
            return addComplexMessage(new BaseComponent[]{textComponent});
        }

        @Override
        public boolean addComplexMessage(@Nullable BaseComponent[] baseComponents) {
            return addMessageComponent(plugin.getProviders().getUIProvider()
                    .createComplexMessageComponent(baseComponents));
        }

        @Override
        public boolean addComplexMessage(@Nullable String message, @Nullable String command, @Nullable String suggest,
                                         @Nullable String tooltip) {
            return addMessageComponent(plugin.getProviders().getUIProvider()
                    .createComplexMessageComponent(message, command, suggest, tooltip));
        }

        @Override
        public boolean addRawMessage(@Nullable String message) {
            return addMessageComponent(plugin.getProviders().getUIProvider()
                    .createRawMessageComponent(message));
        }

        @Override
        public boolean addSound(Sound sound, float volume, float pitch) {
            return addSound(new GameSoundImpl(sound, volume, pitch));
        }

        @Override
        public boolean addSound(@Nullable GameSound gameSound) {
            return addMessageComponent(SoundComponent.of(gameSound));
        }

        @Override
        public boolean addTitle(@Nullable String titleMessage, @Nullable String subtitleMessage,
                                int fadeIn, int stay, int fadeOut) {
            return addMessageComponent(plugin.getProviders().getUIProvider()
                    .createTitleComponent(titleMessage, subtitleMessage, fadeIn, stay, fadeOut));
        }

        @Override
        public boolean addMessageComponent(IMessageComponent messageComponent) {
            Preconditions.checkNotNull(messageComponent, "Cannot add null message components.");

            if (messageComponent.getType() != IMessageComponent.Type.EMPTY) {
                messageComponents.add(messageComponent);
                return true;
            }

            return false;
        }

        @Override
        public IMessageComponent build() {
            return MultipleComponents.of(messageComponents);
        }

    }

}
