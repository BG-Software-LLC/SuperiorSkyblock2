package com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.level.lighting;

import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.BlockPosition;
import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.MappedObject;
import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.SectionPosition;
import net.minecraft.world.level.EnumSkyBlock;
import net.minecraft.world.level.chunk.NibbleArray;

public class LightEngine extends MappedObject<net.minecraft.world.level.lighting.LightEngine> {

    public LightEngine(net.minecraft.world.level.lighting.LightEngine handle) {
        super(handle);
    }

    public LightEngineLayerEventListener getLayer(EnumSkyBlock lightType) {
        return new LightEngineLayerEventListener(handle.a(lightType));
    }

    public void queueData(EnumSkyBlock lightLayer, SectionPosition sectionPosition, NibbleArray nibbleArray, boolean b) {
        handle.a(lightLayer, sectionPosition.getHandle(), nibbleArray, b);
    }

    public void updateSectionStatus(BlockPosition blockPosition, boolean flag) {
        handle.a(blockPosition.getHandle(), flag);
    }

    public void checkBlock(BlockPosition blockPosition) {
        handle.a(blockPosition.getHandle());
    }

    public int getMinSection() {
        return handle.c();
    }

    public int getMaxSection() {
        return handle.d();
    }

}
