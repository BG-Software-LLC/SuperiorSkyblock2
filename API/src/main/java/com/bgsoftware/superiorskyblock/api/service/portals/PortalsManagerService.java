package com.bgsoftware.superiorskyblock.api.service.portals;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.Location;
import org.bukkit.PortalType;
import org.bukkit.entity.Entity;

public interface PortalsManagerService {

    EntityPortalResult handlePlayerPortal(SuperiorPlayer superiorPlayer, Location portalLocation, PortalType portalType,
                                          Location destinationLocation, boolean ignoreImmunedPortalsStatus);

    EntityPortalResult handlePlayerPortalFromIsland(SuperiorPlayer superiorPlayer, Island island,
                                                    Location portalLocation, PortalType portalType,
                                                    boolean ignoreImmunedPortalsStatus);

    EntityPortalResult handleEntityPortalFromIsland(Entity entity, Island island, Location portalLocation, PortalType portalType);

}
