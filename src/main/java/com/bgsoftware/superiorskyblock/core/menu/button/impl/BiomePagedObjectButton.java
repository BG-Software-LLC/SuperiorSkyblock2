package com.bgsoftware.superiorskyblock.core.menu.button.impl;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.menu.button.PagedMenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.menu.button.click.ButtonClickContext;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.GameSoundImpl;
import com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEvent;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEventsFactory;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.itemstack.ItemBuilder;
import com.bgsoftware.superiorskyblock.core.menu.Menus;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractPagedMenuButton;
import com.bgsoftware.superiorskyblock.core.menu.button.PagedMenuTemplateButtonImpl;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuBiomes;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class BiomePagedObjectButton extends AbstractPagedMenuButton<MenuBiomes.View, MenuBiomes.BiomeInfo> {

    private BiomePagedObjectButton(MenuTemplateButton<MenuBiomes.View> templateButton, MenuBiomes.View menuView) {
        super(templateButton, menuView);
    }

    @Override
    public void onButtonClick(ButtonClickContext<MenuBiomes.View> context) {
        SuperiorPlayer inventoryViewer = menuView.getInventoryViewer();
        Player player = inventoryViewer.asPlayer();

        String requiredPermission = pagedObject.getRequiredPermission();

        if (requiredPermission != null && !inventoryViewer.hasPermission(requiredPermission)) {
            GameSoundImpl.playSound(player, pagedObject.getNoAccessSound());
            return;
        }

        PluginEvent<PluginEventArgs.IslandBiomeChange> event = PluginEventsFactory.callIslandBiomeChangeEvent(
                menuView.getIsland(), inventoryViewer, menuView.getDimension(), pagedObject.getBiome());

        if (event.isCancelled()) {
            GameSoundImpl.playSound(player, pagedObject.getNoAccessSound());
            return;
        }

        GameSoundImpl.playSound(player, pagedObject.getAccessSound());

        pagedObject.getAccessCommands().forEach(command -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                command.replace("%player%", inventoryViewer.getName())));

        menuView.getIsland().setBiome(menuView.getDimension(), event.getArgs().biome);
        Message.CHANGED_BIOME.send(inventoryViewer,
                Formatters.CAPITALIZED_FORMATTER.format(event.getArgs().biome.name()),
                Formatters.CAPITALIZED_FORMATTER.format(menuView.getDimension().getName()));

        BukkitExecutor.sync(menuView::closeView, 1L);
    }

    @Override
    public void onButtonClickLackPermission(ButtonClickContext<MenuBiomes.View> context) {
        super.onButtonClickLackPermission(context);
        pagedObject.getNoAccessCommands().forEach(command -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                command.replace("%player%", menuView.getInventoryViewer().getName())));
    }

    @Override
    public ItemStack modifyViewItem(ItemBuilder itemBuilder) {
        ItemStack buttonItem = null;
        SuperiorPlayer inventoryViewer = menuView.getInventoryViewer();

        String requiredPermission = pagedObject.getRequiredPermission();
        if (requiredPermission == null || inventoryViewer.hasPermission(requiredPermission)) {
            buttonItem = pagedObject.getAccessItem().build(inventoryViewer);
        } else if (pagedObject.getNoAccessItem() != null) {
            buttonItem = pagedObject.getNoAccessItem().build(inventoryViewer);
        }

        if (buttonItem == null || !Menus.MENU_BIOMES.isCurrentBiomeGlow()) {
            return buttonItem;
        }

        Island island = inventoryViewer.getIsland();
        if (island == null || island.getBiome(menuView.getDimension()) != pagedObject.getBiome()) {
            return buttonItem;
        }

        return new ItemBuilder(buttonItem).makeItemGlow().build();
    }

    public static class Builder extends PagedMenuTemplateButtonImpl.AbstractBuilder<MenuBiomes.View, MenuBiomes.BiomeInfo> {

        @Override
        public PagedMenuTemplateButton<MenuBiomes.View, MenuBiomes.BiomeInfo> build() {
            return new PagedMenuTemplateButtonImpl<>(this, BiomePagedObjectButton.class,
                    BiomePagedObjectButton::new);
        }

    }

}
