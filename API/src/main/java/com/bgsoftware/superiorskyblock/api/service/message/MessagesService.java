package com.bgsoftware.superiorskyblock.api.service.message;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.service.bossbar.BossBar;
import com.bgsoftware.superiorskyblock.api.world.GameSound;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.Locale;

public interface MessagesService {

    /**
     * Parse a message from config file into a message component.
     *
     * @param config The configuration file.
     * @param path   The path of the message.
     * @return The parsed component message.
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

    /**
     * Create a new builder for message components.
     */
    Builder newBuilder();

    /**
     * Builder class for creating {@link IMessageComponent}s
     */
    interface Builder {

        /**
         * Add an action bar to the message component.
         *
         * @param message The action bar text.
         * @return {@code true} if the action bar was successfully added.
         */
        boolean addActionBar(@Nullable String message);

        /**
         * Add a boss bar to the message component.
         *
         * @param message  The boss bar text.
         * @param color    The color of the boss bar.
         * @param duration The duration of the boss bar, in ticks.
         * @return {@code true} if the boss bar was successfully added.
         * @deprecated See {@link #addBossBar(String, BossBar.Color, BossBar.Style, int)}
         */
        @Deprecated
        boolean addBossBar(@Nullable String message, BossBar.Color color, int duration);

        /**
         * Add a boss bar to the message component.
         *
         * @param message  The boss bar text.
         * @param color    The color of the boss bar.
         * @param style    The style of the boss bar.
         * @param duration The duration of the boss bar, in ticks.
         * @return {@code true} if the boss bar was successfully added.
         */
        boolean addBossBar(@Nullable String message, BossBar.Color color, BossBar.Style style, int duration);

        /**
         * Add a complex message to the message component.
         *
         * @param textComponent The text component.
         * @return {@code true} if the complex message was successfully added.
         */
        boolean addComplexMessage(@Nullable TextComponent textComponent);

        /**
         * Add a complex message to the message component.
         *
         * @param baseComponents The base components.
         * @return {@code true} if the complex message was successfully added.
         */
        boolean addComplexMessage(@Nullable BaseComponent[] baseComponents);

        /**
         * Add a complex message to the message component.
         * Note: {@param command} is prioritized over {@param suggest} when mixed together.
         *
         * @param message The content of the complex message.
         * @param command The command to be executed when clicking the message.
         * @param suggest The command to suggest when clicking the message.
         * @param tooltip The text to show when hovering over the message.
         * @return {@code true} if the complex message was successfully added.
         */
        boolean addComplexMessage(@Nullable String message, @Nullable String command, @Nullable String suggest,
                                  @Nullable String tooltip);

        /**
         * Add a raw message to the message component.
         *
         * @param message The raw text.
         * @return {@code true} if the raw message was successfully added.
         */
        boolean addRawMessage(@Nullable String message);

        /**
         * Add a sound to the message component.
         *
         * @param sound  The sound to play.
         * @param volume The volume of the sound.
         * @param pitch  The pitch of the sound.
         * @return {@code true} if the sound was successfully added.
         */
        boolean addSound(Sound sound, float volume, float pitch);

        /**
         * Add a sound to the message component.
         *
         * @param gameSound The game sound to play.
         * @return {@code true} if the sound was successfully added.
         */
        boolean addSound(@Nullable GameSound gameSound);

        /**
         * Add a title to the message component.
         *
         * @param titleMessage    The title text.
         * @param subtitleMessage The subtitle text.
         * @param fadeIn          The duration of the fade-in phase, in ticks.
         * @param stay            The duration of the stay phase, in ticks.
         * @param fadeOut         The duration of the fade-out phase, in ticks.
         * @return {@code true} if the title was successfully added.
         */
        boolean addTitle(@Nullable String titleMessage, @Nullable String subtitleMessage,
                         int fadeIn, int stay, int fadeOut);

        /**
         * Add another message component to the message component.
         *
         * @param messageComponent The other message component to add.
         * @return {@code true} if the message component was successfully added.
         */
        boolean addMessageComponent(IMessageComponent messageComponent);

        /**
         * Build a new message component from the given builder.
         */
        IMessageComponent build();

    }

}
