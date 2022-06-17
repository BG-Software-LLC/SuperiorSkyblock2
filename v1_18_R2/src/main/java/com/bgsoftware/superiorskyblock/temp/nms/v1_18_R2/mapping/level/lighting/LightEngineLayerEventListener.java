package com.bgsoftware.superiorskyblock.temp.nms.v1_18_R2.mapping.level.lighting;

import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.superiorskyblock.temp.nms.v1_18_R2.mapping.BlockPosition;
import com.bgsoftware.superiorskyblock.temp.nms.v1_18_R2.mapping.MappedObject;
import net.minecraft.world.level.lighting.LightEngineBlock;
import net.minecraft.world.level.lighting.LightEngineGraph;

public class LightEngineLayerEventListener extends
        MappedObject<net.minecraft.world.level.lighting.LightEngineLayerEventListener> {

    private static final ReflectMethod<Void> SKY_LIGHT_UPDATE = new ReflectMethod<>(
            LightEngineGraph.class, 1, Long.class, Long.class, Integer.class, Boolean.class);

    public LightEngineLayerEventListener(net.minecraft.world.level.lighting.LightEngineLayerEventListener handle) {
        super(handle);
    }

    public void flagDirty(BlockPosition blockPosition, byte lightData) {
        if (handle instanceof LightEngineBlock) {
            flagBlockDirty(blockPosition, lightData);
        } else {
            flagSkyDirty(blockPosition, lightData);
        }
    }

    public int getLightLevel(BlockPosition blockPosition) {
        return handle.b(blockPosition.getHandle());
    }

    private void flagBlockDirty(BlockPosition blockPosition, byte lightData) {
        try {
            handle.a(blockPosition.getHandle(), lightData);
        } catch (Exception ignored) {
        }
    }

    private void flagSkyDirty(BlockPosition blockPosition, byte lightData) {
        try {
            SKY_LIGHT_UPDATE.invoke(handle, 9223372036854775807L,
                    blockPosition.asLong(), 15 - lightData, true);
        } catch (Exception ignored) {
        }
    }

}
