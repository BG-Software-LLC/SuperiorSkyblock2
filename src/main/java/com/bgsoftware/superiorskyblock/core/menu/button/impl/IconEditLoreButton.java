package com.bgsoftware.superiorskyblock.core.menu.button.impl;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.world.GameSound;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuTemplateButton;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuViewButton;
import com.bgsoftware.superiorskyblock.core.menu.button.MenuTemplateButtonImpl;
import com.bgsoftware.superiorskyblock.core.menu.view.AbstractIconProviderMenu;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.player.chat.PlayerChat;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;
import java.util.Objects;

public class IconEditLoreButton<E> extends AbstractMenuViewButton<AbstractIconProviderMenu.View<E>> {

    private IconEditLoreButton(AbstractMenuTemplateButton<AbstractIconProviderMenu.View<E>> templateButton, AbstractIconProviderMenu.View<E> menuView) {
        super(templateButton, menuView);
    }

    @Override
    public Template<E> getTemplate() {
        return (Template<E>) super.getTemplate();
    }

    @Override
    public void onButtonClick(InventoryClickEvent clickEvent) {
        Player player = (Player) clickEvent.getWhoClicked();

        getTemplate().newLoreMessage.send(player);

        menuView.closeView();

        PlayerChat.listen(player, message -> {
            if (!message.equalsIgnoreCase("-cancel")) {
                menuView.getIconTemplate().getEditableBuilder().withLore(message.split("\\\\n"));
            }

            PlayerChat.remove(player);

            menuView.refreshView();

            return true;
        });
    }

    public static class Builder<E> extends AbstractMenuTemplateButton.AbstractBuilder<AbstractIconProviderMenu.View<E>> {

        private final Message newLoreMessage;

        public Builder(Message newLoreMessage) {
            this.newLoreMessage = newLoreMessage;
        }

        @Override
        public MenuTemplateButton<AbstractIconProviderMenu.View<E>> build() {
            return new Template<>(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound, newLoreMessage);
        }

    }

    public static class Template<E> extends MenuTemplateButtonImpl<AbstractIconProviderMenu.View<E>> {

        private final Message newLoreMessage;

        Template(@Nullable TemplateItem buttonItem, @Nullable GameSound clickSound, @Nullable List<String> commands,
                 @Nullable String requiredPermission, @Nullable GameSound lackPermissionSound, Message newLoreMessage) {
            super(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound,
                    IconEditLoreButton.class, IconEditLoreButton::new);
            this.newLoreMessage = Objects.requireNonNull(newLoreMessage, "newLoreMessage cannot be null");
        }

    }

}
