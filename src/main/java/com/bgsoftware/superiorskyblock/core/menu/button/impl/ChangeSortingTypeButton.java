package com.bgsoftware.superiorskyblock.core.menu.button.impl;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.island.SortingType;
import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.world.GameSound;
import com.bgsoftware.superiorskyblock.core.itemstack.GlowEnchantment;
import com.bgsoftware.superiorskyblock.core.itemstack.ItemBuilder;
import com.bgsoftware.superiorskyblock.core.menu.Menus;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuTemplateButton;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuViewButton;
import com.bgsoftware.superiorskyblock.core.menu.button.MenuTemplateButtonImpl;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuTopIslands;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Objects;

public class ChangeSortingTypeButton extends AbstractMenuViewButton<MenuTopIslands.View> {

    private ChangeSortingTypeButton(AbstractMenuTemplateButton<MenuTopIslands.View> templateButton, MenuTopIslands.View menuView) {
        super(templateButton, menuView);
    }

    @Override
    public Template getTemplate() {
        return (Template) super.getTemplate();
    }

    @Override
    public ItemStack createViewItem() {
        ItemStack buttonItem = super.createViewItem();

        if (buttonItem == null || !Menus.MENU_TOP_ISLANDS.isSortGlowWhenSelected() ||
                menuView.getSortingType() != getTemplate().sortingType)
            return buttonItem;

        return new ItemBuilder(buttonItem).withEnchant(GlowEnchantment.getGlowEnchant(), 1).build();
    }

    @Override
    public void onButtonClick(InventoryClickEvent clickEvent) {
        SortingType sortingType = getTemplate().sortingType;

        if (menuView.getSortingType() == sortingType)
            return;

        boolean notSortedAlready = menuView.setSortingType(sortingType);

        if (notSortedAlready) {
            plugin.getGrid().sortIslands(sortingType, menuView::refreshView);
        } else {
            menuView.refreshView();
        }
    }

    public static class Builder extends AbstractMenuTemplateButton.AbstractBuilder<MenuTopIslands.View> {

        private SortingType sortingType;

        public Builder setSortingType(SortingType sortingType) {
            this.sortingType = sortingType;
            return this;
        }

        @Override
        public MenuTemplateButton<MenuTopIslands.View> build() {
            return new Template(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound, sortingType);
        }

    }

    public static class Template extends MenuTemplateButtonImpl<MenuTopIslands.View> {

        private final SortingType sortingType;

        Template(@Nullable TemplateItem buttonItem, @Nullable GameSound clickSound, @Nullable List<String> commands,
                 @Nullable String requiredPermission, @Nullable GameSound lackPermissionSound, SortingType sortingType) {
            super(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound,
                    ChangeSortingTypeButton.class, ChangeSortingTypeButton::new);
            this.sortingType = Objects.requireNonNull(sortingType, "sortingType cannot be null");
        }

    }

}
