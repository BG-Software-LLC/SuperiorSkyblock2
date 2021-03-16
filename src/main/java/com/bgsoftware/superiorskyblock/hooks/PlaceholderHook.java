package com.bgsoftware.superiorskyblock.hooks;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.island.SortingType;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.island.SpawnIsland;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.islands.IslandPrivileges;
import com.bgsoftware.superiorskyblock.utils.islands.SortingTypes;
import com.bgsoftware.superiorskyblock.utils.key.ConstantKeys;
import com.bgsoftware.superiorskyblock.utils.key.Key;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import com.bgsoftware.superiorskyblock.wrappers.SBlockPosition;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("WeakerAccess")
public abstract class PlaceholderHook {

    protected static SuperiorSkyblockPlugin plugin;
    private static boolean PlaceholderAPI = false;

    private static final Pattern ISLAND_PLACEHOLDER_PATTERN = Pattern.compile("island_(.+)");
    private static final Pattern PLAYER_PLACEHOLDER_PATTERN = Pattern.compile("player_(.+)");
    private static final Pattern PERMISSION_PLACEHOLDER_PATTERN = Pattern.compile("island_permission_(.+)");
    private static final Pattern UPGRADE_PLACEHOLDER_PATTERN = Pattern.compile("island_upgrade_(.+)");
    private static final Pattern COUNT_PLACEHOLDER_PATTERN = Pattern.compile("island_count_(.+)");
    private static final Pattern BLOCK_LIMIT_PLACEHOLDER_PATTERN = Pattern.compile("island_block_limit_(.+)");
    private static final Pattern ENTITY_LIMIT_PLACEHOLDER_PATTERN = Pattern.compile("island_entity_limit_(.+)");
    private static final Pattern TOP_PLACEHOLDER_PATTERN = Pattern.compile("island_top_(.+)");
    private static final Pattern TOP_WORTH_PLACEHOLDER_PATTERN = Pattern.compile("worth_(.+)");
    private static final Pattern TOP_LEVEL_PLACEHOLDER_PATTERN = Pattern.compile("level_(.+)");
    private static final Pattern TOP_RATING_PLACEHOLDER_PATTERN = Pattern.compile("rating_(.+)");
    private static final Pattern TOP_PLAYERS_PLACEHOLDER_PATTERN = Pattern.compile("players_(.+)");
    private static final Pattern TOP_VALUE_FORMAT_PLACEHOLDER_PATTERN = Pattern.compile("value_format_(.+)");
    private static final Pattern TOP_VALUE_RAW_PLACEHOLDER_PATTERN = Pattern.compile("value_raw_(.+)");
    private static final Pattern TOP_VALUE_PLACEHOLDER_PATTERN = Pattern.compile("value_(.+)");
    private static final Pattern TOP_LEADER_PLACEHOLDER_PATTERN = Pattern.compile("leader_(.+)");
    private static final Pattern MEMBER_PLACEHOLDER_PATTERN = Pattern.compile("member_(.+)");
    private static final Pattern VISITOR_LAST_JOIN_PLACEHOLDER_PATTERN = Pattern.compile("visitor_last_join_(.+)");

    public static void register(SuperiorSkyblockPlugin plugin){
        PlaceholderHook.plugin = plugin;

        Executor.ensureMain(() -> {
            if(Bukkit.getPluginManager().isPluginEnabled("MVdWPlaceholderAPI")) {
                new PlaceholderHook_MVdW();
            }
            if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                new PlaceholderHook_PAPI();
                PlaceholderAPI = true;
            }
        });
    }

    public static String parse(SuperiorPlayer superiorPlayer, String str){
        return parse(superiorPlayer.asOfflinePlayer(), str);
    }

    public static String parse(OfflinePlayer offlinePlayer, String str){
        if (PlaceholderAPI && str.contains("%"))
            str = PlaceholderHook_PAPI.parse(offlinePlayer, str);

        return str;
    }

    protected String parsePlaceholder(OfflinePlayer offlinePlayer, String placeholder) {
        Player player = offlinePlayer.isOnline() ? offlinePlayer.getPlayer() : null;
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(offlinePlayer.getUniqueId());
        Island island = superiorPlayer.getIsland();

        // Island may be null, however we catch NPE.
        assert island != null;

        try {
            Matcher matcher;

            placeholder = placeholder.toLowerCase();

            if((matcher = PLAYER_PLACEHOLDER_PATTERN.matcher(placeholder)).matches()){
                String subPlaceholder = matcher.group(1).toLowerCase();
                switch (subPlaceholder){
                    case "texture":
                        return superiorPlayer.getTextureValue();
                    case "role":
                        return superiorPlayer.getPlayerRole().toString();
                    case "locale":
                        return StringUtils.format(superiorPlayer.getUserLocale());
                    case "world_border":
                        return superiorPlayer.hasWorldBorderEnabled() ? "Yes" : "No";
                    case "blocks_stacker":
                        return superiorPlayer.hasBlocksStackerEnabled() ? "Yes" : "No";
                    case "schematics":
                        return superiorPlayer.hasSchematicModeEnabled() ? "Yes" : "No";
                    case "team_chat":
                        return superiorPlayer.hasTeamChatEnabled() ? "Yes" : "No";
                    case "bypass":
                        return superiorPlayer.hasBypassModeEnabled() ? "Yes" : "No";
                    case "disbands":
                        return String.valueOf(superiorPlayer.getDisbands());
                    case "panel":
                        return superiorPlayer.hasToggledPanel() ? "Yes" : "No";
                    case "fly":
                        return superiorPlayer.hasIslandFlyEnabled() ? "Yes" : "No";
                    case "chat_spy":
                        return superiorPlayer.hasAdminSpyEnabled() ? "Yes" : "No";
                    case "border_color":
                        return superiorPlayer.getBorderColor().name();
                    case "missions_completed":
                        return String.valueOf(superiorPlayer.getCompletedMissions().size());
                }
            }

            else if ((matcher = ISLAND_PLACEHOLDER_PATTERN.matcher(placeholder)).matches()) {
                String subPlaceholder = matcher.group(1).toLowerCase();

                if (subPlaceholder.startsWith("location_")) {
                    if(player == null)
                        throw new NullPointerException();

                    island = plugin.getGrid().getIslandAt(player.getLocation());

                    if (island == null || island instanceof SpawnIsland)
                        return plugin.getSettings().defaultPlaceholders.get(placeholder, "");

                    subPlaceholder = subPlaceholder.replace("location_", "");
                }

                if ((matcher = PERMISSION_PLACEHOLDER_PATTERN.matcher(placeholder)).matches()) {
                    String permission = matcher.group(1);

                    try {
                        IslandPrivilege islandPermission = IslandPrivilege.getByName(permission);
                        return String.valueOf(island.hasPermission(superiorPlayer, islandPermission));
                    } catch (IllegalArgumentException ex) {
                        return "";
                    }
                }

                else if ((matcher = UPGRADE_PLACEHOLDER_PATTERN.matcher(placeholder)).matches()) {
                    String upgradeName = matcher.group(1);
                    return String.valueOf(island.getUpgradeLevel(plugin.getUpgrades().getUpgrade(upgradeName)).getLevel());
                }

                else if ((matcher = COUNT_PLACEHOLDER_PATTERN.matcher(placeholder)).matches()) {
                    String keyName = matcher.group(1).toUpperCase();
                    return StringUtils.format(island.getBlockCountAsBigInteger(Key.of(keyName)));
                }

                else if ((matcher = BLOCK_LIMIT_PLACEHOLDER_PATTERN.matcher(placeholder)).matches()) {
                    String keyName = matcher.group(1).toUpperCase();
                    return String.valueOf(island.getBlockLimit(Key.of(keyName)));
                }

                else if ((matcher = ENTITY_LIMIT_PLACEHOLDER_PATTERN.matcher(placeholder)).matches()) {
                    String keyName = matcher.group(1).toUpperCase();
                    return String.valueOf(island.getEntityLimit(EntityType.valueOf(keyName)));
                }

                else if ((matcher = TOP_PLACEHOLDER_PATTERN.matcher(placeholder)).matches()) {
                    String topType = matcher.group(1);
                    SortingType sortingType;

                    if((matcher = TOP_WORTH_PLACEHOLDER_PATTERN.matcher(topType)).matches()){
                        sortingType = SortingTypes.BY_WORTH;
                    }
                    else if((matcher = TOP_LEVEL_PLACEHOLDER_PATTERN.matcher(topType)).matches()){
                        sortingType = SortingTypes.BY_LEVEL;
                    }
                    else if((matcher = TOP_RATING_PLACEHOLDER_PATTERN.matcher(topType)).matches()){
                        sortingType = SortingTypes.BY_RATING;
                    }
                    else if((matcher = TOP_PLAYERS_PLACEHOLDER_PATTERN.matcher(topType)).matches()){
                        sortingType = SortingTypes.BY_PLAYERS;
                    }
                    else{
                        throw new NullPointerException("Cannot find valid top type.");
                    }

                    String matcherValue = matcher.group(1);

                    if(matcherValue.equals("position")){
                        return String.valueOf(plugin.getGrid().getIslandPosition(island, sortingType) + 1);
                    }
                    else {
                        boolean value = false, formattedValue = false, rawValue = false, leader = false;

                        if((matcher = TOP_VALUE_FORMAT_PLACEHOLDER_PATTERN.matcher(matcherValue)).matches()){
                            value = true;
                            formattedValue = true;
                            matcherValue = matcher.group(1);
                        }

                        else if((matcher = TOP_VALUE_RAW_PLACEHOLDER_PATTERN.matcher(matcherValue)).matches()){
                            value = true;
                            rawValue = true;
                            matcherValue = matcher.group(1);
                        }

                        else if((matcher = TOP_VALUE_PLACEHOLDER_PATTERN.matcher(matcherValue)).matches()){
                            value = true;
                            matcherValue = matcher.group(1);
                        }

                        else if((matcher = TOP_LEADER_PLACEHOLDER_PATTERN.matcher(matcherValue)).matches()){
                            leader = true;
                            matcherValue = matcher.group(1);
                        }

                        try {
                            int index = Integer.parseInt(matcherValue);
                            if (index > 0) {
                                Island _island = plugin.getGrid().getIsland(index - 1, sortingType);
                                assert _island != null;

                                if(value){
                                    if(sortingType.equals(SortingTypes.BY_WORTH)){
                                        return formattedValue ? StringUtils.fancyFormat(_island.getWorth(), superiorPlayer.getUserLocale()) :
                                                rawValue ? _island.getWorth().toString() : StringUtils.format(_island.getWorth());
                                    }
                                    else if(sortingType.equals(SortingTypes.BY_LEVEL)){
                                        return formattedValue ? StringUtils.fancyFormat(_island.getIslandLevel(), superiorPlayer.getUserLocale()) :
                                                rawValue ? _island.getIslandLevel().toString() : StringUtils.format(_island.getIslandLevel());
                                    }
                                    else if(sortingType.equals(SortingTypes.BY_RATING)){
                                        return StringUtils.format(_island.getTotalRating());
                                    }
                                    else if(sortingType.equals(SortingTypes.BY_PLAYERS)){
                                        return StringUtils.format(_island.getAllPlayersInside().size());
                                    }
                                }

                                else{
                                    return leader || _island.getName().isEmpty() ? _island.getOwner().getName() : _island.getName();
                                }
                            }
                        } catch (IllegalArgumentException ignored) { }
                    }
                }

                else if ((matcher = MEMBER_PLACEHOLDER_PATTERN.matcher(subPlaceholder)).matches()) {
                    try{
                        int index = Integer.parseInt(matcher.group(1)) - 1;
                        if(index >= 0) {
                            List<SuperiorPlayer> members = island.getIslandMembers(false);
                            if(index < members.size())
                                return members.get(index).getName();
                        }
                    }catch(IllegalArgumentException ignored){}
                }

                else if ((matcher = VISITOR_LAST_JOIN_PLACEHOLDER_PATTERN.matcher(subPlaceholder)).matches()) {
                    String visitorName = matcher.group(1);

                    Pair<SuperiorPlayer, Long> visitorData = island.getUniqueVisitorsWithTimes().stream().filter(pair -> pair.getKey().getName().equalsIgnoreCase(visitorName))
                            .findFirst().orElse(null);

                    return visitorData == null ? "Haven't Joined" : StringUtils.formatDate(visitorData.getValue());
                }

                else switch (subPlaceholder) {
                    case "center":
                        return SBlockPosition.of(island.getCenter(World.Environment.NORMAL)).toString();
                    case "x":
                        return String.valueOf(island.getCenter(World.Environment.NORMAL).getBlockX());
                    case "y":
                        return String.valueOf(island.getCenter(World.Environment.NORMAL).getBlockY());
                    case "z":
                        return String.valueOf(island.getCenter(World.Environment.NORMAL).getBlockZ());
                    case "world":
                        return island.getCenter(World.Environment.NORMAL).getWorld().getName();
                    case "team_size":
                        return String.valueOf(island.getIslandMembers(true).size());
                    case "team_size_online":
                        return String.valueOf(island.getIslandMembers(true).stream().filter(SuperiorPlayer::isOnline).count());
                    case "team_limit":
                        return String.valueOf(island.getTeamLimit());
                    case "coop_limit":
                        return String.valueOf(island.getCoopLimit());
                    case "leader":
                        return island.getOwner().getName();
                    case "size_format":
                    case "size":
                        int size = island.getIslandSize() * 2 + 1, rounded = 5 * (Math.round(size / 5.0F));
                        if(subPlaceholder.contains("format") && Math.abs(size - rounded) == 1)
                            size = rounded;
                        return size + " x " + size;
                    case "radius":
                        return String.valueOf(island.getIslandSize());
                    case "biome":
                        return StringUtils.format(island.getBiome().name());
                    case "level":
                        return StringUtils.format(island.getIslandLevel());
                    case "level_raw":
                        return island.getIslandLevel().toString();
                    case "level_format":
                        return StringUtils.fancyFormat(island.getIslandLevel(), superiorPlayer.getUserLocale());
                    case "level_int":
                        return island.getIslandLevel().toBigInteger().toString();
                    case "worth":
                        return StringUtils.format(island.getWorth());
                    case "worth_raw":
                        return island.getWorth().toString();
                    case "worth_format":
                        return StringUtils.fancyFormat(island.getWorth(), superiorPlayer.getUserLocale());
                    case "worth_int":
                        return island.getWorth().toBigInteger().toString();
                    case "raw_worth":
                        return StringUtils.format(island.getRawWorth());
                    case "raw_worth_format":
                        return StringUtils.fancyFormat(island.getRawWorth(), superiorPlayer.getUserLocale());
                    case "bank":
                        return StringUtils.format(island.getIslandBank().getBalance());
                    case "bank_raw":
                        return island.getIslandBank().getBalance().toString();
                    case "bank_format":
                        return StringUtils.fancyFormat(island.getIslandBank().getBalance(), superiorPlayer.getUserLocale());
                    case "bank_next_interest":
                        return StringUtils.formatTime(superiorPlayer.getUserLocale(), island.getNextInterest());
                    case "hoppers_limit":
                        return String.valueOf(island.getBlockLimit(ConstantKeys.HOPPER));
                    case "crops_multiplier":
                        return String.valueOf(island.getCropGrowthMultiplier());
                    case "spawners_multiplier":
                        return String.valueOf(island.getSpawnerRatesMultiplier());
                    case "drops_multiplier":
                        return String.valueOf(island.getMobDropsMultiplier());
                    case "discord":
                        return island.hasPermission(superiorPlayer, IslandPrivileges.DISCORD_SHOW) ? island.getDiscord() : "None";
                    case "paypal":
                        return island.hasPermission(superiorPlayer, IslandPrivileges.PAYPAL_SHOW) ? island.getPaypal() : "None";
                    case "discord_all":
                        return island.getDiscord();
                    case "paypal_all":
                        return island.getPaypal();
                    case "exists":
                        return island != null ? "Yes" : "No";
                    case "locked":
                        return island.isLocked() ? "Yes" : "No";
                    case "name":
                        return plugin.getSettings().islandNamesColorSupport ? StringUtils.translateColors(island.getName()) : island.getName();
                    case "name_leader":
                        return island.getName().isEmpty() ? island.getOwner().getName() : plugin.getSettings().islandNamesColorSupport ?
                                StringUtils.translateColors(island.getName()) : island.getName();
                    case "is_leader":
                        return island.getOwner().equals(superiorPlayer) ? "Yes" : "No";
                    case "is_member":
                        return island.isMember(superiorPlayer) ? "Yes" : "No";
                    case "is_coop":
                        return island.isCoop(superiorPlayer) ? "Yes" : "No";
                    case "rating":
                        return StringUtils.format(island.getTotalRating());
                    case "rating_stars":
                        return StringUtils.formatRating(superiorPlayer.getUserLocale(), island.getTotalRating());
                    case "warps_limit":
                        return String.valueOf(island.getWarpsLimit());
                    case "warps":
                        return String.valueOf(island.getIslandWarps().size());
                    case "creation_time":
                        return island.getCreationTimeDate();
                    case "total_worth":
                        return StringUtils.format(plugin.getGrid().getTotalWorth());
                    case "total_worth_format":
                        return StringUtils.fancyFormat(plugin.getGrid().getTotalWorth(), superiorPlayer.getUserLocale());
                    case "total_level":
                        return StringUtils.format(plugin.getGrid().getTotalLevel());
                    case "total_level_format":
                        return StringUtils.fancyFormat(plugin.getGrid().getTotalLevel(), superiorPlayer.getUserLocale());
                    case "nether_unlocked":
                        return island.isNetherEnabled() ? "Yes" : "No";
                    case "end_unlocked":
                        return island.isEndEnabled() ? "Yes" : "No";
                }

            }
        }catch(NullPointerException ignored){}

        return plugin.getSettings().defaultPlaceholders.get(placeholder, "");
    }

}
