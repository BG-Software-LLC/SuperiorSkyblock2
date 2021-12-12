package com.bgsoftware.superiorskyblock.menu.pattern.impl;

import com.bgsoftware.superiorskyblock.api.menu.ISuperiorMenu;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.menu.button.SuperiorMenuButton;
import com.bgsoftware.superiorskyblock.menu.pattern.SuperiorMenuPattern;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;

public final class RegularMenuPattern extends SuperiorMenuPattern {

    private RegularMenuPattern(String title, InventoryType inventoryType, SuperiorMenuButton[] buttons,
                               SoundWrapper openingSound, boolean isPreviousMoveAllowed) {
        super(title, inventoryType, buttons, openingSound, isPreviousMoveAllowed);
    }

    @Override
    public void setupInventory(Inventory inventory, ISuperiorMenu superiorMenu,
                               SuperiorPlayer inventoryViewer, @Nullable SuperiorPlayer targetPlayer) {
        // Set all buttons in the menu
        for (int slot = 0; slot < this.buttons.length; ++slot) {
            SuperiorMenuButton button = this.buttons[slot];
            ItemStack buttonItem = button.getButtonItem(inventoryViewer, targetPlayer);
            if (buttonItem != null) {
                inventory.setItem(slot, buttonItem);
            }
        }
    }

    public static final class Builder extends AbstractBuilder<Builder, RegularMenuPattern> {

        @Override
        public RegularMenuPattern build() {
            return new RegularMenuPattern(this.title, this.inventoryType, this.buttons,
                    this.openingSound, this.isPreviousMoveAllowed);
        }

    }

}
