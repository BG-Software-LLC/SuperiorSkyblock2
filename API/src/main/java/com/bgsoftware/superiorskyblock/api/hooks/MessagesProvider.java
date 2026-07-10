package com.bgsoftware.superiorskyblock.api.hooks;

import com.bgsoftware.superiorskyblock.api.service.message.IMessageComponent;

public interface MessagesProvider {

    /**
     * Creates an {@link IMessageComponent} representing an action bar message.
     *
     * @param message The action bar text.
     * @return The created message component.
     */
    IMessageComponent createActionBarComponent(String message);

    /**
     * Creates an {@link IMessageComponent} representing a boss bar.
     *
     * @param name The boss bar name.
     * @param color The boss bar color.
     * @param style The boss bar style.
     * @param duration The boss bar duration, in ticks.
     * @return The created message component.
     */
    IMessageComponent createBossBarComponent(String name, String color, String style, int duration);

    /**
     * Creates an {@link IMessageComponent} representing a regular chat message.
     *
     * @param message The message text.
     * @return The created message component.
     */
    IMessageComponent createRawMessageComponent(String message);

    /**
     * Creates an {@link IMessageComponent} representing a title.
     *
     * @param title The title text.
     * @param subtitle The subtitle text.
     * @param fadeIn The fade-in duration, in ticks.
     * @param stay The time the title stays visible, in ticks.
     * @param fadeOut The fade-out duration, in ticks.
     * @return The created message component.
     */
    IMessageComponent createTitleComponent(String title, String subtitle, int fadeIn, int stay, int fadeOut);

}
