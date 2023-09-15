package com.bgsoftware.superiorskyblock.api.service.message;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.service.bossbar.BossBar;
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

    /**
     * Create a new builder for message components.
     */
    Builder newBuilder();

    /**
     * Builder class for creating {@link IMessageComponent}s
     */
    interface Builder {

        /**
         * Add an action bar text to the message component.
         *
         * @param message The action bar text.
         * @return true if the action bar was successfully added.
         */
        boolean addActionBar(@Nullable String message);

        /**
         * Add boss bar to the message component.
         *
         * @param message The text to be displayed in the boss bar
         * @param color   The color of the boss bar
         * @param ticks   The duration of the boss bar, in ticks.
         * @return true if the boss bar was successfully added.
         */
        boolean addBossBar(@Nullable String message, BossBar.Color color, int ticks);

        /**
         * Add a complex message to the message component.
         *
         * @param textComponent The text component.
         * @return true if the complex message was successfully added.
         */
        boolean addComplexMessage(@Nullable TextComponent textComponent);

        /**
         * Add a complex message to the message component.
         *
         * @param baseComponents The base components of the message.
         * @return true if the complex message was successfully added.
         */
        boolean addComplexMessage(@Nullable BaseComponent[] baseComponents);

        /**
         * Add a raw message to the message component.
         *
         * @param message The raw text.
         * @return true if the raw message was successfully added.
         */
        boolean addRawMessage(@Nullable String message);

        /**
         * Add a sound to the message component.
         *
         * @param sound  The sound to be played.
         * @param volume The volume of the sound.
         * @param pitch  The pitch of the sound.
         * @return true if the sound was successfully added.
         */
        boolean addSound(Sound sound, float volume, float pitch);

        /**
         * Add a title to the message component.
         *
         * @param titleMessage    The first line of the title.
         * @param subtitleMessage The second line of the title.
         * @param fadeIn          The duration of the fade in animation, in ticks.
         * @param duration        The duration of the title to last, in ticks.
         * @param fadeOut         The duration of the fade out animation, in ticks.
         * @return true if the title was successfully added.
         */
        boolean addTitle(@Nullable String titleMessage, @Nullable String subtitleMessage, int fadeIn, int duration, int fadeOut);

        /**
         * Add another message component to the message component.
         *
         * @param messageComponent The other message component to add.
         * @return true if the message component was successfully added.
         */
        boolean addMessageComponent(IMessageComponent messageComponent);

        /**
         * Build a new message component from the given builder.
         */
        IMessageComponent build();

    }

}
