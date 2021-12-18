package com.bgsoftware.superiorskyblock.menu.button.impl.menu;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.schematic.Schematic;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.lang.Message;
import com.bgsoftware.superiorskyblock.menu.button.SuperiorMenuButton;
import com.bgsoftware.superiorskyblock.menu.impl.MenuIslandCreation;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

public final class IslandCreationButton extends SuperiorMenuButton<MenuIslandCreation> {

    private final SoundWrapper accessSound;
    private final List<String> accessCommands;
    private final ItemBuilder lackPermissionItem;
    private final List<String> lackPermissionCommands;
    private final Biome biome;
    private final BigDecimal bonusWorth;
    private final BigDecimal bonusLevel;
    private final boolean isOffset;
    private final Schematic schematic;

    private IslandCreationButton(String requiredPermission, SoundWrapper lackPermissionSound, SoundWrapper accessSound,
                                 List<String> accessCommands, ItemBuilder lackPermissionItem,
                                 List<String> lackPermissionCommands, Biome biome, BigDecimal bonusWorth,
                                 BigDecimal bonusLevel, boolean isOffset, ItemBuilder accessItem, Schematic schematic) {
        super(accessItem, null, null, requiredPermission, lackPermissionSound);
        this.accessSound = accessSound;
        this.accessCommands = accessCommands == null ? Collections.emptyList() : accessCommands;
        this.lackPermissionItem = lackPermissionItem;
        this.lackPermissionCommands = lackPermissionCommands == null ? Collections.emptyList() : lackPermissionCommands;
        this.biome = biome;
        this.bonusWorth = bonusWorth;
        this.bonusLevel = bonusLevel;
        this.isOffset = isOffset;
        this.schematic = schematic;
    }

    public List<String> getLackPermissionCommands() {
        return lackPermissionCommands;
    }

    public Schematic getSchematic() {
        return schematic;
    }

    @Nullable
    @Override
    public ItemStack getButtonItem(MenuIslandCreation superiorMenu) {
        SuperiorPlayer inventoryViewer = superiorMenu.getInventoryViewer();
        return (requiredPermission == null || inventoryViewer.hasPermission(requiredPermission) ?
                buttonItem : lackPermissionItem).clone().build(inventoryViewer);
    }

    @Override
    public void onButtonClick(SuperiorSkyblockPlugin plugin, MenuIslandCreation superiorMenu,
                              InventoryClickEvent clickEvent) {
        clickButton(plugin, (Player) clickEvent.getWhoClicked(), clickEvent.getClick().isRightClick(),
                superiorMenu.getIslandName(), superiorMenu);
    }

    public void clickButton(SuperiorSkyblockPlugin plugin, Player whoClicked, boolean isRightClick,
                            String islandName, @Nullable MenuIslandCreation superiorMenu) {
        SuperiorPlayer clickedPlayer = plugin.getPlayers().getSuperiorPlayer(whoClicked);

        // Checking for preview of islands.
        if (isRightClick) {
            Location previewLocation = plugin.getSettings().getPreviewIslands().get(schematic.getName());
            if (previewLocation != null) {
                plugin.getGrid().startIslandPreview(clickedPlayer, schematic.getName(), islandName);
                return;
            }
        }

        if (accessSound != null)
            accessSound.playSound(whoClicked);

        accessCommands.forEach(command -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                command.replace("%player%", clickedPlayer.getName())));

        Message.ISLAND_CREATE_PROCCESS_REQUEST.send(clickedPlayer);

        if (superiorMenu != null)
            superiorMenu.closePage();

        World.Environment environment = plugin.getSettings().getWorlds().getDefaultWorld();
        boolean offset = isOffset || (environment == World.Environment.NORMAL ? plugin.getSettings().getWorlds().getNormal().isSchematicOffset() :
                environment == World.Environment.NETHER ? plugin.getSettings().getWorlds().getNether().isSchematicOffset() :
                        plugin.getSettings().getWorlds().getEnd().isSchematicOffset());

        plugin.getGrid().createIsland(clickedPlayer, schematic.getName(), bonusWorth, bonusLevel, biome,
                islandName, offset);
    }

    public static class Builder extends AbstractBuilder<Builder, IslandCreationButton, MenuIslandCreation> {

        private final Schematic schematic;
        private ItemBuilder noAccessItem = null;
        private List<String> noAccessCommands = null;
        private Biome biome;
        private BigDecimal bonusWorth;
        private BigDecimal bonusLevel;
        private boolean isOffset;

        public Builder(Schematic schematic) {
            this.schematic = schematic;
        }

        public Builder setAccessItem(ItemBuilder accessItem) {
            this.buttonItem = accessItem;
            return this;
        }

        public Builder setNoAccessItem(ItemBuilder noAccessItem) {
            this.noAccessItem = noAccessItem;
            return this;
        }

        public Builder setAccessSound(SoundWrapper accessSound) {
            this.clickSound = accessSound;
            return this;
        }

        public Builder setNoAccessSound(SoundWrapper noAccessSound) {
            this.lackPermissionSound = noAccessSound;
            return this;
        }

        public Builder setAccessCommands(List<String> accessCommands) {
            this.commands = accessCommands;
            return this;
        }

        public Builder setNoAccessCommands(List<String> noAccessCommands) {
            this.noAccessCommands = noAccessCommands;
            return this;
        }

        public Builder setBiome(Biome biome) {
            this.biome = biome;
            return this;
        }

        public Builder setBonusWorth(BigDecimal bonusWorth) {
            this.bonusWorth = bonusWorth;
            return this;
        }

        public Builder setBonusLevel(BigDecimal bonusLevel) {
            this.bonusLevel = bonusLevel;
            return this;
        }

        public Builder setOffset(boolean isOffset) {
            this.isOffset = isOffset;
            return this;
        }

        @Override
        public IslandCreationButton build() {
            return new IslandCreationButton(requiredPermission, lackPermissionSound, clickSound, commands,
                    noAccessItem, noAccessCommands, biome, bonusWorth, bonusLevel, isOffset, buttonItem, schematic);
        }

    }

}
