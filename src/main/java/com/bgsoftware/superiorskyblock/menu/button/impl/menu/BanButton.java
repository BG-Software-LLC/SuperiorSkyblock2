package com.bgsoftware.superiorskyblock.menu.button.impl.menu;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.menu.button.SuperiorMenuButton;
import com.bgsoftware.superiorskyblock.menu.impl.MenuConfirmBan;
import com.bgsoftware.superiorskyblock.threads.Executor;
import com.bgsoftware.superiorskyblock.utils.islands.IslandUtils;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;

public final class BanButton extends SuperiorMenuButton<MenuConfirmBan> {

    private final boolean banPlayer;

    private BanButton(ItemBuilder buttonItem, SoundWrapper clickSound, List<String> commands,
                      String requiredPermission, SoundWrapper lackPermissionSound, boolean banPlayer) {
        super(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound);
        this.banPlayer = banPlayer;
    }

    @Override
    public void onButtonClick(SuperiorSkyblockPlugin plugin, MenuConfirmBan superiorMenu, InventoryClickEvent clickEvent) {
        SuperiorPlayer clickedPlayer = plugin.getPlayers().getSuperiorPlayer(clickEvent.getWhoClicked());

        if (banPlayer)
            IslandUtils.handleBanPlayer(clickedPlayer, superiorMenu.getTargetIsland(), superiorMenu.getTargetPlayer());

        Executor.sync(superiorMenu::closePage, 1L);
    }

    public static class Builder extends AbstractBuilder<Builder, BanButton, MenuConfirmBan> {

        private boolean banPlayer;

        public Builder setBanPlayer(boolean banPlayer) {
            this.banPlayer = banPlayer;
            return this;
        }

        @Override
        public BanButton build() {
            return new BanButton(buttonItem, clickSound, commands, requiredPermission,
                    lackPermissionSound, banPlayer);
        }

    }

}
