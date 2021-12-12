package com.bgsoftware.superiorskyblock.menu.button.impl.menu;

import com.bgsoftware.superiorskyblock.api.island.bank.BankTransaction;
import com.bgsoftware.superiorskyblock.menu.SuperiorMenu;
import com.bgsoftware.superiorskyblock.menu.button.PagedObjectButton;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;

public final class CoopsPagedObjectButton extends PagedObjectButton<BankTransaction> {

    private CoopsPagedObjectButton(ItemBuilder buttonItem, SoundWrapper clickSound, List<String> commands,
                                   String requiredPermission, SoundWrapper lackPermissionSound,
                                   ItemBuilder nullItem) {
        super(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound, nullItem);
    }

    @Override
    public void onButtonClick(SuperiorMenu superiorMenu, InventoryClickEvent clickEvent) {
        // Dummy button
    }

    @Override
    public ItemBuilder modifyButtonItem(ItemBuilder buttonItem, BankTransaction transaction) {
        return buttonItem
                .replaceAll("{0}", targetPlayer.getName())
                .replaceAll("{1}", targetPlayer.getPlayerRole() + "")
                .asSkullOf(targetPlayer);
    }

    public static class Builder extends PagedObjectBuilder<Builder, CoopsPagedObjectButton> {

        @Override
        public CoopsPagedObjectButton build() {
            return new CoopsPagedObjectButton(buttonItem, clickSound, commands, requiredPermission,
                    lackPermissionSound, nullItem);
        }

    }

}
