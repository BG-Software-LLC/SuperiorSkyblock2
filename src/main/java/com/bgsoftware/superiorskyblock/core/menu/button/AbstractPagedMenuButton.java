package com.bgsoftware.superiorskyblock.core.menu.button;

import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.menu.button.PagedMenuViewButton;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import com.bgsoftware.superiorskyblock.core.itemstack.ItemBuilder;
import org.bukkit.inventory.ItemStack;

public abstract class AbstractPagedMenuButton<V extends MenuView<V, ?>, E>
        extends AbstractMenuViewButton<V> implements PagedMenuViewButton<V, E> {

    protected E pagedObject = null;

    protected AbstractPagedMenuButton(MenuTemplateButton<V> templateButton, V menuView) {
        super(templateButton, menuView);
    }

    @Override
    public void updateObject(E pagedObject) {
        this.pagedObject = pagedObject;
    }

    @Override
    public E getPagedObject() {
        return pagedObject;
    }

    @Override
    public final ItemStack createViewItem() {
        return modifyViewItem(getItemBuilder());
    }

    @Override
    public final ItemStack modifyViewItem(ItemStack buttonItem) {
        return modifyViewItem(new ItemBuilder(buttonItem));
    }

    public abstract ItemStack modifyViewItem(ItemBuilder itemBuilder);

    private ItemBuilder getItemBuilder() {
        if (getButtonTemplateItem() != null) {
            return getButtonTemplateItem().getBuilder();
        }

        return null;
    }

}
