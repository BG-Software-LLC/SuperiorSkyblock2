package com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.tags;

import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.level.block.Block;
import net.minecraft.tags.Tag;

public class TagsBlock {

    public static final Tag.e<net.minecraft.world.level.block.Block> SLABS = net.minecraft.tags.TagsBlock.E;
    public static final Tag.e<net.minecraft.world.level.block.Block> WOODEN_SLABS = net.minecraft.tags.TagsBlock.j;

    public static boolean isTagged(Tag.e<net.minecraft.world.level.block.Block> tag, Block value) {
        return tag.a(value.getHandle());
    }

}
