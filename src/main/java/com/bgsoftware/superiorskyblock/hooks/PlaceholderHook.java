package com.bgsoftware.superiorskyblock.hooks;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPermission;
import com.bgsoftware.superiorskyblock.api.island.SortingType;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.utils.islands.SortingTypes;
import com.bgsoftware.superiorskyblock.wrappers.SBlockPosition;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("WeakerAccess")
public abstract class PlaceholderHook {

    protected static SuperiorSkyblockPlugin plugin;

    public static void register(SuperiorSkyblockPlugin plugin){
        PlaceholderHook.plugin = plugin;

        if(Bukkit.getPluginManager().isPluginEnabled("MVdWPlaceholderAPI"))
            new PlaceholderHook_MVdW();
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI"))
            new PlaceholderHook_PAPI();
    }

    protected String parsePlaceholder(OfflinePlayer offlinePlayer, String placeholder) {
        try {
            Player player = offlinePlayer.isOnline() ? offlinePlayer.getPlayer() : null;
            SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(offlinePlayer.getUniqueId());
            Island island = superiorPlayer.getIsland();
            Matcher matcher;

            placeholder = placeholder.toLowerCase();

            if(placeholder.equals("superior_panel_toggle")){
                return superiorPlayer.hasToggledPanel() ? "Yes" : "No";
            }

            if ((matcher = Pattern.compile("island_(.+)").matcher(placeholder)).matches()) {
                String subPlaceholder = matcher.group(1).toLowerCase();

                if (subPlaceholder.startsWith("location_")) {
                    if(player == null)
                        throw new NullPointerException();

                    island = plugin.getGrid().getIslandAt(player.getLocation());

                    if (island == null)
                        return plugin.getSettings().defaultPlaceholders.getOrDefault(placeholder, "");

                    subPlaceholder = subPlaceholder.replace("location_", "");
                }

                if ((matcher = Pattern.compile("island_permission_(.+)").matcher(placeholder)).matches()) {
                    String permission = matcher.group(1);

                    try {
                        IslandPermission islandPermission = IslandPermission.valueOf(permission.toUpperCase());
                        return String.valueOf(island.hasPermission(superiorPlayer, islandPermission));
                    } catch (IllegalArgumentException ex) {
                        return "";
                    }
                }

                else if ((matcher = Pattern.compile("island_upgrade_(.+)").matcher(placeholder)).matches()) {
                    String upgradeName = matcher.group(1);
                    return String.valueOf(island.getUpgradeLevel(upgradeName));
                }

                else if ((matcher = Pattern.compile("island_count_(.+)").matcher(placeholder)).matches()) {
                    String keyName = matcher.group(1).toUpperCase();
                    return String.valueOf(island.getBlockCount(Key.of(keyName)));
                }

                else if ((matcher = Pattern.compile("island_top_(.+)").matcher(placeholder)).matches()) {
                    String topType = matcher.group(1);
                    SortingType sortingType;

                    if((matcher = Pattern.compile("worth_(.+)").matcher(topType)).matches()){
                        sortingType = SortingTypes.BY_WORTH;
                    }
                    else if((matcher = Pattern.compile("level_(.+)").matcher(topType)).matches()){
                        sortingType = SortingTypes.BY_LEVEL;
                    }
                    else if((matcher = Pattern.compile("rating_(.+)").matcher(topType)).matches()){
                        sortingType = SortingTypes.BY_RATING;
                    }
                    else if((matcher = Pattern.compile("players_(.+)").matcher(topType)).matches()){
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
                        boolean value = false;

                        if((matcher = Pattern.compile("value_(.+)").matcher(matcherValue)).matches()){
                            value = true;
                            matcherValue = matcher.group(1);
                        }

                        try {
                            int index = Integer.parseInt(matcherValue);
                            if (index > 0) {
                                Island _island = plugin.getGrid().getIsland(index - 1, sortingType);
                                assert _island != null;

                                if(value){
                                    if(sortingType.equals(SortingTypes.BY_WORTH)){
                                        return _island.getWorthAsBigDecimal().toString();
                                    }
                                    else if(sortingType.equals(SortingTypes.BY_LEVEL)){
                                        return _island.getIslandLevelAsBigDecimal().toString();
                                    }
                                    else if(sortingType.equals(SortingTypes.BY_RATING)){
                                        return StringUtils.format(_island.getTotalRating());
                                    }
                                    else if(sortingType.equals(SortingTypes.BY_PLAYERS)){
                                        return StringUtils.format(_island.getAllPlayersInside().size());
                                    }
                                }

                                else{
                                    return _island.getName().isEmpty() ? _island.getOwner().getName() : _island.getName();
                                }
                            }
                        } catch (IllegalArgumentException ignored) { }
                    }
                }

                else if ((matcher = Pattern.compile("island_member_(.+)").matcher(placeholder)).matches()) {
                    try{
                        int index = Integer.parseInt(matcher.group(1));
                        if(index > 0) {
                            List<SuperiorPlayer> members = island.getIslandMembers(false);
                            if(index < members.size())
                                return members.get(index - 1).getName();
                        }
                    }catch(IllegalArgumentException ignored){}
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
                    case "team_limit":
                        return String.valueOf(island.getTeamLimit());
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
                        return island.getIslandLevelAsBigDecimal().toString();
                    case "level_format":
                        return StringUtils.fancyFormat(island.getIslandLevelAsBigDecimal());
                    case "worth":
                        return island.getWorthAsBigDecimal().toString();
                    case "worth_format":
                        return StringUtils.fancyFormat(island.getWorthAsBigDecimal());
                    case "raw_worth":
                        return island.getRawWorthAsBigDecimal().toString();
                    case "raw_worth_format":
                        return StringUtils.fancyFormat(island.getRawWorthAsBigDecimal());
                    case "bank":
                        return island.getMoneyInBankAsBigDecimal().toString();
                    case "bank_format":
                        return StringUtils.fancyFormat(island.getMoneyInBankAsBigDecimal());
                    case "hoppers_limit":
                        return String.valueOf(island.getBlockLimit(Key.of("HOPPER")));
                    case "crops_multiplier":
                        return String.valueOf(island.getCropGrowthMultiplier());
                    case "spawners_multiplier":
                        return String.valueOf(island.getSpawnerRatesMultiplier());
                    case "drops_multiplier":
                        return String.valueOf(island.getMobDropsMultiplier());
                    case "discord":
                        return island.hasPermission(superiorPlayer, IslandPermission.DISCORD_SHOW) ? island.getDiscord() : "None";
                    case "paypal":
                        return island.hasPermission(superiorPlayer, IslandPermission.PAYPAL_SHOW) ? island.getPaypal() : "None";
                    case "discord_all":
                        return island.getDiscord();
                    case "paypal_all":
                        return island.getPaypal();
                    case "exists":
                        return island != null ? "Yes" : "No";
                    case "locked":
                        return island.isLocked() ? "Yes" : "No";
                    case "name":
                        return plugin.getSettings().islandNamesColorSupport ? ChatColor.translateAlternateColorCodes('&', island.getName()) : island.getName();
                    case "name_leader":
                        return island.getName().isEmpty() ? island.getOwner().getName() : plugin.getSettings().islandNamesColorSupport ? ChatColor.translateAlternateColorCodes('&', island.getName()) : island.getName();
                    case "is_leader":
                        return island.getOwner().equals(superiorPlayer) ? "Yes" : "No";
                    case "rating":
                        return StringUtils.format(island.getTotalRating());
                    case "rating_stars":
                        return StringUtils.formatRating(island.getTotalRating());
                }

            }
        }catch(NullPointerException ignored){}

        return plugin.getSettings().defaultPlaceholders.getOrDefault(placeholder, "");
    }

}
