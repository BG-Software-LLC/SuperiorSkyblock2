package com.bgsoftware.superiorskyblock.menu.button.impl.menu;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.menu.button.PagedObjectButton;
import com.bgsoftware.superiorskyblock.menu.impl.MenuMembers;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public final class MembersPagedObjectButton extends PagedObjectButton<MenuMembers, SuperiorPlayer> {

    private MembersPagedObjectButton(ItemBuilder buttonItem, SoundWrapper clickSound, List<String> commands,
                                     String requiredPermission, SoundWrapper lackPermissionSound,
                                     ItemBuilder nullItem) {
        super(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound, nullItem);
    }


    @Override
    public void onButtonClick(SuperiorSkyblockPlugin plugin, MenuMembers superiorMenu, InventoryClickEvent clickEvent) {
        SuperiorPlayer clickedPlayer = plugin.getPlayers().getSuperiorPlayer(clickEvent.getWhoClicked());
        superiorMenu.setPreviousMove(false);
        plugin.getMenus().openMemberManage(clickedPlayer, superiorMenu, pagedObject);
    }

    @Override
    public ItemStack modifyButtonItem(ItemStack buttonItem, MenuMembers superiorMenu, SuperiorPlayer islandMember) {
        return new ItemBuilder(buttonItem)
                .replaceAll("{0}", islandMember.getName())
                .replaceAll("{1}", islandMember.getPlayerRole() + "")
                .asSkullOf(islandMember)
                .build(islandMember);
    }

    public static class Builder extends PagedObjectBuilder<Builder, MembersPagedObjectButton, MenuMembers> {

        @Override
        public MembersPagedObjectButton build() {
            return new MembersPagedObjectButton(buttonItem, clickSound, commands, requiredPermission,
                    lackPermissionSound, nullItem);
        }

    }

}
