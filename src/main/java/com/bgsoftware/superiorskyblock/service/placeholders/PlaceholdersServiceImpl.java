package com.bgsoftware.superiorskyblock.service.placeholders;

import com.bgsoftware.common.annotations.NotNull;
import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandFlag;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.island.SortingType;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.service.placeholders.IslandPlaceholderParser;
import com.bgsoftware.superiorskyblock.api.service.placeholders.PlaceholdersService;
import com.bgsoftware.superiorskyblock.api.service.placeholders.PlayerPlaceholderParser;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.key.ConstantKeys;
import com.bgsoftware.superiorskyblock.core.key.Keys;
import com.bgsoftware.superiorskyblock.external.placeholders.PlaceholdersProvider;
import com.bgsoftware.superiorskyblock.island.privilege.IslandPrivileges;
import com.bgsoftware.superiorskyblock.island.top.SortingTypes;
import com.bgsoftware.superiorskyblock.service.IService;
import com.google.common.collect.ImmutableMap;
import org.bukkit.OfflinePlayer;

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
    private static final Pattern PERMISSION_PLACEHOLDER_PATTERN = Pattern.compile("island_permission_(.+)");
    private static final Pattern PERMISSION_ROLE_PLACEHOLDER_PATTERN = Pattern.compile("island_permission_role_(.+)");
    private static final Pattern UPGRADE_PLACEHOLDER_PATTERN = Pattern.compile("island_upgrade_(.+)");
    private static final Pattern COUNT_PLACEHOLDER_PATTERN = Pattern.compile("island_count_(.+)");
    private static final Pattern BLOCK_LIMIT_PLACEHOLDER_PATTERN = Pattern.compile("island_block_limit_(.+)");
    private static final Pattern ENTITY_LIMIT_PLACEHOLDER_PATTERN = Pattern.compile("island_entity_limit_(.+)");
    private static final Pattern ENTITY_COUNT_PLACEHOLDER_PATTERN = Pattern.compile("island_entity_count_(.+)");
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
    private static final Pattern MEMBER_PLACEHOLDER_PATTERN = Pattern.compile("member_(.+)");
    private static final Pattern VISITOR_LAST_JOIN_PLACEHOLDER_PATTERN = Pattern.compile("visitor_last_join_(.+)");
    private static final Pattern ISLAND_FLAG_PLACEHOLDER_PATTERN = Pattern.compile("flag_(.+)");
    private static final Pattern MISSIONS_COMPLETED_PATTERN = Pattern.compile("missions_completed_(.+)");

    private static final Map<String, PlayerPlaceholderParser> PLAYER_PARSES =
            new ImmutableMap.Builder<String, PlayerPlaceholderParser>()
                    .put("texture", SuperiorPlayer::getTextureValue)
                    .put("role", superiorPlayer -> superiorPlayer.getPlayerRole().toString())
                    .put("role_display", superiorPlayer -> superiorPlayer.getPlayerRole().getDisplayName())
                    .put("locale", superiorPlayer -> Formatters.LOCALE_FORMATTER.format(superiorPlayer.getUserLocale()))
                    .put("world_border", superiorPlayer -> Formatters.BOOLEAN_FORMATTER.format(superiorPlayer.hasWorldBorderEnabled(), superiorPlayer.getUserLocale()))
                    .put("blocks_stacker", superiorPlayer -> Formatters.BOOLEAN_FORMATTER.format(superiorPlayer.hasBlocksStackerEnabled(), superiorPlayer.getUserLocale()))
                    .put("schematics", superiorPlayer -> Formatters.BOOLEAN_FORMATTER.format(superiorPlayer.hasSchematicModeEnabled(), superiorPlayer.getUserLocale()))
                    .put("team_chat", superiorPlayer -> Formatters.BOOLEAN_FORMATTER.format(superiorPlayer.hasTeamChatEnabled(), superiorPlayer.getUserLocale()))
                    .put("bypass", superiorPlayer -> Formatters.BOOLEAN_FORMATTER.format(superiorPlayer.hasBypassModeEnabled(), superiorPlayer.getUserLocale()))
                    .put("disbands", superiorPlayer -> superiorPlayer.getDisbands() + "")
                    .put("panel", superiorPlayer -> Formatters.BOOLEAN_FORMATTER.format(superiorPlayer.hasToggledPanel(), superiorPlayer.getUserLocale()))
                    .put("fly", superiorPlayer -> Formatters.BOOLEAN_FORMATTER.format(superiorPlayer.hasIslandFlyEnabled(), superiorPlayer.getUserLocale()))
                    .put("chat_spy", superiorPlayer -> Formatters.BOOLEAN_FORMATTER.format(superiorPlayer.hasAdminSpyEnabled(), superiorPlayer.getUserLocale()))
                    .put("border_color", superiorPlayer ->
                            Formatters.BORDER_COLOR_FORMATTER.format(superiorPlayer.getBorderColor(), superiorPlayer.getUserLocale()))
                    .put("missions_completed", superiorPlayer -> superiorPlayer.getCompletedMissions().size() + "")
                    .build();

    @SuppressWarnings("ConstantConditions")
    private static final Map<String, IslandPlaceholderParser> ISLAND_PARSES =
            new ImmutableMap.Builder<String, IslandPlaceholderParser>()
                    .put("center", (island, superiorPlayer) ->
                            Formatters.LOCATION_FORMATTER.format(island.getCenter(plugin.getSettings().getWorlds().getDefaultWorld())))
                    .put("x", (island, superiorPlayer) ->
                            island.getCenter(plugin.getSettings().getWorlds().getDefaultWorld()).getBlockX() + "")
                    .put("y", (island, superiorPlayer) ->
                            island.getCenter(plugin.getSettings().getWorlds().getDefaultWorld()).getBlockY() + "")
                    .put("z", (island, superiorPlayer) ->
                            island.getCenter(plugin.getSettings().getWorlds().getDefaultWorld()).getBlockZ() + "")
                    .put("world", (island, superiorPlayer) ->
                            island.getCenter(plugin.getSettings().getWorlds().getDefaultWorld()).getWorld().getName())
                    .put("team_size", (island, superiorPlayer) -> island.getIslandMembers(true).size() + "")
                    .put("team_size_online", (island, superiorPlayer) ->
                            island.getIslandMembers(true).stream().filter(SuperiorPlayer::isShownAsOnline).count() + "")
                    .put("team_limit", (island, superiorPlayer) -> island.getTeamLimit() + "")
                    .put("coop_limit", (island, superiorPlayer) -> island.getCoopLimit() + "")
                    .put("leader", (island, superiorPlayer) -> island.getOwner().getName())
                    .put("size_format", (island, superiorPlayer) -> {
                        int size = island.getIslandSize() * 2 + 1;
                        int rounded = 5 * (Math.round(size / 5.0F));
                        if (Math.abs(size - rounded) == 1)
                            size = rounded;
                        return size + " x " + size;
                    })
                    .put("size", (island, superiorPlayer) -> {
                        int size = island.getIslandSize() * 2 + 1;
                        return size + " x " + size;
                    })
                    .put("radius", (island, superiorPlayer) -> island.getIslandSize() + "")
                    .put("biome", (island, superiorPlayer) -> Formatters.CAPITALIZED_FORMATTER.format(island.getBiome().name()))
                    .put("level", (island, superiorPlayer) -> Formatters.NUMBER_FORMATTER.format(island.getIslandLevel()))
                    .put("level_raw", (island, superiorPlayer) -> island.getIslandLevel().toString())
                    .put("level_format", (island, superiorPlayer) ->
                            Formatters.FANCY_NUMBER_FORMATTER.format(island.getIslandLevel(), superiorPlayer.getUserLocale()))
                    .put("level_int", (island, superiorPlayer) -> island.getIslandLevel().toBigInteger().toString())
                    .put("worth", (island, superiorPlayer) -> Formatters.NUMBER_FORMATTER.format(island.getWorth()))
                    .put("worth_raw", (island, superiorPlayer) -> island.getWorth().toString())
                    .put("worth_format", (island, superiorPlayer) ->
                            Formatters.FANCY_NUMBER_FORMATTER.format(island.getWorth(), superiorPlayer.getUserLocale()))
                    .put("worth_int", (island, superiorPlayer) -> island.getWorth().toBigInteger().toString())
                    .put("raw_worth", (island, superiorPlayer) -> Formatters.NUMBER_FORMATTER.format(island.getRawWorth()))
                    .put("raw_worth_format", (island, superiorPlayer) ->
                            Formatters.FANCY_NUMBER_FORMATTER.format(island.getRawWorth(), superiorPlayer.getUserLocale()))
                    .put("bank", (island, superiorPlayer) -> Formatters.NUMBER_FORMATTER.format(island.getIslandBank().getBalance()))
                    .put("bank_raw", (island, superiorPlayer) -> island.getIslandBank().getBalance().toString())
                    .put("bank_format", (island, superiorPlayer) ->
                            Formatters.FANCY_NUMBER_FORMATTER.format(island.getIslandBank().getBalance(), superiorPlayer.getUserLocale()))
                    .put("bank_next_interest", (island, superiorPlayer) ->
                            Formatters.TIME_FORMATTER.format(Duration.ofSeconds(island.getNextInterest()), superiorPlayer.getUserLocale()))
                    .put("hoppers_limit", (island, superiorPlayer) -> island.getBlockLimit(ConstantKeys.HOPPER) + "")
                    .put("crops_multiplier", (island, superiorPlayer) -> island.getCropGrowthMultiplier() + "")
                    .put("spawners_multiplier", (island, superiorPlayer) -> island.getSpawnerRatesMultiplier() + "")
                    .put("drops_multiplier", (island, superiorPlayer) -> island.getMobDropsMultiplier() + "")
                    .put("discord", (island, superiorPlayer) ->
                            island.hasPermission(superiorPlayer, IslandPrivileges.DISCORD_SHOW) ? island.getDiscord() : "None")
                    .put("paypal", (island, superiorPlayer) ->
                            island.hasPermission(superiorPlayer, IslandPrivileges.PAYPAL_SHOW) ? island.getPaypal() : "None")
                    .put("discord_all", (island, superiorPlayer) -> island.getDiscord())
                    .put("paypal_all", (island, superiorPlayer) -> island.getPaypal())
                    .put("exists", (island, superiorPlayer) -> Formatters.BOOLEAN_FORMATTER.format(island != null, superiorPlayer.getUserLocale()))
                    .put("locked", (island, superiorPlayer) -> Formatters.BOOLEAN_FORMATTER.format(island.isLocked(), superiorPlayer.getUserLocale()))
                    .put("name", (island, superiorPlayer) -> {
                        return plugin.getSettings().getIslandNames().isColorSupport() ?
                                Formatters.COLOR_FORMATTER.format(island.getName()) : island.getName();
                    })
                    .put("name_leader", (island, superiorPlayer) -> {
                        return island.getName().isEmpty() ? island.getOwner().getName() :
                                plugin.getSettings().getIslandNames().isColorSupport() ?
                                        Formatters.COLOR_FORMATTER.format(island.getName()) : island.getName();
                    })
                    .put("is_leader", (island, superiorPlayer) ->
                            Formatters.BOOLEAN_FORMATTER.format(island.getOwner().equals(superiorPlayer), superiorPlayer.getUserLocale()))
                    .put("is_member", (island, superiorPlayer) -> Formatters.BOOLEAN_FORMATTER.format(island.isMember(superiorPlayer), superiorPlayer.getUserLocale()))
                    .put("is_coop", (island, superiorPlayer) -> Formatters.BOOLEAN_FORMATTER.format(island.isCoop(superiorPlayer), superiorPlayer.getUserLocale()))
                    .put("rating", (island, superiorPlayer) -> Formatters.NUMBER_FORMATTER.format(island.getTotalRating()))
                    .put("rating_amount", (island, superiorPlayer) -> Formatters.NUMBER_FORMATTER.format(island.getRatingAmount()))
                    .put("rating_stars", (island, superiorPlayer) ->
                            Formatters.RATING_FORMATTER.format(island.getTotalRating(), superiorPlayer.getUserLocale()))
                    .put("warps_limit", (island, superiorPlayer) -> island.getWarpsLimit() + "")
                    .put("warps", (island, superiorPlayer) -> island.getIslandWarps().size() + "")
                    .put("creation_time", (island, superiorPlayer) -> island.getCreationTimeDate() + "")
                    .put("total_worth", (island, superiorPlayer) ->
                            Formatters.NUMBER_FORMATTER.format(plugin.getGrid().getTotalWorth()))
                    .put("total_worth_format", (island, superiorPlayer) ->
                            Formatters.FANCY_NUMBER_FORMATTER.format(plugin.getGrid().getTotalWorth(), superiorPlayer.getUserLocale()))
                    .put("total_level", (island, superiorPlayer) ->
                            Formatters.NUMBER_FORMATTER.format(plugin.getGrid().getTotalLevel()))
                    .put("total_level_format", (island, superiorPlayer) ->
                            Formatters.FANCY_NUMBER_FORMATTER.format(plugin.getGrid().getTotalLevel(), superiorPlayer.getUserLocale()))
                    .put("nether_unlocked", (island, superiorPlayer) -> Formatters.BOOLEAN_FORMATTER.format(island.isNetherEnabled(), superiorPlayer.getUserLocale()))
                    .put("end_unlocked", (island, superiorPlayer) -> Formatters.BOOLEAN_FORMATTER.format(island.isEndEnabled(), superiorPlayer.getUserLocale()))
                    .put("visitors_count", (island, superiorPlayer) -> {
                        return island.getIslandVisitors(false).size() + "";
                    })
                    .put("bank_limit", (island, superiorPlayer) -> Formatters.NUMBER_FORMATTER.format(island.getBankLimit()))
                    .put("bank_limit_format", (island, superiorPlayer) ->
                            Formatters.FANCY_NUMBER_FORMATTER.format(island.getBankLimit(), superiorPlayer.getUserLocale()))
                    .put("uuid", (island, superiorPlayer) -> island.getUniqueId() + "")
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
                    Island island = isLocationPlaceholder ? plugin.getGrid().getIslandAt(superiorPlayer.getLocation()) :
                            superiorPlayer.getIsland();
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
                Island island = superiorPlayer == null ? null : subPlaceholder.startsWith("location_") ?
                        plugin.getGrid().getIslandAt(superiorPlayer.getLocation()) : superiorPlayer.getIsland();
                placeholderResult = parsePlaceholdersForIsland(island, superiorPlayer,
                        placeholder.replace("location_", ""),
                        subPlaceholder.replace("location_", ""));
            }
        }

        return placeholderResult.orElse(plugin.getSettings().getDefaultPlaceholders()
                .getOrDefault(placeholder, ""));
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
            if ((matcher = PERMISSION_ROLE_PLACEHOLDER_PATTERN.matcher(placeholder)).matches()) {
                return handlePermissionRolesPlaceholder(island, matcher.group(1));
            }

            if (superiorPlayer != null) {
                if ((matcher = PERMISSION_PLACEHOLDER_PATTERN.matcher(placeholder)).matches()) {
                    return handlePermissionsPlaceholder(island, superiorPlayer, matcher.group(1));
                } else if ((matcher = UPGRADE_PLACEHOLDER_PATTERN.matcher(placeholder)).matches()) {
                    String upgradeName = matcher.group(1);
                    return Optional.of(island.getUpgradeLevel(plugin.getUpgrades()
                            .getUpgrade(upgradeName)).getLevel() + "");
                } else if ((matcher = COUNT_PLACEHOLDER_PATTERN.matcher(placeholder)).matches()) {
                    String keyName = matcher.group(1);
                    return Optional.of(Formatters.NUMBER_FORMATTER.format(island
                            .getBlockCountAsBigInteger(Keys.ofMaterialAndData(keyName))));
                } else if ((matcher = BLOCK_LIMIT_PLACEHOLDER_PATTERN.matcher(placeholder)).matches()) {
                    String keyName = matcher.group(1);
                    return Optional.of(island.getBlockLimit(Keys.ofMaterialAndData(keyName)) + "");
                } else if ((matcher = ENTITY_LIMIT_PLACEHOLDER_PATTERN.matcher(placeholder)).matches()) {
                    String keyName = matcher.group(1);
                    return Optional.of(island.getEntityLimit(Keys.ofEntityType(keyName)) + "");
                } else if ((matcher = ENTITY_COUNT_PLACEHOLDER_PATTERN.matcher(placeholder)).matches()) {
                    String keyName = matcher.group(1);
                    return Optional.of(Formatters.NUMBER_FORMATTER.format(island.getEntitiesTracker().getEntityCount(Keys.ofEntityType(keyName))));
                } else if ((matcher = MEMBER_PLACEHOLDER_PATTERN.matcher(subPlaceholder)).matches()) {
                    return handleMembersPlaceholder(island, matcher.group(1));
                } else if ((matcher = VISITOR_LAST_JOIN_PLACEHOLDER_PATTERN.matcher(subPlaceholder)).matches()) {
                    String visitorName = matcher.group(1);
                    return Optional.of(island.getUniqueVisitorsWithTimes().stream()
                            .filter(uniqueVisitor -> uniqueVisitor.getKey().getName().equalsIgnoreCase(visitorName))
                            .findFirst()
                            .map(Pair::getValue).map(value -> Formatters.DATE_FORMATTER.format(new Date(value)))
                            .orElse("Haven't Joined"));
                } else if ((matcher = ISLAND_FLAG_PLACEHOLDER_PATTERN.matcher(subPlaceholder)).matches()) {
                    return handleIslandFlagsPlaceholder(island, matcher.group(1));
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

    private static Optional<String> handlePermissionRolesPlaceholder(@NotNull Island island,
                                                                     String placeholder) {
        try {
            IslandPrivilege islandPrivilege = IslandPrivilege.getByName(placeholder);
            return Optional.of(island.getRequiredPlayerRole(islandPrivilege).getDisplayName());
        } catch (NullPointerException ex) {
            return Optional.empty();
        }
    }

    private static Optional<String> handlePermissionsPlaceholder(@NotNull Island island,
                                                                 @NotNull SuperiorPlayer superiorPlayer,
                                                                 String placeholder) {
        try {
            IslandPrivilege islandPrivilege = IslandPrivilege.getByName(placeholder);
            return Optional.of(island.hasPermission(superiorPlayer, islandPrivilege) + "");
        } catch (NullPointerException ex) {
            return Optional.empty();
        }
    }

    private static Optional<String> handleIslandFlagsPlaceholder(@NotNull Island island, String placeholder) {
        try {
            IslandFlag islandFlag = IslandFlag.getByName(placeholder);
            return Optional.of(island.hasSettingsEnabled(islandFlag) + "");
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

}
