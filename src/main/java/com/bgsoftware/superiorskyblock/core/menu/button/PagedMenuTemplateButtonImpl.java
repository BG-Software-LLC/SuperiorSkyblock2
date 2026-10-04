package com.bgsoftware.superiorskyblock.core.menu.button;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.menu.button.PagedMenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.menu.button.PagedMenuViewButton;
import com.bgsoftware.superiorskyblock.api.menu.dialog.DialogButton;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import com.bgsoftware.superiorskyblock.core.Either;
import com.bgsoftware.superiorskyblock.core.itemstack.ItemBuilder;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;
import com.google.common.base.Preconditions;
import org.bukkit.inventory.ItemStack;

public class PagedMenuTemplateButtonImpl<V extends MenuView<V, ?>, E> extends AbstractMenuTemplateButton<V> implements PagedMenuTemplateButton<V, E> {

    protected static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private final Either<TemplateItem, DialogButton> nullItem;
    private final PagedMenuViewButtonCreator<V, E> viewButtonCreator;
    private int buttonIndex;

    public PagedMenuTemplateButtonImpl(AbstractBuilder<V, E> builder, Class<?> viewButtonType,
                                       PagedMenuViewButtonCreator<V, E> viewButtonCreator) {
        super(builder, viewButtonType);
        this.nullItem = builder.nullItem;
        this.buttonIndex = builder.getButtonIndex();
        this.viewButtonCreator = viewButtonCreator;
    }

    @Override
    public ItemStack getNullItem() {
        TemplateItem nullItem = getNullTemplateItem();
        return nullItem == null ? null : nullItem.getBuilder().build();
    }

    public TemplateItem getNullTemplateItem() {
        return this.nullItem == null || this.nullItem.isRight() ? null : this.nullItem.getLeft();
    }

    @Override
    public int getButtonIndex() {
        return this.buttonIndex;
    }

    @Override
    public void setButtonIndex(int buttonIndex) {
        Preconditions.checkArgument(buttonIndex >= 0, "buttonIndex cannot be negative.");
        this.buttonIndex = buttonIndex;
    }

    @Override
    public PagedMenuViewButton<V, E> createViewButton(V menuView) {
        return ensureCorrectType(this.viewButtonCreator.create(this, menuView));
    }

    public static <V extends MenuView<V, ?>, E> AbstractBuilder<V, E> newBuilder(Class<?> viewButtonType, PagedMenuViewButtonCreator<V, E> viewButtonCreator) {
        return new AbstractBuilder<V, E>() {
            @Override
            public PagedMenuTemplateButton<V, E> build() {
                return new PagedMenuTemplateButtonImpl<>(this, viewButtonType, viewButtonCreator);
            }
        };
    }

    public static abstract class AbstractBuilder<V extends MenuView<V, ?>, E>
            extends AbstractMenuTemplateButton.AbstractBuilder<V> implements PagedMenuTemplateButton.Builder<V, E> {

        private int buttonIndex = 0;
        protected Either<TemplateItem, DialogButton> nullItem = null;

        @Override
        public AbstractBuilder<V, E> setNullItem(ItemStack nullItem) {
            return this.setNullItem(new TemplateItem(new ItemBuilder(nullItem)));
        }

        public AbstractBuilder<V, E> setNullItem(TemplateItem nullItem) {
            this.nullItem = Either.left(nullItem);
            return this;
        }

        @Override
        public AbstractBuilder<V, E> setNullButtonDialog(DialogButton nullButtonDialog) {
            this.nullItem = Either.right(nullButtonDialog);
            return this;
        }

        protected int getButtonIndex() {
            return buttonIndex++;
        }

    }

}
