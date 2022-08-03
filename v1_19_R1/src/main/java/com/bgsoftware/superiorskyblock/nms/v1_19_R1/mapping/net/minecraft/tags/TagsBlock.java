package com.bgsoftware.superiorskyblock.nms.v1_19_R1.mapping.net.minecraft.tags;

import com.bgsoftware.superiorskyblock.nms.mapping.Remap;
import com.bgsoftware.superiorskyblock.nms.v1_19_R1.mapping.net.minecraft.world.level.block.Block;
import net.minecraft.tags.TagKey;

public final class TagsBlock {

    @Remap(classPath = "net.minecraft.tags.BlockTags", name = "SLABS", type = Remap.Type.FIELD, remappedName = "G")
    public static final TagKey<net.minecraft.world.level.block.Block> SLABS = net.minecraft.tags.TagsBlock.G;
    @Remap(classPath = "net.minecraft.tags.BlockTags", name = "WOODEN_SLABS", type = Remap.Type.FIELD, remappedName = "i")
    public static final TagKey<net.minecraft.world.level.block.Block> WOODEN_SLABS = net.minecraft.tags.TagsBlock.i;

    @Remap(classPath = "net.minecraft.world.level.block.state.BlockBehaviour$BlockStateBase",
            name = "is",
            type = Remap.Type.METHOD,
            remappedName = "a")
    public static boolean isTagged(TagKey<net.minecraft.world.level.block.Block> tag, Block value) {
        return value.getBlockData().getHandle().a(tag);
    }

}
