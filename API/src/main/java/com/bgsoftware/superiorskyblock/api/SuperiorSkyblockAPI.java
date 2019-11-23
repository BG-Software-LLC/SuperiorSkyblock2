package com.bgsoftware.superiorskyblock.api;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.schematic.Schematic;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.UUID;

public final class SuperiorSkyblockAPI {

    private static SuperiorSkyblock plugin;

    /*
     *  Player Methods
     */

    /**
     * Get the superior player object from a player instance.
     *
     * @param player player instance
     * @return The superior player object. null if doesn't exist.
     */
    public static SuperiorPlayer getPlayer(Player player){
        return plugin.getPlayers().getSuperiorPlayer(player.getUniqueId());
    }

    /**
     * Get the superior player object from a player's name.
     *
     * @param name player name
     * @return The superior player object. null if doesn't exist.
     */
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
     * Delete an island
     */
    public static void deleteIsland(Island island){
        plugin.getGrid().deleteIsland(island);
    }

    /**
     * Get the island by it's index in top islands.
     *
     * @param index the index to check
     * @return the island at the index. might be null.
     */
    public static Island getIsland(int index){
        return plugin.getGrid().getIsland(index);
    }

    /**
     * Get the spawn island.
     *
     * @return the spawn island.
     */
    public static Island getSpawnIsland(){
        return plugin.getGrid().getSpawnIsland();
    }

    /**
     * Get the islands normal world.
     *
     * @return the islands normal world.
     */
    public static World getIslandsWorld(){
        return getIslandsWorld(World.Environment.NORMAL);
    }

    /**
     * Get the islands world by world's environment.
     * @param environment The environment.
     *
     * @return the islands world.
     */
    public static World getIslandsWorld(World.Environment environment){
        return plugin.getGrid().getIslandsWorld(environment);
    }

    /**
     * Get an island at a location.
     *
     * @param location the location to check
     * @return the island at the location. might be null.
     */
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
     *
     * @param name the name to check
     * @return the schematic with this name. might be null.
     */
    public static Schematic getSchematic(String name){
        return plugin.getSchematics().getSchematic(name);
    }

    /*
     *  Main Method
     */

    /**
     * Get the superiorskyblock object.
     *
     * @return superiorskyblock object
     */
    public static SuperiorSkyblock getSuperiorSkyblock(){
        return plugin;
    }

}
