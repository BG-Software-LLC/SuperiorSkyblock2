package com.bgsoftware.superiorskyblock.core.zmenu.buttons.border;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.zmenu.buttons.SuperiorButton;
import fr.maxlego08.menu.MenuItemStack;
import fr.maxlego08.menu.api.utils.Placeholders;
import fr.maxlego08.menu.inventory.inventories.InventoryDefault;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class BorderColorToggleButton extends SuperiorButton {

    private final MenuItemStack menuItemStackEnabled;
    private final MenuItemStack menuItemStackDisabled;

    public BorderColorToggleButton(SuperiorSkyblockPlugin plugin, MenuItemStack menuItemStackEnabled, MenuItemStack menuItemStackDisabled) {
        super(plugin);
        this.menuItemStackEnabled = menuItemStackEnabled;
        this.menuItemStackDisabled = menuItemStackDisabled;
    }

    @Override
    public ItemStack getCustomItemStack(Player player) {
        SuperiorPlayer inventoryViewer = getSuperiorPlayer(player);
        return (inventoryViewer.hasWorldBorderEnabled() ? this.menuItemStackEnabled : this.menuItemStackDisabled).build(player, false);
    }

    @Override
    public void onClick(Player player, InventoryClickEvent event, InventoryDefault inventory, int slot, Placeholders placeholders) {
        super.onClick(player, event, inventory, slot, placeholders);
        plugin.getCommands().dispatchSubCommand(player, "toggle", "border");
    }
}
