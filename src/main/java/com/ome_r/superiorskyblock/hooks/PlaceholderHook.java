package com.ome_r.superiorskyblock.hooks;

import com.ome_r.superiorskyblock.SuperiorSkyblock;
import com.ome_r.superiorskyblock.island.Island;
import com.ome_r.superiorskyblock.island.IslandPermission;
import com.ome_r.superiorskyblock.utils.StringUtil;
import com.ome_r.superiorskyblock.wrappers.WrappedLocation;
import com.ome_r.superiorskyblock.wrappers.WrappedPlayer;
import me.clip.placeholderapi.external.EZPlaceholderHook;
import org.bukkit.entity.Player;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("deprecation")
public class PlaceholderHook extends EZPlaceholderHook {

    private SuperiorSkyblock plugin;

    public PlaceholderHook(SuperiorSkyblock plugin){
        super(plugin, "superior");
        this.plugin = plugin;
    }

    @Override
    public String onPlaceholderRequest(Player player, String placeholder) {
        if(player == null)
            return "";

        WrappedPlayer wrappedPlayer = WrappedPlayer.of(player);
        Island island = plugin.getGrid().getIsland(wrappedPlayer);
        Matcher matcher;

        if((matcher = Pattern.compile("island_(.+)").matcher(placeholder)).matches()){
            if(island == null)
                return "";

            String subPlaceholder = matcher.group(1);

            if(subPlaceholder.startsWith("location_")){
                island = plugin.getGrid().getIslandAt(player.getLocation());
                if(island == null)
                    return "";
                subPlaceholder = subPlaceholder.replace("location_", "");
            }

            if((matcher = Pattern.compile("island_permission_(.+)").matcher(placeholder)).matches()){
                String permission = matcher.group(1);

                try {
                    IslandPermission islandPermission = IslandPermission.valueOf(permission.toUpperCase());
                    return String.valueOf(island.hasPermission(wrappedPlayer, islandPermission));
                }catch(IllegalArgumentException ex){
                    return "";
                }
            }


            if((matcher = Pattern.compile("island_upgrade_(.+)").matcher(placeholder)).matches()){
                String upgradeName = matcher.group(1);
                return String.valueOf(island.getUpgradeLevel(upgradeName));
            }

            switch (subPlaceholder.toLowerCase()){
                case "center":
                    return WrappedLocation.of(island.getCenter()).toString();
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
                    return String.valueOf(island.getIslandSize());
                case "biome":
                    return StringUtil.format(island.getCenter().getBlock().getBiome().name());
                case "level":
                    return String.valueOf(island.getIslandLevel());
                case "worth":
                    return String.valueOf(island.getWorth());
                case "raw_worth":
                    return String.valueOf(island.getRawWorth());
                case "bank":
                    return String.valueOf(island.getMoneyInBank());
                case "hoppers_limit":
                    return String.valueOf(island.getHoppersLimit());
                case "crops_multiplier":
                    return String.valueOf(island.getCropGrowthMultiplier());
                case "spawners_multiplier":
                    return String.valueOf(island.getSpawnerRatesMultiplier());
                case "drops_multiplier":
                    return String.valueOf(island.getMobDropsMultiplier());
                case "discord":
                    return island.hasPermission(wrappedPlayer, IslandPermission.DISCORD_SHOW) ? island.getDiscord() : "None";
                case "paypal":
                    return island.hasPermission(wrappedPlayer, IslandPermission.PAYPAL_SHOW) ? island.getPaypal() : "None";
                case "discord_all":
                    return island.getDiscord();
                case "paypal_all":
                    return island.getPaypal();
            }

        }


        return null;
    }
}
