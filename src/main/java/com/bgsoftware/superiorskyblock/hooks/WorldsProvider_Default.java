package com.bgsoftware.superiorskyblock.hooks;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.hooks.WorldsProvider;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.handlers.SettingsHandler;
import com.bgsoftware.superiorskyblock.wrappers.SBlockPosition;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.block.BlockFace;

import java.util.EnumMap;
import java.util.Set;
import java.util.UUID;

public final class WorldsProvider_Default implements WorldsProvider {

    private final Set<SBlockPosition> servedPositions = Sets.newHashSet();
    private final EnumMap<World.Environment, World> islandWorlds = new EnumMap<>(World.Environment.class);
    private final SuperiorSkyblockPlugin plugin;

    public WorldsProvider_Default(SuperiorSkyblockPlugin plugin){
        this.plugin = plugin;
    }

    @Override
    public void prepareWorlds() {
        SettingsHandler settingsHandler = new SettingsHandler(plugin);
        Difficulty difficulty = Difficulty.valueOf(settingsHandler.worldsDifficulty.toUpperCase());
        loadWorld(settingsHandler.islandWorldName, difficulty, World.Environment.NORMAL);
        if(settingsHandler.netherWorldEnabled)
            loadWorld(settingsHandler.netherWorldName, difficulty, World.Environment.NETHER);
        if(settingsHandler.endWorldEnabled)
            loadWorld(settingsHandler.endWorldName, difficulty, World.Environment.THE_END);
    }

    @Override
    public World getIslandsWorld(Island island, World.Environment environment) {
        Preconditions.checkNotNull(environment, "environment parameter cannot be null.");
        return islandWorlds.get(environment);
    }

    @Override
    public boolean isIslandsWorld(World world) {
        Preconditions.checkNotNull(world, "world parameter cannot be null.");
        World islandsWorld = getIslandsWorld(null, world.getEnvironment());
        return islandsWorld != null && world.getUID().equals(islandsWorld.getUID());
    }

    @Override
    public Location getNextLocation(Location previousLocation, int islandsHeight, int maxIslandSize, UUID islandOwner, UUID islandUUID) {
        Preconditions.checkNotNull(previousLocation, "previousLocation parameter cannot be null.");

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
            return getNextLocation(location.clone(), islandsHeight, maxIslandSize, islandOwner, islandUUID);
        }

        servedPositions.add(SBlockPosition.of(location));

        return location;
    }

    @Override
    public void finishIslandCreation(Location islandLocation, UUID islandOwner, UUID islandUUID) {
        Preconditions.checkNotNull(islandLocation, "islandLocation parameter cannot be null.");
        servedPositions.remove(SBlockPosition.of(islandLocation));
    }

    @Override
    public void prepareTeleport(Island island, Location location, Runnable finishCallback) {
        finishCallback.run();
    }

    @Override
    public boolean isNetherEnabled() {
        return plugin.getSettings().netherWorldEnabled;
    }

    @Override
    public boolean isNetherUnlocked() {
        return plugin.getSettings().netherWorldUnlocked;
    }

    @Override
    public boolean isEndEnabled() {
        return plugin.getSettings().endWorldEnabled;
    }

    @Override
    public boolean isEndUnlocked() {
        return plugin.getSettings().endWorldUnlocked;
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

    private void loadWorld(String worldName, Difficulty difficulty, World.Environment environment){
        if(Bukkit.getWorld(worldName) != null){
            throw new RuntimeException("The world " + worldName + " is already loaded. This can occur by one of the following reasons:\n" +
                    "- Another plugin loaded it manually before SuperiorSkyblock.\n" +
                    "- Your level-name property in server.properties is set to " + worldName + ".");
        }

        World world = WorldCreator.name(worldName).type(WorldType.FLAT).environment(environment).generator(plugin.getGenerator()).createWorld();
        world.setDifficulty(difficulty);
        islandWorlds.put(environment, world);

        if(Bukkit.getPluginManager().isPluginEnabled("Multiverse-Core")){
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mv import " + worldName + " normal -g " + plugin.getName());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mv modify set generator " + plugin.getName() + " " + worldName);
        }
    }

}
