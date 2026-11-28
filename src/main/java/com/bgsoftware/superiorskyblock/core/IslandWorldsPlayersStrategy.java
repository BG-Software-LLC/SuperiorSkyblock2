package com.bgsoftware.superiorskyblock.core;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.world.WorldInfo;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.List;

public abstract class IslandWorldsPlayersStrategy implements ObjectsPool.Releasable, AutoCloseable {

    private static final LazyReference<ObjectsPool<IslandWorldsPlayersStrategy>> POOL = new LazyReference<ObjectsPool<IslandWorldsPlayersStrategy>>() {
        @Override
        protected ObjectsPool<IslandWorldsPlayersStrategy> create() {
            return plugin.getProviders().hasCustomWorldsSupport() ? new ObjectsPool<>(IslandWorldsPlayersStrategyMultiWorld::new) :
                    new ObjectsPool<>(IslandWorldsPlayersStrategySingleWorld::new);
        }
    };

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    public static IslandWorldsPlayersStrategy create(Island island) {
        return POOL.get().obtain().initialize(island);
    }

    private IslandWorldsPlayersStrategy() {

    }

    protected abstract IslandWorldsPlayersStrategy initialize(Island island);

    @Override
    public void close() {
        release();
    }

    public abstract List<Player> getPlayers(WorldInfo worldInfo);

    public abstract List<SuperiorPlayer> getSuperiorPlayers(WorldInfo worldInfo);

    private static class IslandWorldsPlayersStrategySingleWorld extends IslandWorldsPlayersStrategy {

        private List<SuperiorPlayer> playersInIsland = null;

        private IslandWorldsPlayersStrategySingleWorld() {
        }

        protected IslandWorldsPlayersStrategySingleWorld initialize(Island island) {
            this.playersInIsland = island.getAllPlayersInside();
            return this;
        }

        @Override
        public void release() {
            this.playersInIsland = null;
        }

        @Override
        public List<Player> getPlayers(WorldInfo worldInfo) {
            return new SequentialListBuilder<Player>()
                    .filter(player -> player.getWorld().getName().equals(worldInfo.getName()))
                    .build(this.playersInIsland, SuperiorPlayer::asPlayer);
        }

        @Override
        public List<SuperiorPlayer> getSuperiorPlayers(WorldInfo worldInfo) {
            return new SequentialListBuilder<SuperiorPlayer>()
                    .filter(player -> player.getWorld().getName().equals(worldInfo.getName()))
                    .build(this.playersInIsland);
        }

    }


    private static class IslandWorldsPlayersStrategyMultiWorld extends IslandWorldsPlayersStrategy {

        private Island island = null;

        private IslandWorldsPlayersStrategyMultiWorld() {

        }

        protected IslandWorldsPlayersStrategyMultiWorld initialize(Island island) {
            this.island = island;
            return this;
        }

        @Override
        public void release() {
            this.island = null;
        }

        @Override
        public List<Player> getPlayers(WorldInfo worldInfo) {
            World bukkitWorld = Bukkit.getWorld(worldInfo.getName());
            try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
                return new SequentialListBuilder<Player>()
                        .filter(player -> island.isInside(player.getLocation(wrapper.getHandle())))
                        .build(bukkitWorld.getPlayers());
            }
        }

        @Override
        public List<SuperiorPlayer> getSuperiorPlayers(WorldInfo worldInfo) {
            World bukkitWorld = Bukkit.getWorld(worldInfo.getName());
            try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
                return new SequentialListBuilder<SuperiorPlayer>()
                        .filter(player -> island.isInside(player.getLocation(wrapper.getHandle())))
                        .build(bukkitWorld.getPlayers(), plugin.getPlayers()::getSuperiorPlayer);
            }
        }

    }

}
