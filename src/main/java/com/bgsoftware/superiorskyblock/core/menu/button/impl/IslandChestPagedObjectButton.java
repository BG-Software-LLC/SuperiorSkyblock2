package com.bgsoftware.superiorskyblock.core.menu.button.impl;

import com.bgsoftware.superiorskyblock.api.island.IslandChest;
import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.menu.button.PagedMenuTemplateButton;
import com.bgsoftware.superiorskyblock.core.itemstack.ItemBuilder;
import com.bgsoftware.superiorskyblock.api.menu.button.click.ButtonClickContext;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractPagedMenuButton;
import com.bgsoftware.superiorskyblock.core.menu.button.PagedMenuTemplateButtonImpl;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuIslandChest;
import org.bukkit.inventory.ItemStack;

public class IslandChestPagedObjectButton extends AbstractPagedMenuButton<MenuIslandChest.View, IslandChest> {

    private IslandChestPagedObjectButton(MenuTemplateButton<MenuIslandChest.View> templateButton, MenuIslandChest.View menuView) {
        super(templateButton, menuView);
    }

    @Override
    public void onButtonClick(ButtonClickContext<MenuIslandChest.View> context) {
        menuView.setPreviousMove(false);
        pagedObject.openChest(menuView.getInventoryViewer());
    }

    @Override
    public ItemStack modifyViewItem(ItemBuilder itemBuilder) {
        return itemBuilder
                .replaceAll("{0}", (pagedObject.getIndex() + 1) + "")
                .replaceAll("{1}", (pagedObject.getRows() * 9) + "")
                .build(menuView.getInventoryViewer());
    }

    public static class Builder extends PagedMenuTemplateButtonImpl.AbstractBuilder<MenuIslandChest.View, IslandChest> {

        @Override
        public PagedMenuTemplateButton<MenuIslandChest.View, IslandChest> build() {
            return new PagedMenuTemplateButtonImpl<>(this, IslandChestPagedObjectButton.class,
                    IslandChestPagedObjectButton::new);
        }

    }

}
