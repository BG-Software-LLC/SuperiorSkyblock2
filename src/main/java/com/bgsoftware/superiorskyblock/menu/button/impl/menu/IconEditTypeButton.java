package com.bgsoftware.superiorskyblock.menu.button.impl.menu;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.menu.SuperiorMenuIconEdit;
import com.bgsoftware.superiorskyblock.menu.button.SuperiorMenuButton;
import com.bgsoftware.superiorskyblock.utils.chat.PlayerChat;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;

public final class IconEditTypeButton<M extends SuperiorMenuIconEdit<M, T>, T> extends SuperiorMenuButton<M> {

    private final Locale newTypeMessage;

    private IconEditTypeButton(ItemBuilder buttonItem, SoundWrapper clickSound, List<String> commands,
                               String requiredPermission, SoundWrapper lackPermissionSound, Locale newTypeMessage) {
        super(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound);
        this.newTypeMessage = newTypeMessage;
    }

    @Override
    public void onButtonClick(SuperiorSkyblockPlugin plugin, M superiorMenu, InventoryClickEvent clickEvent) {
        Player player = (Player) clickEvent.getWhoClicked();

        newTypeMessage.send(player);

        superiorMenu.closePage();

        PlayerChat.listen(player, message -> {
            if (!message.equalsIgnoreCase("-cancel")) {
                String[] sections = message.split(":");
                Material material;

                try {
                    material = Material.valueOf(sections[0].toUpperCase());
                    if (material == Material.AIR)
                        throw new IllegalArgumentException();
                } catch (IllegalArgumentException ex) {
                    Locale.INVALID_MATERIAL.send(player, message);
                    return true;
                }

                String rawMessage = sections.length == 2 ? sections[1] : "0";

                short data;

                try {
                    data = Short.parseShort(rawMessage);
                    if (data < 0)
                        throw new IllegalArgumentException();
                } catch (IllegalArgumentException ex) {
                    Locale.INVALID_MATERIAL_DATA.send(player, rawMessage);
                    return true;
                }

                superiorMenu.getIconBuilder()
                        .withType(material).withDurablity(data);
            }

            PlayerChat.remove(player);

            superiorMenu.open(superiorMenu.getPreviousMenu());

            return true;
        });
    }

    public static class Builder<M extends SuperiorMenuIconEdit<M, T>, T> extends
            AbstractBuilder<Builder<M, T>, IconEditTypeButton<M, T>, M> {

        private final Locale newTypeMessage;

        public Builder(Locale newTypeMessage) {
            this.newTypeMessage = newTypeMessage;
        }

        @Override
        public IconEditTypeButton<M, T> build() {
            return new IconEditTypeButton<>(buttonItem, clickSound, commands, requiredPermission,
                    lackPermissionSound, newTypeMessage);
        }

    }

}