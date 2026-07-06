package com.bgsoftware.superiorskyblock.api.hooks;

import com.bgsoftware.superiorskyblock.api.service.message.IMessageComponent;

public interface MessagesProvider {

    /**
     * Creates an {@link IMessageComponent} that can be sent as action bar.
     *
     * @param message Action bar message.
     */
    IMessageComponent createActionBarComponent(String message);

    /**
     * Creates an {@link IMessageComponent} that can be sent as boss bar.
     *
     * @param name Boss bar name.
     * @param color Boss bar color.
     * @param style Boss bar style.
     * @param duration Boss bar visibility duration.
     */
    IMessageComponent createBossBarComponent(String name, String color, String style, int duration);

    /**
     * Creates an {@link IMessageComponent} that can be sent as raw message.
     *
     * @param message Message text.
     */
    IMessageComponent createRawMessageComponent(String message);

    IMessageComponent createTitleComponent(String title, String subtitle, int fadeIn, int stay, int fadeOut);

}
