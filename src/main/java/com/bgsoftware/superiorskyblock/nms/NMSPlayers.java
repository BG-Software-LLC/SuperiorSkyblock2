package com.bgsoftware.superiorskyblock.nms;

import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.mojang.authlib.properties.Property;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;

public interface NMSPlayers {

    void clearInventory(OfflinePlayer offlinePlayer);

    void setSkinTexture(SuperiorPlayer superiorPlayer);

    void setSkinTexture(SuperiorPlayer superiorPlayer, Property property);

    void sendActionBar(Player player, String message);

    void sendTitle(Player player, String title, String subtitle, int fadeIn, int duration, int fadeOut);

    boolean wasThrownByPlayer(Item item, Player player);

}
