package com.bgsoftware.superiorskyblock.utils.commands;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.enums.Rating;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandFlag;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.island.warps.IslandWarp;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.upgrades.Upgrade;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.island.SPlayerRole;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class CommandArguments {

    private CommandArguments() {

    }

    public static Pair<Island, SuperiorPlayer> getIsland(SuperiorSkyblockPlugin plugin, CommandSender sender, String argument){
        SuperiorPlayer targetPlayer = plugin.getPlayers().getSuperiorPlayer(argument);
        Island island = targetPlayer == null ? plugin.getGrid().getIsland(argument) : targetPlayer.getIsland();

        if(island == null){
            if(argument.equalsIgnoreCase(sender.getName()))
                Locale.INVALID_ISLAND.send(sender);
            else if(targetPlayer == null)
                Locale.INVALID_ISLAND_OTHER_NAME.send(sender, StringUtils.stripColors(argument));
            else
                Locale.INVALID_ISLAND_OTHER.send(sender, targetPlayer.getName());
        }

        return new Pair<>(island, targetPlayer);
    }

    public static Pair<List<Island>, SuperiorPlayer> getMultipleIslands(SuperiorSkyblockPlugin plugin, CommandSender sender, String argument){
        List<Island> islands = new ArrayList<>();
        SuperiorPlayer targetPlayer;

        if(argument.equals("*")){
            targetPlayer = null;
            islands = plugin.getGrid().getIslands();
        }
        else {
            Pair<Island, SuperiorPlayer> arguments = getIsland(plugin, sender, argument);
            targetPlayer = arguments.getValue();
            if(arguments.getKey() != null)
                islands.add(arguments.getKey());
        }

        return new Pair<>(islands, targetPlayer);
    }

    public static Pair<Island, SuperiorPlayer> getSenderIsland(SuperiorSkyblockPlugin plugin, CommandSender sender){
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(sender);
        Island island = superiorPlayer.getIsland();

        if(island == null)
            Locale.INVALID_ISLAND.send(superiorPlayer);

        return new Pair<>(island, superiorPlayer);
    }

    public static SuperiorPlayer getPlayer(SuperiorSkyblockPlugin plugin, SuperiorPlayer superiorPlayer, String argument){
        return getPlayer(plugin, superiorPlayer.asPlayer(), argument);
    }

    public static SuperiorPlayer getPlayer(SuperiorSkyblockPlugin plugin, CommandSender sender, String argument){
        SuperiorPlayer targetPlayer = plugin.getPlayers().getSuperiorPlayer(argument);

        if(targetPlayer == null)
            Locale.INVALID_PLAYER.send(sender, argument);

        return targetPlayer;
    }

    public static List<SuperiorPlayer> getMultiplePlayers(SuperiorSkyblockPlugin plugin, CommandSender sender, String argument){
        List<SuperiorPlayer> players = new ArrayList<>();

        if(argument.equals("*")){
            players = plugin.getPlayers().getAllPlayers();
        }
        else {
            SuperiorPlayer targetPlayer = getPlayer(plugin, sender, argument);
            if(targetPlayer != null)
                players.add(targetPlayer);
        }

        return players;
    }

    public static Pair<Island, SuperiorPlayer> getIslandWhereStanding(SuperiorSkyblockPlugin plugin, CommandSender sender){
        if(!(sender instanceof Player)){
            Locale.sendMessage(sender, "&cYou must specify a player's name.", true);
            return new Pair<>(null, null);
        }

        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(sender);
        Island locationIsland = plugin.getGrid().getIslandAt(superiorPlayer.getLocation());
        Island island = locationIsland == null || locationIsland.isSpawn() ? superiorPlayer.getIsland() : locationIsland;

        if(island == null)
            Locale.INVALID_ISLAND.send(sender);

        return new Pair<>(island, superiorPlayer);
    }

    public static Mission<?> getMission(SuperiorSkyblockPlugin plugin, SuperiorPlayer superiorPlayer, String argument){
        return getMission(plugin, superiorPlayer.asPlayer(), argument);
    }

    public static Mission<?> getMission(SuperiorSkyblockPlugin plugin, CommandSender sender, String argument){
        Mission<?> mission = plugin.getMissions().getMission(argument);

        if(mission == null)
            Locale.INVALID_MISSION.send(sender, argument);

        return mission;
    }

    public static List<Mission<?>> getMultipleMissions(SuperiorSkyblockPlugin plugin, CommandSender sender, String argument){
        List<Mission<?>> missions = new ArrayList<>();

        if(argument.equals("*")){
            missions = plugin.getMissions().getAllMissions();
        }
        else {
            Mission<?> mission = getMission(plugin, sender, argument);
            if(mission != null)
                missions.add(mission);
        }

        return missions;
    }

    public static Upgrade getUpgrade(SuperiorSkyblockPlugin plugin, SuperiorPlayer superiorPlayer, String argument){
        return getUpgrade(plugin, superiorPlayer.asPlayer(), argument);
    }

    public static Upgrade getUpgrade(SuperiorSkyblockPlugin plugin, CommandSender sender, String argument){
        Upgrade upgrade = plugin.getUpgrades().getUpgrade(argument);

        if(upgrade == null)
            Locale.INVALID_UPGRADE.send(sender, argument, StringUtils.getUpgradesString(plugin));

        return upgrade;
    }

    public static String buildLongString(String[] args, int start, boolean colorize){
        StringBuilder stringBuilder = new StringBuilder();

        for(int i = start; i < args.length; i++)
            stringBuilder.append(" ").append(args[i]);

        return colorize ? StringUtils.translateColors(stringBuilder.substring(1)) : stringBuilder.substring(1);
    }

    public static PlayerRole getPlayerRole(CommandSender sender, String argument){
        PlayerRole playerRole = null;

        try{
            playerRole = SPlayerRole.of(argument);
        }catch(IllegalArgumentException ignored){}

        if(playerRole == null)
            Locale.INVALID_ROLE.send(sender, argument, SPlayerRole.getValuesString());

        return playerRole;
    }

    public static Pair<Integer, Boolean> getLimit(CommandSender sender, String argument){
        return getInt(sender, argument, Locale.INVALID_LIMIT);
    }

    public static BigDecimal getBigDecimalAmount(CommandSender sender, String argument){
        BigDecimal amount = null;

        try{
            amount = new BigDecimal(argument);
        }catch(NumberFormatException ex){
            Locale.INVALID_AMOUNT.send(sender);
        }

        return amount;
    }

    public static Pair<Integer, Boolean> getAmount(CommandSender sender, String argument){
        return getInt(sender, argument, Locale.INVALID_AMOUNT);
    }

    public static Pair<Double, Boolean> getMultiplier(CommandSender sender, String argument){
        double multiplier = 0;
        boolean status = true;

        try{
            multiplier = Double.parseDouble(argument);
        }catch(IllegalArgumentException ex){
            Locale.INVALID_MULTIPLIER.send(sender, argument);
            status = false;
        }

        return new Pair<>(multiplier, status);
    }

    public static PotionEffectType getPotionEffect(CommandSender sender, String argument){
        PotionEffectType potionEffectType = PotionEffectType.getByName(argument.toUpperCase());

        if(potionEffectType == null)
            Locale.INVALID_EFFECT.send(sender, argument);

        return potionEffectType;
    }

    public static Pair<Integer, Boolean> getLevel(CommandSender sender, String argument){
        return getInt(sender, argument, Locale.INVALID_LEVEL);
    }

    public static Material getMaterial(CommandSender sender, String argument){
        Material material = null;

        try{
            material = Material.valueOf(argument.split(":")[0].toUpperCase());
        }catch (Exception ex){
            Locale.INVALID_MATERIAL.send(sender, argument);
        }

        return material;
    }

    public static Pair<Integer, Boolean> getSize(CommandSender sender, String argument){
        return getInt(sender, argument, Locale.INVALID_SIZE);
    }

    public static IslandWarp getWarp(CommandSender sender, Island island, String[] args, int start){
        String warpName = buildLongString(args, start, false);
        IslandWarp islandWarp = island.getWarp(warpName);

        if(islandWarp == null)
            Locale.INVALID_WARP.send(sender, warpName);

        return islandWarp;
    }

    public static Biome getBiome(CommandSender sender, String argument){
        Biome biome = null;

        try{
            biome = Biome.valueOf(argument.toUpperCase());
        }catch (Exception ex){
            Locale.INVALID_BIOME.send(sender, argument);
        }

        return biome;
    }

    public static World getWorld(CommandSender sender, String argument){
        World world = Bukkit.getWorld(argument);

        if(world == null)
            Locale.INVALID_WORLD.send(sender, argument);

        return world;
    }

    public static Location getLocation(CommandSender sender, World world, String x, String y, String z){
        Location location = null;

        try{
            int i_x = Integer.parseInt(x), i_y = Integer.parseInt(y), i_z = Integer.parseInt(z);
            location = new Location(world, i_x, i_y, i_z);
        }catch (Throwable ex){
            Locale.INVALID_BLOCK.send(sender, world.getName() + ", " + x + ", " + y + ", " + z);
        }

        return location;
    }

    public static Pair<Integer, Boolean> getPage(CommandSender sender, String argument){
        return getInt(sender, argument, Locale.INVALID_PAGE);
    }

    public static Pair<Integer, Boolean> getRows(CommandSender sender, String argument){
        return getInt(sender, argument, Locale.INVALID_ROWS);
    }

    public static IslandPrivilege getIslandPrivilege(CommandSender sender, String argument){
        IslandPrivilege islandPrivilege = null;

        try{
            islandPrivilege = IslandPrivilege.getByName(argument);
        }catch(IllegalArgumentException ignored){}

        if(islandPrivilege == null)
            Locale.INVALID_ISLAND_PERMISSION.send(sender, argument, StringUtils.getPermissionsString());

        return islandPrivilege;
    }

    public static Rating getRating(CommandSender sender, String argument){
        Rating rating = null;

        try{
            rating = Rating.valueOf(argument.toUpperCase());
        }catch(Exception ex){
            Locale.INVALID_RATE.send(sender, argument, Rating.getValuesString());
        }

        return rating;
    }

    public static IslandFlag getIslandFlag(CommandSender sender, String argument){
        IslandFlag islandFlag = null;

        try{
            islandFlag = IslandFlag.getByName(argument);
        }catch(IllegalArgumentException ignored){}

        if(islandFlag == null)
            Locale.INVALID_SETTINGS.send(sender, argument, StringUtils.getSettingsString());

        return islandFlag;
    }

    public static World.Environment getEnvironment(CommandSender sender, String argument){
        World.Environment environment = null;

        try{
            environment = World.Environment.valueOf(argument.toUpperCase());
        }catch(Exception ignored){}

        if(environment == null)
            Locale.INVALID_ENVIRONMENT.send(sender, argument);

        return environment;
    }

    public static Pair<Integer, Boolean> getInterval(CommandSender sender, String argument){
        Pair<Integer, Boolean> interval = getInt(sender, argument, Locale.INVALID_INTERVAL);

        if(interval.getValue() && interval.getKey() < 0){
            Locale.INVALID_INTERVAL.send(sender, argument);
            return new Pair<>(interval.getKey(), false);
        }

        return interval;
    }

    public static Map<String, String> parseArguments(String[] args){
        Map<String, String> parsedArgs = new HashMap<>();
        String currentKey = null;
        StringBuilder stringBuilder = new StringBuilder();

        for(String arg : args){
            if(arg.startsWith("-")){
                if(currentKey != null && stringBuilder.length() > 0){
                    parsedArgs.put(currentKey, stringBuilder.substring(1));
                }

                currentKey = arg.substring(1).toLowerCase();
                stringBuilder = new StringBuilder();
            }
            else if(currentKey != null){
                stringBuilder.append(" ").append(arg);
            }
        }

        if(currentKey != null && stringBuilder.length() > 0){
            parsedArgs.put(currentKey, stringBuilder.substring(1));
        }

        return parsedArgs;
    }

    private static Pair<Integer, Boolean> getInt(CommandSender sender, String argument, Locale locale){
        int i = 0;
        boolean status = true;

        try{
            i = Integer.parseInt(argument);
        }catch(IllegalArgumentException ex){
            locale.send(sender, argument);
            status = false;
        }

        return new Pair<>(i, status);
    }

}
