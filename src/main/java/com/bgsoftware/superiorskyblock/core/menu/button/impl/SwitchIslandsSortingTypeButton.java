package com.bgsoftware.superiorskyblock.core.menu.button.impl;

import com.bgsoftware.superiorskyblock.api.island.SortingType;
import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.menu.button.click.ButtonClickContext;
import com.bgsoftware.superiorskyblock.core.itemstack.ItemBuilder;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuTemplateButton;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuViewButton;
import com.bgsoftware.superiorskyblock.core.menu.button.MenuTemplateButtonImpl;
import com.bgsoftware.superiorskyblock.core.menu.view.AbstractSortedIslandsMenu;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SwitchIslandsSortingTypeButton<V extends AbstractSortedIslandsMenu.View<V, ?>> extends AbstractMenuViewButton<V> {

    protected SwitchIslandsSortingTypeButton(MenuTemplateButton<V> templateButton, V menuView) {
        super(templateButton, menuView);
    }

    @Override
    public Template<V> getTemplate() {
        return (Template<V>) super.getTemplate();
    }

    @Override
    public ItemStack createViewItem() {
        SortingButtonData data = getTemplate().buttons.get(menuView.getSortingType());

        TemplateItem buttonItem = data.getTemplateItem();
        ItemBuilder itemBuilder = buttonItem.getBuilder();

        if (itemBuilder == null) {
            return null;
        }

        itemBuilder.replaceAll("{1}", data.getDisplayName());

        if (!menuView.getSelectedSortingType().isEmpty() && !menuView.getUnselectedSortingType().isEmpty()) {
            List<String> sortingTypes = new LinkedList<>();

            getTemplate().buttons.forEach((sortingType, buttonData) -> {
                if (sortingType == menuView.getSortingType()) {
                    sortingTypes.add(menuView.getSelectedSortingType().replace("{0}", buttonData.getDisplayName()));
                } else {
                    sortingTypes.add(menuView.getUnselectedSortingType().replace("{0}", buttonData.getDisplayName()));
                }
            });

            itemBuilder.replaceLoreWithLines("{0}", sortingTypes);
        }

        return itemBuilder.build(menuView.getInventoryViewer());
    }

    @Override
    public void onButtonClick(ButtonClickContext<V> context) {
        int size = getTemplate().order.size();

        if (size <= 1) {
            return;
        }

        SortingType sortingType = menuView.getSortingType();
        int index = getTemplate().order.indexOf(sortingType);

        if (context.getClickType().isLeftClick()) {
            index = (index - 1 + size) % size;
        } else {
            index = (index + 1) % size;
        }

        sortingType = getTemplate().order.get(index);

        boolean notSortedAlready = menuView.setSortingType(sortingType);

        if (notSortedAlready) {
            plugin.getGrid().sortIslands(sortingType, menuView::refreshView);
        } else {
            menuView.refreshView();
        }
    }

    public static class Builder<V extends AbstractSortedIslandsMenu.View<V, ?>> extends AbstractMenuTemplateButton.AbstractBuilder<V> {

        private final Map<SortingType, SortingButtonData> buttons = new LinkedHashMap<>();

        public Builder<V> addItem(SortingType sortingType, String displayName, TemplateItem templateItem) {
            this.buttons.put(sortingType, new SortingButtonData(displayName, templateItem));
            return this;
        }

        @Override
        public MenuTemplateButton<V> build() {
            return new Template<>(this, buttons);
        }

    }

    public static class Template<V extends AbstractSortedIslandsMenu.View<V, ?>> extends MenuTemplateButtonImpl<V> {

        private final Map<SortingType, SortingButtonData> buttons;
        private final List<SortingType> order;

        Template(AbstractBuilder<V> builder,
                 Map<SortingType, SortingButtonData> buttons) {
            super(builder, SwitchIslandsSortingTypeButton.class, SwitchIslandsSortingTypeButton::new);

            this.buttons = Objects.requireNonNull(buttons, "buttons cannot be null");
            this.order = new ArrayList<>(buttons.keySet());
        }

    }

    private static class SortingButtonData {

        private final String displayName;
        private final TemplateItem templateItem;

        public SortingButtonData(String displayName, TemplateItem templateItem) {
            this.displayName = displayName;
            this.templateItem = templateItem;
        }

        public String getDisplayName() {
            return displayName;
        }

        public TemplateItem getTemplateItem() {
            return templateItem;
        }

    }

}