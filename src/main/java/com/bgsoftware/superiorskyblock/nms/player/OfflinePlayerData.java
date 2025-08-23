package com.bgsoftware.superiorskyblock.nms.player;

import com.bgsoftware.superiorskyblock.core.ObjectsPool;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface OfflinePlayerData extends ObjectsPool.Releasable {

    Player getFakeOnlinePlayer();

    void setLocation(Location location);

    void applyChanges();

    @Override
    void release();

}
