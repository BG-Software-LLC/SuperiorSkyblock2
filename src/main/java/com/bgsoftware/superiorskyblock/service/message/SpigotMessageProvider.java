package com.bgsoftware.superiorskyblock.service.message;

import com.bgsoftware.superiorskyblock.api.service.message.MessageProvider;
import org.bukkit.entity.Player;

public class SpigotMessageProvider implements MessageProvider {

    @Override
    public void sendMessage(Player player, String message) {
        player.sendMessage(message);
    }
}
