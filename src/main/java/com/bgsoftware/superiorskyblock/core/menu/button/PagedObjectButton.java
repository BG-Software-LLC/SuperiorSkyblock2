package com.bgsoftware.superiorskyblock.core.menu.button;

import com.bgsoftware.superiorskyblock.api.menu.ISuperiorMenu;
import com.bgsoftware.superiorskyblock.core.GameSound;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class PagedObjectButton<M extends ISuperiorMenu, T> extends SuperiorMenuButton<M> {

    private final TemplateItem nullItem;
    private final int objectIndex;

    protected T pagedObject = null;

    protected PagedObjectButton(TemplateItem buttonItem, GameSound clickSound, List<String> commands,
                                String requiredPermission, GameSound lackPermissionSound,
                                TemplateItem nullItem, int objectIndex) {
        super(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound);
        this.nullItem = nullItem == null ? TemplateItem.AIR : nullItem;
        this.objectIndex = objectIndex;
    }

    public void updateObject(T pagedObject) {
        this.pagedObject = pagedObject;
    }

    public TemplateItem getNullItem() {
        return nullItem;
    }

    public int getObjectIndex() {
        return objectIndex;
    }

    public boolean countTowardsPageObjects() {
        return true;
    }

    @Nullable
    @Override
    public ItemStack getButtonItem(M superiorMenu) {
        return modifyButtonItem(super.getButtonItem(superiorMenu), superiorMenu, pagedObject);
    }

    public abstract ItemStack modifyButtonItem(ItemStack buttonItem, M superiorMenu, T pagedObject);

    @SuppressWarnings("unchecked")
    public static abstract class PagedObjectBuilder<B extends AbstractBuilder<B, T, M>,
            T extends SuperiorMenuButton<M>, M extends ISuperiorMenu>
            extends AbstractBuilder<B, T, M> {

        private int objectIndex = 0;
        protected TemplateItem nullItem = null;

        public B setNullItem(TemplateItem nullItem) {
            this.nullItem = nullItem;
            return (B) this;
        }

        protected int getObjectIndex() {
            return objectIndex++;
        }

    }

}
