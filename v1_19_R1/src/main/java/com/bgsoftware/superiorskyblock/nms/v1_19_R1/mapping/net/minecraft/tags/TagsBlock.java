package com.bgsoftware.superiorskyblock.nms.v1_19_R1.mapping.net.minecraft.tags;

import com.bgsoftware.superiorskyblock.nms.v1_19_R1.mapping.net.minecraft.world.level.block.Block;
import net.minecraft.tags.TagKey;

public final class TagsBlock {

    public static final TagKey<net.minecraft.world.level.block.Block> SLABS = net.minecraft.tags.TagsBlock.G;
    public static final TagKey<net.minecraft.world.level.block.Block> WOODEN_SLABS = net.minecraft.tags.TagsBlock.i;

    public static boolean isTagged(TagKey<net.minecraft.world.level.block.Block> tag, Block value) {
        return value.getBlockData().getHandle().a(tag);
    }

}
