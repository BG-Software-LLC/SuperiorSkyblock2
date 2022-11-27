package com.bgsoftware.superiorskyblock.core.menu.button.impl;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.schematic.Schematic;
import com.bgsoftware.superiorskyblock.api.world.GameSound;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.GameSoundImpl;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuTemplateButton;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuViewButton;
import com.bgsoftware.superiorskyblock.core.menu.button.MenuTemplateButtonImpl;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuIslandCreation;
import com.bgsoftware.superiorskyblock.core.messages.Message;
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

public class IslandCreationButton extends AbstractMenuViewButton<MenuIslandCreation.View> {

    private IslandCreationButton(AbstractMenuTemplateButton<MenuIslandCreation.View> templateButton, MenuIslandCreation.View menuView) {
        super(templateButton, menuView);
    }

    @Override
    public Template getTemplate() {
        return (Template) super.getTemplate();
    }

    @Override
    public ItemStack createViewItem() {
        SuperiorPlayer inventoryViewer = menuView.getInventoryViewer();
        String requiredPermission = getTemplate().getRequiredPermission();
        return (requiredPermission == null || inventoryViewer.hasPermission(requiredPermission) ?
                getTemplate().getButtonTemplateItem() : getTemplate().lackPermissionItem).build(inventoryViewer);
    }

    @Override
    public void onButtonClick(InventoryClickEvent clickEvent) {
        clickButton(plugin, (Player) clickEvent.getWhoClicked(), clickEvent.getClick().isRightClick(),
                menuView.getIslandName(), menuView);
    }

    @Override
    public void onButtonClickLackPermission(InventoryClickEvent clickEvent) {
        super.onButtonClickLackPermission(clickEvent);
        getTemplate().lackPermissionCommands.forEach(command -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                command.replace("%player%", clickEvent.getWhoClicked().getName())));
    }

    public void clickButton(SuperiorSkyblockPlugin plugin, Player whoClicked, boolean isRightClick,
                            String islandName, @Nullable MenuIslandCreation.View menuView) {
        SuperiorPlayer clickedPlayer = plugin.getPlayers().getSuperiorPlayer(whoClicked);

        // Checking for preview of islands.
        if (isRightClick) {
            Location previewLocation = plugin.getSettings().getPreviewIslands().get(getTemplate().schematic.getName());
            if (previewLocation != null) {
                plugin.getGrid().startIslandPreview(clickedPlayer, getTemplate().schematic.getName(), islandName);
                return;
            }
        }

        GameSoundImpl.playSound(whoClicked, getTemplate().accessSound);

        getTemplate().accessCommands.forEach(command -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                command.replace("%player%", clickedPlayer.getName())));

        Message.ISLAND_CREATE_PROCCESS_REQUEST.send(clickedPlayer);

        if (menuView != null)
            menuView.closeView();

        World.Environment environment = plugin.getSettings().getWorlds().getDefaultWorld();
        boolean offset = getTemplate().isOffset || (environment == World.Environment.NORMAL ?
                plugin.getSettings().getWorlds().getNormal().isSchematicOffset() :
                environment == World.Environment.NETHER ? plugin.getSettings().getWorlds().getNether().isSchematicOffset() :
                        plugin.getSettings().getWorlds().getEnd().isSchematicOffset());

        plugin.getGrid().createIsland(clickedPlayer, getTemplate().schematic.getName(), getTemplate().bonusWorth,
                getTemplate().bonusLevel, getTemplate().biome, islandName, offset);
    }

    public static class Builder extends AbstractMenuTemplateButton.AbstractBuilder<MenuIslandCreation.View> {

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

        public void setAccessItem(TemplateItem accessItem) {
            this.buttonItem = accessItem;
        }

        public void setNoAccessItem(TemplateItem noAccessItem) {
            this.noAccessItem = noAccessItem;
        }

        public void setAccessSound(GameSound accessSound) {
            this.clickSound = accessSound;
        }

        public void setNoAccessSound(GameSound noAccessSound) {
            this.lackPermissionSound = noAccessSound;
        }

        public void setAccessCommands(List<String> accessCommands) {
            this.commands = accessCommands;
        }

        public void setNoAccessCommands(List<String> noAccessCommands) {
            this.noAccessCommands = noAccessCommands;
        }

        public void setBiome(Biome biome) {
            this.biome = biome;
        }

        public void setBonusWorth(BigDecimal bonusWorth) {
            this.bonusWorth = bonusWorth;
        }

        public void setBonusLevel(BigDecimal bonusLevel) {
            this.bonusLevel = bonusLevel;
        }

        public void setOffset(boolean isOffset) {
            this.isOffset = isOffset;
        }

        @Override
        public MenuTemplateButton<MenuIslandCreation.View> build() {
            return new Template(requiredPermission, lackPermissionSound, clickSound, commands,
                    noAccessItem, noAccessCommands, biome, bonusWorth, bonusLevel, isOffset, buttonItem, schematic);
        }

    }

    public static class Template extends MenuTemplateButtonImpl<MenuIslandCreation.View> {

        private final GameSound accessSound;
        private final List<String> accessCommands;
        private final TemplateItem lackPermissionItem;
        private final List<String> lackPermissionCommands;
        private final Biome biome;
        private final BigDecimal bonusWorth;
        private final BigDecimal bonusLevel;
        private final boolean isOffset;
        private final Schematic schematic;

        Template(String requiredPermission, GameSound lackPermissionSound, GameSound accessSound,
                 List<String> accessCommands, TemplateItem lackPermissionItem, List<String> lackPermissionCommands,
                 Biome biome, BigDecimal bonusWorth, BigDecimal bonusLevel, boolean isOffset, TemplateItem accessItem,
                 Schematic schematic) {
            super(accessItem, null, null, requiredPermission, lackPermissionSound,
                    IslandCreationButton.class, IslandCreationButton::new);
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

        public Schematic getSchematic() {
            return schematic;
        }
    }

}
