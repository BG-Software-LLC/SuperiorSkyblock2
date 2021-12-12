package com.bgsoftware.superiorskyblock.menu.button.impl.menu;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.menu.SuperiorMenu;
import com.bgsoftware.superiorskyblock.menu.button.SuperiorMenuButton;
import com.bgsoftware.superiorskyblock.menu.impl.MenuConfirmKick;
import com.bgsoftware.superiorskyblock.utils.islands.IslandUtils;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import com.google.common.base.Preconditions;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;

public final class KickButton extends SuperiorMenuButton {

    private final boolean kickPlayer;

    private KickButton(ItemBuilder buttonItem, SoundWrapper clickSound, List<String> commands,
                       String requiredPermission, SoundWrapper lackPermissionSound, boolean kickPlayer) {
        super(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound);
        this.kickPlayer = kickPlayer;
    }

    @Override
    public void onButtonClick(SuperiorSkyblockPlugin plugin, SuperiorMenu superiorMenu, InventoryClickEvent clickEvent) {
        Preconditions.checkArgument(superiorMenu instanceof MenuConfirmKick, "superiorMenu must be MenuConfirmKick");

        MenuConfirmKick menuConfirmKick = (MenuConfirmKick) superiorMenu;

        SuperiorPlayer clickedPlayer = plugin.getPlayers().getSuperiorPlayer(clickEvent.getWhoClicked());

        if (kickPlayer)
            IslandUtils.handleKickPlayer(clickedPlayer, menuConfirmKick.getTargetIsland(), superiorMenu.getTargetPlayer());

        Executor.sync(superiorMenu::closePage, 1L);
    }

    public static class Builder extends AbstractBuilder<Builder, KickButton> {

        private boolean kickPlayer;

        public Builder setKickPlayer(boolean kickPlayer) {
            this.kickPlayer = kickPlayer;
            return this;
        }

        @Override
        public KickButton build() {
            return new KickButton(buttonItem, clickSound, commands, requiredPermission,
                    lackPermissionSound, kickPlayer);
        }

    }

}
