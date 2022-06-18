package com.bgsoftware.superiorskyblock.core.menu.button.impl.menu;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.GameSound;
import com.bgsoftware.superiorskyblock.core.itemstack.ItemBuilder;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;
import com.bgsoftware.superiorskyblock.core.menu.button.PagedObjectButton;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuCoops;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class CoopsPagedObjectButton extends PagedObjectButton<MenuCoops, SuperiorPlayer> {

    private CoopsPagedObjectButton(TemplateItem buttonItem, GameSound clickSound, List<String> commands,
                                   String requiredPermission, GameSound lackPermissionSound,
                                   TemplateItem nullItem, int objectIndex) {
        super(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound, nullItem, objectIndex);
    }

    @Override
    public void onButtonClick(SuperiorSkyblockPlugin plugin, MenuCoops superiorMenu, InventoryClickEvent clickEvent) {
        // Dummy button
    }

    @Override
    public ItemStack modifyButtonItem(ItemStack buttonItem, MenuCoops superiorMenu, SuperiorPlayer superiorPlayer) {
        return new ItemBuilder(buttonItem)
                .replaceAll("{0}", superiorPlayer.getName())
                .replaceAll("{1}", superiorPlayer.getPlayerRole() + "")
                .asSkullOf(superiorPlayer)
                .build(superiorPlayer);
    }

    public static class Builder extends PagedObjectBuilder<Builder, CoopsPagedObjectButton, MenuCoops> {

        @Override
        public CoopsPagedObjectButton build() {
            return new CoopsPagedObjectButton(buttonItem, clickSound, commands, requiredPermission,
                    lackPermissionSound, nullItem, getObjectIndex());
        }

    }

}
