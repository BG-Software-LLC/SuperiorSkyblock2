package com.bgsoftware.superiorskyblock.core.menu.button.impl;

import com.bgsoftware.superiorskyblock.api.menu.button.click.ButtonClickContext;
import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuTemplateButton;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuViewButton;
import com.bgsoftware.superiorskyblock.core.menu.button.MenuTemplateButtonImpl;
import com.bgsoftware.superiorskyblock.core.menu.view.AbstractIconProviderMenu;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.player.PlayerLocales;
import com.bgsoftware.superiorskyblock.player.chat.PlayerChat;
import org.bukkit.entity.Player;

import java.util.Objects;

public class IconRenameButton<E> extends AbstractMenuViewButton<AbstractIconProviderMenu.View<E>> {

    private IconRenameButton(AbstractMenuTemplateButton<AbstractIconProviderMenu.View<E>> templateButton, AbstractIconProviderMenu.View<E> menuView) {
        super(templateButton, menuView);
    }

    @Override
    public Template<E> getTemplate() {
        return (Template<E>) super.getTemplate();
    }

    @Override
    public void onButtonClick(ButtonClickContext<AbstractIconProviderMenu.View<E>> context) {
        Player player = context.getPlayer();

        String cancelText = getTemplate().cancelMessage.getMessage(PlayerLocales.getLocale(player));
        getTemplate().newNameMessage.send(player, cancelText);

        menuView.closeView();

        PlayerChat.listen(player, message -> {
            if (!message.equalsIgnoreCase(cancelText)) {
                menuView.getIconTemplate().getEditableBuilder().withName(message);
            }

            PlayerChat.remove(player);

            menuView.refreshView();

            return true;
        });
    }

    public static class Builder<E> extends AbstractMenuTemplateButton.AbstractBuilder<AbstractIconProviderMenu.View<E>> {

        private final Message newNameMessage;
        private final Message cancelMessage;

        public Builder(Message newNameMessage, Message cancelMessage) {
            this.newNameMessage = newNameMessage;
            this.cancelMessage = cancelMessage;
        }

        @Override
        public MenuTemplateButton<AbstractIconProviderMenu.View<E>> build() {
            return new Template<>(this, newNameMessage, cancelMessage);
        }

    }

    public static class Template<E> extends MenuTemplateButtonImpl<AbstractIconProviderMenu.View<E>> {

        private final Message newNameMessage;
        private final Message cancelMessage;

        Template(AbstractBuilder<AbstractIconProviderMenu.View<E>> builder, Message newNameMessage, Message cancelMessage) {
            super(builder, IconRenameButton.class, IconRenameButton::new);
            this.newNameMessage = Objects.requireNonNull(newNameMessage, "newNameMessage cannot be null");
            this.cancelMessage = Objects.requireNonNull(cancelMessage, "cancelMessage cannot be null");
        }

    }

}
