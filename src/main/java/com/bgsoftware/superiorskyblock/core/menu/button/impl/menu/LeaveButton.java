package com.bgsoftware.superiorskyblock.core.menu.button.impl.menu;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.GameSound;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.island.IslandUtils;
import com.bgsoftware.superiorskyblock.core.menu.button.SuperiorMenuButton;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuConfirmLeave;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LeaveButton extends SuperiorMenuButton<MenuConfirmLeave> {

    private final boolean leaveIsland;

    private LeaveButton(TemplateItem buttonItem, GameSound clickSound, List<String> commands,
                        String requiredPermission, GameSound lackPermissionSound, boolean leaveIsland) {
        super(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound);
        this.leaveIsland = leaveIsland;
    }

    @Override
    public void onButtonClick(SuperiorSkyblockPlugin plugin, MenuConfirmLeave superiorMenu, InventoryClickEvent clickEvent) {
        SuperiorPlayer clickedPlayer = plugin.getPlayers().getSuperiorPlayer(clickEvent.getWhoClicked());
        Island island = clickedPlayer.getIsland();

        if (leaveIsland && island != null && plugin.getEventsBus().callIslandQuitEvent(clickedPlayer, island)) {
            island.kickMember(clickedPlayer);

            IslandUtils.sendMessage(island, Message.LEAVE_ANNOUNCEMENT, Collections.emptyList(), clickedPlayer.getName());

            Message.LEFT_ISLAND.send(clickedPlayer);
        }

        BukkitExecutor.sync(superiorMenu::closePage, 1L);
    }

    public static class Builder extends AbstractBuilder<Builder, LeaveButton, MenuConfirmLeave> {

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
