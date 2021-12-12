package com.bgsoftware.superiorskyblock.menu.button.impl.menu;

import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.menu.SuperiorMenu;
import com.bgsoftware.superiorskyblock.menu.button.PagedObjectButton;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public final class CoopsPagedObjectButton extends PagedObjectButton<SuperiorPlayer> {

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
    public ItemStack modifyButtonItem(ItemStack buttonItem, SuperiorPlayer superiorPlayer) {
        return new ItemBuilder(buttonItem)
                .replaceAll("{0}", superiorPlayer.getName())
                .replaceAll("{1}", superiorPlayer.getPlayerRole() + "")
                .asSkullOf(superiorPlayer)
                .build(superiorPlayer);
    }

    public static class Builder extends PagedObjectBuilder<Builder, CoopsPagedObjectButton> {

        @Override
        public CoopsPagedObjectButton build() {
            return new CoopsPagedObjectButton(buttonItem, clickSound, commands, requiredPermission,
                    lackPermissionSound, nullItem);
        }

    }

}
