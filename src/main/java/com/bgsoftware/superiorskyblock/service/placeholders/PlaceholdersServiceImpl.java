package com.bgsoftware.superiorskyblock.service.placeholders;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandFlag;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.island.SortingType;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.api.missions.MissionCategory;
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
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.external.placeholders.PlaceholdersProvider;
import com.bgsoftware.superiorskyblock.island.IslandUtils;
import com.bgsoftware.superiorskyblock.island.flag.IslandFlags;
import com.bgsoftware.superiorskyblock.island.privilege.IslandPrivileges;
import com.bgsoftware.superiorskyblock.island.role.SPlayerRole;
import com.bgsoftware.superiorskyblock.player.chat.ChatStates;
import com.bgsoftware.superiorskyblock.service.IService;
import com.bgsoftware.superiorskyblock.world.Dimensions;
import com.google.common.base.Preconditions;
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
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("deprecation")
public class PlaceholdersServiceImpl implements PlaceholdersService, IService {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private static final Pattern ISLAND_PLACEHOLDER_PATTERN = Pattern.compile("island_(.+)");
    private static final Pattern PLAYER_PLACEHOLDER_PATTERN = Pattern.compile("player_(.+)");

    private static final Pattern BAN_INDEX_PLACEHOLDER_PATTERN = Pattern.compile("ban_(\\d+)");
    private static final Pattern BIOME_PLACEHOLDER_PATTERN = Pattern.compile("biome_(.+)");
    private static final Pattern BLOCK_COUNT_PLACEHOLDER_PATTERN = Pattern.compile("block_count_(.+)");
    private static final Pattern BLOCK_LEVEL_PLACEHOLDER_PATTERN = Pattern.compile("block_level_(.+)");
    private static final Pattern BLOCK_LIMIT_PLACEHOLDER_PATTERN = Pattern.compile("block_limit_(.+)");
    private static final Pattern BLOCK_TOTAL_LEVEL_PLACEHOLDER_PATTERN = Pattern.compile("block_total_level_(.+)");
    private static final Pattern BLOCK_TOTAL_WORTH_PLACEHOLDER_PATTERN = Pattern.compile("block_total_worth_(.+)");
    private static final Pattern BLOCK_WORTH_PLACEHOLDER_PATTERN = Pattern.compile("block_worth_(.+)");
    private static final Pattern COOP_INDEX_PLACEHOLDER_PATTERN = Pattern.compile("coop_(\\d+)");
    private static final Pattern COUNT_PLACEHOLDER_PATTERN = Pattern.compile("count_(.+)");
    private static final Pattern DATA_PLACEHOLDER_PATTERN = Pattern.compile("data_(.+)");
    private static final Pattern EFFECT_PLACEHOLDER_PATTERN = Pattern.compile("effect_(.+)");
    private static final Pattern ENTITY_COUNT_PLACEHOLDER_PATTERN = Pattern.compile("entity_count_(.+)");
    private static final Pattern ENTITY_LIMIT_PLACEHOLDER_PATTERN = Pattern.compile("entity_limit_(.+)");
    private static final Pattern FLAG_PLACEHOLDER_PATTERN = Pattern.compile("flag_(.+)");
    private static final Pattern GENERATOR_AMOUNT_PLACEHOLDER_PATTERN = Pattern.compile("generator_amount_(.+)");
    private static final Pattern GENERATOR_PERCENTAGE_PLACEHOLDER_PATTERN = Pattern.compile("generator_percentage_(.+)");
    private static final Pattern MEMBER_INDEX_PLACEHOLDER_PATTERN = Pattern.compile("member_(\\d+)");
    private static final Pattern MISSIONS_COMPLETED_PATTERN = Pattern.compile("missions_completed_(.+)");
    private static final Pattern MISSION_STATUS_PATTERN = Pattern.compile("mission_status_(.+)");
    private static final Pattern PERMISSION_PLACEHOLDER_PATTERN = Pattern.compile("permission_(.+)");
    private static final Pattern PERMISSION_ROLE_PLACEHOLDER_PATTERN = Pattern.compile("permission_role_(.+)");
    private static final Pattern PLAYER_INDEX_PLACEHOLDER_PATTERN = Pattern.compile("player_(\\d+)");
    private static final Pattern ROLE_COUNT_PLACEHOLDER_PATTERN = Pattern.compile("role_count_(.+)");
    private static final Pattern ROLE_LIMIT_PLACEHOLDER_PATTERN = Pattern.compile("role_limit_(.+)");
    private static final Pattern TOP_PLACEHOLDER_PATTERN = Pattern.compile("top_(.+)");
    private static final Pattern TOP_VALUE_FORMAT_PLACEHOLDER_PATTERN = Pattern.compile("value_format_(.+)");
    private static final Pattern TOP_VALUE_RAW_PLACEHOLDER_PATTERN = Pattern.compile("value_raw_(.+)");
    private static final Pattern TOP_VALUE_PLACEHOLDER_PATTERN = Pattern.compile("value_(.+)");
    private static final Pattern TOP_LEADER_PLACEHOLDER_PATTERN = Pattern.compile("leader_(.+)");
    private static final Pattern TOP_CUSTOM_PLACEHOLDER_PATTERN = Pattern.compile("(\\d+)_(.+)");
    private static final Pattern UNIQUE_VISITOR_INDEX_PLACEHOLDER_PATTERN = Pattern.compile("unique_visitor_(\\d+)");
    private static final Pattern UPGRADE_PLACEHOLDER_PATTERN = Pattern.compile("upgrade_(.+)");
    private static final Pattern VISITOR_INDEX_PLACEHOLDER_PATTERN = Pattern.compile("visitor_(\\d+)");
    private static final Pattern VISITOR_LAST_JOIN_PLACEHOLDER_PATTERN = Pattern.compile("visitor_last_join_(.+)");
    private static final Pattern WORLD_ENABLED_PLACEHOLDER_PATTERN = Pattern.compile("world_enabled_(.+)");
    private static final Pattern WORLD_GENERATED_PLACEHOLDER_PATTERN = Pattern.compile("world_generated_(.+)");
    private static final Pattern WORLD_UNLOCKED_PLACEHOLDER_PATTERN = Pattern.compile("world_unlocked_(.+)");

    private static final String ISLAND_PREFIX = "island_";
    private static final String LOCATION_PREFIX = "location_";

    private static final Map<String, PlayerPlaceholderParser> PLAYER_PLACEHOLDER_PARSERS =
            new ImmutableMap.Builder<String, PlayerPlaceholderParser>()
                    .put("blocks_stacker", superiorPlayer ->
                            Formatters.BOOLEAN_FORMATTER.format(superiorPlayer.hasBlocksStackerEnabled(), superiorPlayer.getUserLocale()))
                    .put("border_color", superiorPlayer ->
                            Formatters.BORDER_COLOR_FORMATTER.format(superiorPlayer.getBorderColor(), superiorPlayer.getUserLocale()))
                    .put("bypass", superiorPlayer ->
                            Formatters.BOOLEAN_FORMATTER.format(superiorPlayer.hasBypassModeEnabled(), superiorPlayer.getUserLocale()))
                    .put("chat_spy", superiorPlayer ->
                            Formatters.BOOLEAN_FORMATTER.format(superiorPlayer.hasAdminSpyEnabled(), superiorPlayer.getUserLocale()))
                    .put("chat_state", superiorPlayer ->
                            Formatters.CAPITALIZED_FORMATTER.format(superiorPlayer.getChatState().getName()))
                    .put("disbands", superiorPlayer ->
                            String.valueOf(superiorPlayer.getDisbands()))
                    .put("fly", superiorPlayer ->
                            Formatters.BOOLEAN_FORMATTER.format(superiorPlayer.hasIslandFlyEnabled(), superiorPlayer.getUserLocale()))
                    .put("local_chat", superiorPlayer ->
                            Formatters.BOOLEAN_FORMATTER.format(superiorPlayer.getChatState() == ChatStates.LOCAL_CHAT, superiorPlayer.getUserLocale()))
                    .put("locale", superiorPlayer ->
                            Formatters.LOCALE_FORMATTER.format(superiorPlayer.getUserLocale()))
                    .put("missions_completed", superiorPlayer ->
                            String.valueOf(superiorPlayer.getCompletedMissions().size()))
                    .put("panel", superiorPlayer ->
                            Formatters.BOOLEAN_FORMATTER.format(superiorPlayer.hasToggledPanel(), superiorPlayer.getUserLocale()))
                    .put("role", superiorPlayer ->
                            superiorPlayer.getPlayerRole().toString())
                    .put("role_display", superiorPlayer ->
                            superiorPlayer.getPlayerRole().getDisplayName())
                    .put("schematics", superiorPlayer ->
                            Formatters.BOOLEAN_FORMATTER.format(superiorPlayer.hasSchematicModeEnabled(), superiorPlayer.getUserLocale()))
                    .put("team_chat", superiorPlayer ->
                            Formatters.BOOLEAN_FORMATTER.format(superiorPlayer.getChatState() == ChatStates.TEAM_CHAT, superiorPlayer.getUserLocale()))
                    .put("texture", SuperiorPlayer::getTextureValue)
                    .put("world_border", superiorPlayer ->
                            Formatters.BOOLEAN_FORMATTER.format(superiorPlayer.hasWorldBorderEnabled(), superiorPlayer.getUserLocale()))
                    .build();

    private static final Map<String, IslandPlaceholderParser> ISLAND_PLACEHOLDER_PARSERS =
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
                            String.valueOf(island.getBannedPlayers().size()))
                    .put("bans_list", (island, superiorPlayer) ->
                            Formatters.COMMA_FORMATTER.format(island.getBannedPlayers().stream().map(SuperiorPlayer::getName)))
                    .put("biome", (island, superiorPlayer) ->
                            Formatters.CAPITALIZED_FORMATTER.format(island.getBiome(getDefaultWorldDimension()).name()))
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
                            String.valueOf(island.getCenterPosition().getX()))
                    .put("center_y", (island, superiorPlayer) ->
                            String.valueOf(island.getCenterPosition().getY()))
                    .put("center_z", (island, superiorPlayer) ->
                            String.valueOf(island.getCenterPosition().getZ()))
                    .put("chest_size", (island, superiorPlayer) ->
                            String.valueOf(island.getChestSize()))
                    .put("coop_limit", (island, superiorPlayer) ->
                            String.valueOf(island.getCoopLimit()))
                    .put("coop_list", (island, superiorPlayer) ->
                            Formatters.COMMA_FORMATTER.format(island.getCoopPlayers().stream().map(SuperiorPlayer::getName)))
                    .put("coop_size", (island, superiorPlayer) ->
                            String.valueOf(island.getCoopPlayers().size()))
                    .put("creation_time", (island, superiorPlayer) ->
                            island.getCreationTimeDate())
                    .put("crops_multiplier", (island, superiorPlayer) ->
                            String.valueOf(island.getCropGrowthMultiplier()))
                    .put("description", (island, superiorPlayer) ->
                            island.getDescription())
                    .put("discord", (island, superiorPlayer) ->
                            island.hasPermission(superiorPlayer, IslandPrivileges.DISCORD_SHOW) ? island.getDiscord() : IslandUtils.DEFAULT_NONE_VALUE)
                    .put("discord_all", (island, superiorPlayer) ->
                            island.getDiscord())
                    .put("drops_multiplier", (island, superiorPlayer) ->
                            String.valueOf(island.getMobDropsMultiplier()))
                    .put("exists", (island, superiorPlayer) ->
                            Formatters.BOOLEAN_FORMATTER.format(island != null, superiorPlayer.getUserLocale()))
                    .put("home", (island, superiorPlayer) -> {
                        WorldInfo worldInfo = getDefaultWorldInfo(island);
                        return Formatters.LOCATION_FORMATTER.format(island.getIslandHomePosition(worldInfo.getDimension()).toLocation(worldInfo));
                    })
                    .put("home_x", (island, superiorPlayer) ->
                            String.valueOf(island.getIslandHomePosition(getDefaultWorldDimension()).getX()))
                    .put("home_y", (island, superiorPlayer) ->
                            String.valueOf(island.getIslandHomePosition(getDefaultWorldDimension()).getY()))
                    .put("home_z", (island, superiorPlayer) ->
                            String.valueOf(island.getIslandHomePosition(getDefaultWorldDimension()).getZ()))
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
                    .put("missions_completed", (island, superiorPlayer) ->
                            String.valueOf(island.getCompletedMissions().size()))
                    .put("name", (island, superiorPlayer) ->
                            island.getName())
                    .put("name_formatted", (island, superiorPlayer) ->
                            island.getFormattedName())
                    .put("name_leader", (island, superiorPlayer) ->
                            island.getName().isEmpty() ? island.getOwner().getName() : island.getName())
                    .put("name_stripped", (island, superiorPlayer) ->
                            island.getStrippedName())
                    .put("paypal", (island, superiorPlayer) ->
                            island.hasPermission(superiorPlayer, IslandPrivileges.PAYPAL_SHOW) ? island.getPaypal() : IslandUtils.DEFAULT_NONE_VALUE)
                    .put("paypal_all", (island, superiorPlayer) ->
                            island.getPaypal())
                    .put("players_count", (island, superiorPlayer) ->
                            String.valueOf(island.getAllPlayersInside().size()))
                    .put("players_list", (island, superiorPlayer) ->
                            Formatters.COMMA_FORMATTER.format(island.getAllPlayersInside().stream().map(SuperiorPlayer::getName)))
                    .put("radius", (island, superiorPlayer) ->
                            String.valueOf(island.getIslandSize()))
                    .put("rating", (island, superiorPlayer) ->
                            String.valueOf(island.getTotalRating()))
                    .put("rating_amount", (island, superiorPlayer) ->
                            String.valueOf(island.getRatingAmount()))
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
                            String.valueOf(island.getCoopLimitRaw()))
                    .put("raw_crops_multiplier", (island, superiorPlayer) ->
                            String.valueOf(island.getCropGrowthRaw()))
                    .put("raw_drops_multiplier", (island, superiorPlayer) ->
                            String.valueOf(island.getMobDropsRaw()))
                    .put("raw_level", (island, superiorPlayer) ->
                            Formatters.NUMBER_FORMATTER.format(island.getRawLevel()))
                    .put("raw_level_format", (island, superiorPlayer) ->
                            Formatters.FANCY_NUMBER_FORMATTER.format(island.getRawLevel(), superiorPlayer.getUserLocale()))
                    .put("raw_level_int", (island, superiorPlayer) ->
                            island.getRawLevel().toBigInteger().toString())
                    .put("raw_level_raw", (island, superiorPlayer) ->
                            island.getRawLevel().toString())
                    .put("raw_radius", (island, superiorPlayer) ->
                            String.valueOf(island.getIslandSizeRaw()))
                    .put("raw_spawners_multiplier", (island, superiorPlayer) ->
                            String.valueOf(island.getSpawnerRatesRaw()))
                    .put("raw_team_limit", (island, superiorPlayer) ->
                            String.valueOf(island.getTeamLimitRaw()))
                    .put("raw_warps_limit", (island, superiorPlayer) ->
                            String.valueOf(island.getWarpsLimitRaw()))
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

                        if (Math.abs(size - rounded) == 1) {
                            size = rounded;
                        }

                        return size + " x " + size;
                    })
                    .put("spawners_multiplier", (island, superiorPlayer) ->
                            String.valueOf(island.getSpawnerRatesMultiplier()))
                    .put("team_limit", (island, superiorPlayer) ->
                            String.valueOf(island.getTeamLimit()))
                    .put("team_list", (island, superiorPlayer) ->
                            Formatters.COMMA_FORMATTER.format(island.getIslandMembers(true).stream().map(SuperiorPlayer::getName)))
                    .put("team_size", (island, superiorPlayer) ->
                            String.valueOf(island.getIslandMembers(true).size()))
                    .put("team_size_online", (island, superiorPlayer) ->
                            String.valueOf(island.getIslandMembers(true).stream().filter(SuperiorPlayer::isShownAsOnline).count()))
                    .put("unique_visitors_count", (island, superiorPlayer) ->
                            String.valueOf(island.getUniqueVisitors().size()))
                    .put("unique_visitors_list", (island, superiorPlayer) ->
                            Formatters.COMMA_FORMATTER.format(island.getUniqueVisitors().stream().map(SuperiorPlayer::getName)))
                    .put("uuid", (island, superiorPlayer) ->
                            String.valueOf(island.getUniqueId()))
                    .put("visitors_count", (island, superiorPlayer) ->
                            String.valueOf(island.getIslandVisitors(false).size()))
                    .put("visitors_list", (island, superiorPlayer) ->
                            Formatters.COMMA_FORMATTER.format(island.getIslandVisitors(false).stream().map(SuperiorPlayer::getName)))
                    .put("visitors_location", (island, superiorPlayer) -> {
                        WorldInfo worldInfo = getDefaultWorldInfo(island);
                        return Formatters.LOCATION_FORMATTER.format(island.getVisitorsPosition(null /*unused*/).toLocation(worldInfo));
                    })
                    .put("visitors_location_x", (island, superiorPlayer) ->
                            String.valueOf(island.getVisitorsPosition(getDefaultWorldDimension()).getX()))
                    .put("visitors_location_y", (island, superiorPlayer) ->
                            String.valueOf(island.getVisitorsPosition(getDefaultWorldDimension()).getY()))
                    .put("visitors_location_z", (island, superiorPlayer) ->
                            String.valueOf(island.getVisitorsPosition(getDefaultWorldDimension()).getZ()))
                    .put("warps", (island, superiorPlayer) ->
                            String.valueOf(island.getIslandWarps().size()))
                    .put("warps_limit", (island, superiorPlayer) ->
                            String.valueOf(island.getWarpsLimit()))
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
                    // Deprecated Island Placeholders
                    .put("end_unlocked", legacyPlaceholder("superior_island_end_unlocked", "superior_island_world_unlocked_the_end", (island, superiorPlayer) ->
                            Formatters.BOOLEAN_FORMATTER.format(island.isEndEnabled(), superiorPlayer.getUserLocale())))
                    .put("nether_unlocked", legacyPlaceholder("superior_island_nether_unlocked", "superior_island_world_unlocked_nether", (island, superiorPlayer) ->
                            Formatters.BOOLEAN_FORMATTER.format(island.isNetherEnabled(), superiorPlayer.getUserLocale())))
                    .put("normal_unlocked", legacyPlaceholder("superior_island_normal_unlocked", "superior_island_world_unlocked_normal", (island, superiorPlayer) ->
                            Formatters.BOOLEAN_FORMATTER.format(island.isNormalEnabled(), superiorPlayer.getUserLocale())))
                    .put("hoppers_limit", legacyPlaceholder("superior_island_hoppers_limit", "superior_island_block_limit_hopper", (island, superiorPlayer) ->
                            String.valueOf(island.getBlockLimit(ConstantKeys.HOPPER))))
                    .put("x", legacyPlaceholder("superior_island_x", "superior_island_center_x", (island, superiorPlayer) ->
                            String.valueOf(island.getCenterPosition().getX())))
                    .put("y", legacyPlaceholder("superior_island_y", "superior_island_center_y", (island, superiorPlayer) ->
                            String.valueOf(island.getCenterPosition().getY())))
                    .put("z", legacyPlaceholder("superior_island_z", "superior_island_center_z", (island, superiorPlayer) ->
                            String.valueOf(island.getCenterPosition().getZ())))
                    // Global Placeholders
                    .put("total_count", (island, superiorPlayer) ->
                            Formatters.NUMBER_FORMATTER.format(plugin.getGrid().getIslands().size()))
                    .put("total_count_format", (island, superiorPlayer) ->
                            Formatters.FANCY_NUMBER_FORMATTER.format(plugin.getGrid().getIslands().size(), superiorPlayer.getUserLocale()))
                    .put("total_count_raw", (island, superiorPlayer) ->
                            String.valueOf(plugin.getGrid().getIslands().size()))
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

    private final Map<String, IslandPlaceholderParser> customIslandPlaceholderParsers = new HashMap<>();
    private final Map<String, PlayerPlaceholderParser> customPlayerPlaceholdersParsers = new HashMap<>();

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

    @Override
    public String parsePlaceholders(@Nullable OfflinePlayer offlinePlayer, String value) {
        for (PlaceholdersProvider placeholdersProvider : this.placeholdersProviders) {
            value = placeholdersProvider.parsePlaceholders(offlinePlayer, value);
        }

        return value;
    }

    @Override
    public void registerPlaceholder(String placeholderName, PlayerPlaceholderParser placeholderFunction) {
        Preconditions.checkNotNull(placeholderName, "placeholderName cannot be null.");
        Preconditions.checkNotNull(placeholderFunction, "placeholderFunction cannot be null.");

        this.customPlayerPlaceholdersParsers.put(placeholderName, placeholderFunction);
    }

    @Override
    public void registerPlaceholder(String placeholderName, IslandPlaceholderParser placeholderFunction) {
        Preconditions.checkNotNull(placeholderName, "placeholderName cannot be null.");
        Preconditions.checkNotNull(placeholderFunction, "placeholderFunction cannot be null.");

        this.customIslandPlaceholderParsers.put(placeholderName, placeholderFunction);
    }

    public String handlePluginPlaceholder(@Nullable OfflinePlayer offlinePlayer, String placeholder) {
        SuperiorPlayer superiorPlayer = offlinePlayer == null ? null :
                plugin.getPlayers().getSuperiorPlayer(offlinePlayer.getUniqueId());

        Optional<String> placeholderResult = Optional.empty();

        Matcher matcher;

        if (superiorPlayer != null) {
            PlayerPlaceholderParser playerPlaceholderParser = this.customPlayerPlaceholdersParsers.get(placeholder);

            if (playerPlaceholderParser != null) {
                placeholderResult = Optional.ofNullable(playerPlaceholderParser.apply(superiorPlayer));
            } else {
                String parsedPlaceholder = placeholder;
                boolean isLocationPlaceholder = parsedPlaceholder.startsWith(LOCATION_PREFIX);

                if (isLocationPlaceholder) {
                    parsedPlaceholder = parsedPlaceholder.substring(LOCATION_PREFIX.length());
                }

                IslandPlaceholderParser islandPlaceholderParser = this.customIslandPlaceholderParsers.get(parsedPlaceholder);

                if (islandPlaceholderParser != null) {
                    Island island;

                    if (isLocationPlaceholder) {
                        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
                            island = plugin.getGrid().getIslandAt(superiorPlayer.getLocation(wrapper.getHandle()));
                        }
                    } else {
                        island = superiorPlayer.getIsland();
                    }

                    placeholderResult = Optional.ofNullable(islandPlaceholderParser.apply(island, superiorPlayer));
                }
            }
        }

        if (placeholderResult.isPresent()) {
            return placeholderResult.get();
        }

        if ((matcher = PLAYER_PLACEHOLDER_PATTERN.matcher(placeholder)).matches()) {
            String subPlaceholder = matcher.group(1).toLowerCase(Locale.ENGLISH);

            placeholderResult = parsePlaceholdersForPlayer(superiorPlayer, subPlaceholder);
        } else if ((matcher = ISLAND_PLACEHOLDER_PATTERN.matcher(placeholder)).matches()) {
            String subPlaceholder = matcher.group(1).toLowerCase(Locale.ENGLISH);

            Island island;

            if (superiorPlayer == null) {
                island = null;
            } else if (subPlaceholder.startsWith(LOCATION_PREFIX)) {
                subPlaceholder = subPlaceholder.substring(LOCATION_PREFIX.length());
                placeholder = ISLAND_PREFIX  + subPlaceholder;

                try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
                    island = plugin.getGrid().getIslandAt(superiorPlayer.getLocation(wrapper.getHandle()));
                }
            } else {
                island = superiorPlayer.getIsland();
            }

            placeholderResult = parsePlaceholdersForIsland(island, superiorPlayer, subPlaceholder);
        }

        if (placeholderResult.isPresent()) {
            return placeholderResult.get();
        }

        String defaultPlaceholderValue = plugin.getSettings().getDefaultPlaceholders().get(placeholder);

        if (defaultPlaceholderValue != null) {
            return defaultPlaceholderValue;
        }

        // We try to look for prefixes of placeholders
        for (Map.Entry<String, String> entry : plugin.getSettings().getDefaultPlaceholders().entrySet()) {
            if (placeholder.startsWith(entry.getKey())) {
                return entry.getValue();
            }
        }

        return "";
    }

    private static Optional<String> parsePlaceholdersForPlayer(@Nullable SuperiorPlayer superiorPlayer,
                                                               String subPlaceholder) {
        Matcher matcher;

        if (superiorPlayer != null) {
            if ((matcher = MISSIONS_COMPLETED_PATTERN.matcher(subPlaceholder)).matches()) {
                MissionCategory missionCategory = plugin.getMissions().getMissionCategory(matcher.group(1));

                if (missionCategory == null) {
                    return Optional.empty();
                }

                return handleCompletedMissionsPlaceholder(superiorPlayer.getCompletedMissions(), missionCategory);
            }

            if ((matcher = MISSION_STATUS_PATTERN.matcher(subPlaceholder)).matches()) {
                Mission<?> mission = plugin.getMissions().getMission(matcher.group(1));

                if (mission == null || mission.getIslandMission()) {
                    return Optional.empty();
                }

                return Optional.of(Formatters.BOOLEAN_FORMATTER.format(superiorPlayer.hasCompletedMission(mission),
                        superiorPlayer.getUserLocale()));
            }

            return Optional.ofNullable(PLAYER_PLACEHOLDER_PARSERS.get(subPlaceholder))
                    .map(placeholderParser -> placeholderParser.apply(superiorPlayer));
        }

        return Optional.empty();
    }

    private static Optional<String> parsePlaceholdersForIsland(@Nullable Island island,
                                                               @Nullable SuperiorPlayer superiorPlayer,
                                                               String subPlaceholder) {
        Matcher matcher;

        if (island != null) {
            if ((matcher = BAN_INDEX_PLACEHOLDER_PATTERN.matcher(subPlaceholder)).matches()) {
                return handlePlayersIndexPlaceholder(island.getBannedPlayers(), matcher.group(1));
            }

            if ((matcher = BIOME_PLACEHOLDER_PATTERN.matcher(subPlaceholder)).matches()) {
                Dimension dimension = Dimensions.getSafe(matcher.group(1));

                if (dimension == null) {
                    return Optional.empty();
                }

                return Optional.of(Formatters.CAPITALIZED_FORMATTER.format(island.getBiome(dimension).name()));
            }

            if ((matcher = BLOCK_COUNT_PLACEHOLDER_PATTERN.matcher(subPlaceholder)).matches() ||
                    (matcher = COUNT_PLACEHOLDER_PATTERN.matcher(subPlaceholder)).matches()) {
                Key key = Keys.ofMaterialAndData(matcher.group(1));

                return Optional.of(String.valueOf(island.getBlockCountAsBigInteger(key)));
            }

            if ((matcher = BLOCK_LIMIT_PLACEHOLDER_PATTERN.matcher(subPlaceholder)).matches()) {
                Key key = Keys.ofMaterialAndData(matcher.group(1));

                return Optional.of(String.valueOf(island.getBlockLimit(key)));
            }

            if ((matcher = BLOCK_TOTAL_LEVEL_PLACEHOLDER_PATTERN.matcher(subPlaceholder)).matches()) {
                Key key = Keys.ofMaterialAndData(matcher.group(1));

                BigDecimal blockLevel = plugin.getBlockValues().getBlockLevel(key);
                BigDecimal blockCount = new BigDecimal(island.getBlockCountAsBigInteger(key));

                return Optional.of(String.valueOf(blockLevel.multiply(blockCount)));
            }

            if ((matcher = BLOCK_TOTAL_WORTH_PLACEHOLDER_PATTERN.matcher(subPlaceholder)).matches()) {
                Key key = Keys.ofMaterialAndData(matcher.group(1));

                BigDecimal blockWorth = plugin.getBlockValues().getBlockWorth(key);
                BigDecimal blockCount = new BigDecimal(island.getBlockCountAsBigInteger(key));

                return Optional.of(String.valueOf(blockWorth.multiply(blockCount)));
            }

            if ((matcher = COOP_INDEX_PLACEHOLDER_PATTERN.matcher(subPlaceholder)).matches()) {
                return handlePlayersIndexPlaceholder(island.getCoopPlayers(), matcher.group(1));
            }

            if ((matcher = DATA_PLACEHOLDER_PATTERN.matcher(subPlaceholder)).matches()) {
                Object object = island.getPersistentDataContainer().get(matcher.group(1));

                if (object == null) {
                    return Optional.empty();
                }

                return Optional.of(object.toString());
            }

            if ((matcher = EFFECT_PLACEHOLDER_PATTERN.matcher(subPlaceholder)).matches()) {
                PotionEffectType potionEffectType = PotionEffectType.getByName(matcher.group(1));

                if (potionEffectType == null) {
                    return Optional.empty();
                }

                return Optional.of(String.valueOf(island.getPotionEffectLevel(potionEffectType)));
            }

            if ((matcher = ENTITY_COUNT_PLACEHOLDER_PATTERN.matcher(subPlaceholder)).matches()) {
                Key key = Keys.ofEntityType(matcher.group(1));

                return Optional.of(String.valueOf(island.getEntitiesTracker().getEntityCount(key)));
            }

            if ((matcher = ENTITY_LIMIT_PLACEHOLDER_PATTERN.matcher(subPlaceholder)).matches()) {
                Key key = Keys.ofEntityType(matcher.group(1));

                return Optional.of(String.valueOf(island.getEntityLimit(key)));
            }

            if ((matcher = FLAG_PLACEHOLDER_PATTERN.matcher(subPlaceholder)).matches()) {
                return handleIslandFlagPlaceholder(island, superiorPlayer, matcher.group(1));
            }

            if ((matcher = GENERATOR_AMOUNT_PLACEHOLDER_PATTERN.matcher(subPlaceholder)).matches()) {
                return handleGeneratorAmountPlaceholder(island, matcher.group(1));
            }

            if ((matcher = GENERATOR_PERCENTAGE_PLACEHOLDER_PATTERN.matcher(subPlaceholder)).matches()) {
                return handleGeneratorPercentagePlaceholder(island, matcher.group(1));
            }

            if ((matcher = MEMBER_INDEX_PLACEHOLDER_PATTERN.matcher(subPlaceholder)).matches()) {
                return handlePlayersIndexPlaceholder(island.getIslandMembers(false), matcher.group(1));
            }

            if ((matcher = MISSION_STATUS_PATTERN.matcher(subPlaceholder)).matches()) {
                Mission<?> mission = plugin.getMissions().getMission(matcher.group(1));

                if (mission == null || !mission.getIslandMission()) {
                    return Optional.empty();
                }

                return Optional.of(Formatters.BOOLEAN_FORMATTER.format(island.hasCompletedMission(mission), superiorPlayer.getUserLocale()));
            }

            if ((matcher = MISSIONS_COMPLETED_PATTERN.matcher(subPlaceholder)).matches()) {
                MissionCategory missionCategory = plugin.getMissions().getMissionCategory(matcher.group(1));

                if (missionCategory == null) {
                    return Optional.empty();
                }

                return handleCompletedMissionsPlaceholder(island.getCompletedMissions(), missionCategory);
            }

            if ((matcher = PERMISSION_PLACEHOLDER_PATTERN.matcher(subPlaceholder)).matches()) {
                return handleIslandPrivilegePlaceholder(island, superiorPlayer, matcher.group(1));
            }

            if ((matcher = PERMISSION_ROLE_PLACEHOLDER_PATTERN.matcher(subPlaceholder)).matches()) {
                return handleRequiredPlayerRoleForIslandPrivilegePlaceholder(island, matcher.group(1));
            }

            if ((matcher = PLAYER_INDEX_PLACEHOLDER_PATTERN.matcher(subPlaceholder)).matches()) {
                return handlePlayersIndexPlaceholder(island.getAllPlayersInside(), matcher.group(1));
            }

            if ((matcher = ROLE_COUNT_PLACEHOLDER_PATTERN.matcher(subPlaceholder)).matches()) {
                PlayerRole playerRole = SPlayerRole.getSafe(matcher.group(1));

                if (playerRole == null) {
                    return Optional.empty();
                }

                return Optional.of(String.valueOf(island.getIslandMembers(playerRole).size()));
            }

            if ((matcher = ROLE_LIMIT_PLACEHOLDER_PATTERN.matcher(subPlaceholder)).matches()) {
                PlayerRole playerRole = SPlayerRole.getSafe(matcher.group(1));

                if (playerRole == null) {
                    return Optional.empty();
                }

                return Optional.of(String.valueOf(island.getRoleLimit(playerRole)));
            }

            if ((matcher = UNIQUE_VISITOR_INDEX_PLACEHOLDER_PATTERN.matcher(subPlaceholder)).matches()) {
                return handlePlayersIndexPlaceholder(island.getUniqueVisitors(), matcher.group(1));
            }

            if ((matcher = UPGRADE_PLACEHOLDER_PATTERN.matcher(subPlaceholder)).matches()) {
                Upgrade upgrade = plugin.getUpgrades().getUpgrade(matcher.group(1));

                if (upgrade == null) {
                    return Optional.empty();
                }

                return Optional.of(String.valueOf(island.getUpgradeLevel(upgrade).getLevel()));
            }

            if ((matcher = VISITOR_INDEX_PLACEHOLDER_PATTERN.matcher(subPlaceholder)).matches()) {
                return handlePlayersIndexPlaceholder(island.getIslandVisitors(), matcher.group(1));
            }

            if ((matcher = VISITOR_LAST_JOIN_PLACEHOLDER_PATTERN.matcher(subPlaceholder)).matches()) {
                String visitorName = matcher.group(1);
                return Optional.of(island.getUniqueVisitorsWithTimes().stream()
                        .filter(uniqueVisitor -> uniqueVisitor.getKey().getName().equalsIgnoreCase(visitorName))
                        .findFirst()
                        .map(Pair::getValue).map(value -> Formatters.DATE_FORMATTER.format(new Date(value)))
                        .orElse("Haven't Joined"));
            }

            if ((matcher = WORLD_ENABLED_PLACEHOLDER_PATTERN.matcher(subPlaceholder)).matches()) {
                Dimension dimension = Dimensions.getSafe(matcher.group(1));

                if (dimension == null) {
                    return Optional.empty();
                }

                return Optional.of(Formatters.BOOLEAN_FORMATTER.format(island.isDimensionEnabled(dimension), superiorPlayer.getUserLocale()));
            }

            if ((matcher = WORLD_GENERATED_PLACEHOLDER_PATTERN.matcher(subPlaceholder)).matches()) {
                Dimension dimension = Dimensions.getSafe(matcher.group(1));

                if (dimension == null) {
                    return Optional.empty();
                }

                return Optional.of(Formatters.BOOLEAN_FORMATTER.format(island.wasSchematicGenerated(dimension), superiorPlayer.getUserLocale()));
            }

            if ((matcher = WORLD_UNLOCKED_PLACEHOLDER_PATTERN.matcher(subPlaceholder)).matches()) {
                Dimension dimension = Dimensions.getSafe(matcher.group(1));

                if (dimension == null) {
                    return Optional.empty();
                }

                return Optional.of(Formatters.BOOLEAN_FORMATTER.format(island.getUnlockedWorlds().contains(dimension), superiorPlayer.getUserLocale()));
            }
        }

        if ((matcher = BLOCK_LEVEL_PLACEHOLDER_PATTERN.matcher(subPlaceholder)).matches()) {
            Key key = Keys.ofMaterialAndData(matcher.group(1));
            BigDecimal blockLevel = plugin.getBlockValues().getBlockLevel(key);

            return Optional.of(String.valueOf(blockLevel));
        }

        if ((matcher = BLOCK_WORTH_PLACEHOLDER_PATTERN.matcher(subPlaceholder)).matches()) {
            Key key = Keys.ofMaterialAndData(matcher.group(1));
            BigDecimal blockWorth = plugin.getBlockValues().getBlockWorth(key);

            return Optional.of(String.valueOf(blockWorth));
        }

        if ((matcher = TOP_PLACEHOLDER_PATTERN.matcher(subPlaceholder)).matches()) {
            return handleTopIslandsPlaceholder(island, superiorPlayer, matcher.group(1));
        }

        try {
            return Optional.ofNullable(ISLAND_PLACEHOLDER_PARSERS.get(subPlaceholder))
                    .map(placeholderParser -> placeholderParser.apply(island, superiorPlayer));
        } catch (NullPointerException ignored) {
            // One of the island parses failed due to invalid island being sent.
        }

        return Optional.empty();
    }

    private static Optional<String> handleCompletedMissionsPlaceholder(List<Mission<?>> missions, MissionCategory missionCategory) {
        int count = 0;

        for (Mission<?> mission : missions) {
            if (missionCategory == mission.getMissionCategory()) {
                count++;
            }
        }

        return Optional.of(String.valueOf(count));
    }

    private static Optional<String> handleGeneratorAmountPlaceholder(Island island, String placeholder) {
        Dimension dimension = extractDimension(placeholder);

        if (dimension == null) {
            return Optional.empty();
        }

        Key key = Keys.ofMaterialAndData(placeholder.substring(dimension.getName().length() + 1));

        return Optional.of(String.valueOf(island.getGeneratorAmount(key, dimension)));
    }

    private static Optional<String> handleGeneratorPercentagePlaceholder(Island island, String placeholder) {
        Dimension dimension = extractDimension(placeholder);

        if (dimension == null) {
            return Optional.empty();
        }

        Key key = Keys.ofMaterialAndData(placeholder.substring(dimension.getName().length() + 1));

        return Optional.of(String.valueOf(IslandUtils.getGeneratorPercentageDecimal(island, key, dimension)));
    }

    private static Optional<String> handleIslandFlagPlaceholder(Island island, SuperiorPlayer superiorPlayer, String name) {
        IslandFlag islandFlag = IslandFlags.getSafe(name);

        if (islandFlag == null) {
            return Optional.empty();
        }

        return Optional.of(Formatters.BOOLEAN_FORMATTER.format(island.hasSettingsEnabled(islandFlag),
                superiorPlayer.getUserLocale()));
    }

    private static Optional<String> handleIslandPrivilegePlaceholder(Island island, SuperiorPlayer superiorPlayer, String name) {
        IslandPrivilege islandPrivilege = IslandPrivileges.getSafe(name);

        if (islandPrivilege == null) {
            return Optional.empty();
        }

        return Optional.of(Formatters.BOOLEAN_FORMATTER.format(island.hasPermission(superiorPlayer, islandPrivilege),
                superiorPlayer.getUserLocale()));
    }

    private static Optional<String> handleRequiredPlayerRoleForIslandPrivilegePlaceholder(Island island, String name) {
        IslandPrivilege islandPrivilege = IslandPrivileges.getSafe(name);

        if (islandPrivilege == null) {
            return Optional.empty();
        }

        return Optional.of(island.getRequiredPlayerRole(islandPrivilege).getDisplayName());
    }

    private static Optional<String> handlePlayersIndexPlaceholder(List<SuperiorPlayer> players, String position) {
        int index = -1;

        try {
            index = Integer.parseInt(position) - 1;
        } catch (NumberFormatException ignored) {
        }

        if (index < 0 || index >= players.size()) {
            return Optional.empty();
        }

        return Optional.of(players.get(index).getName());
    }

    private static Optional<String> handleTopIslandsPlaceholder(@Nullable Island island, @Nullable SuperiorPlayer superiorPlayer,
                                                                String subPlaceholder) {
        SortingType sortingType = extractSortingType(subPlaceholder);

        if (sortingType == null) {
            return Optional.empty();
        }

        String placeholderValue = subPlaceholder.substring(sortingType.getName().length() + 1);

        if (placeholderValue.equalsIgnoreCase("position")) {
            if (island == null) {
                return Optional.empty();
            }

            return Optional.of(String.valueOf(plugin.getGrid().getIslandPosition(island, sortingType) + 1));
        }

        Function<Island, String> function;
        Matcher matcher;

        if ((matcher = TOP_VALUE_FORMAT_PLACEHOLDER_PATTERN.matcher(placeholderValue)).matches()) {
            function = targetIsland -> sortingType.getValue(targetIsland)
                    .map(value -> Formatters.FANCY_NUMBER_FORMATTER.format(value, superiorPlayer.getUserLocale()))
                    .orElse(null);
        } else if ((matcher = TOP_VALUE_RAW_PLACEHOLDER_PATTERN.matcher(placeholderValue)).matches()) {
            function = targetIsland -> sortingType.getValue(targetIsland)
                    .map(String::valueOf)
                    .orElse(null);
        } else if ((matcher = TOP_VALUE_PLACEHOLDER_PATTERN.matcher(placeholderValue)).matches()) {
            function = targetIsland -> sortingType.getValue(targetIsland)
                    .map(Formatters.NUMBER_FORMATTER::format)
                    .orElse(null);
        } else if ((matcher = TOP_LEADER_PLACEHOLDER_PATTERN.matcher(placeholderValue)).matches()) {
            function = targetIsland -> targetIsland.getOwner().getName();
        } else if ((matcher = TOP_CUSTOM_PLACEHOLDER_PATTERN.matcher(placeholderValue)).matches()) {
            String customPlaceholder = matcher.group(2);
            function = targetIsland -> parsePlaceholdersForIsland(targetIsland, superiorPlayer,
                    customPlaceholder).orElse(null);
        } else {
            function = targetIsland -> targetIsland.getName().isEmpty() ?
                    targetIsland.getOwner().getName() : targetIsland.getName();
        }

        int targetPosition;

        try {
            targetPosition = Integer.parseInt(matcher.matches() ? matcher.group(1) : placeholderValue);
        } catch (NumberFormatException error) {
            return Optional.empty();
        }

        Island targetIsland = plugin.getGrid().getIsland(targetPosition - 1, sortingType);

        return Optional.ofNullable(targetIsland).map(function);
    }

    @Nullable
    private static Dimension extractDimension(String placeholder) {
        String[] sections = placeholder.split("_");

        if (sections.length < 2) {
            return null;
        }

        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < sections.length - 1; i++) {
            if (i > 0) {
                stringBuilder.append("_");
            }

            stringBuilder.append(sections[i]);

            Dimension dimension = Dimensions.getSafe(stringBuilder.toString());

            if (dimension != null) {
                return dimension;
            }
        }

        return null;
    }

    @Nullable
    private static SortingType extractSortingType(String placeholder) {
        String[] sections = placeholder.split("_");

        if (sections.length < 2) {
            return null;
        }

        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < sections.length - 1; i++) {
            if (i > 0) {
                stringBuilder.append("_");
            }

            stringBuilder.append(sections[i]);

            SortingType sortingType = SortingType.getByName(stringBuilder.toString());

            if (sortingType != null) {
                return sortingType;
            }
        }

        return null;
    }

    private static WorldInfo getDefaultWorldInfo(Island island) {
        return plugin.getGrid().getIslandsWorldInfo(island, getDefaultWorldDimension());
    }

    private static Dimension getDefaultWorldDimension() {
        return plugin.getSettings().getWorlds().getDefaultWorldDimension();
    }

    private static IslandPlaceholderParser legacyPlaceholder(String placeholder, String correctPlaceholder, IslandPlaceholderParser placeholderParser) {
        return new LegacyIslandPlaceholderParser(placeholder, correctPlaceholder, placeholderParser);
    }

    private static class LegacyIslandPlaceholderParser implements IslandPlaceholderParser {

        private final String placeholder;
        private final String correctPlaceholder;
        private final IslandPlaceholderParser originalPlaceholderParser;
        private boolean promptDeprecated = true;

        LegacyIslandPlaceholderParser(String placeholder, String correctPlaceholder, IslandPlaceholderParser originalPlaceholderParser) {
            this.placeholder = placeholder;
            this.correctPlaceholder = correctPlaceholder;
            this.originalPlaceholderParser = originalPlaceholderParser;
        }

        @Override
        public String apply(Island island, SuperiorPlayer superiorPlayer) {
            if (this.promptDeprecated) {
                this.promptDeprecated = false;
                Log.error(new Throwable(), "Deprecated placeholder was used: " + this.placeholder + ". Use this one instead: " + this.correctPlaceholder);
            }

            return this.originalPlaceholderParser.apply(island, superiorPlayer);
        }

    }

}
