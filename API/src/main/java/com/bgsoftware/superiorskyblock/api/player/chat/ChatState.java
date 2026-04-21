package com.bgsoftware.superiorskyblock.api.player.chat;

import com.bgsoftware.superiorskyblock.api.objects.Enumerable;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.google.common.base.Preconditions;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public abstract class ChatState implements Enumerable {

    private static final Map<String, ChatState> chatStates = new HashMap<>();
    private static int ordinalCounter = 0;

    private final String name;
    private final int ordinal;

    protected ChatState(String name) {
        Preconditions.checkArgument(!chatStates.containsKey(name), "name already exists.");
        this.name = name.toUpperCase(Locale.ENGLISH);
        this.ordinal = ordinalCounter++;
    }

    @Override
    public final int ordinal() {
        return this.ordinal;
    }

    /**
     * Get the name of the chat state.
     */
    public final String getName() {
        return name;
    }

    /**
     * Get the target players for the player.
     *
     * @param superiorPlayer The player to get the target players for.
     */
    public abstract List<SuperiorPlayer> getTargetPlayers(SuperiorPlayer superiorPlayer);

    @Override
    public String toString() {
        return "ChatState{name=" + name + "}";
    }

    /**
     * Get all the chat states.
     */
    public static Collection<ChatState> values() {
        return chatStates.values();
    }

    /**
     * Get a chat state by its name.
     *
     * @param name The name to check.
     */
    public static ChatState getByName(String name) {
        Preconditions.checkNotNull(name, "name parameter cannot be null.");

        ChatState chatState = chatStates.get(name.toUpperCase(Locale.ENGLISH));

        Preconditions.checkNotNull(chatState, "Couldn't find an ChatState with the name " + name + ".");

        return chatState;
    }

    /**
     * Register a new chat state.
     *
     * @param chatState The chat state to register.
     */
    public static void register(ChatState chatState) {
        Preconditions.checkNotNull(chatState, "chatState parameter cannot be null.");

        String name = chatState.getName().toUpperCase(Locale.ENGLISH);

        Preconditions.checkState(!chatStates.containsKey(name), "ChatState with the name " + name + " already exists.");

        chatStates.put(name, chatState);
    }

}