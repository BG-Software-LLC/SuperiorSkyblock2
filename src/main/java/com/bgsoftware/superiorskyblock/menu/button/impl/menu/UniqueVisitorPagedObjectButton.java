package com.bgsoftware.superiorskyblock.menu.button.impl.menu;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.menu.button.PagedObjectButton;
import com.bgsoftware.superiorskyblock.menu.impl.MenuUniqueVisitors;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.utils.items.TemplateItem;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public final class UniqueVisitorPagedObjectButton extends
        PagedObjectButton<MenuUniqueVisitors, MenuUniqueVisitors.UniqueVisitorInfo> {

    private UniqueVisitorPagedObjectButton(TemplateItem buttonItem, SoundWrapper clickSound, List<String> commands,
                                           String requiredPermission, SoundWrapper lackPermissionSound,
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
                .replaceAll("{3}", StringUtils.formatDate(uniqueVisitorInfo.getVisitTime()))
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
