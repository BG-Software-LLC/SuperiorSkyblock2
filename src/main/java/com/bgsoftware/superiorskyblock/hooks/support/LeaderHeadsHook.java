package com.bgsoftware.superiorskyblock.hooks.support;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.SortingType;
import com.bgsoftware.superiorskyblock.utils.islands.SortingTypes;
import me.robin.leaderheads.datacollectors.DataCollector;
import me.robin.leaderheads.objects.BoardType;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public final class LeaderHeadsHook {

    private static SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private LeaderHeadsHook(){

    }

    private final static class WorthDataController extends SuperiorDataController{

        WorthDataController(){
            super("worth-islands", "topislandsworth", SortingTypes.BY_WORTH);
        }

        @Override
        public Double getValue(Island island) {
            return island.getWorth().doubleValue();
        }
    }

    private final static class LevelDataController extends SuperiorDataController{

        LevelDataController(){
            super("level-islands", "topislandslevel", SortingTypes.BY_LEVEL);
        }

        @Override
        public Double getValue(Island island) {
            return island.getIslandLevel().doubleValue();
        }
    }

    private final static class RatingDataController extends SuperiorDataController{

        RatingDataController(){
            super("rating-islands", "topislandsrating", SortingTypes.BY_RATING);
        }

        @Override
        public Double getValue(Island island) {
            return island.getTotalRating();
        }
    }

    private final static class PlayersDataController extends SuperiorDataController{

        PlayersDataController(){
            super("players-islands", "topislandsplayers", SortingTypes.BY_PLAYERS);
        }

        @Override
        public Double getValue(Island island) {
            return (double) island.getAllPlayersInside().size();
        }
    }

    private static abstract class SuperiorDataController extends DataCollector{

        private SortingType sortingType;

        SuperiorDataController(String name, String command, SortingType sortingType){
            super(
                    name,
                    "SuperiorSkyblock2",
                    BoardType.DEFAULT,
                    "&lTop Islands",
                    command,
                    Arrays.asList("", "{name}", "{amount}", ""),
                    true,
                    UUID.class
            );
            this.sortingType = sortingType;
        }

        @Override
        public List<Map.Entry<?, Double>> requestAll() {
            return plugin.getGrid().getIslands(sortingType).stream().map(island -> new Map.Entry<UUID, Double>(){
                @Override
                public UUID getKey() {
                    return island.getOwner().getUniqueId();
                }

                @Override
                public Double getValue() {
                    return SuperiorDataController.this.getValue(island);
                }

                @Override
                public Double setValue(Double value) {
                    return null;
                }
            }).collect(Collectors.toList());
        }

        public abstract Double getValue(Island island);

    }

    public static void register(){
        new WorthDataController();
        new LevelDataController();
        new RatingDataController();
        new PlayersDataController();
    }

}
