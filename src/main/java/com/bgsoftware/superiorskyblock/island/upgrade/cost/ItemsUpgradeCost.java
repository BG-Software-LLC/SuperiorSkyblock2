package com.bgsoftware.superiorskyblock.island.upgrade.cost;

import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeySet;
import com.bgsoftware.superiorskyblock.api.upgrades.cost.UpgradeCost;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.math.BigDecimal;

public class ItemsUpgradeCost extends UpgradeCostAbstract {

    private final KeySet keySet;

    public ItemsUpgradeCost(BigDecimal cost, KeySet keySet) {
        super(cost, "items");
        this.keySet = keySet;
    }

    @Override
    public boolean hasEnoughBalance(SuperiorPlayer superiorPlayer) {
        int currentAmount = 0;

        Inventory playerInventory = superiorPlayer.asPlayer().getInventory();
        for (ItemStack itemStack : playerInventory.getContents()) {
            if (itemStack != null && itemStack.getType() != Material.AIR && keySet.contains(Key.of(itemStack))) {
                currentAmount += itemStack.getAmount();
            }
        }

        return currentAmount >= cost.intValue();
    }

    @Override
    public void withdrawCost(SuperiorPlayer superiorPlayer) {
        int amountToRemove = cost.intValue();

        PlayerInventory playerInventory = superiorPlayer.asPlayer().getInventory();
        for (ItemStack itemStack : playerInventory.getContents()) {
            if (itemStack != null && itemStack.getType() != Material.AIR && keySet.contains(Key.of(itemStack))) {
                int amount = itemStack.getAmount();

                int newAmount = Math.max(0, amount - amountToRemove);
                itemStack.setAmount(newAmount);

                amountToRemove -= amount;
                if (amountToRemove <= 0) {
                    return;
                }
            }
        }
    }

    @Override
    public UpgradeCost clone(BigDecimal cost) {
        return new ItemsUpgradeCost(cost, keySet);
    }

}
