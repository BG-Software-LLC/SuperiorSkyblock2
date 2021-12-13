package com.bgsoftware.superiorskyblock.menu.button.impl.menu;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.menu.SuperiorMenuIconEdit;
import com.bgsoftware.superiorskyblock.menu.button.SuperiorMenuButton;
import com.bgsoftware.superiorskyblock.utils.chat.PlayerChat;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;

public final class IconRenameButton<M extends SuperiorMenuIconEdit<M, T>, T> extends SuperiorMenuButton<M> {

    private final Locale newNameMessage;

    private IconRenameButton(ItemBuilder buttonItem, SoundWrapper clickSound, List<String> commands,
                             String requiredPermission, SoundWrapper lackPermissionSound, Locale newNameMessage) {
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
                superiorMenu.getIconBuilder().withName(message);
            }

            PlayerChat.remove(player);

            superiorMenu.open(superiorMenu.getPreviousMenu());

            return true;
        });
    }

    public static class Builder<M extends SuperiorMenuIconEdit<M, T>, T> extends
            AbstractBuilder<Builder<M, T>, IconRenameButton<M, T>, M> {

        private final Locale newNameMessage;

        public Builder(Locale newNameMessage) {
            this.newNameMessage = newNameMessage;
        }

        @Override
        public IconRenameButton<M, T> build() {
            return new IconRenameButton<>(buttonItem, clickSound, commands, requiredPermission,
                    lackPermissionSound, newNameMessage);
        }

    }

}
