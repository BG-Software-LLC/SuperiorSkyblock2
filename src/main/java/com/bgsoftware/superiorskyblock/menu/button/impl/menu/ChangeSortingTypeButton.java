package com.bgsoftware.superiorskyblock.menu.button.impl.menu;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.SortingType;
import com.bgsoftware.superiorskyblock.menu.button.SuperiorMenuButton;
import com.bgsoftware.superiorskyblock.menu.impl.MenuTopIslands;
import com.bgsoftware.superiorskyblock.utils.items.EnchantsUtils;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.utils.items.TemplateItem;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class ChangeSortingTypeButton extends SuperiorMenuButton<MenuTopIslands> {

    private final SortingType sortingType;

    private ChangeSortingTypeButton(TemplateItem buttonItem, SoundWrapper clickSound, List<String> commands,
                                    String requiredPermission, SoundWrapper lackPermissionSound, SortingType sortingType) {
        super(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound);
        this.sortingType = sortingType;
    }

    @Nullable
    @Override
    public ItemStack getButtonItem(MenuTopIslands superiorMenu) {
        ItemStack buttonItem = super.getButtonItem(superiorMenu);

        if (buttonItem == null || !MenuTopIslands.sortGlowWhenSelected || superiorMenu.getSortingType() != sortingType)
            return buttonItem;

        return new ItemBuilder(buttonItem).withEnchant(EnchantsUtils.getGlowEnchant(), 1).build();
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
