package com.bgsoftware.superiorskyblock.hooks;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.hooks.WorldsManager;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.wrappers.SBlockPosition;
import com.google.common.collect.Sets;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;

import java.util.Set;
import java.util.UUID;

public final class WorldsManager_Default implements WorldsManager {

    private final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
    private final Set<SBlockPosition> servedPositions = Sets.newHashSet();

    @Override
    public Location getNextLocation(Location previousLocation, int islandsHeight, int maxIslandSize, UUID islandOwner) {
        Location location = previousLocation.clone();
        location.setY(islandsHeight);
        BlockFace islandFace = getIslandFace(location);

        int islandRange = maxIslandSize * 3;

        if(islandFace == BlockFace.NORTH){
            location.add(islandRange, 0, 0);
        }else if(islandFace == BlockFace.EAST){
            if(location.getX() == -location.getZ())
                location.add(islandRange, 0, 0);
            else if(location.getX() == location.getZ())
                location.subtract(islandRange, 0, 0);
            else
                location.add(0, 0, islandRange);
        }else if(islandFace == BlockFace.SOUTH){
            if(location.getX() == -location.getZ())
                location.subtract(0, 0, islandRange);
            else
                location.subtract(islandRange, 0, 0);
        }else if(islandFace == BlockFace.WEST){
            if(location.getX() == location.getZ())
                location.add(islandRange, 0, 0);
            else
                location.subtract(0, 0, islandRange);
        }

        if(servedPositions.contains(SBlockPosition.of(location)) || plugin.getGrid().getIslandAt(location) != null){
            return getNextLocation(location.clone(), islandsHeight, maxIslandSize, islandOwner);
        }

        servedPositions.add(SBlockPosition.of(location));

        return location;
    }

    @Override
    public void finishIslandCreation(Location islandLocation, UUID islandOwner) {
        if(islandLocation != null)
            servedPositions.remove(SBlockPosition.of(islandLocation));
    }

    @Override
    public void prepareTeleport(Island island, Runnable finishCallback) {
        finishCallback.run();
    }

    private BlockFace getIslandFace(Location location){
        //Possibilities: North / East
        if(location.getX() >= location.getZ()) {
            return -location.getX() > location.getZ() ? BlockFace.NORTH : BlockFace.EAST;
        }
        //Possibilities: South / West
        else{
            return -location.getX() > location.getZ() ? BlockFace.WEST : BlockFace.SOUTH;
        }
    }

}
