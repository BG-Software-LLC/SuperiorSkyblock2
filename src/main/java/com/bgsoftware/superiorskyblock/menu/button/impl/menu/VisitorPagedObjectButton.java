package com.bgsoftware.superiorskyblock.menu.button.impl.menu;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.menu.button.PagedObjectButton;
import com.bgsoftware.superiorskyblock.menu.impl.MenuVisitors;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public final class VisitorPagedObjectButton extends PagedObjectButton<MenuVisitors, SuperiorPlayer> {

    private VisitorPagedObjectButton(ItemBuilder buttonItem, SoundWrapper clickSound, List<String> commands,
                                     String requiredPermission, SoundWrapper lackPermissionSound,
                                     ItemBuilder nullItem) {
        super(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound, nullItem);
    }

    @Override
    public ItemStack modifyButtonItem(ItemStack buttonItem, MenuVisitors superiorMenu, SuperiorPlayer islandVisitor) {
        Island island = islandVisitor.getIsland();

        String islandOwner = island != null ? island.getOwner().getName() : "None";
        String islandName = island != null ? island.getName().isEmpty() ? islandOwner : island.getName() : "None";

        return new ItemBuilder(buttonItem)
                .replaceAll("{0}", islandVisitor.getName())
                .replaceAll("{1}", islandOwner)
                .replaceAll("{2}", islandName)
                .asSkullOf(islandVisitor)
                .build(islandVisitor);
    }

    @Override
    public void onButtonClick(SuperiorSkyblockPlugin plugin, MenuVisitors superiorMenu, InventoryClickEvent clickEvent) {
        String subCommandToExecute = clickEvent.getClick().name().contains("RIGHT") ? "invite" :
                clickEvent.getClick().name().contains("LEFT") ? "expel" : null;

        if (subCommandToExecute == null)
            return;

        plugin.getCommands().dispatchSubCommand(clickEvent.getWhoClicked(), subCommandToExecute, pagedObject.getName());
    }

    public static class Builder extends PagedObjectBuilder<Builder, VisitorPagedObjectButton, MenuVisitors> {

        @Override
        public VisitorPagedObjectButton build() {
            return new VisitorPagedObjectButton(buttonItem, clickSound, commands, requiredPermission,
                    lackPermissionSound, nullItem);
        }

    }

}
