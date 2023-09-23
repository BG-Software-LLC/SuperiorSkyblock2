package com.bgsoftware.superiorskyblock.api.service.portals;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.Location;
import org.bukkit.PortalType;
import org.bukkit.entity.Entity;

public interface PortalsManagerService {

    /**
     * Handle a player going through a portal.
     *
     * @param superiorPlayer            The player that entered the portal.
     * @param portalLocation            The location of the portal.
     * @param portalType                The type of the portal.
     * @param destinationLocation       The location that the player should be teleported to.
     * @param checkImmunedPortalsStatus Whether to check if the player is immuned to portal teleports.
     * @return The result of going through the portal.
     */
    EntityPortalResult handlePlayerPortal(SuperiorPlayer superiorPlayer, Location portalLocation, PortalType portalType,
                                          Location destinationLocation, boolean checkImmunedPortalsStatus);

    /**
     * Handle a player going through a portal on an island.
     *
     * @param superiorPlayer            The player that entered the portal.
     * @param island                    The island the portal is inside.
     * @param portalLocation            The location of the portal.
     * @param portalType                The type of the portal.
     * @param checkImmunedPortalsStatus Whether to check if the player is immuned to portal teleports.
     * @return The result of going through the portal.
     */
    EntityPortalResult handlePlayerPortalFromIsland(SuperiorPlayer superiorPlayer, Island island,
                                                    Location portalLocation, PortalType portalType,
                                                    boolean checkImmunedPortalsStatus);

    /**
     * Handle an entity going through a portal on an island.
     *
     * @param entity         The entity that entered the portal.
     * @param island         The island the portal is inside.
     * @param portalLocation The location of the portal.
     * @param portalType     The type of the portal.
     * @return The result of going through the portal.
     */
    EntityPortalResult handleEntityPortalFromIsland(Entity entity, Island island, Location portalLocation, PortalType portalType);

}
