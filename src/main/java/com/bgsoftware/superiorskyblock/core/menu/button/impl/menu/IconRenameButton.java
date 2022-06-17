package com.bgsoftware.superiorskyblock.core.menu.button.impl.menu;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.core.GameSound;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.player.chat.PlayerChat;
import com.bgsoftware.superiorskyblock.core.menu.SuperiorMenuIconEdit;
import com.bgsoftware.superiorskyblock.core.menu.button.SuperiorMenuButton;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;

public class IconRenameButton<M extends SuperiorMenuIconEdit<M, T>, T> extends SuperiorMenuButton<M> {

    private final Message newNameMessage;

    private IconRenameButton(TemplateItem buttonItem, GameSound clickSound, List<String> commands,
                             String requiredPermission, GameSound lackPermissionSound, Message newNameMessage) {
        super(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound);
        this.newNameMessage = newNameMessage;
    }

    @Override
    public void onButtonClick(SuperiorSkyblockPlugin plugin, M superiorMenu, InventoryClickEvent clickEvent) {
        Player player = (Player) clickEvent.getWhoClicked();

        newNameMessage.send(player);

        superiorMenu.closePage();

        PlayerChat.listen(player, message -> {
            if (!message.equalsIgnoreCase("-cancel")) {
                superiorMenu.getIconTemplate().getEditableBuilder().withName(message);
            }

            PlayerChat.remove(player);

            superiorMenu.open(superiorMenu.getPreviousMenu());

            return true;
        });
    }

    public static class Builder<M extends SuperiorMenuIconEdit<M, T>, T> extends
            AbstractBuilder<Builder<M, T>, IconRenameButton<M, T>, M> {

        private final Message newNameMessage;

        public Builder(Message newNameMessage) {
            this.newNameMessage = newNameMessage;
        }

        @Override
        public IconRenameButton<M, T> build() {
            return new IconRenameButton<>(buttonItem, clickSound, commands, requiredPermission,
                    lackPermissionSound, newNameMessage);
        }

    }

}
