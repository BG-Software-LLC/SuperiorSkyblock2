package com.bgsoftware.superiorskyblock.nms.v1_19_R1.mapping.net.minecraft.world.level.lighting;

import com.bgsoftware.superiorskyblock.nms.mapping.Remap;
import com.bgsoftware.superiorskyblock.nms.v1_19_R1.mapping.net.minecraft.core.BlockPosition;
import com.bgsoftware.superiorskyblock.nms.v1_19_R1.mapping.MappedObject;
import com.bgsoftware.superiorskyblock.nms.v1_19_R1.mapping.net.minecraft.core.SectionPosition;
import net.minecraft.world.level.EnumSkyBlock;
import net.minecraft.world.level.chunk.NibbleArray;

public final class LightEngine extends MappedObject<net.minecraft.world.level.lighting.LightEngine> {

    public LightEngine(net.minecraft.world.level.lighting.LightEngine handle) {
        super(handle);
    }

    @Remap(classPath = "net.minecraft.world.level.lighting.LevelLightEngine",
            name = "getLayerListener",
            type = Remap.Type.METHOD,
            remappedName = "a")
    public LightEngineLayerEventListener getLayer(EnumSkyBlock lightType) {
        return new LightEngineLayerEventListener(handle.a(lightType));
    }

    @Remap(classPath = "net.minecraft.world.level.lighting.LevelLightEngine",
            name = "queueSectionData",
            type = Remap.Type.METHOD,
            remappedName = "a")
    public void queueData(EnumSkyBlock lightLayer, SectionPosition sectionPosition, NibbleArray nibbleArray, boolean b) {
        handle.a(lightLayer, sectionPosition.getHandle(), nibbleArray, b);
    }

    @Remap(classPath = "net.minecraft.world.level.lighting.LevelLightEngine",
            name = "updateSectionStatus",
            type = Remap.Type.METHOD,
            remappedName = "a")
    public void updateSectionStatus(BlockPosition blockPosition, boolean flag) {
        handle.a(blockPosition.getHandle(), flag);
    }

    @Remap(classPath = "net.minecraft.world.level.lighting.LevelLightEngine",
            name = "checkBlock",
            type = Remap.Type.METHOD,
            remappedName = "a")
    public void checkBlock(BlockPosition blockPosition) {
        handle.a(blockPosition.getHandle());
    }

    @Remap(classPath = "net.minecraft.world.level.lighting.LevelLightEngine",
            name = "getMinLightSection",
            type = Remap.Type.METHOD,
            remappedName = "c")
    public int getMinSection() {
        return handle.c();
    }

    @Remap(classPath = "net.minecraft.world.level.lighting.LevelLightEngine",
            name = "getMaxLightSection",
            type = Remap.Type.METHOD,
            remappedName = "d")
    public int getMaxSection() {
        return handle.d();
    }

}
