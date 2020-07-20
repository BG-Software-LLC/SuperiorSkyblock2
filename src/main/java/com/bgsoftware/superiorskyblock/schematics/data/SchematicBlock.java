package com.bgsoftware.superiorskyblock.schematics.data;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.utils.blocks.BlockChangeTask;
import com.bgsoftware.superiorskyblock.utils.tags.CompoundTag;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.SkullType;
import org.bukkit.block.BlockFace;
import org.bukkit.block.banner.Pattern;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

public class SchematicBlock {

    public final static SchematicBlock AIR = of(0, null);

    private final int combinedId;
    private final CompoundTag statesTag;

    private SchematicBlock(int combinedId, CompoundTag statesTag){
        this.combinedId = combinedId;
        this.statesTag = statesTag;
    }

    public int getCombinedId() {
        return combinedId;
    }

    public void applyBlock(BlockChangeTask blockChangeTask, Location location, Island island){
        applyBlock(blockChangeTask, location, BlockType.BLOCK);
    }

    protected void applyBlock(BlockChangeTask blockChangeTask, Location location, BlockType blockType, Object... args){
        blockChangeTask.setBlock(location, combinedId, statesTag, blockType, args);
    }

    private static class SchematicBanner extends SchematicBlock{

        private final DyeColor baseColor;
        private final List<Pattern> patterns;

        private SchematicBanner(int combinedId, CompoundTag statesTag, DyeColor baseColor, List<Pattern> patterns){
            super(combinedId, statesTag);
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
        private final String name;

        private SchematicInventoryHolder(int combinedId, CompoundTag statesTag, ItemStack[] contents, String name){
            super(combinedId, statesTag);
            this.contents = contents;
            this.name = name;
        }

        @Override
        public void applyBlock(BlockChangeTask blockChangeTask, Location location, Island island) {
            super.applyBlock(blockChangeTask, location, BlockType.INVENTORY_HOLDER, contents, name);
        }
    }

    private static class SchematicFlowerPot extends SchematicBlock{

        private final ItemStack flower;

        private SchematicFlowerPot(int combinedId, CompoundTag statesTag, ItemStack flower){
            super(combinedId, statesTag);
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

        private SchematicSkull(int combinedId, CompoundTag statesTag, SkullType skullType, BlockFace rotation, String owner){
            super(combinedId, statesTag);
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

        private SchematicSign(int combinedId, CompoundTag statesTag, String[] lines){
            super(combinedId, statesTag);
            this.lines = lines;
        }

        @Override
        public void applyBlock(BlockChangeTask blockChangeTask, Location location, Island island) {
            String[] lines = Arrays.copyOf(this.lines, this.lines.length);

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

        private SchematicSpawner(int combinedId, CompoundTag statesTag, EntityType spawnedType){
            super(combinedId, statesTag);
            this.spawnedType = spawnedType;
        }

        @Override
        public void applyBlock(BlockChangeTask blockChangeTask, Location location, Island island) {
            super.applyBlock(blockChangeTask, location, BlockType.SPAWNER, spawnedType);
        }
    }

    public static SchematicBlock of(int combinedId, CompoundTag statesTag){
        return new SchematicBlock(combinedId, statesTag);
    }

    public static SchematicBanner of(int combinedId, CompoundTag statesTag, DyeColor baseColor, List<Pattern> patterns){
        return new SchematicBanner(combinedId, statesTag, baseColor, patterns);
    }

    public static SchematicInventoryHolder of(int combinedId, CompoundTag statesTag, ItemStack[] contents, String name){
        return new SchematicInventoryHolder(combinedId, statesTag, contents, name);
    }

    public static SchematicFlowerPot of(int combinedId, CompoundTag statesTag, ItemStack flower){
        return new SchematicFlowerPot(combinedId, statesTag, flower);
    }

    public static SchematicSkull of(int combinedId, CompoundTag statesTag, SkullType skullType, BlockFace rotation, String owner){
        return new SchematicSkull(combinedId, statesTag, skullType, rotation, owner);
    }

    public static SchematicSign of(int combinedId, CompoundTag statesTag, String[] lines){
        return new SchematicSign(combinedId, statesTag, lines);
    }

    public static SchematicSpawner of(int combinedId, CompoundTag statesTag, EntityType entityType){
        return new SchematicSpawner(combinedId, statesTag, entityType);
    }

}
