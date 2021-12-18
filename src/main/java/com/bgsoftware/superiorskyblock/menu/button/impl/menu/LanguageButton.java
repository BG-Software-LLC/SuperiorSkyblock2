package com.bgsoftware.superiorskyblock.menu.button.impl.menu;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.lang.Message;
import com.bgsoftware.superiorskyblock.menu.button.SuperiorMenuButton;
import com.bgsoftware.superiorskyblock.menu.impl.MenuPlayerLanguage;
import com.bgsoftware.superiorskyblock.threads.Executor;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;

public final class LanguageButton extends SuperiorMenuButton<MenuPlayerLanguage> {

    private final java.util.Locale language;

    private LanguageButton(ItemBuilder buttonItem, SoundWrapper clickSound, List<String> commands,
                           String requiredPermission, SoundWrapper lackPermissionSound, java.util.Locale language) {
        super(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound);
        this.language = language;
    }

    @Override
    public void onButtonClick(SuperiorSkyblockPlugin plugin, MenuPlayerLanguage superiorMenu, InventoryClickEvent clickEvent) {
        SuperiorPlayer clickedPlayer = plugin.getPlayers().getSuperiorPlayer(clickEvent.getWhoClicked());
        clickedPlayer.setUserLocale(language);
        Message.CHANGED_LANGUAGE.send(clickedPlayer);
        Executor.sync(superiorMenu::closePage, 1L);
    }

    public static class Builder extends AbstractBuilder<Builder, LanguageButton, MenuPlayerLanguage> {

        private java.util.Locale language;

        public Builder setLanguage(java.util.Locale language) {
            this.language = language;
            return this;
        }

        @Override
        public LanguageButton build() {
            return new LanguageButton(buttonItem, clickSound, commands, requiredPermission,
                    lackPermissionSound, language);
        }

    }

}
