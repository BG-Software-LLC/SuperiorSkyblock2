package com.bgsoftware.superiorskyblock.core.menu.button.impl;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.schematic.Schematic;
import com.bgsoftware.superiorskyblock.api.world.GameSound;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.menu.Menus;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuTemplateButton;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuViewButton;
import com.bgsoftware.superiorskyblock.core.menu.button.MenuTemplateButtonImpl;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuIslandCreation;
import org.bukkit.Bukkit;
import org.bukkit.block.Biome;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

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
                getTemplate().getAccessItem() : getTemplate().lackPermissionItem).build(inventoryViewer);
    }

    @Override
    public void onButtonClick(InventoryClickEvent clickEvent) {
        SuperiorPlayer clickedPlayer = plugin.getPlayers().getSuperiorPlayer(clickEvent.getWhoClicked());
        Menus.MENU_ISLAND_CREATION.simulateClick(clickedPlayer, menuView.getIslandName(), getTemplate(),
                clickEvent.getClick().isRightClick(), menuView);
    }

    @Override
    public void onButtonClickLackPermission(InventoryClickEvent clickEvent) {
        super.onButtonClickLackPermission(clickEvent);
        getTemplate().lackPermissionCommands.forEach(command -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                command.replace("%player%", clickEvent.getWhoClicked().getName())));
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

        @Nullable
        private final GameSound accessSound;
        private final List<String> accessCommands;
        private final TemplateItem lackPermissionItem;
        private final List<String> lackPermissionCommands;
        private final Biome biome;
        private final BigDecimal bonusWorth;
        private final BigDecimal bonusLevel;
        private final boolean isOffset;
        private final Schematic schematic;

        Template(@Nullable String requiredPermission, @Nullable GameSound lackPermissionSound,
                 @Nullable GameSound accessSound, @Nullable List<String> accessCommands,
                 @Nullable TemplateItem lackPermissionItem, @Nullable List<String> lackPermissionCommands,
                 Biome biome, @Nullable BigDecimal bonusWorth, @Nullable BigDecimal bonusLevel, boolean isOffset,
                 @Nullable TemplateItem accessItem, Schematic schematic) {
            super(accessItem == null ? TemplateItem.AIR : accessItem, null, null, requiredPermission,
                    lackPermissionSound, IslandCreationButton.class, IslandCreationButton::new);
            this.accessSound = accessSound;
            this.accessCommands = accessCommands == null ? Collections.emptyList() : accessCommands;
            this.lackPermissionItem = lackPermissionItem == null ? TemplateItem.AIR : lackPermissionItem;
            this.lackPermissionCommands = lackPermissionCommands == null ? Collections.emptyList() : lackPermissionCommands;
            this.biome = Objects.requireNonNull(biome, "biome cannot be null");
            this.bonusWorth = bonusWorth == null ? BigDecimal.ZERO : bonusWorth;
            this.bonusLevel = bonusLevel == null ? BigDecimal.ZERO : bonusLevel;
            this.isOffset = isOffset;
            this.schematic = Objects.requireNonNull(schematic, "schematic cannot be null");
        }

        public TemplateItem getAccessItem() {
            return super.getButtonTemplateItem();
        }

        @Nullable
        public GameSound getAccessSound() {
            return accessSound;
        }

        public List<String> getAccessCommands() {
            return accessCommands;
        }

        public Biome getBiome() {
            return biome;
        }

        public BigDecimal getBonusWorth() {
            return bonusWorth;
        }

        public BigDecimal getBonusLevel() {
            return bonusLevel;
        }

        public boolean isOffset() {
            return isOffset;
        }

        public Schematic getSchematic() {
            return schematic;
        }
    }

}
