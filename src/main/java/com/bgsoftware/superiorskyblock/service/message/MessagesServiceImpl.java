package com.bgsoftware.superiorskyblock.service.message;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.service.bossbar.BossBar;
import com.bgsoftware.superiorskyblock.api.service.message.IMessageComponent;
import com.bgsoftware.superiorskyblock.api.service.message.MessagesService;
import com.bgsoftware.superiorskyblock.core.EnumHelper;
import com.bgsoftware.superiorskyblock.core.GameSoundImpl;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.core.messages.component.MultipleComponents;
import com.bgsoftware.superiorskyblock.core.messages.component.impl.ActionBarComponent;
import com.bgsoftware.superiorskyblock.core.messages.component.impl.BossBarComponent;
import com.bgsoftware.superiorskyblock.core.messages.component.impl.ComplexMessageComponent;
import com.bgsoftware.superiorskyblock.core.messages.component.impl.RawMessageComponent;
import com.bgsoftware.superiorskyblock.core.messages.component.impl.SoundComponent;
import com.bgsoftware.superiorskyblock.core.messages.component.impl.TitleComponent;
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

    public MessagesServiceImpl() {

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
            return RawMessageComponent.of(Formatters.COLOR_FORMATTER.format(config.getString(path, "")));
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

        private final List<IMessageComponent> messageComponents = new LinkedList<>();

        @Override
        public boolean addActionBar(@Nullable String message) {
            return addMessageComponent(ActionBarComponent.of(message));
        }

        @Override
        public boolean addBossBar(@Nullable String message, BossBar.Color color, int ticks) {
            return addMessageComponent(BossBarComponent.of(message, color, ticks));
        }

        @Override
        public boolean addComplexMessage(@Nullable TextComponent textComponent) {
            return addComplexMessage(new BaseComponent[]{textComponent});
        }

        @Override
        public boolean addComplexMessage(@Nullable BaseComponent[] baseComponents) {
            return addMessageComponent(ComplexMessageComponent.of(baseComponents));
        }

        @Override
        public boolean addRawMessage(@Nullable String message) {
            return addMessageComponent(RawMessageComponent.of(message));
        }

        @Override
        public boolean addSound(Sound sound, float volume, float pitch) {
            return addMessageComponent(SoundComponent.of(new GameSoundImpl(sound, volume, pitch)));
        }

        @Override
        public boolean addTitle(@Nullable String titleMessage, @Nullable String subtitleMessage, int fadeIn, int duration, int fadeOut) {
            return addMessageComponent(TitleComponent.of(titleMessage, subtitleMessage, fadeIn, duration, fadeOut));
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
