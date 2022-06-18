package com.bgsoftware.superiorskyblock.core.menu.button.impl.menu;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.GameSound;
import com.bgsoftware.superiorskyblock.core.itemstack.ItemBuilder;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;
import com.bgsoftware.superiorskyblock.core.menu.button.PagedObjectButton;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuVisitors;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class VisitorPagedObjectButton extends PagedObjectButton<MenuVisitors, SuperiorPlayer> {

    private VisitorPagedObjectButton(TemplateItem buttonItem, GameSound clickSound, List<String> commands,
                                     String requiredPermission, GameSound lackPermissionSound,
                                     TemplateItem nullItem, int objectIndex) {
        super(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound, nullItem, objectIndex);
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
        String subCommandToExecute = clickEvent.getClick().isRightClick() ? "invite" :
                clickEvent.getClick().isLeftClick() ? "expel" : null;

        if (subCommandToExecute == null)
            return;

        plugin.getCommands().dispatchSubCommand(clickEvent.getWhoClicked(), subCommandToExecute, pagedObject.getName());
    }

    public static class Builder extends PagedObjectBuilder<Builder, VisitorPagedObjectButton, MenuVisitors> {

        @Override
        public VisitorPagedObjectButton build() {
            return new VisitorPagedObjectButton(buttonItem, clickSound, commands, requiredPermission,
                    lackPermissionSound, nullItem, getObjectIndex());
        }

    }

}
