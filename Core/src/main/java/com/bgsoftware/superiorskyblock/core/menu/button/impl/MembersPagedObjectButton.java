package com.bgsoftware.superiorskyblock.core.menu.button.impl;

import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.menu.button.PagedMenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.itemstack.ItemBuilder;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractPagedMenuButton;
import com.bgsoftware.superiorskyblock.core.menu.button.PagedMenuTemplateButtonImpl;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuIslandMembers;
import com.bgsoftware.superiorskyblock.api.menu.button.click.ButtonClickContext;
import com.bgsoftware.superiorskyblock.core.menu.view.MenuViewWrapper;
import org.bukkit.inventory.ItemStack;

public class MembersPagedObjectButton extends AbstractPagedMenuButton<MenuIslandMembers.View, SuperiorPlayer> {

    private MembersPagedObjectButton(MenuTemplateButton<MenuIslandMembers.View> templateButton, MenuIslandMembers.View menuView) {
        super(templateButton, menuView);
    }

    @Override
    public void onButtonClick(ButtonClickContext<MenuIslandMembers.View> context) {
        menuView.setPreviousMove(false);
        plugin.getMenus().openMemberManage(menuView.getInventoryViewer(), MenuViewWrapper.fromView(menuView), pagedObject);
    }

    @Override
    public ItemStack modifyViewItem(ItemBuilder itemBuilder) {
        return itemBuilder
                .replaceAll("{0}", pagedObject.getName())
                .replaceAll("{1}", pagedObject.getPlayerRole() + "")
                .asSkullOf(pagedObject)
                .build(pagedObject);
    }

    public static class Builder extends PagedMenuTemplateButtonImpl.AbstractBuilder<MenuIslandMembers.View, SuperiorPlayer> {

        @Override
        public PagedMenuTemplateButton<MenuIslandMembers.View, SuperiorPlayer> build() {
            return new PagedMenuTemplateButtonImpl<>(this, MembersPagedObjectButton.class,
                    MembersPagedObjectButton::new);
        }

    }

}
