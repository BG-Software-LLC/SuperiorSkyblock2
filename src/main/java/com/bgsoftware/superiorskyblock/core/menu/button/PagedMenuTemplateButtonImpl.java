package com.bgsoftware.superiorskyblock.core.menu.button;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.menu.button.PagedMenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.menu.button.PagedMenuViewButton;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import com.bgsoftware.superiorskyblock.api.world.GameSound;
import com.bgsoftware.superiorskyblock.core.itemstack.ItemBuilder;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class PagedMenuTemplateButtonImpl<V extends MenuView<V, ?>, E> extends AbstractMenuTemplateButton<V> implements PagedMenuTemplateButton<V, E> {

    protected static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private final TemplateItem nullItem;
    private final int buttonIndex;
    private final PagedMenuViewButtonCreator<V, E> viewButtonCreator;

    public PagedMenuTemplateButtonImpl(TemplateItem buttonItem, GameSound clickSound, List<String> commands,
                                       String requiredPermission, GameSound lackPermissionSound, TemplateItem nullItem,
                                       int buttonIndex, Class<?> viewButtonType, PagedMenuViewButtonCreator<V, E> viewButtonCreator) {
        super(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound, viewButtonType);
        this.nullItem = nullItem;
        this.buttonIndex = buttonIndex;
        this.viewButtonCreator = viewButtonCreator;
    }

    @Override
    public ItemStack getNullItem() {
        return this.nullItem == null ? null : this.nullItem.getBuilder().build();
    }

    public TemplateItem getNullTemplateItem() {
        return this.nullItem;
    }

    @Override
    public int getButtonIndex() {
        return this.buttonIndex;
    }

    @Override
    public PagedMenuViewButton<V, E> createViewButton(V menuView) {
        return ensureCorrectType(this.viewButtonCreator.create(this, menuView));
    }

    public static <V extends MenuView<V, ?>, E> AbstractBuilder<V, E> newBuilder(Class<?> viewButtonType, PagedMenuViewButtonCreator<V, E> viewButtonCreator) {
        return new AbstractBuilder<V, E>() {
            @Override
            public PagedMenuTemplateButton<V, E> build() {
                return new PagedMenuTemplateButtonImpl<>(this.buttonItem, this.clickSound, this.commands,
                        this.requiredPermission, this.lackPermissionSound, this.nullItem, getButtonIndex(),
                        viewButtonType, viewButtonCreator);
            }
        };
    }

    public static abstract class AbstractBuilder<V extends MenuView<V, ?>, E>
            extends AbstractMenuTemplateButton.AbstractBuilder<V> implements PagedMenuTemplateButton.Builder<V, E> {

        private int buttonIndex = 0;
        protected TemplateItem nullItem = null;

        @Override
        public AbstractBuilder<V, E> setNullItem(ItemStack nullItem) {
            return this.setNullItem(new TemplateItem(new ItemBuilder(nullItem)));
        }

        public AbstractBuilder<V, E> setNullItem(TemplateItem nullItem) {
            this.nullItem = nullItem;
            return this;
        }

        protected int getButtonIndex() {
            return buttonIndex++;
        }

    }

}
