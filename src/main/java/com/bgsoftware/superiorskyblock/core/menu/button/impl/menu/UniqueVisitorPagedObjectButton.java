package com.bgsoftware.superiorskyblock.core.menu.button.impl.menu;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.GameSound;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.itemstack.ItemBuilder;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;
import com.bgsoftware.superiorskyblock.core.menu.button.PagedObjectButton;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuUniqueVisitors;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Date;
import java.util.List;

public class UniqueVisitorPagedObjectButton extends
        PagedObjectButton<MenuUniqueVisitors, MenuUniqueVisitors.UniqueVisitorInfo> {

    private UniqueVisitorPagedObjectButton(TemplateItem buttonItem, GameSound clickSound, List<String> commands,
                                           String requiredPermission, GameSound lackPermissionSound,
                                           TemplateItem nullItem, int objectIndex) {
        super(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound, nullItem, objectIndex);
    }

    @Override
    public ItemStack modifyButtonItem(ItemStack buttonItem, MenuUniqueVisitors superiorMenu,
                                      MenuUniqueVisitors.UniqueVisitorInfo uniqueVisitorInfo) {
        SuperiorPlayer visitor = uniqueVisitorInfo.getVisitor();
        Island island = visitor.getIsland();

        String islandOwner = island != null ? island.getOwner().getName() : "None";
        String islandName = island != null ? island.getName().isEmpty() ? islandOwner : island.getName() : "None";

        return new ItemBuilder(buttonItem)
                .replaceAll("{0}", visitor.getName())
                .replaceAll("{1}", islandOwner)
                .replaceAll("{2}", islandName)
                .replaceAll("{3}", Formatters.DATE_FORMATTER.format(new Date(uniqueVisitorInfo.getVisitTime())))
                .asSkullOf(visitor)
                .build(visitor);
    }

    @Override
    public void onButtonClick(SuperiorSkyblockPlugin plugin, MenuUniqueVisitors superiorMenu,
                              InventoryClickEvent clickEvent) {
        String subCommandToExecute = clickEvent.getClick().isRightClick() ? "invite" :
                clickEvent.getClick().isLeftClick() ? "expel" : null;

        if (subCommandToExecute == null)
            return;

        plugin.getCommands().dispatchSubCommand(clickEvent.getWhoClicked(),
                subCommandToExecute, pagedObject.getVisitor().getName());
    }

    public static class Builder extends PagedObjectBuilder<Builder, UniqueVisitorPagedObjectButton, MenuUniqueVisitors> {

        @Override
        public UniqueVisitorPagedObjectButton build() {
            return new UniqueVisitorPagedObjectButton(buttonItem, clickSound, commands, requiredPermission,
                    lackPermissionSound, nullItem, getObjectIndex());
        }

    }

}
