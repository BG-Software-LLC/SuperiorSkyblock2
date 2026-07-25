package com.bgsoftware.superiorskyblock.core.menu.button.impl;

import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.menu.button.PagedMenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.itemstack.ItemBuilder;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractPagedMenuButton;
import com.bgsoftware.superiorskyblock.core.menu.button.PagedMenuTemplateButtonImpl;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuCoops;
import com.bgsoftware.superiorskyblock.api.menu.button.click.ButtonClickContext;
import org.bukkit.inventory.ItemStack;

public class CoopsPagedObjectButton extends AbstractPagedMenuButton<MenuCoops.View, SuperiorPlayer> {

    private CoopsPagedObjectButton(MenuTemplateButton<MenuCoops.View> templateButton, MenuCoops.View menuView) {
        super(templateButton, menuView);
    }

    @Override
    public void onButtonClick(ButtonClickContext<MenuCoops.View> context) {
        plugin.getCommands().dispatchSubCommand(context.getPlayer(), "uncoop", pagedObject.getName());
    }

    @Override
    public ItemStack modifyViewItem(ItemBuilder itemBuilder) {
        return itemBuilder
                .replaceAll("{0}", pagedObject.getName())
                .replaceAll("{1}", pagedObject.getPlayerRole() + "")
                .asSkullOf(pagedObject)
                .build(pagedObject);
    }

    public static class Builder extends PagedMenuTemplateButtonImpl.AbstractBuilder<MenuCoops.View, SuperiorPlayer> {

        @Override
        public PagedMenuTemplateButton<MenuCoops.View, SuperiorPlayer> build() {
            return new PagedMenuTemplateButtonImpl<>(this, CoopsPagedObjectButton.class,
                    CoopsPagedObjectButton::new);
        }

    }

}
