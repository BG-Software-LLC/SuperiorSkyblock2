package com.bgsoftware.superiorskyblock.hooks;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPermission;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.StringUtil;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import com.bgsoftware.superiorskyblock.wrappers.SBlockPosition;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import me.clip.placeholderapi.external.EZPlaceholderHook;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("deprecation")
public final class PlaceholderHook_PAPI extends EZPlaceholderHook {

    private SuperiorSkyblockPlugin plugin;

    private PlaceholderHook_PAPI(SuperiorSkyblockPlugin plugin){
        super(plugin, "superior");
        this.plugin = plugin;
        hook();
    }

    @Override
    public String onPlaceholderRequest(Player player, String placeholder) {
        try {
            SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(player);
            Island island = superiorPlayer.getIsland();
            Matcher matcher;

            if ((matcher = Pattern.compile("island_(.+)").matcher(placeholder)).matches()) {
                String subPlaceholder = matcher.group(1).toLowerCase();

                if (island == null)
                    return subPlaceholder.equals("exists") ? "No" : "";

                if (subPlaceholder.startsWith("location_")) {
                    island = plugin.getGrid().getIslandAt(player.getLocation());
                    if (island == null)
                        return "";
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
                        return StringUtil.format(island.getCenter().getBlock().getBiome().name());
                    case "level":
                        return island.getIslandLevelAsBigDecimal().toString();
                    case "worth":
                        return island.getWorthAsBigDecimal().toString();
                    case "raw_worth":
                        return island.getRawWorthAsBigDecimal().toString();
                    case "bank":
                        return island.getMoneyInBankAsBigDecimal().toString();
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
                }

            }
        }catch(NullPointerException ignored){}

        return "";
    }

    public static void register(){
        if(Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI"))
            new PlaceholderHook_PAPI(SuperiorSkyblockPlugin.getPlugin());
    }

}
