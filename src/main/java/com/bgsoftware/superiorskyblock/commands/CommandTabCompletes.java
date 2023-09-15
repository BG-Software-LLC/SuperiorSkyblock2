package com.bgsoftware.superiorskyblock.commands;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.enums.BorderColor;
import com.bgsoftware.superiorskyblock.api.enums.Rating;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandFlag;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.api.modules.PluginModule;
import com.bgsoftware.superiorskyblock.api.upgrades.Upgrade;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.Materials;
import com.bgsoftware.superiorskyblock.core.SequentialListBuilder;
import com.bgsoftware.superiorskyblock.core.menu.MenuIdentifiers;
import com.bgsoftware.superiorskyblock.core.menu.impl.internal.MenuCustom;
import com.bgsoftware.superiorskyblock.world.BukkitEntities;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class CommandTabCompletes {

    private CommandTabCompletes() {

    }

    public static List<String> getPlayerIslandsExceptSender(SuperiorSkyblockPlugin plugin, CommandSender sender, String argument, boolean hideVanish) {
        return getPlayerIslandsExceptSender(plugin, sender, argument, hideVanish, (onlinePlayer, onlineIsland) -> true);
    }

    public static List<String> getPlayerIslandsExceptSender(SuperiorSkyblockPlugin plugin, CommandSender sender,
                                                            String argument, boolean hideVanish,
                                                            BiPredicate<SuperiorPlayer, Island> islandPredicate) {
        SuperiorPlayer superiorPlayer = sender instanceof Player ? plugin.getPlayers().getSuperiorPlayer(sender) : null;
        Island island = superiorPlayer == null ? null : superiorPlayer.getIsland();
        return getOnlinePlayersWithIslands(plugin, argument, hideVanish, (onlinePlayer, onlineIsland) ->
                onlineIsland != null && (superiorPlayer == null || island == null || !island.equals(onlineIsland)) &&
                        islandPredicate.test(onlinePlayer, onlineIsland));
    }

    public static List<String> getIslandMembersWithLowerRole(Island island, String argument, PlayerRole maxRole) {
        return getIslandMembers(island, argument, islandMember -> islandMember.getPlayerRole().isLessThan(maxRole));
    }

    public static List<String> getIslandMembers(Island island, String argument, Predicate<SuperiorPlayer> predicate) {
        return getPlayers(island.getIslandMembers(false), argument, predicate);
    }

    public static List<String> getIslandMembers(Island island, String argument) {
        return getPlayers(island.getIslandMembers(false), argument);
    }

    public static List<String> getOnlinePlayers(SuperiorSkyblockPlugin plugin, String argument, boolean hideVanish) {
        String lowerArgument = argument.toLowerCase(Locale.ENGLISH);
        return new SequentialListBuilder<SuperiorPlayer>()
                .filter(onlinePlayer -> (!hideVanish || onlinePlayer.isShownAsOnline()) &&
                        onlinePlayer.getName().toLowerCase(Locale.ENGLISH).contains(lowerArgument))
                .map(getOnlineSuperiorPlayers(plugin), SuperiorPlayer::getName);
    }

    public static List<String> getOnlinePlayers(SuperiorSkyblockPlugin plugin, String argument, boolean hideVanish, Predicate<SuperiorPlayer> predicate) {
        String lowerArgument = argument.toLowerCase(Locale.ENGLISH);
        return new SequentialListBuilder<SuperiorPlayer>()
                .filter(onlinePlayer -> (!hideVanish || onlinePlayer.isShownAsOnline()) &&
                        predicate.test(onlinePlayer) && onlinePlayer.getName().toLowerCase(Locale.ENGLISH).contains(lowerArgument))
                .map(getOnlineSuperiorPlayers(plugin), SuperiorPlayer::getName);
    }

    public static List<String> getOnlinePlayersWithIslands(SuperiorSkyblockPlugin plugin, String argument,
                                                           boolean hideVanish,
                                                           @Nullable BiPredicate<SuperiorPlayer, Island> predicate) {
        List<String> tabArguments = new LinkedList<>();
        String lowerArgument = argument.toLowerCase(Locale.ENGLISH);

        for (Player player : Bukkit.getOnlinePlayers()) {
            SuperiorPlayer onlinePlayer = plugin.getPlayers().getSuperiorPlayer(player);
            if (!hideVanish || onlinePlayer.isShownAsOnline()) {
                Island onlineIsland = onlinePlayer.getIsland();
                if (predicate == null || predicate.test(onlinePlayer, onlineIsland)) {
                    if (onlinePlayer.getName().toLowerCase(Locale.ENGLISH).contains(lowerArgument))
                        tabArguments.add(onlinePlayer.getName());
                    if (onlineIsland != null && onlineIsland.getName().toLowerCase(Locale.ENGLISH).contains(lowerArgument))
                        tabArguments.add(onlineIsland.getName());
                }
            }
        }

        return Collections.unmodifiableList(tabArguments);
    }

    public static List<String> getIslandWarps(Island island, String argument) {
        return filterByArgument(island.getIslandWarps().keySet(), argument.toLowerCase(Locale.ENGLISH));
    }

    public static List<String> getIslandVisitors(Island island, String argument, boolean hideVanish) {
        return getPlayers(island.getIslandVisitors(!hideVanish), argument);
    }

    public static List<String> getCustomComplete(String argument, String... tabVariables) {
        return filterByArgument(Arrays.asList(tabVariables), argument.toLowerCase(Locale.ENGLISH));
    }

    public static List<String> getCustomComplete(String argument, Predicate<String> predicate, String... tabVariables) {
        String lowerArgument = argument.toLowerCase(Locale.ENGLISH);
        return new SequentialListBuilder<String>()
                .filter(var -> var.contains(lowerArgument) && predicate.test(var))
                .build(Arrays.asList(tabVariables));
    }

    public static List<String> getCustomComplete(String argument, IntStream tabVariables) {
        String lowerArgument = argument.toLowerCase(Locale.ENGLISH);
        return new SequentialListBuilder<String>()
                .filter(var -> var.contains(lowerArgument))
                .build(Stream.of(tabVariables).map(i -> i + ""));
    }

    public static List<String> getSchematics(SuperiorSkyblockPlugin plugin, String argument) {
        String lowerArgument = argument.toLowerCase(Locale.ENGLISH);
        return new SequentialListBuilder<String>()
                .filter(schematic -> !schematic.endsWith("_nether") && !schematic.endsWith("_the_end") &&
                        schematic.toLowerCase(Locale.ENGLISH).contains(lowerArgument))
                .build(plugin.getSchematics().getSchematics());
    }

    public static List<String> getIslandBannedPlayers(Island island, String argument) {
        return getPlayers(island.getBannedPlayers(), argument);
    }

    public static List<String> getUpgrades(SuperiorSkyblockPlugin plugin, String argument) {
        return filterByArgument(plugin.getUpgrades().getUpgrades(), Upgrade::getName, argument.toLowerCase(Locale.ENGLISH));
    }

    public static List<String> getPlayerRoles(SuperiorSkyblockPlugin plugin, String argument) {
        return filterByArgument(plugin.getRoles().getRoles(), PlayerRole::getName, argument.toLowerCase(Locale.ENGLISH));
    }

    public static List<String> getPlayerRoles(SuperiorSkyblockPlugin plugin, String argument, Predicate<PlayerRole> predicate) {
        String lowerArgument = argument.toLowerCase(Locale.ENGLISH);
        return new SequentialListBuilder<PlayerRole>()
                .filter(playerRole -> predicate.test(playerRole) && playerRole.toString().toLowerCase(Locale.ENGLISH).contains(lowerArgument))
                .map(plugin.getRoles().getRoles(), PlayerRole::getName);
    }

    public static List<String> getMaterials(String argument) {
        String lowerArgument = argument.toLowerCase(Locale.ENGLISH);
        return new SequentialListBuilder<String>().build(Materials.getBlocksNonLegacy().stream()
                .filter(material -> material.isBlock() && !Materials.isLegacy(material))
                .map(material -> material.name().toLowerCase(Locale.ENGLISH))
                .filter(materialName -> materialName.contains(lowerArgument)));
    }

    public static List<String> getPotionEffects(String argument) {
        String lowerArgument = argument.toLowerCase(Locale.ENGLISH);
        return new SequentialListBuilder<PotionEffectType>()
                .filter(potionEffectType -> {
                    try {
                        return potionEffectType != null && potionEffectType.getName().toLowerCase(Locale.ENGLISH).contains(lowerArgument);
                    } catch (Exception ex) {
                        return false;
                    }
                })
                .map(Arrays.asList(PotionEffectType.values()), PotionEffectType::getName);
    }

    public static List<String> getEntitiesForLimit(String argument) {
        String lowerArgument = argument.toLowerCase(Locale.ENGLISH);
        return new SequentialListBuilder<String>().build(Stream.of(EntityType.values())
                .filter(BukkitEntities::canHaveLimit)
                .map(entityType -> entityType.name().toLowerCase(Locale.ENGLISH))
                .filter(entityTypeName -> entityTypeName.contains(lowerArgument)));
    }

    public static List<String> getMaterialsForGenerators(String argument) {
        String lowerArgument = argument.toLowerCase(Locale.ENGLISH);
        return new SequentialListBuilder<String>().build(Materials.getSolids().stream()
                .map(material -> material.name().toLowerCase(Locale.ENGLISH))
                .filter(materialName -> materialName.contains(lowerArgument)));
    }

    public static List<String> getAllMissions(SuperiorSkyblockPlugin plugin) {
        return new SequentialListBuilder<String>()
                .build(plugin.getMissions().getAllMissions(), Mission::getName);
    }

    public static List<String> getMissions(SuperiorSkyblockPlugin plugin, String argument) {
        return filterByArgument(plugin.getMissions().getAllMissions(), Mission::getName, argument.toLowerCase(Locale.ENGLISH));
    }

    public static List<String> getMenus(SuperiorSkyblockPlugin plugin, String argument) {
        return filterByArgument(plugin.getMenus().getMenus().values(), menu -> menu instanceof MenuCustom ?
                        menu.getIdentifier().substring(MenuIdentifiers.MENU_CUSTOM_PREFIX.length()) : menu.getIdentifier(),
                argument.toLowerCase(Locale.ENGLISH));
    }

    public static List<String> getBiomes(String argument) {
        return getFromEnum(Arrays.asList(Biome.values()), argument.toLowerCase(Locale.ENGLISH));
    }

    public static List<String> getWorlds(String argument) {
        return filterByArgument(Bukkit.getWorlds(), World::getName, argument.toLowerCase(Locale.ENGLISH));
    }

    public static List<String> getIslandPrivileges(String argument) {
        String lowerArgument = argument.toLowerCase(Locale.ENGLISH);
        return new SequentialListBuilder<String>()
                .filter(name -> name.contains(lowerArgument))
                .build(IslandPrivilege.values(), islandPrivilege -> islandPrivilege.getName().toLowerCase(Locale.ENGLISH));
    }

    public static List<String> getRatedPlayers(SuperiorSkyblockPlugin plugin, Island island, String argument) {
        return filterByArgument(island.getRatings().keySet(),
                playerUUID -> plugin.getPlayers().getSuperiorPlayer(playerUUID).getName(),
                argument.toLowerCase(Locale.ENGLISH));
    }

    public static List<String> getRatings(String argument) {
        return getFromEnum(Arrays.asList(Rating.values()), argument.toLowerCase(Locale.ENGLISH));
    }

    public static List<String> getIslandFlags(String argument) {
        String lowerArgument = argument.toLowerCase(Locale.ENGLISH);
        return new SequentialListBuilder<String>()
                .filter(name -> name.contains(lowerArgument))
                .build(IslandFlag.values(), islandFlag -> islandFlag.getName().toLowerCase(Locale.ENGLISH));
    }

    public static List<String> getEnvironments(String argument) {
        return getFromEnum(Arrays.asList(World.Environment.values()), argument.toLowerCase(Locale.ENGLISH));
    }

    public static List<String> getBorderColors(String argument) {
        return getFromEnum(Arrays.asList(BorderColor.values()), argument.toLowerCase(Locale.ENGLISH));
    }

    public static List<String> getModules(SuperiorSkyblockPlugin plugin, String argument) {
        return filterByArgument(plugin.getModules().getModules(), PluginModule::getName, argument.toLowerCase(Locale.ENGLISH));
    }

    private static List<String> getPlayers(Collection<SuperiorPlayer> players, String argument) {
        return filterByArgument(players, SuperiorPlayer::getName, argument.toLowerCase(Locale.ENGLISH));
    }

    private static List<String> getPlayers(Collection<SuperiorPlayer> players, String argument, Predicate<SuperiorPlayer> predicate) {
        String lowerArgument = argument.toLowerCase(Locale.ENGLISH);
        return new SequentialListBuilder<SuperiorPlayer>()
                .filter(player -> predicate.test(player) && player.getName().toLowerCase(Locale.ENGLISH).contains(lowerArgument))
                .map(players, SuperiorPlayer::getName);
    }

    private static List<SuperiorPlayer> getOnlineSuperiorPlayers(SuperiorSkyblockPlugin plugin) {
        return new SequentialListBuilder<SuperiorPlayer>()
                .mutable()
                .build(Bukkit.getOnlinePlayers(), player -> plugin.getPlayers().getSuperiorPlayer(player));
    }

    private static List<String> filterByArgument(Collection<String> collection, String argument) {
        return new SequentialListBuilder<String>()
                .filter(name -> name.toLowerCase(Locale.ENGLISH).contains(argument))
                .build(collection);
    }

    private static <E> List<String> filterByArgument(Collection<E> collection, Function<E, String> mapper, String argument) {
        return new SequentialListBuilder<String>()
                .filter(name -> name.toLowerCase(Locale.ENGLISH).contains(argument))
                .build(collection, mapper);
    }

    private static List<String> getFromEnum(Collection<Enum<?>> enums, String argument) {
        String lowerArgument = argument.toLowerCase(Locale.ENGLISH);
        return new SequentialListBuilder<String>()
                .filter(name -> name.contains(lowerArgument))
                .build(enums, enumElement -> enumElement.name().toLowerCase(Locale.ENGLISH));
    }

}
