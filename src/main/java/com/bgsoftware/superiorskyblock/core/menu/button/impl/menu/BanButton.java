package com.bgsoftware.superiorskyblock.core.menu.button.impl.menu;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.GameSound;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.island.IslandUtils;
import com.bgsoftware.superiorskyblock.core.menu.button.SuperiorMenuButton;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuConfirmBan;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;

public class BanButton extends SuperiorMenuButton<MenuConfirmBan> {

    private final boolean banPlayer;

    private BanButton(TemplateItem buttonItem, GameSound clickSound, List<String> commands,
                      String requiredPermission, GameSound lackPermissionSound, boolean banPlayer) {
        super(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound);
        this.banPlayer = banPlayer;
    }

    @Override
    public void onButtonClick(SuperiorSkyblockPlugin plugin, MenuConfirmBan superiorMenu, InventoryClickEvent clickEvent) {
        SuperiorPlayer clickedPlayer = plugin.getPlayers().getSuperiorPlayer(clickEvent.getWhoClicked());

        if (banPlayer)
            IslandUtils.handleBanPlayer(clickedPlayer, superiorMenu.getTargetIsland(), superiorMenu.getTargetPlayer());

        BukkitExecutor.sync(superiorMenu::closePage, 1L);
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
