package com.bgsoftware.superiorskyblock.hooks;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import me.robin.leaderheads.api.LeaderHeadsAPI;
import me.robin.leaderheads.datacollectors.DataCollector;
import me.robin.leaderheads.objects.BoardType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class LeaderHeadsHook extends DataCollector {

    private static SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private LeaderHeadsHook(){
        super("top-islands", "SuperiorSkyblock2", BoardType.DEFAULT, "&lTop Islands", "islandstop", Arrays.asList(null, null, "&e{amount} worth", null), true, String.class);
    }

    @Override
    public List<Map.Entry<?, Double>> requestAll() {
        Map<String, Double> request = new HashMap<>();

        plugin.getGrid().getAllIslands().forEach(uuid -> {
            Island island = plugin.getGrid().getIsland(uuid);
            request.put(island.getOwner().getName(), island.getWorthAsBigDecimal().doubleValue());
        });

        return LeaderHeadsAPI.sortMap(request);
    }

    public static void register(){
        new LeaderHeadsHook();
    }

}
