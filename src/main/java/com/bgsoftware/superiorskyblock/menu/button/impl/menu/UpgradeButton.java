package com.bgsoftware.superiorskyblock.menu.button.impl.menu;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.upgrades.Upgrade;
import com.bgsoftware.superiorskyblock.api.upgrades.UpgradeLevel;
import com.bgsoftware.superiorskyblock.api.upgrades.cost.UpgradeCost;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.menu.button.SuperiorMenuButton;
import com.bgsoftware.superiorskyblock.menu.impl.MenuUpgrades;
import com.bgsoftware.superiorskyblock.upgrade.SUpgradeLevel;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public final class UpgradeButton extends SuperiorMenuButton<MenuUpgrades> {

    private static final ItemBuilder INVALID_ITEM = new ItemBuilder(Material.BEDROCK).withName("&c&lInvalid Item");

    private final Upgrade upgrade;
    private final UpgradeLevel upgradeLevel;

    private UpgradeButton(String requiredPermission, SoundWrapper lackPermissionSound, Upgrade upgrade,
                          UpgradeLevel upgradeLevel) {
        super(null, null, null, requiredPermission, lackPermissionSound);
        this.upgrade = upgrade;
        this.upgradeLevel = upgradeLevel;
    }

    @Nullable
    @Override
    public ItemStack getButtonItem(MenuUpgrades superiorMenu) {
        SUpgradeLevel.ItemData itemData = ((SUpgradeLevel) upgradeLevel).getItemData();

        if(itemData == null)
            return null;

        SuperiorPlayer inventoryViewer = superiorMenu.getInventoryViewer();

        UpgradeLevel nextUpgradeLevel = upgrade.getUpgradeLevel(upgradeLevel.getLevel() + 1);

        UpgradeCost levelCost = upgradeLevel.getCost();
        String permission = nextUpgradeLevel == null ? "" : nextUpgradeLevel.getPermission();
        String requirements = nextUpgradeLevel == null ? "" : nextUpgradeLevel.checkRequirements(inventoryViewer);

        boolean nextLevel = levelCost.hasEnoughBalance(inventoryViewer) &&
                (permission.isEmpty() || inventoryViewer.hasPermission(permission)) && requirements.isEmpty();

        ItemBuilder buttonItem = nextLevel ? itemData.hasNextLevel : itemData.noNextLevel;

        return (buttonItem == null ? INVALID_ITEM : buttonItem).clone().build(inventoryViewer);
    }

    @Override
    public void onButtonClick(SuperiorSkyblockPlugin plugin, MenuUpgrades superiorMenu,
                              InventoryClickEvent clickEvent) {
        Upgrade upgrade = plugin.getUpgrades().getUpgrade(clickEvent.getRawSlot());

        if(upgrade == null)
            return;

        plugin.getCommands().dispatchSubCommand(clickEvent.getWhoClicked(), "rankup", upgrade.getName());
        superiorMenu.refreshPage();
    }

    public static class Builder extends AbstractBuilder<Builder, UpgradeButton, MenuUpgrades> {

        private final Upgrade upgrade;
        private final UpgradeLevel upgradeLevel;

        public Builder(Upgrade upgrade, UpgradeLevel upgradeLevel) {
            this.upgrade = upgrade;
            this.upgradeLevel = upgradeLevel;
        }

        @Override
        public UpgradeButton build() {
            return new UpgradeButton(requiredPermission, lackPermissionSound, upgrade, upgradeLevel);
        }

    }

}
