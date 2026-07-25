package com.bgsoftware.superiorskyblock.player.chat;

import com.bgsoftware.superiorskyblock.api.player.chat.ChatState;

public class ChatStates {

    public static final ChatState GLOBAL = register("GLOBAL");

    public static final ChatState LOCAL_CHAT = register("LOCAL_CHAT");

    public static final ChatState TEAM_CHAT = register("TEAM_CHAT");

    public static void registerStates() {
        // Do nothing, only trigger all the register calls
    }

    private static ChatState register(String name) {
        ChatState.register(name);
        return ChatState.getByName(name);
    }

}
