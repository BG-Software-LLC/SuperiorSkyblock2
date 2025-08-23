package com.bgsoftware.superiorskyblock.service.placeholders;

import com.bgsoftware.common.annotations.NotNull;
import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandFlag;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.island.SortingType;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.service.placeholders.IslandPlaceholderParser;
import com.bgsoftware.superiorskyblock.api.service.placeholders.PlaceholdersService;
import com.bgsoftware.superiorskyblock.api.service.placeholders.PlayerPlaceholderParser;
import com.bgsoftware.superiorskyblock.api.upgrades.Upgrade;
import com.bgsoftware.superiorskyblock.api.world.Dimension;
import com.bgsoftware.superiorskyblock.api.world.WorldInfo;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.ObjectsPools;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.key.ConstantKeys;
import com.bgsoftware.superiorskyblock.core.key.Keys;
import com.bgsoftware.superiorskyblock.core.values.BlockValue;
import com.bgsoftware.superiorskyblock.external.placeholders.PlaceholdersProvider;
import com.bgsoftware.superiorskyblock.island.IslandUtils;
import com.bgsoftware.superiorskyblock.island.privilege.IslandPrivileges;
import com.bgsoftware.superiorskyblock.island.role.SPlayerRole;
import com.bgsoftware.superiorskyblock.island.top.SortingTypes;
import com.bgsoftware.superiorskyblock.service.IService;
import com.bgsoftware.superiorskyblock.world.Dimensions;
import com.google.common.collect.ImmutableMap;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.potion.PotionEffectType;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlaceholdersServiceImpl implements PlaceholdersService, IService {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private static final Pattern ISLAND_PLACEHOLDER_PATTERN = Pattern.compile("island_(.+)");
    private static final Pattern PLAYER_PLACEHOLDER_PATTERN = Pattern.compile("player_(.+)");

    private static final Pattern BLOCK_COUNT_PLACEHOLDER_PATTERN = Pattern.compile("island_block_count_(.+)");
    private static final Pattern BLOCK_LEVEL_PLACEHOLDER_PATTERN = Pattern.compile("island_block_level_(.+)");
    private static final Pattern BLOCK_LIMIT_PLACEHOLDER_PATTERN = Pattern.compile("island_block_limit_(.+)");
    private static final Pattern BLOCK_TOTAL_LEVEL_PLACEHOLDER_PATTERN = Pattern.compile("island_block_total_level_(.+)");
    private static final Pattern BLOCK_TOTAL_WORTH_PLACEHOLDER_PATTERN = Pattern.compile("island_block_total_worth_(.+)");
    private static final Pattern BLOCK_WORTH_PLACEHOLDER_PATTERN = Pattern.compile("island_block_worth_(.+)");
    private static final Pattern COUNT_PLACEHOLDER_PATTERN = Pattern.compile("island_block_count_(.+)");
    private static final Pattern DATA_PLACEHOLDER_PATTERN = Pattern.compile("island_data_(.+)");
    private static final Pattern EFFECT_PLACEHOLDER_PATTERN = Pattern.compile("island_effect_(.+)");
    private static final Pattern ENTITY_COUNT_PLACEHOLDER_PATTERN = Pattern.compile("island_entity_count_(.+)");
    private static final Pattern ENTITY_LIMIT_PLACEHOLDER_PATTERN = Pattern.compile("island_entity_limit_(.+)");
    private static final Pattern FLAG_PLACEHOLDER_PATTERN = Pattern.compile("flag_(.+)");
    private static final Pattern GENERATOR_AMOUNT_PLACEHOLDER_PATTERN = Pattern.compile("island_generator_amount_(.+)");
    private static final Pattern GENERATOR_PERCENTAGE_PLACEHOLDER_PATTERN = Pattern.compile("island_generator_percentage_(.+)");
    private static final Pattern GENERATOR_NORMAL_PLACEHOLDER_PATTERN = Pattern.compile("normal_(.+)");
    private static final Pattern GENERATOR_NETHER_PLACEHOLDER_PATTERN = Pattern.compile("nether_(.+)");
    private static final Pattern GENERATOR_END_PLACEHOLDER_PATTERN = Pattern.compile("end_(.+)");
    private static final Pattern MEMBER_PLACEHOLDER_PATTERN = Pattern.compile("member_(.+)");
    private static final Pattern MISSIONS_COMPLETED_PATTERN = Pattern.compile("missions_completed_(.+)");
    private static final Pattern MISSION_STATUS_PATTERN = Pattern.compile("mission_status_(.+)");
    private static final Pattern PERMISSION_PLACEHOLDER_PATTERN = Pattern.compile("island_permission_(.+)");
    private static final Pattern PERMISSION_ROLE_PLACEHOLDER_PATTERN = Pattern.compile("island_permission_role_(.+)");
    private static final Pattern ROLE_COUNT_PLACEHOLDER_PATTERN = Pattern.compile("island_role_count_(.+)");
    private static final Pattern ROLE_LIMIT_PLACEHOLDER_PATTERN = Pattern.compile("island_role_limit_(.+)");
    private static final Pattern UPGRADE_PLACEHOLDER_PATTERN = Pattern.compile("island_upgrade_(.+)");
    private static final Pattern TOP_PLACEHOLDER_PATTERN = Pattern.compile("island_top_(.+)");
    private static final Pattern TOP_WORTH_PLACEHOLDER_PATTERN = Pattern.compile("worth_(.+)");
    private static final Pattern TOP_LEVEL_PLACEHOLDER_PATTERN = Pattern.compile("level_(.+)");
    private static final Pattern TOP_RATING_PLACEHOLDER_PATTERN = Pattern.compile("rating_(.+)");
    private static final Pattern TOP_PLAYERS_PLACEHOLDER_PATTERN = Pattern.compile("players_(.+)");
    private static final Pattern TOP_VALUE_FORMAT_PLACEHOLDER_PATTERN = Pattern.compile("value_format_(.+)");
    private static final Pattern TOP_VALUE_RAW_PLACEHOLDER_PATTERN = Pattern.compile("value_raw_(.+)");
    private static final Pattern TOP_VALUE_PLACEHOLDER_PATTERN = Pattern.compile("value_(.+)");
    private static final Pattern TOP_LEADER_PLACEHOLDER_PATTERN = Pattern.compile("leader_(.+)");
    private static final Pattern TOP_CUSTOM_PLACEHOLDER_PATTERN = Pattern.compile("(\\d+)_(.+)");
    private static final Pattern VISITOR_LAST_JOIN_PLACEHOLDER_PATTERN = Pattern.compile("visitor_last_join_(.+)");

    private static final Map<String, PlayerPlaceholderParser> PLAYER_PARSES =
            new ImmutableMap.Builder<String, PlayerPlaceholderParser>()
                    .put("blocks_stacker", superiorPlayer ->
                            Formatters.BOOLEAN_FORMATTER.format(superiorPlayer.hasBlocksStackerEnabled(), superiorPlayer.getUserLocale()))
                    .put("border_color", superiorPlayer ->
                            Formatters.BORDER_COLOR_FORMATTER.format(superiorPlayer.getBorderColor(), superiorPlayer.getUserLocale()))
                    .put("bypass", superiorPlayer ->
                            Formatters.BOOLEAN_FORMATTER.format(superiorPlayer.hasBypassModeEnabled(), superiorPlayer.getUserLocale()))
                    .put("chat_spy", superiorPlayer ->
                            Formatters.BOOLEAN_FORMATTER.format(superiorPlayer.hasAdminSpyEnabled(), superiorPlayer.getUserLocale()))
                    .put("disbands", superiorPlayer ->
                            superiorPlayer.getDisbands() + "")
                    .put("fly", superiorPlayer ->
                            Formatters.BOOLEAN_FORMATTER.format(superiorPlayer.hasIslandFlyEnabled(), superiorPlayer.getUserLocale()))
                    .put("locale", superiorPlayer ->
                            Formatters.LOCALE_FORMATTER.format(superiorPlayer.getUserLocale()))
                    .put("missions_completed", superiorPlayer ->
                            superiorPlayer.getCompletedMissions().size() + "")
                    .put("panel", superiorPlayer ->
                            Formatters.BOOLEAN_FORMATTER.format(superiorPlayer.hasToggledPanel(), superiorPlayer.getUserLocale()))
                    .put("role", superiorPlayer ->
                            superiorPlayer.getPlayerRole().toString())
                    .put("role_display", superiorPlayer ->
                            superiorPlayer.getPlayerRole().getDisplayName())
                    .put("schematics", superiorPlayer ->
                            Formatters.BOOLEAN_FORMATTER.format(superiorPlayer.hasSchematicModeEnabled(), superiorPlayer.getUserLocale()))
                    .put("team_chat", superiorPlayer ->
                            Formatters.BOOLEAN_FORMATTER.format(superiorPlayer.hasTeamChatEnabled(), superiorPlayer.getUserLocale()))
                    .put("texture", SuperiorPlayer::getTextureValue)
                    .put("world_border", superiorPlayer ->
                            Formatters.BOOLEAN_FORMATTER.format(superiorPlayer.hasWorldBorderEnabled(), superiorPlayer.getUserLocale()))
                    .build();

    @SuppressWarnings("ConstantConditions")
    private static final Map<String, IslandPlaceholderParser> ISLAND_PARSES =
            new ImmutableMap.Builder<String, IslandPlaceholderParser>()
                    // Island Placeholders
                    .put("bank", (island, superiorPlayer) ->
                            Formatters.NUMBER_FORMATTER.format(island.getIslandBank().getBalance()))
                    .put("bank_format", (island, superiorPlayer) ->
                            Formatters.FANCY_NUMBER_FORMATTER.format(island.getIslandBank().getBalance(), superiorPlayer.getUserLocale()))
                    .put("bank_int", (island, superiorPlayer) ->
                            island.getIslandBank().getBalance().toBigInteger().toString())
                    .put("bank_raw", (island, superiorPlayer) ->
                            island.getIslandBank().getBalance().toString())
                    .put("bank_limit", (island, superiorPlayer) ->
                            Formatters.NUMBER_FORMATTER.format(island.getBankLimit()))
                    .put("bank_limit_format", (island, superiorPlayer) ->
                            Formatters.FANCY_NUMBER_FORMATTER.format(island.getBankLimit(), superiorPlayer.getUserLocale()))
                    .put("bank_limit_int", (island, superiorPlayer) ->
                            island.getBankLimit().toBigInteger().toString())
                    .put("bank_limit_raw", (island, superiorPlayer) ->
                            island.getBankLimit().toString())
                    .put("bank_last_interest", (island, superiorPlayer) ->
                            Formatters.TIME_FORMATTER.format(Duration.ofSeconds(island.getLastInterestTime()), superiorPlayer.getUserLocale()))
                    .put("bank_next_interest", (island, superiorPlayer) ->
                            Formatters.TIME_FORMATTER.format(Duration.ofSeconds(island.getNextInterest()), superiorPlayer.getUserLocale()))
                    .put("bans_count", (island, superiorPlayer) ->
                            island.getBannedPlayers().size() + "")
                    .put("bans_list", (island, superiorPlayer) -> {
                        StringBuilder teamBuilder = new StringBuilder();
                        List<SuperiorPlayer> players = island.getBannedPlayers();
                        if (players.isEmpty()) {
                            return "";
                        }
                        for (SuperiorPlayer player : players) {
                            teamBuilder.append(", ").append(player.getName());
                        }
                        return teamBuilder.substring(2);
                    })
                    .put("biome", (island, superiorPlayer) ->
                            Formatters.CAPITALIZED_FORMATTER.format(island.getBiome().name()))
                    .put("bonus_level", (island, superiorPlayer) ->
                            Formatters.NUMBER_FORMATTER.format(island.getBonusLevel()))
                    .put("bonus_level_format", (island, superiorPlayer) ->
                            Formatters.FANCY_NUMBER_FORMATTER.format(island.getBonusLevel(), superiorPlayer.getUserLocale()))
                    .put("bonus_level_int", (island, superiorPlayer) ->
                            island.getBonusLevel().toBigInteger().toString())
                    .put("bonus_level_raw", (island, superiorPlayer) ->
                            island.getBonusLevel().toString())
                    .put("bonus_worth", (island, superiorPlayer) ->
                            Formatters.NUMBER_FORMATTER.format(island.getBonusWorth()))
                    .put("bonus_worth_format", (island, superiorPlayer) ->
                            Formatters.FANCY_NUMBER_FORMATTER.format(island.getBonusWorth(), superiorPlayer.getUserLocale()))
                    .put("bonus_worth_int", (island, superiorPlayer) ->
                            island.getBonusWorth().toBigInteger().toString())
                    .put("bonus_worth_raw", (island, superiorPlayer) ->
                            island.getBonusWorth().toString())
                    .put("center", (island, superiorPlayer) ->
                            Formatters.BLOCK_POSITION_FORMATTER.format(island.getCenterPosition(), getDefaultWorldInfo(island)))
                    .put("center_x", (island, superiorPlayer) ->
                            island.getCenterPosition().getX() + "")
                    .put("center_y", (island, superiorPlayer) ->
                            island.getCenterPosition().getY() + "")
                    .put("center_z", (island, superiorPlayer) ->
                            island.getCenterPosition().getZ() + "")
                    .put("chest_size", (island, superiorPlayer) ->
                            island.getChestSize() + "")
                    .put("coop_limit", (island, superiorPlayer) ->
                            island.getCoopLimit() + "")
                    .put("coop_list", (island, superiorPlayer) -> {
                        StringBuilder teamBuilder = new StringBuilder();
                        List<SuperiorPlayer> players = island.getCoopPlayers();
                        if (players.isEmpty()) {
                            return "";
                        }
                        for (SuperiorPlayer player : players) {
                            teamBuilder.append(", ").append(player.getName());
                        }
                        return teamBuilder.substring(2);
                    })
                    .put("coop_size", (island, superiorPlayer) ->
                            island.getCoopPlayers().size() + "")
                    .put("creation_time", (island, superiorPlayer) ->
                            island.getCreationTimeDate())
                    .put("crops_multiplier", (island, superiorPlayer) ->
                            island.getCropGrowthMultiplier() + "")
                    .put("description", (island, superiorPlayer) ->
                            island.getDescription())
                    .put("discord", (island, superiorPlayer) ->
                            island.hasPermission(superiorPlayer, IslandPrivileges.DISCORD_SHOW) ? island.getDiscord() : "None")
                    .put("discord_all", (island, superiorPlayer) ->
                            island.getDiscord())
                    .put("drops_multiplier", (island, superiorPlayer) ->
                            island.getMobDropsMultiplier() + "")
                    .put("end_unlocked", (island, superiorPlayer) ->
                            Formatters.BOOLEAN_FORMATTER.format(island.isEndEnabled(), superiorPlayer.getUserLocale()))
                    .put("exists", (island, superiorPlayer) ->
                            Formatters.BOOLEAN_FORMATTER.format(island != null, superiorPlayer.getUserLocale()))
                    .put("home", (island, superiorPlayer) ->
                            Formatters.LOCATION_FORMATTER.format(island.getIslandHome(getDefaultWorldInfo(island).getDimension())))
                    .put("home_x", (island, superiorPlayer) ->
                            island.getIslandHome(getDefaultWorldInfo(island).getDimension()).getBlockX() + "")
                    .put("home_y", (island, superiorPlayer) ->
                            island.getIslandHome(getDefaultWorldInfo(island).getDimension()).getBlockY() + "")
                    .put("home_z", (island, superiorPlayer) ->
                            island.getIslandHome(getDefaultWorldInfo(island).getDimension()).getBlockZ() + "")
                    .put("is_coop", (island, superiorPlayer) ->
                            Formatters.BOOLEAN_FORMATTER.format(island.isCoop(superiorPlayer), superiorPlayer.getUserLocale()))
                    .put("is_leader", (island, superiorPlayer) ->
                            Formatters.BOOLEAN_FORMATTER.format(island.getOwner().equals(superiorPlayer), superiorPlayer.getUserLocale()))
                    .put("is_member", (island, superiorPlayer) ->
                            Formatters.BOOLEAN_FORMATTER.format(island.isMember(superiorPlayer), superiorPlayer.getUserLocale()))
                    .put("is_visitor", (island, superiorPlayer) ->
                            Formatters.BOOLEAN_FORMATTER.format(island.isVisitor(superiorPlayer, true), superiorPlayer.getUserLocale()))
                    .put("last_time_updated", (island, superiorPlayer) ->
                            Formatters.TIME_FORMATTER.format(Duration.ofSeconds(island.getLastTimeUpdate()), superiorPlayer.getUserLocale()))
                    .put("leader", (island, superiorPlayer) ->
                            island.getOwner().getName())
                    .put("level", (island, superiorPlayer) ->
                            Formatters.NUMBER_FORMATTER.format(island.getIslandLevel()))
                    .put("level_format", (island, superiorPlayer) ->
                            Formatters.FANCY_NUMBER_FORMATTER.format(island.getIslandLevel(), superiorPlayer.getUserLocale()))
                    .put("level_int", (island, superiorPlayer) ->
                            island.getIslandLevel().toBigInteger().toString())
                    .put("level_raw", (island, superiorPlayer) ->
                            island.getIslandLevel().toString())
                    .put("locked", (island, superiorPlayer) ->
                            Formatters.BOOLEAN_FORMATTER.format(island.isLocked(), superiorPlayer.getUserLocale()))
                    .put("name", (island, superiorPlayer) ->
                            plugin.getSettings().getIslandNames().isColorSupport() ?
                                    Formatters.COLOR_FORMATTER.format(island.getName()) : island.getName())
                    .put("name_leader", (island, superiorPlayer) ->
                            island.getName().isEmpty() ? island.getOwner().getName() :
                                    plugin.getSettings().getIslandNames().isColorSupport() ?
                                            Formatters.COLOR_FORMATTER.format(island.getName()) : island.getName())
                    .put("nether_unlocked", (island, superiorPlayer) ->
                            Formatters.BOOLEAN_FORMATTER.format(island.isNetherEnabled(), superiorPlayer.getUserLocale()))
                    .put("normal_unlocked", (island, superiorPlayer) ->
                            Formatters.BOOLEAN_FORMATTER.format(island.isNormalEnabled(), superiorPlayer.getUserLocale()))
                    .put("paypal", (island, superiorPlayer) ->
                            island.hasPermission(superiorPlayer, IslandPrivileges.PAYPAL_SHOW) ? island.getPaypal() : "None")
                    .put("paypal_all", (island, superiorPlayer) ->
                            island.getPaypal())
                    .put("players_count", (island, superiorPlayer) ->
                            island.getAllPlayersInside().size() + "")
                    .put("players_list", (island, superiorPlayer) -> {
                        StringBuilder teamBuilder = new StringBuilder();
                        List<SuperiorPlayer> players = island.getAllPlayersInside();
                        if (players.isEmpty()) {
                            return "";
                        }
                        for (SuperiorPlayer player : players) {
                            teamBuilder.append(", ").append(player.getName());
                        }
                        return teamBuilder.substring(2);
                    })
                    .put("radius", (island, superiorPlayer) ->
                            island.getIslandSize() + "")
                    .put("rating", (island, superiorPlayer) ->
                            island.getTotalRating() + "")
                    .put("rating_amount", (island, superiorPlayer) ->
                            island.getRatingAmount() + "")
                    .put("rating_stars", (island, superiorPlayer) ->
                            Formatters.RATING_FORMATTER.format(island.getTotalRating(), superiorPlayer.getUserLocale()))
                    .put("raw_bank_limit", (island, superiorPlayer) ->
                            Formatters.NUMBER_FORMATTER.format(island.getBankLimitRaw()))
                    .put("raw_bank_limit_format", (island, superiorPlayer) ->
                            Formatters.FANCY_NUMBER_FORMATTER.format(island.getBankLimitRaw(), superiorPlayer.getUserLocale()))
                    .put("raw_bank_limit_int", (island, superiorPlayer) ->
                            island.getBankLimitRaw().toBigInteger().toString())
                    .put("raw_bank_limit_raw", (island, superiorPlayer) ->
                            island.getBankLimitRaw().toString())
                    .put("raw_coop_limit", (island, superiorPlayer) ->
                            island.getCoopLimitRaw() + "")
                    .put("raw_crops_multiplier", (island, superiorPlayer) ->
                            island.getCropGrowthRaw() + "")
                    .put("raw_drops_multiplier", (island, superiorPlayer) ->
                            island.getMobDropsRaw() + "")
                    .put("raw_level", (island, superiorPlayer) ->
                            Formatters.NUMBER_FORMATTER.format(island.getRawLevel()))
                    .put("raw_level_format", (island, superiorPlayer) ->
                            Formatters.FANCY_NUMBER_FORMATTER.format(island.getRawLevel(), superiorPlayer.getUserLocale()))
                    .put("raw_level_int", (island, superiorPlayer) ->
                            island.getRawLevel().toBigInteger().toString())
                    .put("raw_level_raw", (island, superiorPlayer) ->
                            island.getRawLevel().toString())
                    .put("raw_radius", (island, superiorPlayer) ->
                            island.getIslandSizeRaw() + "")
                    .put("raw_spawners_multiplier", (island, superiorPlayer) ->
                            island.getSpawnerRatesRaw() + "")
                    .put("raw_team_limit", (island, superiorPlayer) ->
                            island.getTeamLimitRaw() + "")
                    .put("raw_warps_limit", (island, superiorPlayer) ->
                            island.getWarpsLimitRaw() + "")
                    .put("raw_worth", (island, superiorPlayer) ->
                            Formatters.NUMBER_FORMATTER.format(island.getRawWorth()))
                    .put("raw_worth_format", (island, superiorPlayer) ->
                            Formatters.FANCY_NUMBER_FORMATTER.format(island.getRawWorth(), superiorPlayer.getUserLocale()))
                    .put("raw_worth_int", (island, superiorPlayer) ->
                            island.getRawWorth().toBigInteger().toString())
                    .put("raw_worth_raw", (island, superiorPlayer) ->
                            island.getRawWorth().toString())
                    .put("schematic", (island, superiorPlayer) ->
                            island.getSchematicName())
                    .put("size", (island, superiorPlayer) -> {
                        int size = island.getIslandSize() * 2 + 1;
                        return size + " x " + size;
                    })
                    .put("size_format", (island, superiorPlayer) -> {
                        int size = island.getIslandSize() * 2 + 1;
                        int rounded = 5 * (Math.round(size / 5.0F));
                        if (Math.abs(size - rounded) == 1)
                            size = rounded;
                        return size + " x " + size;
                    })
                    .put("spawners_multiplier", (island, superiorPlayer) ->
                            island.getSpawnerRatesMultiplier() + "")
                    .put("team_limit", (island, superiorPlayer) ->
                            island.getTeamLimit() + "")
                    .put("team_list", (island, superiorPlayer) -> {
                        StringBuilder teamBuilder = new StringBuilder();
                        List<SuperiorPlayer> players = island.getIslandMembers(true);
                        if (players.isEmpty()) {
                            return "";
                        }
                        for (SuperiorPlayer player : players) {
                            teamBuilder.append(", ").append(player.getName());
                        }
                        return teamBuilder.substring(2);
                    })
                    .put("team_size", (island, superiorPlayer) ->
                            island.getIslandMembers(true).size() + "")
                    .put("team_size_online", (island, superiorPlayer) ->
                            island.getIslandMembers(true).stream().filter(SuperiorPlayer::isShownAsOnline).count() + "")
                    .put("unique_visitors_count", (island, superiorPlayer) ->
                            island.getUniqueVisitors().size() + "")
                    .put("unique_visitors_list", (island, superiorPlayer) -> {
                        StringBuilder teamBuilder = new StringBuilder();
                        List<SuperiorPlayer> players = island.getUniqueVisitors();
                        if (players.isEmpty()) {
                            return "";
                        }
                        for (SuperiorPlayer player : players) {
                            teamBuilder.append(", ").append(player.getName());
                        }
                        return teamBuilder.substring(2);
                    })
                    .put("uuid", (island, superiorPlayer) ->
                            island.getUniqueId() + "")
                    .put("visitors_count", (island, superiorPlayer) ->
                            island.getIslandVisitors(false).size() + "")
                    .put("visitors_list", (island, superiorPlayer) -> {
                        StringBuilder teamBuilder = new StringBuilder();
                        List<SuperiorPlayer> players = island.getIslandVisitors();
                        if (players.isEmpty()) {
                            return "";
                        }
                        for (SuperiorPlayer player : players) {
                            teamBuilder.append(", ").append(player.getName());
                        }
                        return teamBuilder.substring(2);
                    })
                    .put("visitors_location", (island, superiorPlayer) ->
                            Formatters.LOCATION_FORMATTER.format(island.getVisitorsLocation(getDefaultWorldInfo(island).getDimension())))
                    .put("visitors_location_x", (island, superiorPlayer) ->
                            island.getVisitorsLocation(getDefaultWorldInfo(island).getDimension()).getBlockX() + "")
                    .put("visitors_location_y", (island, superiorPlayer) ->
                            island.getVisitorsLocation(getDefaultWorldInfo(island).getDimension()).getBlockY() + "")
                    .put("visitors_location_z", (island, superiorPlayer) ->
                            island.getVisitorsLocation(getDefaultWorldInfo(island).getDimension()).getBlockZ() + "")
                    .put("warps", (island, superiorPlayer) ->
                            island.getIslandWarps().size() + "")
                    .put("warps_limit", (island, superiorPlayer) ->
                            island.getWarpsLimit() + "")
                    .put("world", (island, superiorPlayer) ->
                            getDefaultWorldInfo(island).getName())
                    .put("worth", (island, superiorPlayer) ->
                            Formatters.NUMBER_FORMATTER.format(island.getWorth()))
                    .put("worth_format", (island, superiorPlayer) ->
                            Formatters.FANCY_NUMBER_FORMATTER.format(island.getWorth(), superiorPlayer.getUserLocale()))
                    .put("worth_int", (island, superiorPlayer) ->
                            island.getWorth().toBigInteger().toString())
                    .put("worth_raw", (island, superiorPlayer) ->
                            island.getWorth().toString())
                    // Renamed Island Placeholders
                    .put("hoppers_limit", (island, superiorPlayer) ->
                            island.getBlockLimit(ConstantKeys.HOPPER) + "")
                    .put("x", (island, superiorPlayer) ->
                            island.getCenterPosition().getX() + "")
                    .put("y", (island, superiorPlayer) ->
                            island.getCenterPosition().getY() + "")
                    .put("z", (island, superiorPlayer) ->
                            island.getCenterPosition().getZ() + "")
                    // Global Placeholders
                    .put("total_count", (island, superiorPlayer) ->
                            Formatters.NUMBER_FORMATTER.format(plugin.getGrid().getIslands().size()))
                    .put("total_count_format", (island, superiorPlayer) ->
                            Formatters.FANCY_NUMBER_FORMATTER.format(plugin.getGrid().getIslands().size(), superiorPlayer.getUserLocale()))
                    .put("total_count_raw", (island, superiorPlayer) ->
                            plugin.getGrid().getIslands().size() + "")
                    .put("total_level", (island, superiorPlayer) ->
                            Formatters.NUMBER_FORMATTER.format(plugin.getGrid().getTotalLevel()))
                    .put("total_level_format", (island, superiorPlayer) ->
                            Formatters.FANCY_NUMBER_FORMATTER.format(plugin.getGrid().getTotalLevel(), superiorPlayer.getUserLocale()))
                    .put("total_level_int", (island, superiorPlayer) ->
                            plugin.getGrid().getTotalLevel().toBigInteger().toString())
                    .put("total_level_raw", (island, superiorPlayer) ->
                            plugin.getGrid().getTotalLevel().toString())
                    .put("total_worth", (island, superiorPlayer) ->
                            Formatters.NUMBER_FORMATTER.format(plugin.getGrid().getTotalWorth()))
                    .put("total_worth_format", (island, superiorPlayer) ->
                            Formatters.FANCY_NUMBER_FORMATTER.format(plugin.getGrid().getTotalWorth(), superiorPlayer.getUserLocale()))
                    .put("total_worth_int", (island, superiorPlayer) ->
                            plugin.getGrid().getTotalWorth().toBigInteger().toString())
                    .put("total_worth_raw", (island, superiorPlayer) ->
                            plugin.getGrid().getTotalWorth().toString())
                    .build();

    private static final Map<SortingType, BiFunction<Island, SuperiorPlayer, String>> TOP_VALUE_FORMAT_FUNCTIONS =
            new ImmutableMap.Builder<SortingType, BiFunction<Island, SuperiorPlayer, String>>()
                    .put(SortingTypes.BY_WORTH, (targetIsland, superiorPlayer) ->
                            Formatters.FANCY_NUMBER_FORMATTER.format(targetIsland.getWorth(), superiorPlayer.getUserLocale()))
                    .put(SortingTypes.BY_LEVEL, (targetIsland, superiorPlayer) ->
                            Formatters.FANCY_NUMBER_FORMATTER.format(targetIsland.getIslandLevel(), superiorPlayer.getUserLocale()))
                    .put(SortingTypes.BY_RATING, (targetIsland, superiorPlayer) ->
                            Formatters.NUMBER_FORMATTER.format(targetIsland.getTotalRating()))
                    .put(SortingTypes.BY_PLAYERS, (targetIsland, superiorPlayer) ->
                            Formatters.NUMBER_FORMATTER.format(targetIsland.getAllPlayersInside().size()))
                    .build();

    private static final Map<SortingType, Function<Island, String>> TOP_VALUE_RAW_FUNCTIONS =
            new ImmutableMap.Builder<SortingType, Function<Island, String>>()
                    .put(SortingTypes.BY_WORTH, targetIsland -> targetIsland.getWorth().toString())
                    .put(SortingTypes.BY_LEVEL, targetIsland -> targetIsland.getIslandLevel().toString())
                    .put(SortingTypes.BY_RATING, targetIsland -> targetIsland.getTotalRating() + "")
                    .put(SortingTypes.BY_PLAYERS, targetIsland -> targetIsland.getAllPlayersInside().size() + "")
                    .build();

    private static final Map<SortingType, Function<Island, String>> TOP_VALUE_FUNCTIONS =
            new ImmutableMap.Builder<SortingType, Function<Island, String>>()
                    .put(SortingTypes.BY_WORTH, targetIsland -> Formatters.NUMBER_FORMATTER.format(targetIsland.getWorth()))
                    .put(SortingTypes.BY_LEVEL, targetIsland -> Formatters.NUMBER_FORMATTER.format(targetIsland.getIslandLevel()))
                    .put(SortingTypes.BY_RATING, targetIsland -> Formatters.NUMBER_FORMATTER.format(targetIsland.getTotalRating()))
                    .put(SortingTypes.BY_PLAYERS, targetIsland -> Formatters.NUMBER_FORMATTER.format(targetIsland.getAllPlayersInside().size()))
                    .build();

    private final Map<String, IslandPlaceholderParser> CUSTOM_ISLAND_PARSERS = new HashMap<>();
    private final Map<String, PlayerPlaceholderParser> CUSTOM_PLAYER_PARSERS = new HashMap<>();

    private final List<PlaceholdersProvider> placeholdersProviders = new LinkedList<>();

    public PlaceholdersServiceImpl() {
    }

    @Override
    public Class<?> getAPIClass() {
        return PlaceholdersService.class;
    }

    public void register(List<PlaceholdersProvider> placeholdersProviders) {
        this.placeholdersProviders.addAll(placeholdersProviders);
    }

    public String parsePlaceholders(@Nullable OfflinePlayer offlinePlayer, String str) {
        for (PlaceholdersProvider placeholdersProvider : placeholdersProviders)
            str = placeholdersProvider.parsePlaceholders(offlinePlayer, str);

        return str;
    }

    public String handlePluginPlaceholder(@Nullable OfflinePlayer offlinePlayer, String placeholder) {
        SuperiorPlayer superiorPlayer = offlinePlayer == null ? null :
                plugin.getPlayers().getSuperiorPlayer(offlinePlayer.getUniqueId());

        Optional<String> placeholderResult = Optional.empty();

        Matcher matcher;

        if (superiorPlayer != null) {
            PlayerPlaceholderParser customPlayerParser = CUSTOM_PLAYER_PARSERS.get(placeholder);
            if (customPlayerParser != null) {
                placeholderResult = Optional.ofNullable(customPlayerParser.apply(superiorPlayer));
            } else {
                boolean isLocationPlaceholder = placeholder.startsWith("location_");
                IslandPlaceholderParser customIslandParser = CUSTOM_ISLAND_PARSERS.get(
                        isLocationPlaceholder ? placeholder.substring(9) : placeholder);
                if (customIslandParser != null) {
                    Island island;
                    if (isLocationPlaceholder) {
                        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
                            island = plugin.getGrid().getIslandAt(superiorPlayer.getLocation(wrapper.getHandle()));
                        }
                    } else {
                        island = superiorPlayer.getIsland();
                    }
                    placeholderResult = Optional.ofNullable(customIslandParser.apply(island, superiorPlayer));
                }
            }
        }

        if (!placeholderResult.isPresent()) {
            if ((matcher = PLAYER_PLACEHOLDER_PATTERN.matcher(placeholder)).matches()) {
                String subPlaceholder = matcher.group(1).toLowerCase(Locale.ENGLISH);
                placeholderResult = parsePlaceholdersForPlayer(superiorPlayer, subPlaceholder);
            } else if ((matcher = ISLAND_PLACEHOLDER_PATTERN.matcher(placeholder)).matches()) {
                String subPlaceholder = matcher.group(1).toLowerCase(Locale.ENGLISH);
                Island island;
                boolean isLocationPlaceholder = false;
                if (superiorPlayer == null) {
                    island = null;
                } else if (subPlaceholder.startsWith("location_")) {
                    isLocationPlaceholder = true;
                    try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
                        island = plugin.getGrid().getIslandAt(superiorPlayer.getLocation(wrapper.getHandle()));
                    }
                } else {
                    island = superiorPlayer.getIsland();
                }
                placeholderResult = parsePlaceholdersForIsland(island, superiorPlayer,
                        isLocationPlaceholder ? placeholder.substring(9) : placeholder,
                        isLocationPlaceholder ? subPlaceholder.substring(9) : subPlaceholder);
            }
        }

        if (placeholderResult.isPresent())
            return placeholderResult.get();

        String defaultPlaceholderValue = plugin.getSettings().getDefaultPlaceholders().get(placeholder);
        if (defaultPlaceholderValue != null)
            return defaultPlaceholderValue;

        // We try to look for prefixes of placeholders
        for (Map.Entry<String, String> entry : plugin.getSettings().getDefaultPlaceholders().entrySet()) {
            if (placeholder.startsWith(entry.getKey())) {
                return entry.getValue();
            }
        }

        return "";
    }

    @Override
    public void registerPlaceholder(String placeholderName, PlayerPlaceholderParser placeholderFunction) {
        CUSTOM_PLAYER_PARSERS.put(placeholderName, placeholderFunction);
    }

    @Override
    public void registerPlaceholder(String placeholderName, IslandPlaceholderParser placeholderFunction) {
        CUSTOM_ISLAND_PARSERS.put(placeholderName, placeholderFunction);
    }

    private static Optional<String> parsePlaceholdersForPlayer(@Nullable SuperiorPlayer superiorPlayer,
                                                               String subPlaceholder) {
        Matcher matcher;

        if (superiorPlayer != null) {
            if ((matcher = MISSIONS_COMPLETED_PATTERN.matcher(subPlaceholder)).matches()) {
                String categoryName = matcher.group(1);
                return Optional.of(superiorPlayer.getCompletedMissions().stream().filter(mission ->
                        mission.getMissionCategory().getName().equalsIgnoreCase(categoryName)).count() + "");
            }
        }

        return Optional.ofNullable(PLAYER_PARSES.get(subPlaceholder))
                .map(placeholderParser -> placeholderParser.apply(superiorPlayer));
    }

    private static Optional<String> parsePlaceholdersForIsland(@Nullable Island island,
                                                               @Nullable SuperiorPlayer superiorPlayer,
                                                               String placeholder, String subPlaceholder) {
        Matcher matcher;

        if (island != null) {
            if ((matcher = GENERATOR_AMOUNT_PLACEHOLDER_PATTERN.matcher(placeholder)).matches()) {
                return handleGeneratorAmountsPlaceholder(island, matcher.group(1));
            }

            if ((matcher = GENERATOR_PERCENTAGE_PLACEHOLDER_PATTERN.matcher(placeholder)).matches()) {
                return handleGeneratorPercentagesPlaceholder(island, matcher.group(1));
            }

            if ((matcher = MISSIONS_COMPLETED_PATTERN.matcher(subPlaceholder)).matches()) {
                String categoryName = matcher.group(1);
                return Optional.of(island.getCompletedMissions().stream().filter(mission ->
                        mission.getMissionCategory().getName().equalsIgnoreCase(categoryName)).count() + "");
            }

            if ((matcher = MISSION_STATUS_PATTERN.matcher(subPlaceholder)).matches()) {
                String missionName = matcher.group(1);
                Mission<?> mission = plugin.getMissions().getMission(missionName);
                if (mission == null || (!mission.getIslandMission() && superiorPlayer == null))
                    return Optional.empty();
                boolean completedMission = mission.getIslandMission() ? island.hasCompletedMission(mission) :
                        superiorPlayer.hasCompletedMission(mission);
                return Optional.of(Formatters.BOOLEAN_FORMATTER.format(completedMission, superiorPlayer.getUserLocale()));
            }

            if ((matcher = PERMISSION_ROLE_PLACEHOLDER_PATTERN.matcher(placeholder)).matches()) {
                return handlePermissionRolesPlaceholder(island, matcher.group(1));
            }

            if (superiorPlayer != null) {
                if ((matcher = BLOCK_COUNT_PLACEHOLDER_PATTERN.matcher(placeholder)).matches()) {
                    String keyName = matcher.group(1);
                    return Optional.of(island.getBlockCountAsBigInteger(Keys.ofMaterialAndData(keyName)) + "");
                } else if ((matcher = BLOCK_LEVEL_PLACEHOLDER_PATTERN.matcher(placeholder)).matches()) {
                    String keyName = matcher.group(1);
                    BlockValue blockValue = plugin.getBlockValues().getBlockValue(Keys.ofMaterialAndData(keyName));
                    return Optional.of(blockValue.getLevel() + "");
                } else if ((matcher = BLOCK_LIMIT_PLACEHOLDER_PATTERN.matcher(placeholder)).matches()) {
                    String keyName = matcher.group(1);
                    return Optional.of(island.getBlockLimit(Keys.ofMaterialAndData(keyName)) + "");
                } else if ((matcher = BLOCK_TOTAL_LEVEL_PLACEHOLDER_PATTERN.matcher(placeholder)).matches()) {
                    String keyName = matcher.group(1);
                    BlockValue blockValue = plugin.getBlockValues().getBlockValue(Keys.ofMaterialAndData(keyName));
                    BigDecimal amount = new BigDecimal(island.getBlockCountAsBigInteger(Keys.ofMaterialAndData(keyName)));
                    return Optional.of(blockValue.getLevel().multiply(amount) + "");
                } else if ((matcher = BLOCK_TOTAL_WORTH_PLACEHOLDER_PATTERN.matcher(placeholder)).matches()) {
                    String keyName = matcher.group(1);
                    BlockValue blockValue = plugin.getBlockValues().getBlockValue(Keys.ofMaterialAndData(keyName));
                    BigDecimal amount = new BigDecimal(island.getBlockCountAsBigInteger(Keys.ofMaterialAndData(keyName)));
                    return Optional.of(blockValue.getWorth().multiply(amount) + "");
                } else if ((matcher = BLOCK_WORTH_PLACEHOLDER_PATTERN.matcher(placeholder)).matches()) {
                    String keyName = matcher.group(1);
                    BlockValue blockValue = plugin.getBlockValues().getBlockValue(Keys.ofMaterialAndData(keyName));
                    return Optional.of(blockValue.getWorth() + "");
                } else if ((matcher = COUNT_PLACEHOLDER_PATTERN.matcher(placeholder)).matches()) {
                    String keyName = matcher.group(1);
                    return Optional.of(island.getBlockCountAsBigInteger(Keys.ofMaterialAndData(keyName)) + "");
                } else if ((matcher = DATA_PLACEHOLDER_PATTERN.matcher(placeholder)).matches()) {
                    String keyName = matcher.group(1);
                    Object data = island.getPersistentDataContainer().get(keyName);
                    if (data == null) {
                        return Optional.empty();
                    }
                    return Optional.of(data.toString());
                } else if ((matcher = EFFECT_PLACEHOLDER_PATTERN.matcher(placeholder)).matches()) {
                    String effectName = matcher.group(1);
                    PotionEffectType potionEffectType = PotionEffectType.getByName(effectName);
                    if (potionEffectType == null) {
                        return Optional.empty();
                    }
                    return Optional.of(island.getPotionEffectLevel(potionEffectType) + "");
                } else if ((matcher = ENTITY_COUNT_PLACEHOLDER_PATTERN.matcher(placeholder)).matches()) {
                    String keyName = matcher.group(1);
                    return Optional.of(island.getEntitiesTracker().getEntityCount(Keys.ofEntityType(keyName)) + "");
                } else if ((matcher = ENTITY_LIMIT_PLACEHOLDER_PATTERN.matcher(placeholder)).matches()) {
                    String keyName = matcher.group(1);
                    return Optional.of(island.getEntityLimit(Keys.ofEntityType(keyName)) + "");
                } else if ((matcher = FLAG_PLACEHOLDER_PATTERN.matcher(subPlaceholder)).matches()) {
                    return handleFlagsPlaceholder(island, superiorPlayer, matcher.group(1));
                } else if ((matcher = MEMBER_PLACEHOLDER_PATTERN.matcher(subPlaceholder)).matches()) {
                    return handleMembersPlaceholder(island, matcher.group(1));
                } else if ((matcher = PERMISSION_PLACEHOLDER_PATTERN.matcher(placeholder)).matches()) {
                    return handlePermissionsPlaceholder(island, superiorPlayer, matcher.group(1));
                } else if ((matcher = ROLE_COUNT_PLACEHOLDER_PATTERN.matcher(placeholder)).matches()) {
                    String roleName = matcher.group(1);
                    PlayerRole playerRole;
                    try {
                        playerRole = SPlayerRole.of(roleName);
                    } catch (IllegalArgumentException error) {
                        return Optional.empty();
                    }
                    return Optional.of(island.getIslandMembers(playerRole).size() + "");
                } else if ((matcher = ROLE_LIMIT_PLACEHOLDER_PATTERN.matcher(placeholder)).matches()) {
                    String roleName = matcher.group(1);
                    PlayerRole playerRole;
                    try {
                        playerRole = SPlayerRole.of(roleName);
                    } catch (IllegalArgumentException error) {
                        return Optional.empty();
                    }
                    return Optional.of(island.getRoleLimit(playerRole) + "");
                } else if ((matcher = UPGRADE_PLACEHOLDER_PATTERN.matcher(placeholder)).matches()) {
                    String upgradeName = matcher.group(1);
                    Upgrade upgrade = plugin.getUpgrades().getUpgrade(upgradeName);
                    if (upgrade == null) {
                        return Optional.empty();
                    }
                    return Optional.of(island.getUpgradeLevel(upgrade).getLevel() + "");
                } else if ((matcher = VISITOR_LAST_JOIN_PLACEHOLDER_PATTERN.matcher(subPlaceholder)).matches()) {
                    String visitorName = matcher.group(1);
                    return Optional.of(island.getUniqueVisitorsWithTimes().stream()
                            .filter(uniqueVisitor -> uniqueVisitor.getKey().getName().equalsIgnoreCase(visitorName))
                            .findFirst()
                            .map(Pair::getValue).map(value -> Formatters.DATE_FORMATTER.format(new Date(value)))
                            .orElse("Haven't Joined"));
                }
            }
        }

        if ((matcher = TOP_PLACEHOLDER_PATTERN.matcher(placeholder)).matches()) {
            return handleTopIslandsPlaceholder(island, superiorPlayer, matcher.group(1));
        } else {
            try {
                return Optional.ofNullable(ISLAND_PARSES.get(subPlaceholder))
                        .map(placeholderParser -> placeholderParser.apply(island, superiorPlayer));
            } catch (NullPointerException ignored) {
                // One of the island parses failed due to invalid island being sent.
            }
        }

        return Optional.empty();
    }

    private static Optional<String> handleFlagsPlaceholder(@NotNull Island island,
                                                           @NotNull SuperiorPlayer superiorPlayer,
                                                           String placeholder) {
        try {
            IslandFlag islandFlag = IslandFlag.getByName(placeholder);
            return Optional.of(Formatters.BOOLEAN_FORMATTER.format(island.hasSettingsEnabled(islandFlag),
                    superiorPlayer.getUserLocale()));
        } catch (NullPointerException ex) {
            return Optional.empty();
        }
    }

    private static Optional<String> handleGeneratorAmountsPlaceholder(@Nullable Island island, String placeholder) {
        Matcher matcher;
        Dimension dimension = null;

        if ((matcher = GENERATOR_NORMAL_PLACEHOLDER_PATTERN.matcher(placeholder)).matches()) {
            dimension = Dimensions.NORMAL;
        } else if ((matcher = GENERATOR_NETHER_PLACEHOLDER_PATTERN.matcher(placeholder)).matches()) {
            dimension = Dimensions.NETHER;
        } else if ((matcher = GENERATOR_END_PLACEHOLDER_PATTERN.matcher(placeholder)).matches()) {
            dimension = Dimensions.THE_END;
        }

        if (dimension == null)
            return Optional.empty();

        String keyName = matcher.group(1);

        return Optional.of(island.getGeneratorAmount(Keys.ofMaterialAndData(keyName), dimension) + "");
    }

    private static Optional<String> handleGeneratorPercentagesPlaceholder(@Nullable Island island, String placeholder) {
        Matcher matcher;
        Dimension dimension = null;

        if ((matcher = GENERATOR_NORMAL_PLACEHOLDER_PATTERN.matcher(placeholder)).matches()) {
            dimension = Dimensions.NORMAL;
        } else if ((matcher = GENERATOR_NETHER_PLACEHOLDER_PATTERN.matcher(placeholder)).matches()) {
            dimension = Dimensions.NETHER;
        } else if ((matcher = GENERATOR_END_PLACEHOLDER_PATTERN.matcher(placeholder)).matches()) {
            dimension = Dimensions.THE_END;
        }

        if (dimension == null)
            return Optional.empty();

        String keyName = matcher.group(1);

        return Optional.of(IslandUtils.getGeneratorPercentageDecimal(island, Keys.ofMaterialAndData(keyName), dimension) + "");
    }

    private static Optional<String> handleMembersPlaceholder(@NotNull Island island, String placeholder) {
        List<SuperiorPlayer> members = island.getIslandMembers(false);

        int targetMemberIndex = -1;

        try {
            targetMemberIndex = Integer.parseInt(placeholder) - 1;
        } catch (NumberFormatException ignored) {
        }

        if (targetMemberIndex < 0 || targetMemberIndex >= members.size())
            return Optional.empty();

        return Optional.of(members.get(targetMemberIndex).getName());
    }

    private static Optional<String> handlePermissionsPlaceholder(@NotNull Island island,
                                                                 @NotNull SuperiorPlayer superiorPlayer,
                                                                 String placeholder) {
        try {
            IslandPrivilege islandPrivilege = IslandPrivilege.getByName(placeholder);
            return Optional.of(Formatters.BOOLEAN_FORMATTER.format(island.hasPermission(superiorPlayer, islandPrivilege),
                    superiorPlayer.getUserLocale()));
        } catch (NullPointerException ex) {
            return Optional.empty();
        }
    }

    private static Optional<String> handlePermissionRolesPlaceholder(@NotNull Island island, String placeholder) {
        try {
            IslandPrivilege islandPrivilege = IslandPrivilege.getByName(placeholder);
            return Optional.of(island.getRequiredPlayerRole(islandPrivilege).getDisplayName());
        } catch (NullPointerException ex) {
            return Optional.empty();
        }
    }

    private static Optional<String> handleTopIslandsPlaceholder(@Nullable Island island,
                                                                @Nullable SuperiorPlayer superiorPlayer,
                                                                String subPlaceholder) {
        Matcher matcher;
        SortingType sortingType;

        if ((matcher = TOP_WORTH_PLACEHOLDER_PATTERN.matcher(subPlaceholder)).matches()) {
            sortingType = SortingTypes.BY_WORTH;
        } else if ((matcher = TOP_LEVEL_PLACEHOLDER_PATTERN.matcher(subPlaceholder)).matches()) {
            sortingType = SortingTypes.BY_LEVEL;
        } else if ((matcher = TOP_RATING_PLACEHOLDER_PATTERN.matcher(subPlaceholder)).matches()) {
            sortingType = SortingTypes.BY_RATING;
        } else if ((matcher = TOP_PLAYERS_PLACEHOLDER_PATTERN.matcher(subPlaceholder)).matches()) {
            sortingType = SortingTypes.BY_PLAYERS;
        } else {
            String sortingTypeName = subPlaceholder.split("_")[0];
            sortingType = SortingType.getByName(sortingTypeName);
        }

        if (sortingType == null)
            return Optional.empty();

        String placeholderValue = matcher.group(1);

        if (placeholderValue.equals("position"))
            return island == null ? Optional.empty() : Optional.of((plugin.getGrid().getIslandPosition(island, sortingType) + 1) + "");

        Function<Island, String> getValueFunction;

        if ((matcher = TOP_VALUE_FORMAT_PLACEHOLDER_PATTERN.matcher(placeholderValue)).matches()) {
            getValueFunction = Optional.ofNullable(TOP_VALUE_FORMAT_FUNCTIONS.get(sortingType)).map(function ->
                    (Function<Island, String>) targetIsland -> function.apply(targetIsland, superiorPlayer)).orElse(null);
        } else if ((matcher = TOP_VALUE_RAW_PLACEHOLDER_PATTERN.matcher(placeholderValue)).matches()) {
            getValueFunction = TOP_VALUE_RAW_FUNCTIONS.get(sortingType);
        } else if ((matcher = TOP_VALUE_PLACEHOLDER_PATTERN.matcher(placeholderValue)).matches()) {
            getValueFunction = TOP_VALUE_FUNCTIONS.get(sortingType);
        } else if ((matcher = TOP_LEADER_PLACEHOLDER_PATTERN.matcher(placeholderValue)).matches()) {
            getValueFunction = targetIsland -> targetIsland.getOwner().getName();
        } else if ((matcher = TOP_CUSTOM_PLACEHOLDER_PATTERN.matcher(placeholderValue)).matches()) {
            String customPlaceholder = matcher.group(2);
            getValueFunction = targetIsland -> parsePlaceholdersForIsland(targetIsland, superiorPlayer,
                    "superior_island_" + customPlaceholder,
                    customPlaceholder).orElse(null);
        } else {
            getValueFunction = targetIsland -> targetIsland.getName().isEmpty() ?
                    targetIsland.getOwner().getName() : targetIsland.getName();
        }

        if (getValueFunction == null)
            return Optional.empty();

        int targetPosition;

        try {
            targetPosition = Integer.parseInt(matcher.matches() ? matcher.group(1) : placeholderValue);
        } catch (NumberFormatException error) {
            return Optional.empty();
        }

        Island targetIsland = plugin.getGrid().getIsland(targetPosition - 1, sortingType);

        return Optional.ofNullable(targetIsland).map(getValueFunction);
    }

    private static WorldInfo getDefaultWorldInfo(Island island) {
        return plugin.getGrid().getIslandsWorldInfo(island, plugin.getSettings().getWorlds().getDefaultWorldDimension());
    }

}
