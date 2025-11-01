package com.bgsoftware.superiorskyblock.core.menu;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.menu.MenuIslandCreationConfig;
import com.bgsoftware.superiorskyblock.api.schematic.Schematic;
import com.bgsoftware.superiorskyblock.api.world.GameSound;
import com.bgsoftware.superiorskyblock.api.wrappers.BlockOffset;
import com.bgsoftware.superiorskyblock.core.EnumHelper;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.IslandCreationButton;
import org.bukkit.block.Biome;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;

public class MenuConfig {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private MenuConfig() {

    }

    public static class IslandCreation implements MenuIslandCreationConfig {

        private final Schematic schematic;
        @Nullable
        private final IslandCreationButton.Template template;
        private final Biome biome;

        public IslandCreation(IslandCreationButton.Template template) {
            this(template.getSchematic(), template);
        }

        public IslandCreation(Schematic schematic, IslandCreationButton.Template template) {
            this.schematic = schematic;
            this.template = template;
            Biome biome = template == null ? null : template.getBiome();
            this.biome = biome == null ? EnumHelper.getEnum(Biome.class, plugin.getSettings().getWorlds().getDimensionConfig(
                    plugin.getSettings().getWorlds().getDefaultWorldDimension()).getBiome()) : biome;
        }

        @Override
        public Schematic getSchematic() {
            return this.schematic;
        }

        @Override
        public GameSound getSound() {
            return this.template == null ? null : this.template.getAccessSound();
        }

        @Override
        public Collection<String> getCommands() {
            return this.template == null ? Collections.emptyList() : this.template.getAccessCommands();
        }

        @Override
        public boolean shouldOffsetIslandValue() {
            return this.template != null && this.template.isOffset();
        }

        @Override
        public BlockOffset getSpawnOffset() {
            return this.template == null ? null : this.template.getSpawnOffset();
        }

        @Override
        public BigDecimal getBonusWorth() {
            return this.template == null ? BigDecimal.ZERO : this.template.getBonusWorth();
        }

        @Override
        public BigDecimal getBonusLevel() {
            return this.template == null ? BigDecimal.ZERO : this.template.getBonusLevel();
        }

        @Override
        public Biome getBiome() {
            return this.biome;
        }
    }

}
