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
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.values.BlockValue;
import com.bgsoftware.superiorskyblock.external.placeholders.PlaceholdersProvider;
import com.bgsoftware.superiorskyblock.island.IslandUtils;
import com.bgsoftware.superiorskyblock.island.privilege.IslandPrivileges;
import com.bgsoftware.superiorskyblock.island.role.SPlayerRole;
import com.bgsoftware.superiorskyblock.player.chat.ChatStates;
import com.bgsoftware.superiorskyblock.service.IService;
import com.google.common.collect.ImmutableMap;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Biome;
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

public class PlaceholdersServiceImpl implements PlaceholdersService, IService {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private static final Pattern ISLAND_PLACEHOLDER_PATTERN = Pattern.compile("island_(.+)");
    private static final Pattern PLAYER_PLACEHOLDER_PATTERN = Pattern.compile("player_(.+)");

    private static final Pattern BAN_INDEX_PLACEHOLDER_PATTERN = Pattern.compile("ban_(.+)");
    private static final Pattern BIOME_PLACEHOLDER_PATTERN = Pattern.compile("island_biome_(.+)");
    private static final Pattern BLOCK_COUNT_PLACEHOLDER_PATTERN = Pattern.compile("island_block_count_(.+)");
    private static final Pattern BLOCK_LEVEL_PLACEHOLDER_PATTERN = Pattern.compile("island_block_level_(.+)");
    private static final Pattern BLOCK_LIMIT_PLACEHOLDER_PATTERN = Pattern.compile("island_block_limit_(.+)");
    private static final Pattern BLOCK_TOTAL_LEVEL_PLACEHOLDER_PATTERN = Pattern.compile("island_block_total_level_(.+)");
    private static final Pattern BLOCK_TOTAL_WORTH_PLACEHOLDER_PATTERN = Pattern.compile("island_block_total_worth_(.+)");
    private static final Pattern BLOCK_WORTH_PLACEHOLDER_PATTERN = Pattern.compile("island_block_worth_(.+)");
    private static final Pattern COOP_INDEX_PLACEHOLDER_PATTERN = Pattern.compile("coop_(.+)");
    private static final Pattern COUNT_PLACEHOLDER_PATTERN = Pattern.compile("island_count_(.+)");
    private static final Pattern DATA_PLACEHOLDER_PATTERN = Pattern.compile("island_data_(.+)");
    private static final Pattern EFFECT_PLACEHOLDER_PATTERN = Pattern.compile("island_effect_(.+)");
    private static final Pattern ENTITY_COUNT_PLACEHOLDER_PATTERN = Pattern.compile("island_entity_count_(.+)");
    private static final Pattern ENTITY_LIMIT_PLACEHOLDER_PATTERN = Pattern.compile("island_entity_limit_(.+)");
    private static final Pattern FLAG_PLACEHOLDER_PATTERN = Pattern.compile("flag_(.+)");
    private static final Pattern GENERATOR_AMOUNT_PLACEHOLDER_PATTERN = Pattern.compile("island_generator_amount_(.+)");
    private static final Pattern GENERATOR_PERCENTAGE_PLACEHOLDER_PATTERN = Pattern.compile("island_generator_percentage_(.+)");
    private static final Pattern MEMBER_INDEX_PLACEHOLDER_PATTERN = Pattern.compile("member_(.+)");
    private static final Pattern MISSIONS_COMPLETED_PATTERN = Pattern.compile("missions_completed_(.+)");
    private static final Pattern MISSION_STATUS_PATTERN = Pattern.compile("mission_status_(.+)");
    private static final Pattern PERMISSION_PLACEHOLDER_PATTERN = Pattern.compile("island_permission_(.+)");
    private static final Pattern PERMISSION_ROLE_PLACEHOLDER_PATTERN = Pattern.compile("island_permission_role_(.+)");
    private static final Pattern PLAYER_INDEX_PLACEHOLDER_PATTERN = Pattern.compile("player_(.+)");
    private static final Pattern ROLE_COUNT_PLACEHOLDER_PATTERN = Pattern.compile("island_role_count_(.+)");
    private static final Pattern ROLE_LIMIT_PLACEHOLDER_PATTERN = Pattern.compile("island_role_limit_(.+)");
    private static final Pattern TOP_PLACEHOLDER_PATTERN = Pattern.compile("island_top_(.+)");
    private static final Pattern TOP_TYPE_PLACEHOLDER_PATTERN = Pattern.compile("(.+?)_(.+)");
    private static final Pattern TOP_VALUE_FORMAT_PLACEHOLDER_PATTERN = Pattern.compile("value_format_(.+)");
    private static final Pattern TOP_VALUE_RAW_PLACEHOLDER_PATTERN = Pattern.compile("value_raw_(.+)");
    private static final Pattern TOP_VALUE_PLACEHOLDER_PATTERN = Pattern.compile("value_(.+)");
    private static final Pattern TOP_LEADER_PLACEHOLDER_PATTERN = Pattern.compile("leader_(.+)");
    private static final Pattern TOP_CUSTOM_PLACEHOLDER_PATTERN = Pattern.compile("(\\d+)_(.+)");
    private static final Pattern UNIQUE_VISITOR_INDEX_PLACEHOLDER_PATTERN = Pattern.compile("unique_visitor_(.+)");
    private static final Pattern UPGRADE_PLACEHOLDER_PATTERN = Pattern.compile("island_upgrade_(.+)");
    private static final Pattern VISITOR_INDEX_PLACEHOLDER_PATTERN = Pattern.compile("visitor_(.+)");
    private static final Pattern VISITOR_LAST_JOIN_PLACEHOLDER_PATTERN = Pattern.compile("visitor_last_join_(.+)");
    private static final Pattern WORLD_UNLOCKED_PLACEHOLDER_PATTERN = Pattern.compile("island_world_unlocked_(.+)");
    private static final Pattern WORLD_ENABLED_PLACEHOLDER_PATTERN = Pattern.compile("island_world_enabled_(.+)");
    private static final Pattern WORLD_GENERATED_PLACEHOLDER_PATTERN = Pattern.compile("island_world_generated_(.+)");

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
                    .put("chat_state", superiorPlayer ->
                            Formatters.CAPITALIZED_FORMATTER.format(superiorPlayer.getChatState().getName()))
                    .put("disbands", superiorPlayer ->
                            superiorPlayer.getDisbands() + "")
                    .put("fly", superiorPlayer ->
                            Formatters.BOOLEAN_FORMATTER.format(superiorPlayer.hasIslandFlyEnabled(), superiorPlayer.getUserLocale()))
                    .put("local_chat", superiorPlayer ->
                            Formatters.BOOLEAN_FORMATTER.format(superiorPlayer.getChatState() == ChatStates.LOCAL_CHAT, superiorPlayer.getUserLocale()))
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
                            Formatters.BOOLEAN_FORMATTER.format(superiorPlayer.getChatState() == ChatStates.TEAM_CHAT, superiorPlayer.getUserLocale()))
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
                            island.getCenterPosition().getX() + "")
                    .put("center_y", (island, superiorPlayer) ->
                            island.getCenterPosition().getY() + "")
                    .put("center_z", (island, superiorPlayer) ->
                            island.getCenterPosition().getZ() + "")
                    .put("chest_size", (island, superiorPlayer) ->
                            island.getChestSize() + "")
                    .put("coop_limit", (island, superiorPlayer) ->
                            island.getCoopLimit() + "")
                    .put("coop_list", (island, superiorPlayer) ->
                            Formatters.COMMA_FORMATTER.format(island.getCoopPlayers().stream().map(SuperiorPlayer::getName)))
                    .put("coop_size", (island, superiorPlayer) ->
                            island.getCoopPlayers().size() + "")
                    .put("creation_time", (island, superiorPlayer) ->
                            island.getCreationTimeDate())
                    .put("crops_multiplier", (island, superiorPlayer) ->
                            island.getCropGrowthMultiplier() + "")
                    .put("description", (island, superiorPlayer) ->
                            island.getDescription())
                    .put("discord", (island, superiorPlayer) ->
                            island.hasPermission(superiorPlayer, IslandPrivileges.DISCORD_SHOW) ? island.getDiscord() : IslandUtils.DEFAULT_NONE_VALUE)
                    .put("discord_all", (island, superiorPlayer) ->
                            island.getDiscord())
                    .put("drops_multiplier", (island, superiorPlayer) ->
                            island.getMobDropsMultiplier() + "")
                    .put("exists", (island, superiorPlayer) ->
                            Formatters.BOOLEAN_FORMATTER.format(island != null, superiorPlayer.getUserLocale()))
                    .put("home", (island, superiorPlayer) -> {
                        WorldInfo worldInfo = getDefaultWorldInfo(island);
                        return Formatters.LOCATION_FORMATTER.format(island.getIslandHomePosition(worldInfo.getDimension()).toLocation(worldInfo));
                    })
                    .put("home_x", (island, superiorPlayer) ->
                            island.getIslandHomePosition(getDefaultWorldDimension()).getX() + "")
                    .put("home_y", (island, superiorPlayer) ->
                            island.getIslandHomePosition(getDefaultWorldDimension()).getY() + "")
                    .put("home_z", (island, superiorPlayer) ->
                            island.getIslandHomePosition(getDefaultWorldDimension()).getZ() + "")
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
                            island.getCompletedMissions().size() + "")
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
                            island.getAllPlayersInside().size() + "")
                    .put("players_list", (island, superiorPlayer) ->
                            Formatters.COMMA_FORMATTER.format(island.getAllPlayersInside().stream().map(SuperiorPlayer::getName)))
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
                    .put("team_list", (island, superiorPlayer) ->
                            Formatters.COMMA_FORMATTER.format(island.getIslandMembers(true).stream().map(SuperiorPlayer::getName)))
                    .put("team_size", (island, superiorPlayer) ->
                            island.getIslandMembers(true).size() + "")
                    .put("team_size_online", (island, superiorPlayer) ->
                            island.getIslandMembers(true).stream().filter(SuperiorPlayer::isShownAsOnline).count() + "")
                    .put("unique_visitors_count", (island, superiorPlayer) ->
                            island.getUniqueVisitors().size() + "")
                    .put("unique_visitors_list", (island, superiorPlayer) ->
                            Formatters.COMMA_FORMATTER.format(island.getUniqueVisitors().stream().map(SuperiorPlayer::getName)))
                    .put("uuid", (island, superiorPlayer) ->
                            island.getUniqueId() + "")
                    .put("visitors_count", (island, superiorPlayer) ->
                            island.getIslandVisitors(false).size() + "")
                    .put("visitors_list", (island, superiorPlayer) ->
                            Formatters.COMMA_FORMATTER.format(island.getIslandVisitors().stream().map(SuperiorPlayer::getName)))
                    .put("visitors_location", (island, superiorPlayer) -> {
                        WorldInfo worldInfo = getDefaultWorldInfo(island);
                        return Formatters.LOCATION_FORMATTER.format(island.getVisitorsPosition(null /*unused*/).toLocation(worldInfo));
                    })
                    .put("visitors_location_x", (island, superiorPlayer) ->
                            island.getVisitorsPosition(getDefaultWorldDimension()).getX() + "")
                    .put("visitors_location_y", (island, superiorPlayer) ->
                            island.getVisitorsPosition(getDefaultWorldDimension()).getY() + "")
                    .put("visitors_location_z", (island, superiorPlayer) ->
                            island.getVisitorsPosition(getDefaultWorldDimension()).getZ() + "")
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
                    // Deprecated Island Placeholders
                    .put("end_unlocked", legacyPlaceholder("superior_island_end_unlocked", "superior_island_world_unlocked_the_end", (island, superiorPlayer) ->
                            Formatters.BOOLEAN_FORMATTER.format(island.isEndEnabled(), superiorPlayer.getUserLocale())))
                    .put("nether_unlocked", legacyPlaceholder("superior_island_nether_unlocked", "superior_island_world_unlocked_nether", (island, superiorPlayer) ->
                            Formatters.BOOLEAN_FORMATTER.format(island.isNetherEnabled(), superiorPlayer.getUserLocale())))
                    .put("normal_unlocked", legacyPlaceholder("superior_island_normal_unlocked", "superior_island_world_unlocked_normal", (island, superiorPlayer) ->
                            Formatters.BOOLEAN_FORMATTER.format(island.isNormalEnabled(), superiorPlayer.getUserLocale())))
                    .put("hoppers_limit", legacyPlaceholder("superior_island_hoppers_limit", "superior_island_block_limit_hopper", (island, superiorPlayer) ->
                            island.getBlockLimit(ConstantKeys.HOPPER) + ""))
                    .put("x", legacyPlaceholder("superior_island_x", "superior_island_center_x", (island, superiorPlayer) ->
                            island.getCenterPosition().getX() + ""))
                    .put("y", legacyPlaceholder("superior_island_y", "superior_island_center_y", (island, superiorPlayer) ->
                            island.getCenterPosition().getY() + ""))
                    .put("z", legacyPlaceholder("superior_island_z", "superior_island_center_z", (island, superiorPlayer) ->
                            island.getCenterPosition().getZ() + ""))
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
            if ((matcher = BIOME_PLACEHOLDER_PATTERN.matcher(placeholder)).matches()) {
                Biome biome = island.getBiome(Dimension.getByName(matcher.group(1)));
                return Optional.of(Formatters.CAPITALIZED_FORMATTER.format(biome.name()));
            }

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

            if ((matcher = WORLD_UNLOCKED_PLACEHOLDER_PATTERN.matcher(placeholder)).matches()) {
                boolean unlockedWorld = island.getUnlockedWorlds().contains(Dimension.getByName(matcher.group(1)));
                return Optional.of(Formatters.BOOLEAN_FORMATTER.format(unlockedWorld, superiorPlayer.getUserLocale()));
            }

            if ((matcher = WORLD_ENABLED_PLACEHOLDER_PATTERN.matcher(placeholder)).matches()) {
                boolean enabledWorld = island.isDimensionEnabled(Dimension.getByName(matcher.group(1)));
                return Optional.of(Formatters.BOOLEAN_FORMATTER.format(enabledWorld, superiorPlayer.getUserLocale()));
            }

            if ((matcher = WORLD_GENERATED_PLACEHOLDER_PATTERN.matcher(placeholder)).matches()) {
                boolean generatedWorld = island.wasSchematicGenerated(Dimension.getByName(matcher.group(1)));
                return Optional.of(Formatters.BOOLEAN_FORMATTER.format(generatedWorld, superiorPlayer.getUserLocale()));
            }

            if (superiorPlayer != null) {
                if ((matcher = BAN_INDEX_PLACEHOLDER_PATTERN.matcher(subPlaceholder)).matches()) {
                    return handlePlayersIndexPlaceholder(island.getBannedPlayers(), matcher.group(1));
                }

                if ((matcher = BLOCK_COUNT_PLACEHOLDER_PATTERN.matcher(placeholder)).matches() ||
                        (matcher = COUNT_PLACEHOLDER_PATTERN.matcher(placeholder)).matches()) {
                    String keyName = matcher.group(1);
                    return Optional.of(island.getBlockCountAsBigInteger(Keys.ofMaterialAndData(keyName)) + "");
                }

                if ((matcher = BLOCK_LEVEL_PLACEHOLDER_PATTERN.matcher(placeholder)).matches()) {
                    String keyName = matcher.group(1);
                    BlockValue blockValue = plugin.getBlockValues().getBlockValue(Keys.ofMaterialAndData(keyName));
                    return Optional.of(blockValue.getLevel() + "");
                }

                if ((matcher = BLOCK_LIMIT_PLACEHOLDER_PATTERN.matcher(placeholder)).matches()) {
                    String keyName = matcher.group(1);
                    return Optional.of(island.getBlockLimit(Keys.ofMaterialAndData(keyName)) + "");
                }

                if ((matcher = BLOCK_TOTAL_LEVEL_PLACEHOLDER_PATTERN.matcher(placeholder)).matches()) {
                    String keyName = matcher.group(1);
                    BlockValue blockValue = plugin.getBlockValues().getBlockValue(Keys.ofMaterialAndData(keyName));
                    BigDecimal amount = new BigDecimal(island.getBlockCountAsBigInteger(Keys.ofMaterialAndData(keyName)));
                    return Optional.of(blockValue.getLevel().multiply(amount) + "");
                }

                if ((matcher = BLOCK_TOTAL_WORTH_PLACEHOLDER_PATTERN.matcher(placeholder)).matches()) {
                    String keyName = matcher.group(1);
                    BlockValue blockValue = plugin.getBlockValues().getBlockValue(Keys.ofMaterialAndData(keyName));
                    BigDecimal amount = new BigDecimal(island.getBlockCountAsBigInteger(Keys.ofMaterialAndData(keyName)));
                    return Optional.of(blockValue.getWorth().multiply(amount) + "");
                }

                if ((matcher = BLOCK_WORTH_PLACEHOLDER_PATTERN.matcher(placeholder)).matches()) {
                    String keyName = matcher.group(1);
                    BlockValue blockValue = plugin.getBlockValues().getBlockValue(Keys.ofMaterialAndData(keyName));
                    return Optional.of(blockValue.getWorth() + "");
                }

                if ((matcher = COOP_INDEX_PLACEHOLDER_PATTERN.matcher(subPlaceholder)).matches()) {
                    return handlePlayersIndexPlaceholder(island.getCoopPlayers(), matcher.group(1));
                }

                if ((matcher = DATA_PLACEHOLDER_PATTERN.matcher(placeholder)).matches()) {
                    String keyName = matcher.group(1);
                    Object data = island.getPersistentDataContainer().get(keyName);
                    if (data == null) {
                        return Optional.empty();
                    }
                    return Optional.of(data.toString());
                }

                if ((matcher = EFFECT_PLACEHOLDER_PATTERN.matcher(placeholder)).matches()) {
                    String effectName = matcher.group(1);
                    PotionEffectType potionEffectType = PotionEffectType.getByName(effectName);
                    if (potionEffectType == null) {
                        return Optional.empty();
                    }
                    return Optional.of(island.getPotionEffectLevel(potionEffectType) + "");
                }

                if ((matcher = ENTITY_COUNT_PLACEHOLDER_PATTERN.matcher(placeholder)).matches()) {
                    String keyName = matcher.group(1);
                    return Optional.of(island.getEntitiesTracker().getEntityCount(Keys.ofEntityType(keyName)) + "");
                }

                if ((matcher = ENTITY_LIMIT_PLACEHOLDER_PATTERN.matcher(placeholder)).matches()) {
                    String keyName = matcher.group(1);
                    return Optional.of(island.getEntityLimit(Keys.ofEntityType(keyName)) + "");
                }

                if ((matcher = FLAG_PLACEHOLDER_PATTERN.matcher(subPlaceholder)).matches()) {
                    return handleFlagsPlaceholder(island, superiorPlayer, matcher.group(1));
                }

                if ((matcher = MEMBER_INDEX_PLACEHOLDER_PATTERN.matcher(subPlaceholder)).matches()) {
                    return handlePlayersIndexPlaceholder(island.getIslandMembers(false), matcher.group(1));
                }

                if ((matcher = PERMISSION_PLACEHOLDER_PATTERN.matcher(placeholder)).matches()) {
                    return handlePermissionsPlaceholder(island, superiorPlayer, matcher.group(1));
                }

                if ((matcher = PLAYER_INDEX_PLACEHOLDER_PATTERN.matcher(subPlaceholder)).matches()) {
                    return handlePlayersIndexPlaceholder(island.getAllPlayersInside(), matcher.group(1));
                }

                if ((matcher = ROLE_COUNT_PLACEHOLDER_PATTERN.matcher(placeholder)).matches()) {
                    String roleName = matcher.group(1);
                    PlayerRole playerRole;
                    try {
                        playerRole = SPlayerRole.of(roleName);
                    } catch (IllegalArgumentException error) {
                        return Optional.empty();
                    }
                    return Optional.of(island.getIslandMembers(playerRole).size() + "");
                }

                if ((matcher = ROLE_LIMIT_PLACEHOLDER_PATTERN.matcher(placeholder)).matches()) {
                    String roleName = matcher.group(1);
                    PlayerRole playerRole;
                    try {
                        playerRole = SPlayerRole.of(roleName);
                    } catch (IllegalArgumentException error) {
                        return Optional.empty();
                    }
                    return Optional.of(island.getRoleLimit(playerRole) + "");
                }

                if ((matcher = UNIQUE_VISITOR_INDEX_PLACEHOLDER_PATTERN.matcher(subPlaceholder)).matches()) {
                    return handlePlayersIndexPlaceholder(island.getUniqueVisitors(), matcher.group(1));
                }

                if ((matcher = UPGRADE_PLACEHOLDER_PATTERN.matcher(placeholder)).matches()) {
                    String upgradeName = matcher.group(1);
                    Upgrade upgrade = plugin.getUpgrades().getUpgrade(upgradeName);
                    if (upgrade == null) {
                        return Optional.empty();
                    }
                    return Optional.of(island.getUpgradeLevel(upgrade).getLevel() + "");
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
        String[] placeholderSections = placeholder.split("_");

        if (placeholderSections.length <= 1)
            return Optional.empty();

        Dimension dimension;
        try {
            dimension = Dimension.getByName(placeholderSections[0]);
        } catch (NullPointerException error) {
            return Optional.empty();
        }

        String keyName = String.join("_", placeholderSections).substring(placeholderSections[0].length() + 1);

        return Optional.of(island.getGeneratorAmount(Keys.ofMaterialAndData(keyName), dimension) + "");
    }

    private static Optional<String> handleGeneratorPercentagesPlaceholder(@Nullable Island island, String placeholder) {
        String[] placeholderSections = placeholder.split("_");

        if (placeholderSections.length <= 1)
            return Optional.empty();

        Dimension dimension;
        try {
            dimension = Dimension.getByName(placeholderSections[0]);
        } catch (NullPointerException error) {
            return Optional.empty();
        }

        String keyName = String.join("_", placeholderSections).substring(placeholderSections[0].length() + 1);

        return Optional.of(IslandUtils.getGeneratorPercentageDecimal(island, Keys.ofMaterialAndData(keyName), dimension) + "");
    }

    private static Optional<String> handlePlayersIndexPlaceholder(List<SuperiorPlayer> superiorPlayers, String position) {
        int index = -1;

        try {
            index = Integer.parseInt(position) - 1;
        } catch (NumberFormatException ignored) {
        }

        if (index < 0 || index >= superiorPlayers.size()) {
            return Optional.empty();
        }

        return Optional.of(superiorPlayers.get(index).getName());
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
        Matcher matcher = TOP_TYPE_PLACEHOLDER_PATTERN.matcher(subPlaceholder);
        if (!matcher.matches())
            return Optional.empty();

        SortingType sortingType = SortingType.getByName(matcher.group(1).toUpperCase(Locale.ENGLISH));
        if (sortingType == null)
            return Optional.empty();

        String placeholderValue = matcher.group(2);

        if (placeholderValue.equals("position"))
            return island == null ? Optional.empty() : Optional.of((plugin.getGrid().getIslandPosition(island, sortingType) + 1) + "");

        Function<Island, String> valueFunction;

        if ((matcher = TOP_VALUE_FORMAT_PLACEHOLDER_PATTERN.matcher(placeholderValue)).matches()) {
            valueFunction = targetIsland -> sortingType.getValue(targetIsland)
                    .map(value -> Formatters.FANCY_NUMBER_FORMATTER.format(value, superiorPlayer.getUserLocale()))
                    .orElse(null);
        } else if ((matcher = TOP_VALUE_RAW_PLACEHOLDER_PATTERN.matcher(placeholderValue)).matches()) {
            valueFunction = targetIsland -> sortingType.getValue(targetIsland)
                    .map(String::valueOf)
                    .orElse(null);
        } else if ((matcher = TOP_VALUE_PLACEHOLDER_PATTERN.matcher(placeholderValue)).matches()) {
            valueFunction = targetIsland -> sortingType.getValue(targetIsland)
                    .map(Formatters.NUMBER_FORMATTER::format)
                    .orElse(null);
        } else if ((matcher = TOP_LEADER_PLACEHOLDER_PATTERN.matcher(placeholderValue)).matches()) {
            valueFunction = targetIsland -> targetIsland.getOwner().getName();
        } else if ((matcher = TOP_CUSTOM_PLACEHOLDER_PATTERN.matcher(placeholderValue)).matches()) {
            String customPlaceholder = matcher.group(2);
            valueFunction = targetIsland -> parsePlaceholdersForIsland(targetIsland, superiorPlayer,
                    "superior_island_" + customPlaceholder,
                    customPlaceholder).orElse(null);
        } else {
            valueFunction = targetIsland -> targetIsland.getName().isEmpty() ?
                    targetIsland.getOwner().getName() : targetIsland.getName();
        }

        int targetPosition;

        try {
            targetPosition = Integer.parseInt(matcher.matches() ? matcher.group(1) : placeholderValue);
        } catch (NumberFormatException error) {
            return Optional.empty();
        }

        Island targetIsland = plugin.getGrid().getIsland(targetPosition - 1, sortingType);

        return Optional.ofNullable(targetIsland).map(valueFunction);
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
