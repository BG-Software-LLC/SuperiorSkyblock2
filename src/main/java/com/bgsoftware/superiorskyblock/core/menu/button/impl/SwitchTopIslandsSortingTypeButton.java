package com.bgsoftware.superiorskyblock.core.menu.button.impl;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.island.SortingType;
import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.world.GameSound;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuTemplateButton;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuViewButton;
import com.bgsoftware.superiorskyblock.core.menu.button.MenuTemplateButtonImpl;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuTopIslands;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class SwitchTopIslandsSortingTypeButton extends AbstractMenuViewButton<MenuTopIslands.View> {

    private SwitchTopIslandsSortingTypeButton(AbstractMenuTemplateButton<MenuTopIslands.View> templateButton, MenuTopIslands.View menuView) {
        super(templateButton, menuView);
    }

    @Override
    public Template getTemplate() {
        return (Template) super.getTemplate();
    }

    @Override
    public ItemStack createViewItem() {
        TemplateItem buttonItem = getTemplate().items.get(menuView.getSortingType());
        return buttonItem.build(menuView.getInventoryViewer());
    }

    @Override
    public void onButtonClick(InventoryClickEvent clickEvent) {
        int size = getTemplate().order.size();

        if (size <= 1)
            return;

        SortingType sortingType = menuView.getSortingType();
        int index = getTemplate().order.indexOf(sortingType);

        if (clickEvent.isLeftClick()) {
            index = (index - 1 + size) % size;
        } else {
            index = (index + 1) % size;
        }

        sortingType = getTemplate().order.get(index);

        boolean notSortedAlready = menuView.setSortingType(sortingType);

        if (notSortedAlready)
            plugin.getGrid().sortIslands(sortingType, menuView::refreshView);
        else
            menuView.refreshView();
    }

    public static class Builder extends AbstractMenuTemplateButton.AbstractBuilder<MenuTopIslands.View> {

        private final Map<SortingType, TemplateItem> items = new LinkedHashMap<>();

        public Builder addItem(SortingType sortingType, TemplateItem templateItem) {
            this.items.put(sortingType, templateItem);
            return this;
        }

        @Override
        public MenuTemplateButton<MenuTopIslands.View> build() {
            return new Template(clickSound, commands, requiredPermission, lackPermissionSound, items);
        }

    }

    public static class Template extends MenuTemplateButtonImpl<MenuTopIslands.View> {

        private final Map<SortingType, TemplateItem> items;
        private final List<SortingType> order;

        Template(@Nullable GameSound clickSound, @Nullable List<String> commands, @Nullable String requiredPermission,
                 @Nullable GameSound lackPermissionSound, Map<SortingType, TemplateItem> items) {
            super(null, clickSound, commands, requiredPermission, lackPermissionSound,
                    SwitchTopIslandsSortingTypeButton.class, SwitchTopIslandsSortingTypeButton::new);
            this.items = Objects.requireNonNull(items, "items cannot be null");
            this.order = new ArrayList<>(items.keySet());
        }

    }

}
