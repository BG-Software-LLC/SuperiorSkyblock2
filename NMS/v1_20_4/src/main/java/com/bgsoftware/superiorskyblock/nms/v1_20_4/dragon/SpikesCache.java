package com.bgsoftware.superiorskyblock.nms.v1_20_4.dragon;


import com.bgsoftware.common.annotations.NotNull;
import com.bgsoftware.common.annotations.Nullable;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.CacheStats;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.feature.SpikeFeature;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SpikesCache implements LoadingCache<Long, List<SpikeFeature.EndSpike>> {

    private static final SpikesCache INSTANCE = new SpikesCache();

    private final LoadingCache<BlockPos, List<SpikeFeature.EndSpike>> cachedSpikes = CacheBuilder.newBuilder()
            .expireAfterWrite(5L, TimeUnit.MINUTES).build(new InternalCacheLoader());

    private long worldSeed;

    public static SpikesCache getInstance() {
        return INSTANCE;
    }

    private SpikesCache() {

    }

    @Override
    public @NotNull
    List<SpikeFeature.EndSpike> get(@NotNull Long worldSeed) throws ExecutionException {
        try {
            this.worldSeed = worldSeed;
            return cachedSpikes.get(DragonUtils.getCurrentPodiumPosition());
        } finally {
            this.worldSeed = 0;
        }
    }

    @Override
    public @NotNull
    List<SpikeFeature.EndSpike> getUnchecked(@NotNull Long worldSeed) {
        try {
            this.worldSeed = worldSeed;
            return cachedSpikes.getUnchecked(DragonUtils.getCurrentPodiumPosition());
        } finally {
            this.worldSeed = 0;
        }
    }

    @Override
    public @NotNull
    ImmutableMap<Long, List<SpikeFeature.EndSpike>> getAll(@NotNull Iterable<? extends Long> keys) {
        throw new UnsupportedOperationException("This operation is not supported in SpikesCache.");
    }

    @Override
    public List<SpikeFeature.EndSpike> apply(@NotNull Long worldSeed) {
        throw new UnsupportedOperationException("This operation is not supported in SpikesCache.");
    }

    @Override
    public void refresh(@NotNull Long worldSeed) {
        try {
            this.worldSeed = worldSeed;
            cachedSpikes.refresh(DragonUtils.getCurrentPodiumPosition());
        } finally {
            this.worldSeed = 0;
        }
    }

    @Override
    public @NotNull
    ConcurrentMap<Long, List<SpikeFeature.EndSpike>> asMap() {
        throw new UnsupportedOperationException("This operation is not supported in SpikesCache.");
    }

    @Nullable
    @Override
    public List<SpikeFeature.EndSpike> getIfPresent(@NotNull Object worldSeed) {
        throw new UnsupportedOperationException("This operation is not supported in SpikesCache.");
    }

    @Override
    public @NotNull
    List<SpikeFeature.EndSpike> get(@NotNull Long worldSeed,
                                    @NotNull Callable<? extends List<SpikeFeature.EndSpike>> loader) throws ExecutionException {
        try {
            this.worldSeed = worldSeed;
            return cachedSpikes.get(DragonUtils.getCurrentPodiumPosition(), loader);
        } finally {
            this.worldSeed = 0;
        }
    }

    @Override
    public @NotNull
    ImmutableMap<Long, List<SpikeFeature.EndSpike>> getAllPresent(@NotNull Iterable<?> keys) {
        throw new UnsupportedOperationException("This operation is not supported in SpikesCache.");
    }

    @Override
    public void put(@NotNull Long worldSeed, @NotNull List<SpikeFeature.EndSpike> spikes) {
        try {
            this.worldSeed = worldSeed;
            cachedSpikes.put(DragonUtils.getCurrentPodiumPosition(), spikes);
        } finally {
            this.worldSeed = 0;
        }
    }

    @Override
    public void putAll(Map<? extends Long, ? extends List<SpikeFeature.EndSpike>> spikesMap) {
        spikesMap.forEach(this::put);
    }

    @Override
    public void invalidate(@NotNull Object worldSeed) {
        throw new UnsupportedOperationException("This operation is not supported in SpikesCache.");
    }

    @Override
    public void invalidateAll(@NotNull Iterable<?> keys) {
        throw new UnsupportedOperationException("This operation is not supported in SpikesCache.");
    }

    @Override
    public void invalidateAll() {
        this.cachedSpikes.invalidateAll();
    }

    @Override
    public long size() {
        return this.cachedSpikes.size();
    }

    @Override
    public @NotNull
    CacheStats stats() {
        return this.cachedSpikes.stats();
    }

    @Override
    public void cleanUp() {
        this.cachedSpikes.cleanUp();
    }

    private class InternalCacheLoader extends CacheLoader<BlockPos, List<SpikeFeature.EndSpike>> {

        @Override
        public @NotNull
        List<SpikeFeature.EndSpike> load(@NotNull BlockPos blockPos) {
            List<Integer> list = IntStream.range(0, 10).boxed().collect(Collectors.toList());

            Collections.shuffle(list, new Random(worldSeed));
            List<SpikeFeature.EndSpike> spikesList = Lists.newArrayList();

            for (int i = 0; i < 10; ++i) {
                int spikeX = Mth.floor(42.0D * Math.cos(2.0D * (-3.141592653589793D + 0.3141592653589793D * (double) i)));
                int spikeZ = Mth.floor(42.0D * Math.sin(2.0D * (-3.141592653589793D + 0.3141592653589793D * (double) i)));
                int l = list.get(i);
                int radius = 2 + l / 3;
                int height = 76 + l * 3;
                boolean guarded = l == 1 || l == 2;

                spikesList.add(new SpikeFeature.EndSpike(spikeX + blockPos.getX(), spikeZ + blockPos.getZ(), radius, height, guarded));
            }

            return spikesList;
        }

    }

}
