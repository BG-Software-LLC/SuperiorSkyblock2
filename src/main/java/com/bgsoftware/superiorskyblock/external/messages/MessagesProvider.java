package com.bgsoftware.superiorskyblock.external.messages;

import com.bgsoftware.superiorskyblock.api.service.bossbar.BossBar;
import com.bgsoftware.superiorskyblock.api.service.message.IMessageComponent;

public interface MessagesProvider {

    /**
     * Create an action bar message component.
     *
     * @param message The action bar text.
     * @return The created message component.
     */
    IMessageComponent createActionBarComponent(String message);

    /**
     * Create a boss bar message component.
     *
     * @param message  The boss bar text.
     * @param color    The boss bar color.
     * @param style    The boss bar style.
     * @param duration The boss bar duration, in ticks.
     * @return The created message component.
     */
    IMessageComponent createBossBarComponent(String message, BossBar.Color color, BossBar.Style style, int duration);

    /**
     * Create a raw message component.
     *
     * @param message The raw text.
     * @return The created message component.
     */
    IMessageComponent createRawMessageComponent(String message);

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
    IMessageComponent createTitleComponent(String titleMessage, String subtitleMessage, int fadeIn, int stay, int fadeOut);

}
