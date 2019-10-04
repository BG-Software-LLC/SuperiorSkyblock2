package com.bgsoftware.superiorskyblock.hooks;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPermission;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.StringUtil;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.wrappers.SBlockPosition;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

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

    protected String parsePlaceholder(Player player, String placeholder) {
        try {
            SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(player);
            Island island = superiorPlayer.getIsland();
            Matcher matcher;

            if(placeholder.equalsIgnoreCase("superior_panel_toggle")){
                return superiorPlayer.hasToggledPanel() ? "Yes" : "No";
            }

            if ((matcher = Pattern.compile("island_(.+)").matcher(placeholder)).matches()) {
                String subPlaceholder = matcher.group(1).toLowerCase();

                if (island == null)
                    return subPlaceholder.equals("exists") ? "No" : plugin.getSettings().defaultPlaceholders.getOrDefault(placeholder, "");

                if (subPlaceholder.startsWith("location_")) {
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

                if ((matcher = Pattern.compile("island_upgrade_(.+)").matcher(placeholder)).matches()) {
                    String upgradeName = matcher.group(1);
                    return String.valueOf(island.getUpgradeLevel(upgradeName));
                }

                if ((matcher = Pattern.compile("island_count_(.+)").matcher(placeholder)).matches()) {
                    String keyName = matcher.group(1).toUpperCase();
                    return String.valueOf(island.getBlockCount(Key.of(keyName)));
                }

                if ((matcher = Pattern.compile("island_top_(.+)").matcher(placeholder)).matches()) {
                    try{
                        int index = Integer.valueOf(matcher.group(1));
                        return String.valueOf(plugin.getGrid().getIsland(index).getOwner().getName());
                    }catch(IllegalArgumentException ignored){}
                }

                switch (subPlaceholder) {
                    case "center":
                        return SBlockPosition.of(island.getCenter()).toString();
                    case "x":
                        return String.valueOf(island.getCenter().getBlockX());
                    case "y":
                        return String.valueOf(island.getCenter().getBlockY());
                    case "z":
                        return String.valueOf(island.getCenter().getBlockZ());
                    case "world":
                        return String.valueOf(island.getCenter().getWorld().getName());
                    case "team_size":
                        return String.valueOf(island.getAllMembers().size());
                    case "team_limit":
                        return String.valueOf(island.getTeamLimit());
                    case "leader":
                        return island.getOwner().getName();
                    case "size":
                        int size = island.getIslandSize() * 2;
                        return size + " x " + size;
                    case "radius":
                        return String.valueOf(island.getIslandSize());
                    case "biome":
                        return StringUtil.format(island.getBiome().name());
                    case "level":
                        return island.getIslandLevelAsBigDecimal().toString();
                    case "level_format":
                        return StringUtil.fancyFormat(island.getIslandLevelAsBigDecimal());
                    case "worth":
                        return island.getWorthAsBigDecimal().toString();
                    case "worth_format":
                        return StringUtil.fancyFormat(island.getWorthAsBigDecimal());
                    case "raw_worth":
                        return island.getRawWorthAsBigDecimal().toString();
                    case "raw_worth_format":
                        return StringUtil.fancyFormat(island.getRawWorthAsBigDecimal());
                    case "bank":
                        return island.getMoneyInBankAsBigDecimal().toString();
                    case "bank_format":
                        return StringUtil.fancyFormat(island.getMoneyInBankAsBigDecimal());
                    case "hoppers_limit":
                        return String.valueOf(island.getHoppersLimit());
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
                        return "Yes";
                    case "locked":
                        return island.isLocked() ? "Yes" : "No";
                    case "name":
                        return plugin.getSettings().islandNamesColorSupport ? ChatColor.translateAlternateColorCodes('&', island.getName()) : island.getName();
                    case "is_leader":
                        return island.getOwner().equals(superiorPlayer) ? "Yes" : "No";
                    case "rating":
                        return StringUtil.format(island.getTotalRating());
                    case "rating_stars":
                        return StringUtil.formatRating(island.getTotalRating());
                }

            }
        }catch(NullPointerException ignored){}

        return plugin.getSettings().defaultPlaceholders.getOrDefault(placeholder, "");
    }

}
