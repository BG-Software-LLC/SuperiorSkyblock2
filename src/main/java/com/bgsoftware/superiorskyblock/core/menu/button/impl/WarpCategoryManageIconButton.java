package com.bgsoftware.superiorskyblock.core.menu.button.impl;

import com.bgsoftware.superiorskyblock.api.island.warps.WarpCategory;
import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.GameSoundImpl;
import com.bgsoftware.superiorskyblock.core.events.EventResult;
import com.bgsoftware.superiorskyblock.core.itemstack.ItemBuilder;
import com.bgsoftware.superiorskyblock.core.menu.Menus;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuTemplateButton;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuViewButton;
import com.bgsoftware.superiorskyblock.core.menu.button.MenuTemplateButtonImpl;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuWarpCategoryManage;
import com.bgsoftware.superiorskyblock.core.menu.view.MenuViewWrapper;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.player.chat.PlayerChat;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class WarpCategoryManageIconButton extends AbstractMenuViewButton<MenuWarpCategoryManage.View> {

    private WarpCategoryManageIconButton(AbstractMenuTemplateButton<MenuWarpCategoryManage.View> templateButton, MenuWarpCategoryManage.View menuView) {
        super(templateButton, menuView);
    }

    @Override
    public ItemStack createViewItem() {
        WarpCategory warpCategory = menuView.getWarpCategory();

        ItemBuilder itemBuilder = new ItemBuilder(warpCategory.getRawIcon());
        ItemStack buttonItem = super.createViewItem();

        if (buttonItem != null && buttonItem.hasItemMeta()) {
            ItemMeta itemMeta = buttonItem.getItemMeta();
            if (itemMeta.hasDisplayName())
                itemBuilder.withName(itemMeta.getDisplayName());

            if (itemMeta.hasLore())
                itemBuilder.appendLore(itemMeta.getLore());
        }

        return itemBuilder.build(warpCategory.getIsland().getOwner());
    }

    @Override
    public void onButtonClick(InventoryClickEvent clickEvent) {
        SuperiorPlayer inventoryViewer = menuView.getInventoryViewer();
        WarpCategory warpCategory = menuView.getWarpCategory();

        if (clickEvent.getClick().isRightClick()) {
            menuView.setPreviousMove(false);
            plugin.getMenus().openWarpCategoryIconEdit(inventoryViewer, MenuViewWrapper.fromView(menuView), warpCategory);
            return;
        }

        Player player = (Player) clickEvent.getWhoClicked();

        Message.WARP_CATEGORY_SLOT.send(player);

        menuView.closeView();

        PlayerChat.listen(player, message -> {
            if (!message.equalsIgnoreCase("-cancel")) {
                int rowsSize = Menus.MENU_WARP_CATEGORIES.getRowsSize();

                int slot;

                try {
                    slot = Integer.parseInt(message);
                    if (slot < 0 || slot >= rowsSize * 9)
                        throw new IllegalArgumentException();
                } catch (IllegalArgumentException ex) {
                    Message.INVALID_SLOT.send(player, message);
                    return true;
                }

                if (warpCategory.getIsland().getWarpCategory(slot) != null) {
                    Message.WARP_CATEGORY_SLOT_ALREADY_TAKEN.send(player);
                    return true;
                }

                EventResult<Integer> eventResult = plugin.getEventsBus().callIslandChangeWarpCategorySlotEvent(
                        inventoryViewer, warpCategory.getIsland(), warpCategory, slot, rowsSize * 9);

                if (!eventResult.isCancelled()) {
                    warpCategory.setSlot(eventResult.getResult());

                    Message.WARP_CATEGORY_SLOT_SUCCESS.send(player, eventResult.getResult());

                    GameSoundImpl.playSound(player, Menus.MENU_WARP_CATEGORY_MANAGE.getSuccessUpdateSound());
                }
            }

            PlayerChat.remove(player);

            menuView.refreshView();

            return true;
        });
    }

    public static class Builder extends AbstractMenuTemplateButton.AbstractBuilder<MenuWarpCategoryManage.View> {

        @Override
        public MenuTemplateButton<MenuWarpCategoryManage.View> build() {
            return new MenuTemplateButtonImpl<>(buttonItem, clickSound, commands, requiredPermission,
                    lackPermissionSound, WarpCategoryManageIconButton.class, WarpCategoryManageIconButton::new);
        }

    }

}
