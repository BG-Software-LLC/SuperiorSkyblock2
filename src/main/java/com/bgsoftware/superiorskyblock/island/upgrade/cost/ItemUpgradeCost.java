package com.bgsoftware.superiorskyblock.island.upgrade.cost;

import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeySet;
import com.bgsoftware.superiorskyblock.api.upgrades.cost.UpgradeCost;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.math.BigDecimal;

public class ItemUpgradeCost extends UpgradeCostAbstract {

    private final KeySet keySet;

    public ItemUpgradeCost(BigDecimal cost, KeySet keySet) {
        super(cost, "item");
        this.keySet = keySet;
    }

    @Override
    public boolean hasEnoughBalance(SuperiorPlayer superiorPlayer) {
        int currentAmount = 0;

        for (ItemStack itemStack : superiorPlayer.asPlayer().getInventory()) {
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

        for (int i = 0; i < playerInventory.getSize(); i++) {
            ItemStack itemStack = playerInventory.getItem(i);
            if (itemStack != null && itemStack.getType() != Material.AIR && keySet.contains(Key.of(itemStack))) {
                int amount = itemStack.getAmount();

                if (amount <= amountToRemove) {
                    playerInventory.setItem(i, null);
                    amountToRemove -= amount;
                } else {
                    itemStack.setAmount(amount - amountToRemove);
                    return;
                }
            }
        }
    }

    @Override
    public UpgradeCost clone(BigDecimal cost) {
        return new ItemUpgradeCost(cost, keySet);
    }

}
