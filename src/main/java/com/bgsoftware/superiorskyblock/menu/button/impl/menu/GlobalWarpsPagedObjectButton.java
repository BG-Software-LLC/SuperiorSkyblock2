package com.bgsoftware.superiorskyblock.menu.button.impl.menu;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.menu.SuperiorMenu;
import com.bgsoftware.superiorskyblock.menu.button.PagedObjectButton;
import com.bgsoftware.superiorskyblock.menu.impl.MenuGlobalWarps;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public final class GlobalWarpsPagedObjectButton extends PagedObjectButton<Island> {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private GlobalWarpsPagedObjectButton(ItemBuilder buttonItem, SoundWrapper clickSound, List<String> commands,
                                         String requiredPermission, SoundWrapper lackPermissionSound,
                                         ItemBuilder nullItem) {
        super(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound, nullItem);
    }

    @Override
    public void onButtonClick(SuperiorMenu superiorMenu, InventoryClickEvent clickEvent) {
        if (MenuGlobalWarps.visitorWarps) {
            superiorMenu.setPreviousMove(false);
            plugin.getCommands().dispatchSubCommand(inventoryViewer.asPlayer(), "visit", pagedObject.getOwner().getName());
        } else {
            plugin.getMenus().openWarpCategories(inventoryViewer, superiorMenu, pagedObject);
        }
    }

    @Override
    public ItemStack modifyButtonItem(ItemStack buttonItem, Island island) {
        return new ItemBuilder(buttonItem)
                .asSkullOf(island.getOwner())
                .replaceAll("{0}", island.getOwner().getName())
                .replaceLoreWithLines("{1}", island.getDescription().split("\n"))
                .replaceAll("{2}", island.getIslandWarps().size() + "")
                .build(island.getOwner());
    }

    public static class Builder extends PagedObjectBuilder<Builder, GlobalWarpsPagedObjectButton> {

        @Override
        public GlobalWarpsPagedObjectButton build() {
            return new GlobalWarpsPagedObjectButton(buttonItem, clickSound, commands, requiredPermission,
                    lackPermissionSound, nullItem);
        }

    }

}
