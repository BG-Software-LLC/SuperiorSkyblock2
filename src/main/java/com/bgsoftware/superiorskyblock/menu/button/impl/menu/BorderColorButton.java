package com.bgsoftware.superiorskyblock.menu.button.impl.menu;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.enums.BorderColor;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.menu.button.SuperiorMenuButton;
import com.bgsoftware.superiorskyblock.menu.impl.MenuBorderColor;
import com.bgsoftware.superiorskyblock.threads.Executor;
import com.bgsoftware.superiorskyblock.utils.islands.IslandUtils;
import com.bgsoftware.superiorskyblock.utils.items.TemplateItem;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;

public final class BorderColorButton extends SuperiorMenuButton<MenuBorderColor> {

    private final BorderColor borderColor;

    private BorderColorButton(TemplateItem buttonItem, SoundWrapper clickSound, List<String> commands,
                              String requiredPermission, SoundWrapper lackPermissionSound, BorderColor borderColor) {
        super(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound);
        this.borderColor = borderColor;
    }

    @Override
    public void onButtonClick(SuperiorSkyblockPlugin plugin, MenuBorderColor superiorMenu, InventoryClickEvent clickEvent) {
        SuperiorPlayer clickedPlayer = plugin.getPlayers().getSuperiorPlayer(clickEvent.getWhoClicked());
        if (IslandUtils.handleBorderColorUpdate(clickedPlayer, borderColor))
            Executor.sync(superiorMenu::closePage, 1L);
    }

    public static class Builder extends AbstractBuilder<Builder, BorderColorButton, MenuBorderColor> {

        private BorderColor borderColor;

        public Builder setBorderColor(BorderColor borderColor) {
            this.borderColor = borderColor;
            return this;
        }

        @Override
        public BorderColorButton build() {
            return new BorderColorButton(buttonItem, clickSound, commands, requiredPermission,
                    lackPermissionSound, borderColor);
        }

    }

}
