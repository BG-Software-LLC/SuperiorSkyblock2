package com.bgsoftware.superiorskyblock.core.menu.button.impl.menu;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.GameSound;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.core.menu.button.SuperiorMenuButton;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuPlayerLanguage;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;

public class LanguageButton extends SuperiorMenuButton<MenuPlayerLanguage> {

    private final java.util.Locale language;

    private LanguageButton(TemplateItem buttonItem, GameSound clickSound, List<String> commands,
                           String requiredPermission, GameSound lackPermissionSound, java.util.Locale language) {
        super(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound);
        this.language = language;
    }

    @Override
    public void onButtonClick(SuperiorSkyblockPlugin plugin, MenuPlayerLanguage superiorMenu, InventoryClickEvent clickEvent) {
        SuperiorPlayer clickedPlayer = plugin.getPlayers().getSuperiorPlayer(clickEvent.getWhoClicked());

        if (!plugin.getEventsBus().callPlayerChangeLanguageEvent(clickedPlayer, language))
            return;

        clickedPlayer.setUserLocale(language);

        Message.CHANGED_LANGUAGE.send(clickedPlayer);

        BukkitExecutor.sync(superiorMenu::closePage, 1L);
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
