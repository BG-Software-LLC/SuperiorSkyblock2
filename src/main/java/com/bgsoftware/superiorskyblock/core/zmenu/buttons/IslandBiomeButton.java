package com.bgsoftware.superiorskyblock.core.zmenu.buttons;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.events.EventResult;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import fr.maxlego08.menu.api.utils.Placeholders;
import fr.maxlego08.menu.inventory.inventories.InventoryDefault;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Locale;

public class IslandBiomeButton extends SuperiorButton {

    private final Biome biome;
    private final boolean shouldCurrentBiomeGlow;

    public IslandBiomeButton(SuperiorSkyblockPlugin plugin, Biome biome, boolean shouldCurrentBiomeGlow) {
        super(plugin);
        this.biome = biome;
        this.shouldCurrentBiomeGlow = shouldCurrentBiomeGlow;
    }

    @Override
    public ItemStack getCustomItemStack(Player player) {
        ItemStack itemStack = super.getCustomItemStack(player);

        SuperiorPlayer superiorPlayer = getSuperiorPlayer(player);
        Island island = superiorPlayer.getIsland();

        if (island == null || island.getBiome() != this.biome) return itemStack;

        if (shouldCurrentBiomeGlow) {
            ItemMeta itemMeta = itemStack.getItemMeta();
            plugin.getNMSAlgorithms().makeItemGlow(itemMeta);
        }
        return itemStack;
    }

    @Override
    public void onClick(Player player, InventoryClickEvent event, InventoryDefault inventory, int slot, Placeholders placeholders) {
        super.onClick(player, event, inventory, slot, placeholders);

        SuperiorPlayer inventoryViewer = getSuperiorPlayer(player);
        Island island = inventoryViewer.getIsland();

        EventResult<Biome> biomeEventResult = plugin.getEventsBus().callIslandBiomeChangeEvent(inventoryViewer,
                island, this.biome);

        if (biomeEventResult.isCancelled()) return;

        island.setBiome(biomeEventResult.getResult());
        Message.CHANGED_BIOME.send(inventoryViewer, biomeEventResult.getResult().name().toLowerCase(Locale.ENGLISH));

        player.closeInventory();
    }
}
