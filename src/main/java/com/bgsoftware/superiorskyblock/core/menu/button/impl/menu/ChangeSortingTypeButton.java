package com.bgsoftware.superiorskyblock.core.menu.button.impl.menu;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.SortingType;
import com.bgsoftware.superiorskyblock.core.GameSound;
import com.bgsoftware.superiorskyblock.core.itemstack.GlowEnchantment;
import com.bgsoftware.superiorskyblock.core.itemstack.ItemBuilder;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;
import com.bgsoftware.superiorskyblock.core.menu.button.SuperiorMenuButton;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuTopIslands;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ChangeSortingTypeButton extends SuperiorMenuButton<MenuTopIslands> {

    private final SortingType sortingType;

    private ChangeSortingTypeButton(TemplateItem buttonItem, GameSound clickSound, List<String> commands,
                                    String requiredPermission, GameSound lackPermissionSound, SortingType sortingType) {
        super(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound);
        this.sortingType = sortingType;
    }

    @Nullable
    @Override
    public ItemStack getButtonItem(MenuTopIslands superiorMenu) {
        ItemStack buttonItem = super.getButtonItem(superiorMenu);

        if (buttonItem == null || !MenuTopIslands.sortGlowWhenSelected || superiorMenu.getSortingType() != sortingType)
            return buttonItem;

        return new ItemBuilder(buttonItem).withEnchant(GlowEnchantment.getGlowEnchant(), 1).build();
    }

    @Override
    public void onButtonClick(SuperiorSkyblockPlugin plugin, MenuTopIslands superiorMenu, InventoryClickEvent clickEvent) {
        if (superiorMenu.getSortingType() == sortingType)
            return;

        boolean notSortedAlready = superiorMenu.setSortingType(sortingType);

        if (notSortedAlready) {
            plugin.getGrid().sortIslands(sortingType, superiorMenu::refreshPage);
        } else {
            superiorMenu.refreshPage();
        }
    }

    public static class Builder extends AbstractBuilder<Builder, ChangeSortingTypeButton, MenuTopIslands> {

        private SortingType sortingType;

        public Builder setSortingType(SortingType sortingType) {
            this.sortingType = sortingType;
            return this;
        }

        @Override
        public ChangeSortingTypeButton build() {
            return new ChangeSortingTypeButton(buttonItem, clickSound, commands, requiredPermission,
                    lackPermissionSound, sortingType);
        }

    }

}
