package com.bgsoftware.superiorskyblock.world;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.world.Dimension;
import com.bgsoftware.superiorskyblock.core.EnumHelper;
import com.bgsoftware.superiorskyblock.core.ServerVersion;
import com.bgsoftware.superiorskyblock.core.key.ConstantKeys;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public enum GeneratorType {

    NORMAL(ConstantKeys.COBBLESTONE),
    BASALT(ConstantKeys.BASALT),
    NONE(null);

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private static final BlockFace[] nearbyFaces = new BlockFace[]{
            BlockFace.WEST, BlockFace.EAST, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.UP
    };
    private static final Material BLUE_ICE_MATERIAL = EnumHelper.getEnum(Material.class, "BLUE_ICE");
    private static final Material SOUL_SOIL_MATERIAL = EnumHelper.getEnum(Material.class, "SOUL_SOIL");

    public static GeneratorType fromDimension(Dimension dimension) {
        return dimension.getEnvironment() == World.Environment.NETHER &&
                ServerVersion.isAtLeast(ServerVersion.v1_16) ? BASALT : NORMAL;
    }

    public static GeneratorType detectGenerator(Block block) {
        if (ServerVersion.isAtLeast(ServerVersion.v1_16) &&
                block.getWorld().getEnvironment() == World.Environment.NETHER) {
            for (BlockFace blockFace : nearbyFaces) {
                if (block.getRelative(blockFace).getType() == BLUE_ICE_MATERIAL &&
                        block.getRelative(BlockFace.DOWN).getType() == SOUL_SOIL_MATERIAL)
                    return GeneratorType.BASALT;
            }
        } else {
            for (BlockFace blockFace : nearbyFaces) {
                if (plugin.getNMSWorld().isWaterLogged(block.getRelative(blockFace)))
                    return GeneratorType.NORMAL;
            }
        }

        return GeneratorType.NONE;
    }

    @Nullable
    private final Key defaultBlock;

    GeneratorType(@Nullable Key defaultBlock) {
        this.defaultBlock = defaultBlock;
    }

    @Nullable
    public Key getDefaultBlock() {
        return defaultBlock;
    }

}
