package com.bgsoftware.superiorskyblock.hooks;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPermission;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.StringUtil;
import com.bgsoftware.superiorskyblock.utils.key.SKey;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import com.bgsoftware.superiorskyblock.wrappers.SBlockPosition;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

public final class PlaceholderHook_PAPI extends PlaceholderExpansion {

    private final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    public static void registerPlaceholderAPI() {
        new PlaceholderHook_PAPI().register();
    }

    @Override
    public String getIdentifier() {
        return "superior";
    }

    @Override
    public String getAuthor() {
        return "Ome_R";
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, String placeholder) {
        try {
            SuperiorPlayer sPlayer = SSuperiorPlayer.of(player);
            Island island = sPlayer.getIsland();

            String[] split = placeholder.split("_", 2);

            switch (split[0].toLowerCase()) {
                case "upgrade":
                    return String.valueOf(island.getUpgradeLevel(split[1]));
                case "permission":
                    IslandPermission islandPermission = IslandPermission.valueOf(split[1].toUpperCase());
                    return String.valueOf(island.hasPermission(sPlayer, islandPermission));
                case "count":
                    return String.valueOf(island.getBlockCount(SKey.of(split[1])));
            }

            switch (placeholder) {
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
                    return island.hasPermission(sPlayer, IslandPermission.DISCORD_SHOW) ? island.getDiscord() : "None";
                case "paypal":
                    return island.hasPermission(sPlayer, IslandPermission.PAYPAL_SHOW) ? island.getPaypal() : "None";
                case "discord_all":
                    return island.getDiscord();
                case "paypal_all":
                    return island.getPaypal();
                case "exists":
                    return island == null ? "No" : "Yes";
            }

        } catch (NullPointerException | ArrayIndexOutOfBoundsException ignored) {
        }

        return "unrecognized placeholder";
    }
}
