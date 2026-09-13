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
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Locale;
import java.util.Objects;

public class IconEditTypeButton<E> extends AbstractMenuViewButton<AbstractIconProviderMenu.View<E>> {

    private IconEditTypeButton(AbstractMenuTemplateButton<AbstractIconProviderMenu.View<E>> templateButton, AbstractIconProviderMenu.View<E> menuView) {
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
        getTemplate().newTypeMessage.send(player, cancelText);

        menuView.closeView();

        PlayerChat.listen(player, message -> {
            if (!message.equalsIgnoreCase(cancelText)) {
                String[] sections = message.split(":");
                Material material;

                try {
                    material = Material.valueOf(sections[0].toUpperCase(Locale.ENGLISH));
                    if (material == Material.AIR)
                        throw new IllegalArgumentException();
                } catch (IllegalArgumentException ex) {
                    Message.INVALID_MATERIAL.send(player, message);
                    return true;
                }

                String rawMessage = sections.length == 2 ? sections[1] : "0";

                short data;

                try {
                    data = Short.parseShort(rawMessage);
                    if (data < 0)
                        throw new IllegalArgumentException();
                } catch (IllegalArgumentException ex) {
                    Message.INVALID_MATERIAL_DATA.send(player, rawMessage);
                    return true;
                }

                menuView.getIconTemplate().getEditableBuilder().withType(material).withDurablity(data);
            }

            PlayerChat.remove(player);

            menuView.refreshView();

            return true;
        });
    }

    public static class Builder<E> extends AbstractMenuTemplateButton.AbstractBuilder<AbstractIconProviderMenu.View<E>> {

        private final Message newTypeMessage;
        private final Message cancelMessage;

        public Builder(Message newLoreMessage, Message cancelMessage) {
            this.newTypeMessage = newLoreMessage;
            this.cancelMessage = cancelMessage;
        }

        @Override
        public MenuTemplateButton<AbstractIconProviderMenu.View<E>> build() {
            return new Template<>(this, newTypeMessage, cancelMessage);
        }

    }

    public static class Template<E> extends MenuTemplateButtonImpl<AbstractIconProviderMenu.View<E>> {

        private final Message newTypeMessage;
        private final Message cancelMessage;

        Template(AbstractBuilder<AbstractIconProviderMenu.View<E>> builder, Message newLoreMessage, Message cancelMessage) {
            super(builder, IconEditTypeButton.class, IconEditTypeButton::new);
            this.newTypeMessage = Objects.requireNonNull(newLoreMessage, "newLoreMessage cannot be null");
            this.cancelMessage = Objects.requireNonNull(cancelMessage, "cancelMessage cannot be null");
        }

    }

}
