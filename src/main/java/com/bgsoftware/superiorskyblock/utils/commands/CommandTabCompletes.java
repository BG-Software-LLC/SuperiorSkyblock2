package com.bgsoftware.superiorskyblock.utils.commands;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandFlag;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.api.upgrades.Upgrade;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.menu.SuperiorMenuCustom;
import com.bgsoftware.superiorskyblock.utils.entities.EntityUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public final class CommandTabCompletes {

    private CommandTabCompletes() {

    }

    public static List<String> getPlayerIslandsExceptSender(SuperiorSkyblockPlugin plugin, CommandSender sender, String argument, boolean hideVanish){
        SuperiorPlayer superiorPlayer = sender instanceof Player ? plugin.getPlayers().getSuperiorPlayer(sender) : null;
        Island island = superiorPlayer == null ? null : superiorPlayer.getIsland();
        return getOnlinePlayersWithIslands(plugin, argument, hideVanish, (onlinePlayer, onlineIsland) ->
                onlineIsland != null && (superiorPlayer == null || island == null || !island.equals(onlineIsland)));
    }

    public static List<String> getIslandMembersWithLowerRole(Island island, String argument, PlayerRole maxRole){
        return getIslandMembers(island, argument, islandMember -> islandMember.getPlayerRole().isLessThan(maxRole));
    }

    public static List<String> getIslandMembers(Island island, String argument, Predicate<SuperiorPlayer> predicate){
        return getPlayers(island.getIslandMembers(false), argument, predicate);
    }

    public static List<String> getIslandMembers(Island island, String argument){
        return getPlayers(island.getIslandMembers(false), argument);
    }

    public static List<String> getOnlinePlayers(SuperiorSkyblockPlugin plugin, String argument, boolean hideVanish){
        String lowerArgument = argument.toLowerCase();
        return Bukkit.getOnlinePlayers().stream().map(plugin.getPlayers()::getSuperiorPlayer)
                .filter(onlinePlayer -> (!hideVanish || !onlinePlayer.isVanished()) &&
                        onlinePlayer.getName().toLowerCase().contains(lowerArgument))
                .map(SuperiorPlayer::getName).collect(Collectors.toList());
    }

    public static List<String> getOnlinePlayers(SuperiorSkyblockPlugin plugin, String argument, boolean hideVanish, Predicate<SuperiorPlayer> predicate){
        String lowerArgument = argument.toLowerCase();
        return Bukkit.getOnlinePlayers().stream().map(plugin.getPlayers()::getSuperiorPlayer)
                .filter(onlinePlayer -> (!hideVanish || !onlinePlayer.isVanished()) &&
                        predicate.test(onlinePlayer) && onlinePlayer.getName().toLowerCase().contains(lowerArgument))
                .map(SuperiorPlayer::getName).collect(Collectors.toList());
    }

    public static List<String> getOnlinePlayersWithIslands(SuperiorSkyblockPlugin plugin, String argument, boolean hideVanish){
        Set<String> tabArguments = new HashSet<>();
        String lowerArgument = argument.toLowerCase();

        for(Player player : Bukkit.getOnlinePlayers()){
            SuperiorPlayer onlinePlayer = plugin.getPlayers().getSuperiorPlayer(player);
            if(!hideVanish || !onlinePlayer.isVanished()) {
                Island onlineIsland = onlinePlayer.getIsland();
                if (onlinePlayer.getName().toLowerCase().contains(lowerArgument))
                    tabArguments.add(onlinePlayer.getName());
                if (onlineIsland != null && onlineIsland.getName().toLowerCase().contains(lowerArgument))
                    tabArguments.add(onlineIsland.getName());
            }
        }

        return new ArrayList<>(tabArguments);
    }

    public static List<String> getOnlinePlayersWithIslands(SuperiorSkyblockPlugin plugin, String argument, boolean hideVanish, BiPredicate<SuperiorPlayer, Island> predicate){
        Set<String> tabArguments = new HashSet<>();
        String lowerArgument = argument.toLowerCase();

        for(Player player : Bukkit.getOnlinePlayers()){
            SuperiorPlayer onlinePlayer = plugin.getPlayers().getSuperiorPlayer(player);
            if(!hideVanish || !onlinePlayer.isVanished()) {
                Island onlineIsland = onlinePlayer.getIsland();
                if (predicate.test(onlinePlayer, onlineIsland)) {
                    if (onlinePlayer.getName().toLowerCase().contains(lowerArgument))
                        tabArguments.add(onlinePlayer.getName());
                    if (onlineIsland != null && onlineIsland.getName().toLowerCase().contains(lowerArgument))
                        tabArguments.add(onlineIsland.getName());
                }
            }
        }

        return new ArrayList<>(tabArguments);
    }

    public static List<String> getIslandWarps(Island island, String argument){
        String lowerArgument = argument.toLowerCase();
        return island.getIslandWarps().keySet().stream().filter(warpName -> warpName.toLowerCase().contains(lowerArgument))
                .collect(Collectors.toList());
    }

    public static List<String> getIslandVisitors(Island island, String argument){
        return getPlayers(island.getIslandVisitors(), argument);
    }

    public static List<String> getCustomComplete(String argument, String... tabVariables){
        String lowerArgument = argument.toLowerCase();
        return Stream.of(tabVariables).filter(var -> var.contains(lowerArgument)).collect(Collectors.toList());
    }

    public static List<String> getCustomComplete(String argument, Predicate<String> predicate, String... tabVariables){
        String lowerArgument = argument.toLowerCase();
        return Stream.of(tabVariables).filter(var -> var.contains(lowerArgument) && predicate.test(var)).collect(Collectors.toList());
    }

    public static List<String> getCustomComplete(String argument, IntStream tabVariables){
        String lowerArgument = argument.toLowerCase();
        return Stream.of(tabVariables).map(i -> i + "").filter(var -> var.contains(lowerArgument)).collect(Collectors.toList());
    }

    public static List<String> getSchematics(SuperiorSkyblockPlugin plugin, String argument){
        String lowerArgument = argument.toLowerCase();
        return plugin.getSchematics().getSchematics().stream().filter(schematic -> !schematic.endsWith("_nether") &&
                !schematic.endsWith("_the_end") && schematic.toLowerCase().contains(lowerArgument)).collect(Collectors.toList());
    }

    public static List<String> getIslandBannedPlayers(Island island, String argument){
        return getPlayers(island.getBannedPlayers(), argument);
    }

    public static List<String> getUpgrades(SuperiorSkyblockPlugin plugin, String argument){
        String lowerArgument = argument.toLowerCase();
        return plugin.getUpgrades().getUpgrades().stream()
                .filter(upgrade -> upgrade.getName().toLowerCase().contains(lowerArgument))
                .map(Upgrade::getName).collect(Collectors.toList());
    }

    public static List<String> getPlayerRoles(SuperiorSkyblockPlugin plugin, String argument){
        String lowerArgument = argument.toLowerCase();
        return plugin.getPlayers().getRoles().stream()
                .filter(playerRole -> playerRole.toString().toLowerCase().contains(lowerArgument))
                .map(PlayerRole::toString).collect(Collectors.toList());
    }

    public static List<String> getPlayerRoles(SuperiorSkyblockPlugin plugin, String argument, Predicate<PlayerRole> predicate){
        String lowerArgument = argument.toLowerCase();
        return plugin.getPlayers().getRoles().stream()
                .filter(playerRole -> predicate.test(playerRole) &&  playerRole.toString().toLowerCase().contains(lowerArgument))
                .map(PlayerRole::toString).collect(Collectors.toList());
    }

    public static List<String> getMaterials(String argument){
        String lowerArgument = argument.toLowerCase();
        return Stream.of(Material.values()).filter(material -> material.isBlock() && !material.name().startsWith("LEGACY_") &&
                material.name().toLowerCase().contains(lowerArgument)).map(material -> material.name().toLowerCase())
                .collect(Collectors.toList());
    }

    public static List<String> getPotionEffects(String argument){
        String lowerArgument = argument.toLowerCase();
        return Stream.of(PotionEffectType.values()).filter(potionEffectType -> {
            try{
                return potionEffectType != null && potionEffectType.getName().toLowerCase().contains(lowerArgument);
            }catch (Exception ex){
                return false;
            }
        }).map(PotionEffectType::getName).collect(Collectors.toList());
    }

    public static List<String> getEntitiesForLimit(String argument){
        String lowerArgument = argument.toLowerCase();
        return Stream.of(EntityType.values()).filter(entityType -> EntityUtils.canHaveLimit(entityType) &&
                entityType.name().toLowerCase().contains(lowerArgument)).map(entityType -> entityType.name().toLowerCase())
                .collect(Collectors.toList());
    }

    public static List<String> getMaterialsForGenerators(String argument){
        String lowerArgument = argument.toLowerCase();
        return Stream.of(Material.values()).filter(material -> material.isSolid() &&
                material.name().toLowerCase().contains(lowerArgument)).map(material -> material.name().toLowerCase())
                .collect(Collectors.toList());
    }

    public static List<String> getAllMissions(SuperiorSkyblockPlugin plugin){
        return plugin.getMissions().getAllMissions().stream().map(Mission::getName).collect(Collectors.toList());
    }

    public static List<String> getMissions(SuperiorSkyblockPlugin plugin, String argument){
        String lowerArgument = argument.toLowerCase();
        return  plugin.getMissions().getAllMissions().stream()
                .filter(mission -> mission.getName().toLowerCase().contains(lowerArgument))
                .map(Mission::getName).collect(Collectors.toList());
    }

    public static List<String> getMenus(String argument){
        String lowerArgument = argument.toLowerCase();
        return SuperiorMenuCustom.getCustomMenus().stream()
                .filter(menu -> menu.toLowerCase().contains(lowerArgument))
                .collect(Collectors.toList());
    }

    public static List<String> getBiomes(String argument){
        String lowerArgument = argument.toLowerCase();
        return Stream.of(Biome.values()).filter(biome -> biome.name().toLowerCase().contains(lowerArgument))
                .map(material -> material.name().toLowerCase()).collect(Collectors.toList());
    }

    public static List<String> getWorlds(String argument){
        String lowerArgument = argument.toLowerCase();
        return Bukkit.getWorlds().stream().filter(world -> world.getName().toLowerCase().contains(lowerArgument))
                .map(World::getName).collect(Collectors.toList());
    }

    public static List<String> getIslandPrivileges(String argument){
        String lowerArgument = argument.toLowerCase();
        return IslandPrivilege.values().stream()
                .filter(islandPrivilege -> islandPrivilege.getName().toLowerCase().contains(lowerArgument))
                .map(islandPrivilege -> islandPrivilege.getName().toLowerCase()).collect(Collectors.toList());
    }

    public static List<String> getRatedPlayers(SuperiorSkyblockPlugin plugin, Island island, String argument){
        String lowerArgument = argument.toLowerCase();
        return island.getRatings().keySet().stream().map(plugin.getPlayers()::getSuperiorPlayer)
                .filter(ratePlayer -> ratePlayer.getName().toLowerCase().contains(lowerArgument))
                .map(SuperiorPlayer::getName).collect(Collectors.toList());
    }

    public static List<String> getRatings(String argument){
        String lowerArgument = argument.toLowerCase();
        return IslandPrivilege.values().stream()
                .filter(islandPrivilege -> islandPrivilege.getName().toLowerCase().contains(lowerArgument))
                .map(IslandPrivilege::getName).collect(Collectors.toList());
    }

    public static List<String> getIslandFlags(String argument){
        String lowerArgument = argument.toLowerCase();
        return IslandFlag.values().stream()
                .filter(islandFlag -> islandFlag.getName().toLowerCase().contains(lowerArgument))
                .map(islandFlag -> islandFlag.getName().toLowerCase()).collect(Collectors.toList());
    }

    public static List<String> getEnvironments(String argument){
        String lowerArgument = argument.toLowerCase();
        return Arrays.stream(World.Environment.values())
                .filter(environment -> environment.name().toLowerCase().contains(lowerArgument))
                .map(environment -> environment.name().toLowerCase()).collect(Collectors.toList());
    }

    private static List<String> getPlayers(Collection<SuperiorPlayer> players, String argument){
        String lowerArgument = argument.toLowerCase();
        return players.stream().filter(player -> player.getName().toLowerCase().contains(lowerArgument))
                .map(SuperiorPlayer::getName).collect(Collectors.toList());
    }

    private static List<String> getPlayers(Collection<SuperiorPlayer> players, String argument, Predicate<SuperiorPlayer> predicate){
        String lowerArgument = argument.toLowerCase();
        return players.stream().filter(player -> predicate.test(player) && player.getName().toLowerCase().contains(lowerArgument))
                .map(SuperiorPlayer::getName).collect(Collectors.toList());
    }

}
