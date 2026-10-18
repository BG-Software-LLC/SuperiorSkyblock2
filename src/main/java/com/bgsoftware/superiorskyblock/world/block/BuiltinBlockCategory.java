package com.bgsoftware.superiorskyblock.world.block;

import com.bgsoftware.common.annotations.NotNull;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.block.BlockCategory;
import com.bgsoftware.superiorskyblock.api.key.KeySet;
import com.bgsoftware.superiorskyblock.core.key.KeyIndicator;
import com.bgsoftware.superiorskyblock.core.key.Keys;
import com.bgsoftware.superiorskyblock.core.key.set.KeySets;
import org.bukkit.Material;

import java.lang.ref.WeakReference;
import java.util.Objects;

public enum BuiltinBlockCategory {

    ALL;

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private final KeySet blocks = getBlocksInternal();
    private WeakReference<BlockCategory> blockCategoryReference;

    public KeySet getBlocks() {
        return this.blocks;
    }

    @NotNull
    public BlockCategory getBlockCategory() {
        if (this.blockCategoryReference == null) {
            BlockCategory blockCategory = plugin.getSettings().getBlockCategoriesMap().getCategoryByName(name());
            this.blockCategoryReference = new WeakReference<>(blockCategory);
            return Objects.requireNonNull(blockCategory);
        }

        BlockCategory blockCategory = this.blockCategoryReference.get();
        if (blockCategory == null) {
            blockCategory = plugin.getSettings().getBlockCategoriesMap().getCategoryByName(name());
            this.blockCategoryReference = new WeakReference<>(blockCategory);
        }

        return Objects.requireNonNull(blockCategory);
    }

    private static KeySet getBlocksInternal() {
        KeySet blocks = KeySets.createHashSet(KeyIndicator.MATERIAL);

        for (Material material : Material.values()) {
            if (material.isBlock()) {
                blocks.add(Keys.of(material));
            }
        }

        return blocks;
    }

}
