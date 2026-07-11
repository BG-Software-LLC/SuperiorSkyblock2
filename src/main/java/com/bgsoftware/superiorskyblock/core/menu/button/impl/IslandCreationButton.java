package com.bgsoftware.superiorskyblock.core.menu.button.impl;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.menu.MenuIslandCreationConfig;
import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.menu.button.click.ButtonClickContext;
import com.bgsoftware.superiorskyblock.api.menu.dialog.DialogButton;
import com.bgsoftware.superiorskyblock.api.schematic.Schematic;
import com.bgsoftware.superiorskyblock.api.world.GameSound;
import com.bgsoftware.superiorskyblock.api.wrappers.BlockOffset;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.Either;
import com.bgsoftware.superiorskyblock.core.menu.MenuActions;
import com.bgsoftware.superiorskyblock.core.menu.MenuConfig;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuTemplateButton;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuViewButton;
import com.bgsoftware.superiorskyblock.core.menu.button.MenuTemplateButtonImpl;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuIslandCreation;
import org.bukkit.Bukkit;
import org.bukkit.block.Biome;
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
                getTemplate().getAccessItem() : getTemplate().lackPermissionItem.getLeft()).build(inventoryViewer);
    }

    @Override
    public void onButtonClick(ButtonClickContext<MenuIslandCreation.View> context) {
        SuperiorPlayer clickedPlayer = plugin.getPlayers().getSuperiorPlayer(context.getPlayer());
        MenuActions.simulateIslandCreationClick(clickedPlayer, menuView.getIslandName(),
                getTemplate().getCreationConfig(),
                context.getClickType().isRightClick(), menuView);
    }

    @Override
    public void onButtonClickLackPermission(ButtonClickContext<MenuIslandCreation.View> context) {
        super.onButtonClickLackPermission(context);
        getTemplate().lackPermissionCommands.forEach(command -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                command.replace("%player%", context.getPlayer().getName())));
    }

    public static class Builder extends AbstractMenuTemplateButton.AbstractBuilder<MenuIslandCreation.View> {

        private final Schematic schematic;
        private Either<TemplateItem, DialogButton> noAccessItem = null;
        private List<String> noAccessCommands = null;
        @Nullable
        private Biome biome;
        private BigDecimal bonusWorth;
        private BigDecimal bonusLevel;
        private boolean isOffset;
        private BlockOffset spawnOffset = null;

        public Builder(Schematic schematic) {
            this.schematic = schematic;
        }

        public void setAccessItem(TemplateItem accessItem) {
            this.buttonData = Either.left(accessItem);
        }

        public void setAccessDialog(DialogButton accessDialog) {
            this.buttonData = Either.right(accessDialog);
        }

        public void setNoAccessItem(TemplateItem noAccessItem) {
            this.noAccessItem = Either.left(noAccessItem);
        }

        public void setNoAccessDialog(DialogButton noAccessDialog) {
            this.noAccessItem = Either.right(noAccessDialog);
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

        public void setSpawnOffset(BlockOffset spawnOffset) {
            this.spawnOffset = spawnOffset;
        }

        @Override
        public MenuTemplateButton<MenuIslandCreation.View> build() {
            GameSound accessSound = clickSound;
            List<String> accessCommands = commands;
            this.buttonData = this.buttonData == null ? Either.left(TemplateItem.AIR) : this.buttonData;
            this.clickSound = null;
            this.commands = null;
            try {
                return new Template(this, accessSound, accessCommands, noAccessItem, noAccessCommands, biome,
                        bonusWorth, bonusLevel, isOffset, spawnOffset, schematic);
            } finally {
                this.clickSound = accessSound;
                this.commands = accessCommands;
            }
        }

    }

    public static class Template extends MenuTemplateButtonImpl<MenuIslandCreation.View> {

        @Nullable
        private final GameSound accessSound;
        private final List<String> accessCommands;
        private final Either<TemplateItem, DialogButton> lackPermissionItem;
        private final List<String> lackPermissionCommands;
        @Nullable
        private final Biome biome;
        private final BigDecimal bonusWorth;
        private final BigDecimal bonusLevel;
        private final boolean isOffset;
        private final Schematic schematic;
        @Nullable
        private final BlockOffset spawnOffset;

        private final MenuIslandCreationConfig creationConfig;

        Template(AbstractBuilder<MenuIslandCreation.View> builder,
                 @Nullable GameSound accessSound, @Nullable List<String> accessCommands,
                 @Nullable Either<TemplateItem, DialogButton> lackPermissionItem, @Nullable List<String> lackPermissionCommands,
                 @Nullable Biome biome, @Nullable BigDecimal bonusWorth, @Nullable BigDecimal bonusLevel, boolean isOffset,
                 @Nullable BlockOffset spawnOffset, Schematic schematic) {
            super(builder, IslandCreationButton.class, IslandCreationButton::new);
            this.accessSound = accessSound;
            this.accessCommands = accessCommands == null ? Collections.emptyList() : accessCommands;
            this.lackPermissionItem = lackPermissionItem == null ? Either.left(TemplateItem.AIR) : lackPermissionItem;
            this.lackPermissionCommands = lackPermissionCommands == null ? Collections.emptyList() : lackPermissionCommands;
            this.biome = biome;
            this.bonusWorth = bonusWorth == null ? BigDecimal.ZERO : bonusWorth;
            this.bonusLevel = bonusLevel == null ? BigDecimal.ZERO : bonusLevel;
            this.isOffset = isOffset;
            this.spawnOffset = spawnOffset;
            this.schematic = Objects.requireNonNull(schematic, "schematic cannot be null");
            this.creationConfig = new MenuConfig.IslandCreation(this);
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

        @Nullable
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

        public BlockOffset getSpawnOffset() {
            return spawnOffset;
        }

        public MenuIslandCreationConfig getCreationConfig() {
            return creationConfig;
        }

    }

}
