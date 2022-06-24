package com.bgsoftware.superiorskyblock.commands.arguments;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.enums.BorderColor;
import com.bgsoftware.superiorskyblock.api.enums.Rating;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandFlag;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.island.warps.IslandWarp;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.api.upgrades.Upgrade;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.island.role.SPlayerRole;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CommandArguments {

    private CommandArguments() {

    }

    public static IslandArgument getIsland(SuperiorSkyblockPlugin plugin, CommandSender sender, String argument) {
        SuperiorPlayer targetPlayer = plugin.getPlayers().getSuperiorPlayer(argument);
        Island island = targetPlayer == null ? plugin.getGrid().getIsland(argument) : targetPlayer.getIsland();

        if (island == null) {
            if (argument.equalsIgnoreCase(sender.getName()))
                Message.INVALID_ISLAND.send(sender);
            else if (targetPlayer == null)
                Message.INVALID_ISLAND_OTHER_NAME.send(sender, Formatters.STRIP_COLOR_FORMATTER.format(argument));
            else
                Message.INVALID_ISLAND_OTHER.send(sender, targetPlayer.getName());
        }

        return new IslandArgument(island, targetPlayer);
    }

    public static IslandsListArgument getMultipleIslands(SuperiorSkyblockPlugin plugin, CommandSender sender, String argument) {
        List<Island> islands = new LinkedList<>();
        SuperiorPlayer targetPlayer;

        if (argument.equals("*")) {
            targetPlayer = null;
            islands = plugin.getGrid().getIslands();
        } else {
            IslandArgument arguments = getIsland(plugin, sender, argument);
            targetPlayer = arguments.getSuperiorPlayer();
            if (arguments.getIsland() != null)
                islands.add(arguments.getIsland());
        }

        return new IslandsListArgument(Collections.unmodifiableList(islands), targetPlayer);
    }

    public static IslandArgument getSenderIsland(SuperiorSkyblockPlugin plugin, CommandSender sender) {
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(sender);
        Island island = superiorPlayer.getIsland();

        if (island == null)
            Message.INVALID_ISLAND.send(superiorPlayer);

        return new IslandArgument(island, superiorPlayer);
    }

    public static SuperiorPlayer getPlayer(SuperiorSkyblockPlugin plugin, SuperiorPlayer superiorPlayer, String argument) {
        return getPlayer(plugin, superiorPlayer.asPlayer(), argument);
    }

    public static SuperiorPlayer getPlayer(SuperiorSkyblockPlugin plugin, CommandSender sender, String argument) {
        SuperiorPlayer targetPlayer = plugin.getPlayers().getSuperiorPlayer(argument);

        if (targetPlayer == null)
            Message.INVALID_PLAYER.send(sender, argument);

        return targetPlayer;
    }

    public static List<SuperiorPlayer> getMultiplePlayers(SuperiorSkyblockPlugin plugin, CommandSender sender, String argument) {
        List<SuperiorPlayer> players = new LinkedList<>();

        if (argument.equals("*")) {
            players = plugin.getPlayers().getAllPlayers();
        } else {
            SuperiorPlayer targetPlayer = getPlayer(plugin, sender, argument);
            if (targetPlayer != null)
                players.add(targetPlayer);
        }

        return Collections.unmodifiableList(players);
    }

    public static IslandArgument getIslandWhereStanding(SuperiorSkyblockPlugin plugin, CommandSender sender) {
        if (!(sender instanceof Player)) {
            Message.CUSTOM.send(sender, "&cYou must specify a player's name.", true);
            return IslandArgument.EMPTY;
        }

        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(sender);
        Island locationIsland = plugin.getGrid().getIslandAt(superiorPlayer.getLocation());
        Island island = locationIsland == null || locationIsland.isSpawn() ? superiorPlayer.getIsland() : locationIsland;

        if (island == null)
            Message.INVALID_ISLAND.send(sender);

        return new IslandArgument(island, superiorPlayer);
    }

    public static Mission<?> getMission(SuperiorSkyblockPlugin plugin, SuperiorPlayer superiorPlayer, String argument) {
        return getMission(plugin, superiorPlayer.asPlayer(), argument);
    }

    public static Mission<?> getMission(SuperiorSkyblockPlugin plugin, CommandSender sender, String argument) {
        Mission<?> mission = plugin.getMissions().getMission(argument);

        if (mission == null)
            Message.INVALID_MISSION.send(sender, argument);

        return mission;
    }

    public static List<Mission<?>> getMultipleMissions(SuperiorSkyblockPlugin plugin, CommandSender sender, String argument) {
        List<Mission<?>> missions = new LinkedList<>();

        if (argument.equals("*")) {
            missions = plugin.getMissions().getAllMissions();
        } else {
            Mission<?> mission = getMission(plugin, sender, argument);
            if (mission != null)
                missions.add(mission);
        }

        return Collections.unmodifiableList(missions);
    }

    public static Upgrade getUpgrade(SuperiorSkyblockPlugin plugin, SuperiorPlayer superiorPlayer, String argument) {
        return getUpgrade(plugin, superiorPlayer.asPlayer(), argument);
    }

    public static Upgrade getUpgrade(SuperiorSkyblockPlugin plugin, CommandSender sender, String argument) {
        Upgrade upgrade = plugin.getUpgrades().getUpgrade(argument);

        if (upgrade == null) {
            Message.INVALID_UPGRADE.send(sender, argument, Formatters.COMMA_FORMATTER.format(
                    plugin.getUpgrades().getUpgrades().stream().map(Upgrade::getName)));
        }

        return upgrade;
    }

    public static String buildLongString(String[] args, int start, boolean colorize) {
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = start; i < args.length; i++)
            stringBuilder.append(" ").append(args[i]);

        return colorize ? Formatters.COLOR_FORMATTER.format(stringBuilder.substring(1)) : stringBuilder.substring(1);
    }

    public static PlayerRole getPlayerRole(CommandSender sender, String argument) {
        PlayerRole playerRole = null;

        try {
            playerRole = SPlayerRole.of(argument);
        } catch (IllegalArgumentException ignored) {
        }

        if (playerRole == null)
            Message.INVALID_ROLE.send(sender, argument, SPlayerRole.getValuesString());

        return playerRole;
    }

    public static NumberArgument<Integer> getLimit(CommandSender sender, String argument) {
        return getInt(sender, argument, Message.INVALID_LIMIT);
    }

    public static BigDecimal getBigDecimalAmount(CommandSender sender, String argument) {
        BigDecimal amount = null;

        try {
            amount = new BigDecimal(argument);
        } catch (NumberFormatException ex) {
            Message.INVALID_AMOUNT.send(sender);
        }

        return amount;
    }

    public static NumberArgument<Integer> getAmount(CommandSender sender, String argument) {
        return getInt(sender, argument, Message.INVALID_AMOUNT);
    }

    public static NumberArgument<Double> getMultiplier(CommandSender sender, String argument) {
        double multiplier = 0;
        boolean status = true;

        try {
            multiplier = Double.parseDouble(argument);
            // Makes sure the multiplier is rounded.
            multiplier = Math.round(multiplier * 100) / 100D;
        } catch (IllegalArgumentException ex) {
            Message.INVALID_MULTIPLIER.send(sender, argument);
            status = false;
        }

        return new NumberArgument<>(multiplier, status);
    }

    public static PotionEffectType getPotionEffect(CommandSender sender, String argument) {
        PotionEffectType potionEffectType = PotionEffectType.getByName(argument.toUpperCase(Locale.ENGLISH));

        if (potionEffectType == null)
            Message.INVALID_EFFECT.send(sender, argument);

        return potionEffectType;
    }

    public static NumberArgument<Integer> getLevel(CommandSender sender, String argument) {
        return getInt(sender, argument, Message.INVALID_LEVEL);
    }

    public static Material getMaterial(CommandSender sender, String argument) {
        Material material = null;

        try {
            material = Material.valueOf(argument.split(":")[0].toUpperCase(Locale.ENGLISH));
        } catch (Exception ex) {
            Message.INVALID_MATERIAL.send(sender, argument);
        }

        return material;
    }

    public static NumberArgument<Integer> getSize(CommandSender sender, String argument) {
        return getInt(sender, argument, Message.INVALID_SIZE);
    }

    public static IslandWarp getWarp(CommandSender sender, Island island, String[] args, int start) {
        String warpName = buildLongString(args, start, false);
        IslandWarp islandWarp = island.getWarp(warpName);

        if (islandWarp == null)
            Message.INVALID_WARP.send(sender, warpName);

        return islandWarp;
    }

    public static Biome getBiome(CommandSender sender, String argument) {
        Biome biome = null;

        try {
            biome = Biome.valueOf(argument.toUpperCase(Locale.ENGLISH));
        } catch (Exception ex) {
            Message.INVALID_BIOME.send(sender, argument);
        }

        return biome;
    }

    public static World getWorld(CommandSender sender, String argument) {
        World world = Bukkit.getWorld(argument);

        if (world == null)
            Message.INVALID_WORLD.send(sender, argument);

        return world;
    }

    public static Location getLocation(CommandSender sender, World world, String x, String y, String z) {
        Location location = null;

        try {
            location = new Location(world, Integer.parseInt(x), Integer.parseInt(y), Integer.parseInt(z));
        } catch (Throwable ex) {
            Message.INVALID_BLOCK.send(sender, world.getName() + ", " + x + ", " + y + ", " + z);
        }

        return location;
    }

    public static NumberArgument<Integer> getPage(CommandSender sender, String argument) {
        return getInt(sender, argument, Message.INVALID_PAGE);
    }

    public static NumberArgument<Integer> getRows(CommandSender sender, String argument) {
        return getInt(sender, argument, Message.INVALID_ROWS);
    }

    public static IslandPrivilege getIslandPrivilege(CommandSender sender, String argument) {
        IslandPrivilege islandPrivilege = null;

        try {
            islandPrivilege = IslandPrivilege.getByName(argument);
        } catch (NullPointerException ignored) {
        }

        if (islandPrivilege == null) {
            Message.INVALID_ISLAND_PERMISSION.send(sender, argument, Formatters.COMMA_FORMATTER.format(
                    IslandPrivilege.values().stream()
                            .sorted(Comparator.comparing(IslandPrivilege::getName))
                            .map(_islandPrivilege -> _islandPrivilege.toString().toLowerCase(Locale.ENGLISH))));
        }

        return islandPrivilege;
    }

    public static Rating getRating(CommandSender sender, String argument) {
        Rating rating = null;

        try {
            rating = Rating.valueOf(argument.toUpperCase(Locale.ENGLISH));
        } catch (Exception ex) {
            Message.INVALID_RATE.send(sender, argument, Rating.getValuesString());
        }

        return rating;
    }

    public static IslandFlag getIslandFlag(CommandSender sender, String argument) {
        IslandFlag islandFlag = null;

        try {
            islandFlag = IslandFlag.getByName(argument);
        } catch (NullPointerException ignored) {
        }

        if (islandFlag == null) {
            Message.INVALID_SETTINGS.send(sender, argument, Formatters.COMMA_FORMATTER.format(IslandFlag.values().stream()
                    .sorted(Comparator.comparing(IslandFlag::getName))
                    .map(_islandFlag -> _islandFlag.getName().toLowerCase(Locale.ENGLISH))));
        }

        return islandFlag;
    }

    public static World.Environment getEnvironment(CommandSender sender, String argument) {
        World.Environment environment = null;

        try {
            environment = World.Environment.valueOf(argument.toUpperCase(Locale.ENGLISH));
        } catch (Exception ignored) {
        }

        if (environment == null)
            Message.INVALID_ENVIRONMENT.send(sender, argument);

        return environment;
    }

    public static NumberArgument<Integer> getInterval(CommandSender sender, String argument) {
        NumberArgument<Integer> interval = getInt(sender, argument, Message.INVALID_INTERVAL);

        if (interval.isSucceed() && interval.getNumber() < 0) {
            Message.INVALID_INTERVAL.send(sender, argument);
            return new NumberArgument<>(interval.getNumber(), false);
        }

        return interval;
    }

    public static Map<String, String> parseArguments(String[] args) {
        Map<String, String> parsedArgs = new HashMap<>();
        String currentKey = null;
        StringBuilder stringBuilder = new StringBuilder();

        for (String arg : args) {
            if (arg.startsWith("-")) {
                if (currentKey != null && stringBuilder.length() > 0) {
                    parsedArgs.put(currentKey, stringBuilder.substring(1));
                }

                currentKey = arg.substring(1).toLowerCase(Locale.ENGLISH);
                stringBuilder = new StringBuilder();
            } else if (currentKey != null) {
                stringBuilder.append(" ").append(arg);
            }
        }

        if (currentKey != null && stringBuilder.length() > 0) {
            parsedArgs.put(currentKey, stringBuilder.substring(1));
        }

        return parsedArgs;
    }

    public static BorderColor getBorderColor(CommandSender sender, String argument) {
        BorderColor borderColor = null;

        try {
            borderColor = BorderColor.valueOf(argument.toUpperCase(Locale.ENGLISH));
        } catch (Exception ignored) {
        }

        if (borderColor == null)
            Message.INVALID_BORDER_COLOR.send(sender, argument);

        return borderColor;
    }

    private static NumberArgument<Integer> getInt(CommandSender sender, String argument, Message locale) {
        int i = 0;
        boolean status = true;

        try {
            i = Integer.parseInt(argument);
        } catch (IllegalArgumentException ex) {
            locale.send(sender, argument);
            status = false;
        }

        return new NumberArgument<>(i, status);
    }

}
