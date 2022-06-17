package com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.tags;

import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.level.block.Block;
import net.minecraft.tags.TagKey;

public class TagsBlock {

    public static final TagKey<net.minecraft.world.level.block.Block> SLABS = net.minecraft.tags.TagsBlock.D;
    public static final TagKey<net.minecraft.world.level.block.Block> WOODEN_SLABS = net.minecraft.tags.TagsBlock.i;

    public static boolean isTagged(TagKey<net.minecraft.world.level.block.Block> tag, Block value) {
        return value.getBlockData().getHandle().a(tag);
    }

}
