package com.bgsoftware.superiorskyblock.external.vanish;

import com.bgsoftware.superiorskyblock.api.hooks.VanishProvider;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;

public class VanishProvider_Default implements VanishProvider {

    @Override
    public boolean isVanished(Player player) {
        if (!player.hasMetadata("vanished"))
            return false;

        return player.getMetadata("vanished").stream()
                .anyMatch(MetadataValue::asBoolean);
    }
}
