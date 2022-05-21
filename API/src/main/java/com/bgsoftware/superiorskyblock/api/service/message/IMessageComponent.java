package com.bgsoftware.superiorskyblock.api.service.message;

import org.bukkit.command.CommandSender;

public interface IMessageComponent {

    /**
     * Get the type of this component.
     */
    Type getType();

    /**
     * Get the raw message of this component.
     */
    String getMessage();

    /**
     * Send this message to a {@link CommandSender}.
     *
     * @param sender The {@link CommandSender} to send the message to.
     * @param args   The arguments of the message.
     */
    void sendMessage(CommandSender sender, Object... args);
    
    enum Type {

        /**
         * The component represents an action bar.
         */
        ACTION_BAR,

        /**
         * The component represents a boss bar.
         */
        BOSS_BAR,

        /**
         * The component represents a complex message.
         * Complex messages are raw text that can be sent to players, however they have extra functionalities,
         * such as clickable messages, hover messages, etc.
         */
        COMPLEX_MESSAGE,

        /**
         * The component represents an empty message.
         * This type of component is used when the message of the component is empty or invalid.
         */
        EMPTY,

        /**
         * The component represents multiple components.
         * This type of component is a container for different other components inside it. This gives the ability
         * to combine different component types for the same message - for example, sending a message while playing
         * a sound. Therefore, by calling {@link #sendMessage(CommandSender, Object...)} on it, all the components it contains
         * will be sent at the same time.
         */
        MULTIPLE,

        /**
         * The component represents a raw text message.
         */
        RAW_MESSAGE,

        /**
         * The component represents a sound.
         * When sending this component, a sound will be played instead of a message to be sent.
         */
        SOUND,

        /**
         * The component represents a title.
         * Titles are the big text that can be shown in the middle of the screen of a player.
         */
        TITLE


    }

}
