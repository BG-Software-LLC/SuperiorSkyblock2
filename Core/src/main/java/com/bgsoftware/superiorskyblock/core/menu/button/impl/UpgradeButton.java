package com.bgsoftware.superiorskyblock.core.menu.button.impl;

import com.bgsoftware.superiorskyblock.api.menu.button.click.ButtonClickContext;
import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.upgrades.Upgrade;
import com.bgsoftware.superiorskyblock.api.upgrades.UpgradeLevel;
import com.bgsoftware.superiorskyblock.api.upgrades.cost.UpgradeCost;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.itemstack.ItemBuilder;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuTemplateButton;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuViewButton;
import com.bgsoftware.superiorskyblock.core.menu.button.MenuTemplateButtonImpl;
import com.bgsoftware.superiorskyblock.core.menu.view.impl.IslandMenuView;
import com.bgsoftware.superiorskyblock.island.IslandUtils;
import com.bgsoftware.superiorskyblock.island.upgrade.SUpgradeLevel;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class UpgradeButton extends AbstractMenuViewButton<IslandMenuView> {

    private static final TemplateItem INVALID_ITEM = new TemplateItem(new ItemBuilder(Material.BEDROCK).withName("&c&lInvalid Item"));

    private UpgradeButton(AbstractMenuTemplateButton<IslandMenuView> templateButton, IslandMenuView menuView) {
        super(templateButton, menuView);
    }

    @Override
    public Template getTemplate() {
        return (Template) super.getTemplate();
    }

    @Override
    public ItemStack createViewItem() {
        Upgrade upgrade = getTemplate().upgrade;

        UpgradeLevel upgradeLevel = menuView.getIsland().getUpgradeLevel(upgrade);

        SUpgradeLevel.ItemData itemData = ((SUpgradeLevel) upgradeLevel).getItemData();

        if (itemData == null)
            return null;

        SuperiorPlayer inventoryViewer = menuView.getInventoryViewer();

        UpgradeLevel nextUpgradeLevel = upgrade.getUpgradeLevel(upgradeLevel.getLevel() + 1);

        UpgradeCost levelCost = upgradeLevel.getCosts().get(0);
        String permission = nextUpgradeLevel == null ? "" : nextUpgradeLevel.getPermission();
        String requirements = nextUpgradeLevel == null ? "" : nextUpgradeLevel.checkRequirements(inventoryViewer);

        boolean nextLevel = IslandUtils.hasEnoughBalance(upgradeLevel.getCosts(), inventoryViewer) &&
                (permission.isEmpty() || inventoryViewer.hasPermission(permission)) && requirements.isEmpty();

        TemplateItem buttonItem = nextLevel ? itemData.hasNextLevel : itemData.noNextLevel;

        return (buttonItem == null ? INVALID_ITEM : buttonItem).getBuilder()
                .replaceAll("{0}", levelCost.getCost() + "")
                .replaceAll("{1}", Formatters.NUMBER_FORMATTER.format(levelCost.getCost()))
                .replaceAll("{2}", Formatters.FANCY_NUMBER_FORMATTER.format(levelCost.getCost(), inventoryViewer.getUserLocale()))
                .build(inventoryViewer);
    }

    @Override
    public void onButtonClick(ButtonClickContext<IslandMenuView> context) {
        Upgrade upgrade = plugin.getUpgrades().getUpgrade(context.getClickedSlot());

        if (upgrade == null)
            return;

        plugin.getCommands().dispatchSubCommand(context.getPlayer(), "rankup", upgrade.getName());
        menuView.refreshView();
    }

    public static class Builder extends AbstractMenuTemplateButton.AbstractBuilder<IslandMenuView> {

        private final Upgrade upgrade;

        public Builder(Upgrade upgrade) {
            this.upgrade = upgrade;
        }

        @Override
        public MenuTemplateButton<IslandMenuView> build() {
            return new Template(this, upgrade);
        }

    }

    public static class Template extends MenuTemplateButtonImpl<IslandMenuView> {

        private final Upgrade upgrade;

        Template(AbstractBuilder<IslandMenuView> builder, Upgrade upgrade) {
            super(builder, UpgradeButton.class, UpgradeButton::new);
            this.upgrade = Objects.requireNonNull(upgrade, "upgrade cannot be null");
        }

    }

}
