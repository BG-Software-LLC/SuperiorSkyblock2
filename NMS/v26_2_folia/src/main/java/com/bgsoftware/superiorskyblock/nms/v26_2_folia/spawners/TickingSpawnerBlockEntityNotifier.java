package com.bgsoftware.superiorskyblock.nms.v26_2_folia.spawners;

import net.minecraft.world.level.block.entity.BlockEntity;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.entity.TickingBlockEntity;

import java.util.function.IntFunction;

public class TickingSpawnerBlockEntityNotifier extends com.bgsoftware.superiorskyblock.nms.v26_2.spawners.TickingSpawnerBlockEntityNotifier {

    private final SpawnerBlockEntity spawner;

    public TickingSpawnerBlockEntityNotifier(SpawnerBlockEntity spawner, TickingBlockEntity ticker, IntFunction<Integer> callback) {
        super(spawner, ticker, callback);
        this.spawner = spawner;
    }

    @Override
    public void updateDelay() {
        if (SuperiorSkyblockPlugin.getPlugin().isEnabled())
            super.updateDelay();
    }

    @Override
    public BlockEntity getTileEntity() {
        return this.spawner;
    }
}
