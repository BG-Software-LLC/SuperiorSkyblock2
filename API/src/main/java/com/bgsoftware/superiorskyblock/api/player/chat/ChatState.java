package com.bgsoftware.superiorskyblock.api.player.chat;

import com.bgsoftware.superiorskyblock.api.objects.Enumerable;
import com.google.common.base.Preconditions;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ChatState implements Enumerable {

    private static final Map<String, ChatState> chatStates = new HashMap<>();
    private static int ordinalCounter = 0;

    private final String name;
    private final int ordinal;

    private ChatState(String name) {
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
     * @param name The name for the chat state.
     */
    public static void register(String name) {
        Preconditions.checkNotNull(name, "chatState parameter cannot be null.");

        name = name.toUpperCase(Locale.ENGLISH);

        Preconditions.checkState(!chatStates.containsKey(name), "ChatState with the name " + name + " already exists.");

        chatStates.put(name, new ChatState(name));
    }

}