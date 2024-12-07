package com.bgsoftware.superiorskyblock.nms;

import com.bgsoftware.superiorskyblock.core.ObjectsPool;
import org.bukkit.Location;

public interface ICachedBlock extends ObjectsPool.Releasable {

    void setBlock(Location location);

}
