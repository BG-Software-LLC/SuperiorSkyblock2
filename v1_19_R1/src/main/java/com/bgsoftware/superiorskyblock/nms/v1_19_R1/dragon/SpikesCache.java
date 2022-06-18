package com.bgsoftware.superiorskyblock.nms.v1_19_R1.dragon;

import com.bgsoftware.superiorskyblock.nms.v1_19_R1.mapping.net.minecraft.core.BlockPosition;
import com.bgsoftware.superiorskyblock.nms.v1_19_R1.mapping.net.minecraft.util.MathHelper;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.CacheStats;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import net.minecraft.world.level.levelgen.feature.WorldGenEnder;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
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

public final class SpikesCache implements LoadingCache<Long, List<WorldGenEnder.Spike>> {

    private static final SpikesCache INSTANCE = new SpikesCache();

    private final LoadingCache<net.minecraft.core.BlockPosition, List<WorldGenEnder.Spike>> cachedSpikes = CacheBuilder.newBuilder()
            .expireAfterWrite(5L, TimeUnit.MINUTES).build(new InternalCacheLoader());

    private long worldSeed;

    public static SpikesCache getInstance() {
        return INSTANCE;
    }

    private SpikesCache() {

    }

    @Override
    public @NotNull
    List<WorldGenEnder.Spike> get(@NotNull Long worldSeed) throws ExecutionException {
        try {
            this.worldSeed = worldSeed;
            return cachedSpikes.get(DragonUtils.getCurrentPodiumPosition().getHandle());
        } finally {
            this.worldSeed = 0;
        }
    }

    @Override
    public @NotNull
    List<WorldGenEnder.Spike> getUnchecked(@NotNull Long worldSeed) {
        try {
            this.worldSeed = worldSeed;
            return cachedSpikes.getUnchecked(DragonUtils.getCurrentPodiumPosition().getHandle());
        } finally {
            this.worldSeed = 0;
        }
    }

    @Override
    public @NotNull
    ImmutableMap<Long, List<WorldGenEnder.Spike>> getAll(@NotNull Iterable<? extends Long> keys) {
        throw new UnsupportedOperationException("This operation is not supported in SpikesCache.");
    }

    @Override
    public List<WorldGenEnder.Spike> apply(@NotNull Long worldSeed) {
        throw new UnsupportedOperationException("This operation is not supported in SpikesCache.");
    }

    @Override
    public void refresh(@NotNull Long worldSeed) {
        try {
            this.worldSeed = worldSeed;
            cachedSpikes.refresh(DragonUtils.getCurrentPodiumPosition().getHandle());
        } finally {
            this.worldSeed = 0;
        }
    }

    @Override
    public @NotNull
    ConcurrentMap<Long, List<WorldGenEnder.Spike>> asMap() {
        throw new UnsupportedOperationException("This operation is not supported in SpikesCache.");
    }

    @Nullable
    @Override
    public List<WorldGenEnder.Spike> getIfPresent(@NotNull Object worldSeed) {
        throw new UnsupportedOperationException("This operation is not supported in SpikesCache.");
    }

    @Override
    public @NotNull
    List<WorldGenEnder.Spike> get(@NotNull Long worldSeed,
                                  @NotNull Callable<? extends List<WorldGenEnder.Spike>> loader) throws ExecutionException {
        try {
            this.worldSeed = worldSeed;
            return cachedSpikes.get(DragonUtils.getCurrentPodiumPosition().getHandle(), loader);
        } finally {
            this.worldSeed = 0;
        }
    }

    @Override
    public @NotNull
    ImmutableMap<Long, List<WorldGenEnder.Spike>> getAllPresent(@NotNull Iterable<?> keys) {
        throw new UnsupportedOperationException("This operation is not supported in SpikesCache.");
    }

    @Override
    public void put(@NotNull Long worldSeed, @NotNull List<WorldGenEnder.Spike> spikes) {
        try {
            this.worldSeed = worldSeed;
            cachedSpikes.put(DragonUtils.getCurrentPodiumPosition().getHandle(), spikes);
        } finally {
            this.worldSeed = 0;
        }
    }

    @Override
    public void putAll(Map<? extends Long, ? extends List<WorldGenEnder.Spike>> spikesMap) {
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

    private final class InternalCacheLoader extends CacheLoader<net.minecraft.core.BlockPosition, List<WorldGenEnder.Spike>> {

        @Override
        public @NotNull
        List<WorldGenEnder.Spike> load(@NotNull net.minecraft.core.BlockPosition nmsBlockPosition) {
            BlockPosition blockPosition = new BlockPosition(nmsBlockPosition);

            List<Integer> list = IntStream.range(0, 10).boxed().collect(Collectors.toList());

            Collections.shuffle(list, new Random(worldSeed));
            List<WorldGenEnder.Spike> spikesList = Lists.newArrayList();

            for (int i = 0; i < 10; ++i) {
                int spikeX = MathHelper.floor(42.0D * Math.cos(2.0D * (-3.141592653589793D + 0.3141592653589793D * (double) i)));
                int spikeZ = MathHelper.floor(42.0D * Math.sin(2.0D * (-3.141592653589793D + 0.3141592653589793D * (double) i)));
                int l = list.get(i);
                int radius = 2 + l / 3;
                int height = 76 + l * 3;
                boolean guarded = l == 1 || l == 2;

                spikesList.add(new WorldGenEnder.Spike(spikeX + blockPosition.getX(),
                        spikeZ + blockPosition.getZ(), radius, height, guarded));
            }

            return spikesList;
        }

    }

}
