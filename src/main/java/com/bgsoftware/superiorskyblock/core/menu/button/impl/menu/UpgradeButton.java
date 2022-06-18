package com.bgsoftware.superiorskyblock.core.menu.button.impl.menu;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.upgrades.Upgrade;
import com.bgsoftware.superiorskyblock.api.upgrades.UpgradeLevel;
import com.bgsoftware.superiorskyblock.api.upgrades.cost.UpgradeCost;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.GameSound;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.itemstack.ItemBuilder;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;
import com.bgsoftware.superiorskyblock.island.upgrade.SUpgradeLevel;
import com.bgsoftware.superiorskyblock.core.menu.button.SuperiorMenuButton;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuUpgrades;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class UpgradeButton extends SuperiorMenuButton<MenuUpgrades> {

    private static final TemplateItem INVALID_ITEM = new TemplateItem(new ItemBuilder(Material.BEDROCK).withName("&c&lInvalid Item"));

    private final Upgrade upgrade;

    private UpgradeButton(String requiredPermission, GameSound lackPermissionSound, Upgrade upgrade) {
        super(null, null, null, requiredPermission, lackPermissionSound);
        this.upgrade = upgrade;
    }

    @Nullable
    @Override
    public ItemStack getButtonItem(MenuUpgrades superiorMenu) {
        UpgradeLevel upgradeLevel = superiorMenu.getTargetIsland().getUpgradeLevel(upgrade);

        SUpgradeLevel.ItemData itemData = ((SUpgradeLevel) upgradeLevel).getItemData();

        if (itemData == null)
            return null;

        SuperiorPlayer inventoryViewer = superiorMenu.getInventoryViewer();

        UpgradeLevel nextUpgradeLevel = upgrade.getUpgradeLevel(upgradeLevel.getLevel() + 1);

        UpgradeCost levelCost = upgradeLevel.getCost();
        String permission = nextUpgradeLevel == null ? "" : nextUpgradeLevel.getPermission();
        String requirements = nextUpgradeLevel == null ? "" : nextUpgradeLevel.checkRequirements(inventoryViewer);

        boolean nextLevel = levelCost.hasEnoughBalance(inventoryViewer) &&
                (permission.isEmpty() || inventoryViewer.hasPermission(permission)) && requirements.isEmpty();

        TemplateItem buttonItem = nextLevel ? itemData.hasNextLevel : itemData.noNextLevel;

        return (buttonItem == null ? INVALID_ITEM : buttonItem).getBuilder()
                .replaceAll("{0}", levelCost.getCost() + "")
                .replaceAll("{1}", Formatters.NUMBER_FORMATTER.format(levelCost.getCost()))
                .replaceAll("{2}", Formatters.FANCY_NUMBER_FORMATTER.format(levelCost.getCost(), inventoryViewer.getUserLocale()))
                .build(inventoryViewer);
    }

    @Override
    public void onButtonClick(SuperiorSkyblockPlugin plugin, MenuUpgrades superiorMenu,
                              InventoryClickEvent clickEvent) {
        Upgrade upgrade = plugin.getUpgrades().getUpgrade(clickEvent.getRawSlot());

        if (upgrade == null)
            return;

        plugin.getCommands().dispatchSubCommand(clickEvent.getWhoClicked(), "rankup", upgrade.getName());
        superiorMenu.refreshPage();
    }

    public static class Builder extends AbstractBuilder<Builder, UpgradeButton, MenuUpgrades> {

        private final Upgrade upgrade;

        public Builder(Upgrade upgrade) {
            this.upgrade = upgrade;
        }

        @Override
        public UpgradeButton build() {
            return new UpgradeButton(requiredPermission, lackPermissionSound, upgrade);
        }

    }

}
