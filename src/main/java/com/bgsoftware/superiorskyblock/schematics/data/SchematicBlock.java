package com.bgsoftware.superiorskyblock.schematics.data;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.utils.blocks.BlockChangeTask;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.SkullType;
import org.bukkit.block.BlockFace;
import org.bukkit.block.banner.Pattern;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class SchematicBlock {

    public final static SchematicBlock AIR = of(0);

    private final int combinedId;

    private SchematicBlock(int combinedId){
        this.combinedId = combinedId;
    }

    public int getCombinedId() {
        return combinedId;
    }

    public void applyBlock(BlockChangeTask blockChangeTask, Location location, Island island){
        applyBlock(blockChangeTask, location, BlockType.BLOCK);
    }

    protected void applyBlock(BlockChangeTask blockChangeTask, Location location, BlockType blockType, Object... args){
        blockChangeTask.setBlock(location, combinedId, blockType, args);
    }

    private static class SchematicBanner extends SchematicBlock{

        private final DyeColor baseColor;
        private final List<Pattern> patterns;

        private SchematicBanner(int combinedId, DyeColor baseColor, List<Pattern> patterns){
            super(combinedId);
            this.baseColor = baseColor;
            this.patterns = patterns;
        }

        @Override
        public void applyBlock(BlockChangeTask blockChangeTask, Location location, Island island) {
            super.applyBlock(blockChangeTask, location, BlockType.BANNER, baseColor, patterns);
        }
    }

    private static class SchematicInventoryHolder extends SchematicBlock{

        private final ItemStack[] contents;

        private SchematicInventoryHolder(int combinedId, ItemStack[] contents){
            super(combinedId);
            this.contents = contents;
        }

        @Override
        public void applyBlock(BlockChangeTask blockChangeTask, Location location, Island island) {
            super.applyBlock(blockChangeTask, location, BlockType.INVENTORY_HOLDER, (Object) contents);
        }
    }

    private static class SchematicFlowerPot extends SchematicBlock{

        private final ItemStack flower;

        private SchematicFlowerPot(int combinedId, ItemStack flower){
            super(combinedId);
            this.flower = flower;
        }

        @Override
        public void applyBlock(BlockChangeTask blockChangeTask, Location location, Island island) {
            super.applyBlock(blockChangeTask, location, BlockType.FLOWER_POT, flower);
        }
    }

    private static class SchematicSkull extends SchematicBlock{

        private final SkullType skullType;
        private final BlockFace rotation;
        private final String owner;

        private SchematicSkull(int combinedId, SkullType skullType, BlockFace rotation, String owner){
            super(combinedId);
            this.skullType = skullType;
            this.rotation = rotation;
            this.owner = owner;
        }

        @Override
        public void applyBlock(BlockChangeTask blockChangeTask, Location location, Island island) {
            super.applyBlock(blockChangeTask, location, BlockType.SKULL, skullType, rotation, owner);
        }
    }

    private static class SchematicSign extends SchematicBlock{

        private final String[] lines;

        private SchematicSign(int combinedId, String[] lines){
            super(combinedId);
            this.lines = lines;
        }

        @Override
        public void applyBlock(BlockChangeTask blockChangeTask, Location location, Island island) {
            for(int i = 0; i < 4; i++) {
                lines[i] = lines[i]
                        .replace("{player}", island == null ? "" : island.getOwner().getName())
                        .replace("{island}", island == null ? "" : island.getName());
            }

            super.applyBlock(blockChangeTask, location, BlockType.SIGN, (Object) lines);
        }
    }

    private static class SchematicSpawner extends SchematicBlock{

        private final EntityType spawnedType;

        private SchematicSpawner(int combinedId, EntityType spawnedType){
            super(combinedId);
            this.spawnedType = spawnedType;
        }

        @Override
        public void applyBlock(BlockChangeTask blockChangeTask, Location location, Island island) {
            super.applyBlock(blockChangeTask, location, BlockType.SPAWNER, spawnedType);
        }
    }

    public static SchematicBlock of(int combinedId){
        return new SchematicBlock(combinedId);
    }

    public static SchematicBanner of(int combinedId, DyeColor baseColor, List<Pattern> patterns){
        return new SchematicBanner(combinedId, baseColor, patterns);
    }

    public static SchematicInventoryHolder of(int combinedId, ItemStack[] contents){
        return new SchematicInventoryHolder(combinedId, contents);
    }

    public static SchematicFlowerPot of(int combinedId, ItemStack flower){
        return new SchematicFlowerPot(combinedId, flower);
    }

    public static SchematicSkull of(int combinedId, SkullType skullType, BlockFace rotation, String owner){
        return new SchematicSkull(combinedId, skullType, rotation, owner);
    }

    public static SchematicSign of(int combinedId, String[] lines){
        return new SchematicSign(combinedId, lines);
    }

    public static SchematicSpawner of(int combinedId, EntityType entityType){
        return new SchematicSpawner(combinedId, entityType);
    }

}
