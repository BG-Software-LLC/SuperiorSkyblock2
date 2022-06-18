package com.bgsoftware.superiorskyblock.core.menu.button.impl.menu;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.schematic.Schematic;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.GameSound;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.core.menu.button.SuperiorMenuButton;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuIslandCreation;
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

public class IslandCreationButton extends SuperiorMenuButton<MenuIslandCreation> {

    private final GameSound accessSound;
    private final List<String> accessCommands;
    private final TemplateItem lackPermissionItem;
    private final List<String> lackPermissionCommands;
    private final Biome biome;
    private final BigDecimal bonusWorth;
    private final BigDecimal bonusLevel;
    private final boolean isOffset;
    private final Schematic schematic;

    private IslandCreationButton(String requiredPermission, GameSound lackPermissionSound, GameSound accessSound,
                                 List<String> accessCommands, TemplateItem lackPermissionItem,
                                 List<String> lackPermissionCommands, Biome biome, BigDecimal bonusWorth,
                                 BigDecimal bonusLevel, boolean isOffset, TemplateItem accessItem, Schematic schematic) {
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
                buttonItem : lackPermissionItem).build(inventoryViewer);
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
        private TemplateItem noAccessItem = null;
        private List<String> noAccessCommands = null;
        private Biome biome;
        private BigDecimal bonusWorth;
        private BigDecimal bonusLevel;
        private boolean isOffset;

        public Builder(Schematic schematic) {
            this.schematic = schematic;
        }

        public Builder setAccessItem(TemplateItem accessItem) {
            this.buttonItem = accessItem;
            return this;
        }

        public Builder setNoAccessItem(TemplateItem noAccessItem) {
            this.noAccessItem = noAccessItem;
            return this;
        }

        public Builder setAccessSound(GameSound accessSound) {
            this.clickSound = accessSound;
            return this;
        }

        public Builder setNoAccessSound(GameSound noAccessSound) {
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
