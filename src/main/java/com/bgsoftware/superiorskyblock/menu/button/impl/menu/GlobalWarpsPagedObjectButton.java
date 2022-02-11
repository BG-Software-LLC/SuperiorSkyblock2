package com.bgsoftware.superiorskyblock.menu.button.impl.menu;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.menu.button.PagedObjectButton;
import com.bgsoftware.superiorskyblock.menu.impl.MenuGlobalWarps;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.utils.items.TemplateItem;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public final class GlobalWarpsPagedObjectButton extends PagedObjectButton<MenuGlobalWarps, Island> {

    private GlobalWarpsPagedObjectButton(TemplateItem buttonItem, SoundWrapper clickSound, List<String> commands,
                                         String requiredPermission, SoundWrapper lackPermissionSound,
                                         TemplateItem nullItem, int objectIndex) {
        super(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound, nullItem, objectIndex);
    }

    @Override
    public void onButtonClick(SuperiorSkyblockPlugin plugin, MenuGlobalWarps superiorMenu, InventoryClickEvent clickEvent) {
        if (MenuGlobalWarps.visitorWarps) {
            superiorMenu.setPreviousMove(false);
            plugin.getCommands().dispatchSubCommand(superiorMenu.getInventoryViewer().asPlayer(),
                    "visit", pagedObject.getOwner().getName());
        } else {
            plugin.getMenus().openWarpCategories(superiorMenu.getInventoryViewer(), superiorMenu, pagedObject);
        }
    }

    @Override
    public ItemStack modifyButtonItem(ItemStack buttonItem, MenuGlobalWarps superiorMenu, Island island) {
        return new ItemBuilder(buttonItem)
                .asSkullOf(island.getOwner())
                .replaceAll("{0}", island.getOwner().getName())
                .replaceLoreWithLines("{1}", island.getDescription().split("\n"))
                .replaceAll("{2}", island.getIslandWarps().size() + "")
                .build(island.getOwner());
    }

    public static class Builder extends PagedObjectBuilder<Builder, GlobalWarpsPagedObjectButton, MenuGlobalWarps> {

        @Override
        public GlobalWarpsPagedObjectButton build() {
            return new GlobalWarpsPagedObjectButton(buttonItem, clickSound, commands, requiredPermission,
                    lackPermissionSound, nullItem, getObjectIndex());
        }

    }

}
