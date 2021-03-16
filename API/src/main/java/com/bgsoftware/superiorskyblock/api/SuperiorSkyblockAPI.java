package com.bgsoftware.superiorskyblock.api;

import com.bgsoftware.superiorskyblock.api.commands.SuperiorCommand;
import com.bgsoftware.superiorskyblock.api.handlers.BlockValuesManager;
import com.bgsoftware.superiorskyblock.api.handlers.GridManager;
import com.bgsoftware.superiorskyblock.api.handlers.MissionsManager;
import com.bgsoftware.superiorskyblock.api.handlers.PlayersManager;
import com.bgsoftware.superiorskyblock.api.handlers.UpgradesManager;
import com.bgsoftware.superiorskyblock.api.hooks.SpawnersProvider;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.schematic.Schematic;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.UUID;

public final class SuperiorSkyblockAPI {

    private static SuperiorSkyblock plugin;

    /*
     *  Player Methods
     */

    /**
     * Get the superior player object from a player instance.
     */
    public static SuperiorPlayer getPlayer(Player player){
        return plugin.getPlayers().getSuperiorPlayer(player.getUniqueId());
    }

    /**
     * Get the superior player object by a player's name.
     */
    @Nullable
    public static SuperiorPlayer getPlayer(String name){
        return plugin.getPlayers().getSuperiorPlayer(name);
    }

    /**
     * Get the superior player object from a player's uuid.
     *
     * @param uuid player uuid
     * @return The superior player object. null if doesn't exist.
     */
    public static SuperiorPlayer getPlayer(UUID uuid){
        return plugin.getPlayers().getSuperiorPlayer(uuid);
    }

    /*
     *  Island Methods
     */

    /**
     * Create a new island.
     *
     * @param superiorPlayer owner of the island
     * @param schemName the schematic of the island to be pasted
     * @param bonus The default island bonus level
     * @param biome The default island biome
     * @param islandName The island name
     */
    public static void createIsland(SuperiorPlayer superiorPlayer, String schemName, BigDecimal bonus, Biome biome, String islandName){
        plugin.getGrid().createIsland(superiorPlayer, schemName, bonus, biome, islandName);
    }

    /**
     * Create a new island.
     * @param superiorPlayer The new owner for the island.
     * @param schemName The schematic that should be used.
     * @param bonus A starting worth for the island.
     * @param biome A starting biome for the island.
     * @param islandName The name of the new island.
     * @param offset Should the island have an offset for it's values? If disabled, the bonus will be given.
     */
    public static void createIsland(SuperiorPlayer superiorPlayer, String schemName, BigDecimal bonus, Biome biome, String islandName, boolean offset){
        plugin.getGrid().createIsland(superiorPlayer, schemName, bonus, biome, islandName, offset);
    }

    /**
     * Create a new island.
     * @param superiorPlayer The new owner for the island.
     * @param schemName The schematic that should be used.
     * @param bonusWorth A starting worth for the island.
     * @param bonusLevel A starting level for the island.
     * @param biome A starting biome for the island.
     * @param islandName The name of the new island.
     * @param offset Should the island have an offset for it's values? If disabled, the bonus will be given.
     */
    public static void createIsland(SuperiorPlayer superiorPlayer, String schemName, BigDecimal bonusWorth, BigDecimal bonusLevel, Biome biome, String islandName, boolean offset){
        plugin.getGrid().createIsland(superiorPlayer, schemName, bonusWorth, bonusLevel, biome, islandName, offset);
    }

    /**
     * Delete an island
     */
    public static void deleteIsland(Island island){
        plugin.getGrid().deleteIsland(island);
    }

    /**
     * Get an island by it's name.
     */
    @Nullable
    public static Island getIsland(String islandName){
        return plugin.getGrid().getIsland(islandName);
    }

    /**
     * Get an island by it's uuid.
     */
    @Nullable
    public static Island getIslandByUUID(UUID uuid){
        return plugin.getGrid().getIslandByUUID(uuid);
    }

    /**
     * Get the spawn island.
     */
    public static Island getSpawnIsland(){
        return plugin.getGrid().getSpawnIsland();
    }

    /**
     * Get the world of an island by the world's environment.
     */
    public static World getIslandsWorld(Island island, World.Environment environment){
        return plugin.getGrid().getIslandsWorld(island, environment);
    }

    /**
     * Get an island at a location.
     */
    @Nullable
    public static Island getIslandAt(Location location){
        return plugin.getGrid().getIslandAt(location);
    }

    /**
     * Calculate all island worths on the server
     */
    public static void calcAllIslands(){
        plugin.getGrid().calcAllIslands();
    }

    /*
     *  Schematic Methods
     */

    /**
     * Get a schematic object by it's name
     */
    @Nullable
    public static Schematic getSchematic(String name){
        return plugin.getSchematics().getSchematic(name);
    }

    /*
     *  Providers Methods
     */

    public static void setSpawnersProvider(SpawnersProvider spawnersProvider){
        plugin.getProviders().setSpawnersProvider(spawnersProvider);
    }

    /*
     *  Main Method
     */

    /**
     * Get the grid of the core.
     */
    public static GridManager getGrid(){
        return plugin.getGrid();
    }

    /**
     * Get the blocks manager of the core.
     */
    public static BlockValuesManager getBlockValues(){
        return plugin.getBlockValues();
    }

    /**
     * Get the players manager of the core.
     */
    public static PlayersManager getPlayers(){
        return plugin.getPlayers();
    }

    /**
     * Get the missions manager of the core.
     */
    public static MissionsManager getMissions(){
        return plugin.getMissions();
    }

    /**
     * Get the upgrades manager of the core.
     */
    public static UpgradesManager getUpgrades(){
        return plugin.getUpgrades();
    }

    /**
     * Register a sub-command.
     * @param superiorCommand The sub command to register.
     */
    public static void registerCommand(SuperiorCommand superiorCommand){
        plugin.getCommands().registerCommand(superiorCommand);
    }

    /**
     * Get the superiorskyblock object.
     */
    public static SuperiorSkyblock getSuperiorSkyblock(){
        return plugin;
    }

}
