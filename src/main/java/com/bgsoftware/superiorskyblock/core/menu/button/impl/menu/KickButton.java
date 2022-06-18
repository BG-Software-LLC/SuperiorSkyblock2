package com.bgsoftware.superiorskyblock.core.menu.button.impl.menu;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.GameSound;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.island.IslandUtils;
import com.bgsoftware.superiorskyblock.core.menu.button.SuperiorMenuButton;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuConfirmKick;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;

public class KickButton extends SuperiorMenuButton<MenuConfirmKick> {

    private final boolean kickPlayer;

    private KickButton(TemplateItem buttonItem, GameSound clickSound, List<String> commands,
                       String requiredPermission, GameSound lackPermissionSound, boolean kickPlayer) {
        super(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound);
        this.kickPlayer = kickPlayer;
    }

    @Override
    public void onButtonClick(SuperiorSkyblockPlugin plugin, MenuConfirmKick superiorMenu, InventoryClickEvent clickEvent) {
        SuperiorPlayer clickedPlayer = plugin.getPlayers().getSuperiorPlayer(clickEvent.getWhoClicked());

        if (kickPlayer)
            IslandUtils.handleKickPlayer(clickedPlayer, superiorMenu.getTargetIsland(), superiorMenu.getTargetPlayer());

        BukkitExecutor.sync(superiorMenu::closePage, 1L);
    }

    public static class Builder extends AbstractBuilder<Builder, KickButton, MenuConfirmKick> {

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
