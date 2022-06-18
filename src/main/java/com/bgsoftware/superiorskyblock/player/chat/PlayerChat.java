package com.bgsoftware.superiorskyblock.player.chat;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

public class PlayerChat {

    private static final Map<UUID, PlayerChat> playerChatListeners = new HashMap<>();
    private final Function<String, Boolean> chatConsumer;

    private PlayerChat(Function<String, Boolean> chatConsumer) {
        this.chatConsumer = chatConsumer;
    }

    public static PlayerChat getChatListener(Player player) {
        return playerChatListeners.get(player.getUniqueId());
    }

    public static void listen(Player player, Function<String, Boolean> onChat) {
        playerChatListeners.put(player.getUniqueId(), new PlayerChat(onChat));
    }

    public static void remove(Player player) {
        playerChatListeners.remove(player.getUniqueId());
    }

    public boolean supply(String message) {
        return chatConsumer.apply(message);
    }

}
