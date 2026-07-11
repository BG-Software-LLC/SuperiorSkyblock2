package com.bgsoftware.superiorskyblock.core.menu.button.impl;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.world.GameSound;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.api.menu.dialog.DialogButton;
import com.bgsoftware.superiorskyblock.core.Either;
import com.bgsoftware.superiorskyblock.core.GameSoundImpl;
import com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEvent;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEventsFactory;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.itemstack.ItemBuilder;
import com.bgsoftware.superiorskyblock.core.menu.Menus;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuTemplateButton;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuViewButton;
import com.bgsoftware.superiorskyblock.core.menu.button.MenuTemplateButtonImpl;
import com.bgsoftware.superiorskyblock.core.menu.view.impl.IslandMenuView;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.api.menu.button.click.ButtonClickContext;
import org.bukkit.Bukkit;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class BiomeButton extends AbstractMenuViewButton<IslandMenuView> {

    private BiomeButton(AbstractMenuTemplateButton<IslandMenuView> templateButton, IslandMenuView menuView) {
        super(templateButton, menuView);
    }

    @Override
    public Template getTemplate() {
        return (Template) super.getTemplate();
    }

    @Nullable
    @Override
    public ItemStack createViewItem() {
        ItemStack buttonItem = null;

        SuperiorPlayer inventoryViewer = menuView.getInventoryViewer();

        String requiredPermission = getTemplate().getRequiredPermission();

        if (requiredPermission == null || inventoryViewer.hasPermission(requiredPermission)) {
            buttonItem = super.createViewItem();
        } else if (getTemplate().lackPermissionItem != null) {
            buttonItem = getTemplate().lackPermissionItem.getLeft().build(inventoryViewer);
        }

        if (buttonItem == null || !Menus.MENU_BIOMES.isCurrentBiomeGlow())
            return buttonItem;

        Island island = inventoryViewer.getIsland();

        if (island == null || island.getBiome() != getTemplate().biome)
            return buttonItem;

        return new ItemBuilder(buttonItem)
                .makeItemGlow()
                .build();
    }

    @Override
    public void onButtonClick(ButtonClickContext<IslandMenuView> context) {
        SuperiorPlayer inventoryViewer = menuView.getInventoryViewer();
        Player player = inventoryViewer.asPlayer();

        PluginEvent<PluginEventArgs.IslandBiomeChange> event = PluginEventsFactory.callIslandBiomeChangeEvent(
                menuView.getIsland(), inventoryViewer, getTemplate().biome);

        if (event.isCancelled()) {
            GameSoundImpl.playSound(player, getTemplate().getLackPermissionSound());
            return;
        }

        GameSoundImpl.playSound(player, getTemplate().accessSound);

        getTemplate().accessCommands.forEach(command -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                command.replace("%player%", inventoryViewer.getName())));

        menuView.getIsland().setBiome(event.getArgs().biome);
        Message.CHANGED_BIOME.send(inventoryViewer,
                Formatters.CAPITALIZED_FORMATTER.format(event.getArgs().biome.name()));

        BukkitExecutor.sync(menuView::closeView, 1L);
    }

    @Override
    public void onButtonClickLackPermission(ButtonClickContext<IslandMenuView> context) {
        super.onButtonClickLackPermission(context);
        getTemplate().lackPermissionCommands.forEach(command -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                command.replace("%player%", menuView.getInventoryViewer().getName())));
    }

    public static class Builder extends AbstractMenuTemplateButton.AbstractBuilder<IslandMenuView> {

        private final Biome biome;
        private Either<TemplateItem, DialogButton> noAccessItem = null;
        private List<String> noAccessCommands = null;

        public Builder(Biome biome) {
            this.biome = biome;
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

        @Override
        public MenuTemplateButton<IslandMenuView> build() {
            GameSound accessSound = clickSound;
            List<String> accessCommands = commands;
            this.clickSound = null;
            this.commands = null;
            try {
                return new Template(this, accessSound, accessCommands, noAccessItem, noAccessCommands, biome);
            } finally {
                this.clickSound = accessSound;
                this.commands = accessCommands;
            }
        }

    }

    public static class Template extends MenuTemplateButtonImpl<IslandMenuView> {

        @Nullable
        private final GameSound accessSound;
        private final List<String> accessCommands;
        @Nullable
        private final Either<TemplateItem, DialogButton> lackPermissionItem;
        private final List<String> lackPermissionCommands;
        private final Biome biome;

        Template(AbstractBuilder<IslandMenuView> builder, @Nullable GameSound accessSound,
                 @Nullable List<String> accessCommands, @Nullable Either<TemplateItem, DialogButton> lackPermissionItem,
                 @Nullable List<String> lackPermissionCommands, Biome biome) {
            super(builder, BiomeButton.class, BiomeButton::new);
            this.accessSound = accessSound;
            this.accessCommands = accessCommands == null ? Collections.emptyList() : accessCommands;
            this.lackPermissionItem = lackPermissionItem;
            this.lackPermissionCommands = lackPermissionCommands == null ? Collections.emptyList() : lackPermissionCommands;
            this.biome = Objects.requireNonNull(biome, "biome cannot be null");
        }

    }

}
