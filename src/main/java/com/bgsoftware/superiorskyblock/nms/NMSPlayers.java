package com.bgsoftware.superiorskyblock.nms;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.service.bossbar.BossBar;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.mojang.authlib.properties.Property;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;

import java.util.Locale;

public interface NMSPlayers {

    void clearInventory(OfflinePlayer offlinePlayer);

    void setSkinTexture(SuperiorPlayer superiorPlayer);

    void setSkinTexture(SuperiorPlayer superiorPlayer, Property property);

    void sendActionBar(Player player, String message);

    BossBar createBossBar(Player player, String message, BossBar.Color color, double ticksToRun);

    void sendTitle(Player player, String title, String subtitle, int fadeIn, int duration, int fadeOut);

    boolean wasThrownByPlayer(Item item, SuperiorPlayer superiorPlayer);

    @Nullable
    Locale getPlayerLocale(Player player);

}
