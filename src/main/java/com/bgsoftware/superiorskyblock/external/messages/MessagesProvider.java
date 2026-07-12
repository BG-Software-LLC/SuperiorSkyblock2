package com.bgsoftware.superiorskyblock.external.messages;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.service.bossbar.BossBar;
import com.bgsoftware.superiorskyblock.api.service.message.IMessageComponent;
import net.md_5.bungee.api.chat.BaseComponent;

public interface MessagesProvider {

    /**
     * Create an action bar message component.
     *
     * @param message The action bar text.
     * @return The created message component.
     */
    IMessageComponent createActionBarComponent(@Nullable String message);

    /**
     * Create a boss bar message component.
     *
     * @param message  The boss bar text.
     * @param color    The boss bar color.
     * @param style    The boss bar style.
     * @param duration The boss bar duration, in ticks.
     * @return The created message component.
     */
    IMessageComponent createBossBarComponent(@Nullable String message, BossBar.Color color,
                                             BossBar.Style style, int duration);

    /**
     * Create a raw message component.
     *
     * @param message The raw text.
     * @return The created message component.
     */
    default IMessageComponent createRawMessageComponent(@Nullable String message) {
        return createComplexMessageComponent(message, null, null, null);
    }

    /**
     * Create a complex message component.
     *
     * @param message The content of the complex message.
     * @param command The command to be executed when clicked on the message.
     * @param suggest The command to suggest when clicked on the message.
     * @param tooltip The text to show when hovering over the message.
     * @return The created message component.
     */
    IMessageComponent createComplexMessageComponent(@Nullable String message, @Nullable String command,
                                                    @Nullable String suggest, @Nullable String tooltip);

    /**
     * Create a complex message component.
     *
     * @param components The components of the complex message.
     * @return The created message component.
     */
    IMessageComponent createComplexMessageComponent(@Nullable BaseComponent[] components);

    /**
     * Create a title message component.
     *
     * @param titleMessage    The title text.
     * @param subtitleMessage The subtitle text.
     * @param fadeIn          The duration of the fade-in phase, in ticks.
     * @param stay            The duration of the stay phase, in ticks.
     * @param fadeOut         The duration of the fade-out phase, in ticks.
     * @return The created message component.
     */
    IMessageComponent createTitleComponent(@Nullable String titleMessage, @Nullable String subtitleMessage,
                                           int fadeIn, int stay, int fadeOut);

}
