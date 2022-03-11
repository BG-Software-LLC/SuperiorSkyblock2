package com.bgsoftware.superiorskyblock.menu.button.impl.menu;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.warps.WarpCategory;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.lang.Message;
import com.bgsoftware.superiorskyblock.menu.button.SuperiorMenuButton;
import com.bgsoftware.superiorskyblock.menu.impl.MenuWarpCategories;
import com.bgsoftware.superiorskyblock.menu.impl.MenuWarpCategoryManage;
import com.bgsoftware.superiorskyblock.player.chat.PlayerChat;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.utils.items.TemplateItem;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class WarpCategoryManageIconButton extends SuperiorMenuButton<MenuWarpCategoryManage> {

    private WarpCategoryManageIconButton(TemplateItem buttonItem, SoundWrapper clickSound, List<String> commands,
                                         String requiredPermission, SoundWrapper lackPermissionSound) {
        super(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound);
    }

    @Override
    public void onButtonClick(SuperiorSkyblockPlugin plugin, MenuWarpCategoryManage superiorMenu,
                              InventoryClickEvent clickEvent) {
        SuperiorPlayer clickedPlayer = plugin.getPlayers().getSuperiorPlayer(clickEvent.getWhoClicked());
        WarpCategory warpCategory = superiorMenu.getWarpCategory();

        if (clickEvent.getClick().isRightClick()) {
            superiorMenu.setPreviousMove(false);
            plugin.getMenus().openWarpCategoryIconEdit(clickedPlayer, superiorMenu, warpCategory);
            return;
        }

        Player player = (Player) clickEvent.getWhoClicked();

        Message.WARP_CATEGORY_SLOT.send(player);

        superiorMenu.closePage();

        PlayerChat.listen(player, message -> {
            if (!message.equalsIgnoreCase("-cancel")) {
                int slot;

                try {
                    slot = Integer.parseInt(message);
                    if (slot < 0 || slot >= MenuWarpCategories.rowsSize * 9)
                        throw new IllegalArgumentException();
                } catch (IllegalArgumentException ex) {
                    Message.INVALID_SLOT.send(player, message);
                    return true;
                }

                if (warpCategory.getIsland().getWarpCategory(slot) != null) {
                    Message.WARP_CATEGORY_SLOT_ALREADY_TAKEN.send(player);
                    return true;
                }

                warpCategory.setSlot(slot);
                Message.WARP_CATEGORY_SLOT_SUCCESS.send(player, slot);

                if (MenuWarpCategoryManage.successUpdateSound != null)
                    MenuWarpCategoryManage.successUpdateSound.playSound(player);
            }

            PlayerChat.remove(player);

            superiorMenu.open(superiorMenu.getPreviousMenu());

            return true;
        });
    }

    @Nullable
    @Override
    public ItemStack getButtonItem(MenuWarpCategoryManage superiorMenu) {
        WarpCategory warpCategory = superiorMenu.getWarpCategory();

        ItemBuilder itemBuilder = new ItemBuilder(warpCategory.getRawIcon());
        ItemStack buttonItem = super.getButtonItem(superiorMenu);

        if (buttonItem != null && buttonItem.hasItemMeta()) {
            ItemMeta itemMeta = buttonItem.getItemMeta();
            if (itemMeta.hasDisplayName())
                itemBuilder.withName(itemMeta.getDisplayName());

            if (itemMeta.hasLore())
                itemBuilder.appendLore(itemMeta.getLore());
        }

        return itemBuilder.build(warpCategory.getIsland().getOwner());
    }

    public static class Builder extends AbstractBuilder<Builder, WarpCategoryManageIconButton, MenuWarpCategoryManage> {

        @Override
        public WarpCategoryManageIconButton build() {
            return new WarpCategoryManageIconButton(buttonItem, clickSound, commands, requiredPermission,
                    lackPermissionSound);
        }

    }

}
