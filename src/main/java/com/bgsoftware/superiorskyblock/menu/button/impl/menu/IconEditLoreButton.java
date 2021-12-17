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

public final class IconEditLoreButton<M extends SuperiorMenuIconEdit<M, T>, T> extends SuperiorMenuButton<M> {

    private final Locale newLoreMessage;

    private IconEditLoreButton(ItemBuilder buttonItem, SoundWrapper clickSound, List<String> commands,
                               String requiredPermission, SoundWrapper lackPermissionSound, Locale newLoreMessage) {
        super(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound);
        this.newLoreMessage = newLoreMessage;
    }

    @Override
    public void onButtonClick(SuperiorSkyblockPlugin plugin, M superiorMenu, InventoryClickEvent clickEvent) {
        Player player = (Player) clickEvent.getWhoClicked();

        newLoreMessage.send(player);

        superiorMenu.closePage();

        PlayerChat.listen(player, message -> {
            if (!message.equalsIgnoreCase("-cancel")) {
                superiorMenu.getIconBuilder().withLore(message.split("\\\\n"));
            }

            PlayerChat.remove(player);

            superiorMenu.open(superiorMenu.getPreviousMenu());

            return true;
        });
    }

    public static class Builder<M extends SuperiorMenuIconEdit<M, T>, T> extends
            AbstractBuilder<Builder<M, T>, IconEditLoreButton<M, T>, M> {

        private final Locale newLoreMessage;

        public Builder(Locale newLoreMessage) {
            this.newLoreMessage = newLoreMessage;
        }

        @Override
        public IconEditLoreButton<M, T> build() {
            return new IconEditLoreButton<>(buttonItem, clickSound, commands, requiredPermission,
                    lackPermissionSound, newLoreMessage);
        }

    }

}
