package com.bgsoftware.superiorskyblock.player.chat;

import com.bgsoftware.superiorskyblock.api.player.chat.ChatState;

public class ChatStates {

    public static final ChatState GLOBAL = register(new ChatState("GLOBAL") {});

    public static final ChatState LOCAL_CHAT = register(new ChatState("LOCAL_CHAT") {});

    public static final ChatState TEAM_CHAT = register(new ChatState("TEAM_CHAT") {});

    public static void registerStates() {
        // Do nothing, only trigger all the register calls
    }

    private static ChatState register(ChatState chatState) {
        ChatState.register(chatState);
        return chatState;
    }

}
