package com.bgsoftware.superiorskyblock.hooks.support;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandFlag;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.island.SortingType;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.hooks.provider.PlaceholdersProvider;
import com.bgsoftware.superiorskyblock.island.permissions.IslandPrivileges;
import com.bgsoftware.superiorskyblock.key.ConstantKeys;
import com.bgsoftware.superiorskyblock.key.Key;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.islands.SortingTypes;
import com.bgsoftware.superiorskyblock.wrappers.SBlockPosition;
import com.google.common.collect.ImmutableMap;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("WeakerAccess")
public abstract class PlaceholderHook {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private static final Pattern ISLAND_PLACEHOLDER_PATTERN = Pattern.compile("island_(.+)");
    private static final Pattern PLAYER_PLACEHOLDER_PATTERN = Pattern.compile("player_(.+)");
    private static final Pattern PERMISSION_PLACEHOLDER_PATTERN = Pattern.compile("island_permission_(.+)");
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

    private static final Map<String, PlayerPlaceholderParser> PLAYER_PARSES =
            new ImmutableMap.Builder<String, PlayerPlaceholderParser>()
                    .put("texture", SuperiorPlayer::getTextureValue)
                    .put("role", superiorPlayer -> superiorPlayer.getPlayerRole().toString())
                    .put("locale", superiorPlayer -> StringUtils.format(superiorPlayer.getUserLocale()))
                    .put("world_border", superiorPlayer -> superiorPlayer.hasWorldBorderEnabled() ? "Yes" : "No")
                    .put("blocks_stacker", superiorPlayer -> superiorPlayer.hasBlocksStackerEnabled() ? "Yes" : "No")
                    .put("schematics", superiorPlayer -> superiorPlayer.hasSchematicModeEnabled() ? "Yes" : "No")
                    .put("team_chat", superiorPlayer -> superiorPlayer.hasTeamChatEnabled() ? "Yes" : "No")
                    .put("bypass", superiorPlayer -> superiorPlayer.hasBypassModeEnabled() ? "Yes" : "No")
                    .put("disbands", superiorPlayer -> superiorPlayer.getDisbands() + "")
                    .put("panel", superiorPlayer -> superiorPlayer.hasToggledPanel() ? "Yes" : "No")
                    .put("fly", superiorPlayer -> superiorPlayer.hasIslandFlyEnabled() ? "Yes" : "No")
                    .put("chat_spy", superiorPlayer -> superiorPlayer.hasAdminSpyEnabled() ? "Yes" : "No")
                    .put("border_color", superiorPlayer ->
                            StringUtils.format(superiorPlayer.getUserLocale(), superiorPlayer.getBorderColor()))
                    .put("missions_completed", superiorPlayer -> superiorPlayer.getCompletedMissions().size() + "")
                    .build();

    private static final Map<String, IslandPlaceholderParser> ISLAND_PARSES =
            new ImmutableMap.Builder<String, IslandPlaceholderParser>()
                    .put("center", (island, superiorPlayer) ->
                            SBlockPosition.of(island.getCenter(plugin.getSettings().getWorlds().getDefaultWorld())).toString())
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
                    .put("biome", (island, superiorPlayer) -> StringUtils.format(island.getBiome().name()))
                    .put("level", (island, superiorPlayer) -> StringUtils.format(island.getIslandLevel()))
                    .put("level_raw", (island, superiorPlayer) -> island.getIslandLevel().toString())
                    .put("level_format", (island, superiorPlayer) ->
                            StringUtils.fancyFormat(island.getIslandLevel(), superiorPlayer.getUserLocale()))
                    .put("level_int", (island, superiorPlayer) -> island.getIslandLevel().toBigInteger().toString())
                    .put("worth", (island, superiorPlayer) -> StringUtils.format(island.getWorth()))
                    .put("worth_raw", (island, superiorPlayer) -> island.getWorth().toString())
                    .put("worth_format", (island, superiorPlayer) ->
                            StringUtils.fancyFormat(island.getWorth(), superiorPlayer.getUserLocale()))
                    .put("worth_int", (island, superiorPlayer) -> island.getWorth().toBigInteger().toString())
                    .put("raw_worth", (island, superiorPlayer) -> StringUtils.format(island.getRawWorth()))
                    .put("raw_worth_format", (island, superiorPlayer) ->
                            StringUtils.fancyFormat(island.getRawWorth(), superiorPlayer.getUserLocale()))
                    .put("bank", (island, superiorPlayer) -> StringUtils.format(island.getIslandBank().getBalance()))
                    .put("bank_raw", (island, superiorPlayer) -> island.getIslandBank().getBalance().toString())
                    .put("bank_format", (island, superiorPlayer) ->
                            StringUtils.fancyFormat(island.getIslandBank().getBalance(), superiorPlayer.getUserLocale()))
                    .put("bank_next_interest", (island, superiorPlayer) ->
                            StringUtils.formatTime(superiorPlayer.getUserLocale(), island.getNextInterest(), TimeUnit.SECONDS))
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
                    .put("exists", (island, superiorPlayer) -> "Yes")
                    .put("locked", (island, superiorPlayer) -> island.isLocked() ? "Yes" : "No")
                    .put("name", (island, superiorPlayer) -> {
                        return plugin.getSettings().getIslandNames().isColorSupport() ?
                                StringUtils.translateColors(island.getName()) : island.getName();
                    })
                    .put("name_leader", (island, superiorPlayer) -> {
                        return island.getName().isEmpty() ? island.getOwner().getName() :
                                plugin.getSettings().getIslandNames().isColorSupport() ?
                                        StringUtils.translateColors(island.getName()) : island.getName();
                    })
                    .put("is_leader", (island, superiorPlayer) ->
                            island.getOwner().equals(superiorPlayer) ? "Yes" : "No")
                    .put("is_member", (island, superiorPlayer) -> island.isMember(superiorPlayer) ? "Yes" : "No")
                    .put("is_coop", (island, superiorPlayer) -> island.isCoop(superiorPlayer) ? "Yes" : "No")
                    .put("rating", (island, superiorPlayer) -> StringUtils.format(island.getTotalRating()))
                    .put("rating_amount", (island, superiorPlayer) -> StringUtils.format(island.getRatingAmount()))
                    .put("rating_stars", (island, superiorPlayer) ->
                            StringUtils.formatRating(superiorPlayer.getUserLocale(), island.getTotalRating()))
                    .put("warps_limit", (island, superiorPlayer) -> island.getWarpsLimit() + "")
                    .put("warps", (island, superiorPlayer) -> island.getIslandWarps().size() + "")
                    .put("creation_time", (island, superiorPlayer) -> island.getCreationTimeDate() + "")
                    .put("total_worth", (island, superiorPlayer) ->
                            StringUtils.format(plugin.getGrid().getTotalWorth()))
                    .put("total_worth_format", (island, superiorPlayer) ->
                            StringUtils.fancyFormat(plugin.getGrid().getTotalWorth(), superiorPlayer.getUserLocale()))
                    .put("total_level", (island, superiorPlayer) ->
                            StringUtils.format(plugin.getGrid().getTotalLevel()))
                    .put("total_level_format", (island, superiorPlayer) ->
                            StringUtils.fancyFormat(plugin.getGrid().getTotalLevel(), superiorPlayer.getUserLocale()))
                    .put("nether_unlocked", (island, superiorPlayer) -> island.isNetherEnabled() ? "Yes" : "No")
                    .put("end_unlocked", (island, superiorPlayer) -> island.isEndEnabled() ? "Yes" : "No")
                    .put("visitors_count", (island, superiorPlayer) -> {
                        return island.getIslandVisitors(false).size() + "";
                    })
                    .put("bank_limit", (island, superiorPlayer) -> StringUtils.format(island.getBankLimit()))
                    .put("bank_limit_format", (island, superiorPlayer) ->
                            StringUtils.fancyFormat(island.getBankLimit(), superiorPlayer.getUserLocale()))
                    .build();

    private static final Map<SortingType, BiFunction<Island, SuperiorPlayer, String>> TOP_VALUE_FORMAT_FUNCTIONS =
            new ImmutableMap.Builder<SortingType, BiFunction<Island, SuperiorPlayer, String>>()
                    .put(SortingTypes.BY_WORTH, (targetIsland, superiorPlayer) ->
                            StringUtils.fancyFormat(targetIsland.getWorth(), superiorPlayer.getUserLocale()))
                    .put(SortingTypes.BY_LEVEL, (targetIsland, superiorPlayer) ->
                            StringUtils.fancyFormat(targetIsland.getIslandLevel(), superiorPlayer.getUserLocale()))
                    .put(SortingTypes.BY_RATING, (targetIsland, superiorPlayer) ->
                            StringUtils.format(targetIsland.getTotalRating()))
                    .put(SortingTypes.BY_PLAYERS, (targetIsland, superiorPlayer) ->
                            StringUtils.format(targetIsland.getAllPlayersInside().size()))
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
                    .put(SortingTypes.BY_WORTH, targetIsland -> StringUtils.format(targetIsland.getWorth()))
                    .put(SortingTypes.BY_LEVEL, targetIsland -> StringUtils.format(targetIsland.getIslandLevel()))
                    .put(SortingTypes.BY_RATING, targetIsland -> StringUtils.format(targetIsland.getTotalRating()))
                    .put(SortingTypes.BY_PLAYERS, targetIsland -> StringUtils.format(targetIsland.getAllPlayersInside().size()))
                    .build();

    private static List<PlaceholdersProvider> placeholdersProviders;

    protected PlaceholderHook() {
    }

    public static void register(List<PlaceholdersProvider> placeholdersProviders) {
        PlaceholderHook.placeholdersProviders = placeholdersProviders;
    }

    public static String parse(SuperiorPlayer superiorPlayer, String str) {
        return parse(superiorPlayer.asOfflinePlayer(), str);
    }

    public static String parse(OfflinePlayer offlinePlayer, String str) {
        for (PlaceholdersProvider placeholdersProvider : placeholdersProviders)
            str = placeholdersProvider.parsePlaceholder(offlinePlayer, str);

        return str;
    }

    protected final String handlePluginPlaceholder(OfflinePlayer offlinePlayer, String placeholder) {
        Player player = offlinePlayer.getPlayer();
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(offlinePlayer.getUniqueId());

        Optional<String> placeholderResult = Optional.empty();
        Matcher matcher;

        if ((matcher = PLAYER_PLACEHOLDER_PATTERN.matcher(placeholder)).matches()) {
            placeholderResult = Optional.ofNullable(PLAYER_PARSES.get(matcher.group(1)))
                    .map(placeholderParser -> placeholderParser.apply(superiorPlayer));
        } else if ((matcher = ISLAND_PLACEHOLDER_PATTERN.matcher(placeholder)).matches()) {
            String subPlaceholder = matcher.group(1).toLowerCase();

            Island island = subPlaceholder.startsWith("location_") && player != null ?
                    plugin.getGrid().getIslandAt(player.getLocation()) : superiorPlayer.getIsland();

            subPlaceholder = subPlaceholder.replace("location_", "");

            placeholderResult = parsePlaceholdersForIsland(island, superiorPlayer, placeholder, subPlaceholder);
        }

        return placeholderResult.orElse(plugin.getSettings().getDefaultPlaceholders()
                .getOrDefault(placeholder, ""));
    }

    private static Optional<String> parsePlaceholdersForIsland(Island island, SuperiorPlayer superiorPlayer,
                                                               String placeholder, String subPlaceholder) {
        Matcher matcher;

        if (island == null) {
            return subPlaceholder.equals("exists") ? Optional.of("No") : Optional.empty();
        } else if ((matcher = PERMISSION_PLACEHOLDER_PATTERN.matcher(placeholder)).matches()) {
            return handlePermissionsPlaceholder(island, superiorPlayer, matcher.group(1));
        } else if ((matcher = UPGRADE_PLACEHOLDER_PATTERN.matcher(placeholder)).matches()) {
            String upgradeName = matcher.group(1);
            return Optional.of(island.getUpgradeLevel(plugin.getUpgrades()
                    .getUpgrade(upgradeName)).getLevel() + "");
        } else if ((matcher = COUNT_PLACEHOLDER_PATTERN.matcher(placeholder)).matches()) {
            String keyName = matcher.group(1).toUpperCase();
            return Optional.of(StringUtils.format(island
                    .getBlockCountAsBigInteger(Key.of(keyName))));
        } else if ((matcher = BLOCK_LIMIT_PLACEHOLDER_PATTERN.matcher(placeholder)).matches()) {
            String keyName = matcher.group(1).toUpperCase();
            return Optional.of(island.getBlockLimit(Key.of(keyName)) + "");
        } else if ((matcher = ENTITY_LIMIT_PLACEHOLDER_PATTERN.matcher(placeholder)).matches()) {
            String keyName = matcher.group(1).toUpperCase();
            return Optional.of(island.getEntityLimit(EntityType.valueOf(keyName)) + "");
        } else if ((matcher = ENTITY_COUNT_PLACEHOLDER_PATTERN.matcher(placeholder)).matches()) {
            String keyName = matcher.group(1).toUpperCase();
            return Optional.of(StringUtils.format(island.getEntitiesTracker().getEntityCount(Key.of(keyName))));
        } else if ((matcher = TOP_PLACEHOLDER_PATTERN.matcher(placeholder)).matches()) {
            return handleTopIslandsPlaceholder(island, superiorPlayer, matcher.group(1));
        } else if ((matcher = MEMBER_PLACEHOLDER_PATTERN.matcher(subPlaceholder)).matches()) {
            return handleMembersPlaceholder(island, matcher.group(1));
        } else if ((matcher = VISITOR_LAST_JOIN_PLACEHOLDER_PATTERN.matcher(subPlaceholder)).matches()) {
            String visitorName = matcher.group(1);
            return Optional.of(island.getUniqueVisitorsWithTimes().stream()
                    .filter(uniqueVisitor -> uniqueVisitor.getKey().getName().equalsIgnoreCase(visitorName))
                    .findFirst()
                    .map(Pair::getValue).map(StringUtils::formatDate)
                    .orElse("Haven't Joined"));
        } else if ((matcher = ISLAND_FLAG_PLACEHOLDER_PATTERN.matcher(subPlaceholder)).matches()) {
            return handleIslandFlagsPlaceholder(island, matcher.group(1));
        } else {
            return Optional.ofNullable(ISLAND_PARSES.get(subPlaceholder))
                    .map(placeholderParser -> placeholderParser.apply(island, superiorPlayer));
        }
    }

    private static Optional<String> handlePermissionsPlaceholder(Island island, SuperiorPlayer superiorPlayer,
                                                                 String placeholder) {
        try {
            IslandPrivilege islandPrivilege = IslandPrivilege.getByName(placeholder);
            return Optional.of(island.hasPermission(superiorPlayer, islandPrivilege) + "");
        } catch (NullPointerException ex) {
            return Optional.empty();
        }
    }

    private static Optional<String> handleIslandFlagsPlaceholder(Island island, String placeholder) {
        try {
            IslandFlag islandFlag = IslandFlag.getByName(placeholder);
            return Optional.of(island.hasSettingsEnabled(islandFlag) + "");
        } catch (NullPointerException ex) {
            return Optional.empty();
        }
    }

    private static Optional<String> handleTopIslandsPlaceholder(Island island, SuperiorPlayer superiorPlayer,
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
            return Optional.of((plugin.getGrid().getIslandPosition(island, sortingType) + 1) + "");

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

    private static Optional<String> handleMembersPlaceholder(Island island, String placeholder) {
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

    private interface PlayerPlaceholderParser extends Function<SuperiorPlayer, String> {

    }

    private interface IslandPlaceholderParser extends BiFunction<Island, SuperiorPlayer, String> {

    }

}
