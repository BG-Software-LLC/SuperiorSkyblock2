package com.bgsoftware.superiorskyblock.nms.v1_18_R2.dragon;

import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.BlockPosition;
import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.MathHelper;
import com.google.common.base.Preconditions;
import com.google.common.cache.CacheLoader;
import com.google.common.collect.Lists;
import net.minecraft.world.level.levelgen.feature.WorldGenEnder;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class SpikesCacheLoader extends CacheLoader<Long, List<WorldGenEnder.Spike>> {

    @Override
    public @NotNull List<WorldGenEnder.Spike> load(@NotNull Long key) {
        BlockPosition islandBlockPosition = DragonUtils.getCurrentSpikesLookupPosition();
        Preconditions.checkNotNull(islandBlockPosition, "Suspicious spikes call!");

        List<Integer> list = IntStream.range(0, 10).boxed().collect(Collectors.toList());

        Collections.shuffle(list, new Random(key));
        List<WorldGenEnder.Spike> spikesList = Lists.newArrayList();

        for (int i = 0; i < 10; ++i) {
            int spikeX = MathHelper.floor(42.0D * Math.cos(2.0D * (-3.141592653589793D + 0.3141592653589793D * (double) i)));
            int spikeZ = MathHelper.floor(42.0D * Math.sin(2.0D * (-3.141592653589793D + 0.3141592653589793D * (double) i)));
            int l = list.get(i);
            int radius = 2 + l / 3;
            int height = 76 + l * 3;
            boolean guarded = l == 1 || l == 2;

            spikesList.add(new WorldGenEnder.Spike(spikeX + islandBlockPosition.getX(),
                    spikeZ + islandBlockPosition.getZ(), radius, height, guarded));
        }

        return spikesList;
    }

}
