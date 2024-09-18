package com.bgsoftware.superiorskyblock.core.zmenu.buttons;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.config.SettingsManager;
import com.bgsoftware.superiorskyblock.api.schematic.Schematic;
import com.bgsoftware.superiorskyblock.api.world.Dimension;
import com.bgsoftware.superiorskyblock.api.wrappers.BlockOffset;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.core.zmenu.PlayerCache;
import fr.maxlego08.menu.api.utils.Placeholders;
import fr.maxlego08.menu.inventory.inventories.InventoryDefault;
import org.bukkit.Location;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.math.BigDecimal;

public class IslandCreationButton extends SuperiorButton {
    private final Schematic schematic;
    private final Biome biome;
    private final BigDecimal bonusWorth;
    private final BigDecimal bonusLevel;
    private final boolean isOffset;
    private final BlockOffset spawnOffset;

    public IslandCreationButton(SuperiorSkyblockPlugin plugin, Schematic schematic, Biome biome, BigDecimal bonusWorth, BigDecimal bonusLevel, boolean isOffset, BlockOffset spawnOffset) {
        super(plugin);
        this.schematic = schematic;
        this.biome = biome;
        this.bonusWorth = bonusWorth;
        this.bonusLevel = bonusLevel;
        this.isOffset = isOffset;
        this.spawnOffset = spawnOffset;
    }

    @Override
    public void onClick(Player player, InventoryClickEvent event, InventoryDefault inventory, int slot, Placeholders placeholders) {

        super.onClick(player, event, inventory, slot, placeholders);
        PlayerCache playerCache = getCache(player);
        SuperiorPlayer superiorPlayer = getSuperiorPlayer(player);

        if (event.getClick().isRightClick()) {
            Location previewLocation = plugin.getSettings().getPreviewIslands().get(schematic.getName());
            if (previewLocation != null) {
                plugin.getGrid().startIslandPreview(superiorPlayer, schematic.getName(), playerCache.getIslandName());
                return;
            }
        }

        Message.ISLAND_CREATE_PROCCESS_REQUEST.send(superiorPlayer);

        SettingsManager.Worlds worlds = this.plugin.getSettings().getWorlds();
        Dimension dimension = worlds.getDefaultWorldDimension();
        boolean offset = this.isOffset || worlds.getDimensionConfig(dimension).isSchematicOffset();

        this.plugin.getGrid().createIsland(superiorPlayer, schematic.getName(), bonusWorth, bonusLevel, biome, playerCache.getIslandName(), offset, spawnOffset);
    }
}
