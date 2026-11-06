package com.bgsoftware.superiorskyblock.core.menu.button.impl;

import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.menu.button.click.ButtonClickContext;
import com.bgsoftware.superiorskyblock.core.itemstack.ItemBuilder;
import com.bgsoftware.superiorskyblock.core.menu.Menus;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuTemplateButton;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractMenuViewButton;
import com.bgsoftware.superiorskyblock.core.menu.button.MenuTemplateButtonImpl;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.BankLogsSortButton.SortType;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuBankLogs;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SwitchBankLogsSortingTypeButton extends AbstractMenuViewButton<MenuBankLogs.View> {

    private SwitchBankLogsSortingTypeButton(AbstractMenuTemplateButton<MenuBankLogs.View> templateButton, MenuBankLogs.View menuView) {
        super(templateButton, menuView);
    }

    @Override
    public Template getTemplate() {
        return (Template) super.getTemplate();
    }

    @Override
    public ItemStack createViewItem() {
        if (getTemplate().buttons.isEmpty())
            return null;

        SortType sortingType = menuView.getSortingType();
        SortingButtonData data = getTemplate().buttons.get(sortingType);
        String displayName = data == null ? (sortingType == null ? "" : sortingType.name()) : data.getDisplayName();

        if (data == null)
            data = getTemplate().buttons.values().iterator().next();

        TemplateItem buttonItem = data.getTemplateItem();
        ItemBuilder itemBuilder = buttonItem.getBuilder();

        if (itemBuilder == null) {
            return null;
        }

        itemBuilder.replaceAll("{1}", displayName);

        List<String> sortingTypes = new LinkedList<>();

        if (!Menus.MENU_BANK_LOGS.getSelectedSortingType().isEmpty() && !Menus.MENU_BANK_LOGS.getUnselectedSortingType().isEmpty()) {

            getTemplate().buttons.forEach((buttonSortingType, buttonData) -> {
                if (buttonSortingType == sortingType) {
                    sortingTypes.add(Menus.MENU_BANK_LOGS.getSelectedSortingType().replace("{0}", buttonData.getDisplayName()));
                } else {
                    sortingTypes.add(Menus.MENU_BANK_LOGS.getUnselectedSortingType().replace("{0}", buttonData.getDisplayName()));
                }
            });
        }

        itemBuilder.replaceLoreWithLines("{0}", sortingTypes);

        return itemBuilder.build(menuView.getInventoryViewer());
    }

    @Override
    public void onButtonClick(ButtonClickContext<MenuBankLogs.View> context) {
        int size = getTemplate().order.size();
        int index = getTemplate().order.indexOf(menuView.getSortingType());

        if (size == 0 || (size == 1 && index == 0))
            return;

        if (index < 0) {
            index = context.getClickType().isLeftClick() ? size - 1 : 0;
        } else if (context.getClickType().isLeftClick()) {
            index = (index - 1 + size) % size;
        } else {
            index = (index + 1) % size;
        }

        menuView.setSortingType(getTemplate().order.get(index));
        menuView.refreshView();
    }

    public static class Builder extends AbstractMenuTemplateButton.AbstractBuilder<MenuBankLogs.View> {

        private final Map<SortType, SortingButtonData> buttons = new LinkedHashMap<>();

        public Builder addItem(SortType sortingType, String displayName, TemplateItem templateItem) {
            this.buttons.put(sortingType, new SortingButtonData(displayName, templateItem));
            return this;
        }

        @Override
        public MenuTemplateButton<MenuBankLogs.View> build() {
            return new Template(this, buttons);
        }

    }

    public static class Template extends MenuTemplateButtonImpl<MenuBankLogs.View> {

        private final Map<SortType, SortingButtonData> buttons;
        private final List<SortType> order;

        Template(AbstractBuilder<MenuBankLogs.View> builder, Map<SortType, SortingButtonData> buttons) {
            super(builder, SwitchBankLogsSortingTypeButton.class, SwitchBankLogsSortingTypeButton::new);
            this.buttons = new LinkedHashMap<>(Objects.requireNonNull(buttons, "buttons cannot be null"));
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
