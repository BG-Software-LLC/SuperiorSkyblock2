package com.bgsoftware.superiorskyblock.menu.button.impl.menu;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.menu.SuperiorMenu;
import com.bgsoftware.superiorskyblock.menu.button.SuperiorMenuButton;
import com.bgsoftware.superiorskyblock.menu.impl.MenuConfirmLeave;
import com.bgsoftware.superiorskyblock.utils.events.EventsCaller;
import com.bgsoftware.superiorskyblock.utils.islands.IslandUtils;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import com.google.common.base.Preconditions;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.ArrayList;
import java.util.List;

public final class LeaveButton extends SuperiorMenuButton {

    private final boolean leaveIsland;

    private LeaveButton(ItemBuilder buttonItem, SoundWrapper clickSound, List<String> commands,
                        String requiredPermission, SoundWrapper lackPermissionSound, boolean leaveIsland) {
        super(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound);
        this.leaveIsland = leaveIsland;
    }

    @Override
    public void onButtonClick(SuperiorSkyblockPlugin plugin, SuperiorMenu superiorMenu, InventoryClickEvent clickEvent) {
        Preconditions.checkArgument(superiorMenu instanceof MenuConfirmLeave, "superiorMenu must be MenuConfirmLeave");

        SuperiorPlayer clickedPlayer = plugin.getPlayers().getSuperiorPlayer(clickEvent.getWhoClicked());
        Island island = clickedPlayer.getIsland();

        if (leaveIsland && island != null && EventsCaller.callIslandQuitEvent(clickedPlayer, island)) {
            island.kickMember(clickedPlayer);

            IslandUtils.sendMessage(island, Locale.LEAVE_ANNOUNCEMENT, new ArrayList<>(), clickedPlayer.getName());

            Locale.LEFT_ISLAND.send(clickedPlayer);
        }

        Executor.sync(superiorMenu::closePage, 1L);
    }

    public static class Builder extends AbstractBuilder<Builder, LeaveButton> {

        private boolean leaveIsland;

        public Builder setLeaveIsland(boolean leaveIsland) {
            this.leaveIsland = leaveIsland;
            return this;
        }

        @Override
        public LeaveButton build() {
            return new LeaveButton(buttonItem, clickSound, commands, requiredPermission,
                    lackPermissionSound, leaveIsland);
        }

    }

}
