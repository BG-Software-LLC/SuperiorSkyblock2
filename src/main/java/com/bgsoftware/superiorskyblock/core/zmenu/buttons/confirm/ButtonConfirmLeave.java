package com.bgsoftware.superiorskyblock.core.zmenu.buttons.confirm;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.core.zmenu.PlayerCache;
import com.bgsoftware.superiorskyblock.core.zmenu.buttons.SuperiorButton;
import com.bgsoftware.superiorskyblock.island.IslandUtils;
import fr.maxlego08.menu.api.utils.Placeholders;
import fr.maxlego08.menu.inventory.inventories.InventoryDefault;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.plugin.Plugin;

import java.util.Collections;

public class ButtonConfirmLeave extends SuperiorButton {

    public ButtonConfirmLeave(Plugin plugin) {
        super((SuperiorSkyblockPlugin) plugin);
    }

    @Override
    public void onClick(Player player, InventoryClickEvent event, InventoryDefault inventory, int slot, Placeholders placeholders) {
        super.onClick(player, event, inventory, slot, placeholders);
        SuperiorPlayer superiorPlayer = getSuperiorPlayer(player);
        Island island = superiorPlayer.getIsland();

        if (island != null && plugin.getEventsBus().callIslandQuitEvent(superiorPlayer, island)) {
            island.kickMember(superiorPlayer);

            IslandUtils.sendMessage(island, Message.LEAVE_ANNOUNCEMENT, Collections.emptyList(), superiorPlayer.getName());

            Message.LEFT_ISLAND.send(superiorPlayer);
        }
    }
}
