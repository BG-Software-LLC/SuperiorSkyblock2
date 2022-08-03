package com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.net.minecraft.tags;

import com.bgsoftware.superiorskyblock.nms.mapping.Remap;
import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.net.minecraft.world.level.block.Block;
import net.minecraft.tags.Tag;

public final class TagsBlock {

    @Remap(classPath = "net.minecraft.tags.BlockTags", name = "SLABS", type = Remap.Type.FIELD, remappedName = "E")
    public static final Tag.e<net.minecraft.world.level.block.Block> SLABS = net.minecraft.tags.TagsBlock.E;
    @Remap(classPath = "net.minecraft.tags.BlockTags", name = "WOODEN_SLABS", type = Remap.Type.FIELD, remappedName = "j")
    public static final Tag.e<net.minecraft.world.level.block.Block> WOODEN_SLABS = net.minecraft.tags.TagsBlock.j;

    @Remap(classPath = "net.minecraft.world.level.block.state.BlockBehaviour$BlockStateBase",
            name = "is",
            type = Remap.Type.METHOD,
            remappedName = "a")
    public static boolean isTagged(Tag.e<net.minecraft.world.level.block.Block> tag, Block value) {
        return value.getBlockData().getHandle().a(tag);
    }

}
