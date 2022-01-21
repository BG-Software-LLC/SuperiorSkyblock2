package com.bgsoftware.superiorskyblock.menu.button;

import com.bgsoftware.superiorskyblock.api.menu.ISuperiorMenu;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class PagedObjectButton<M extends ISuperiorMenu, T> extends SuperiorMenuButton<M> {

    private final ItemBuilder nullItem;
    private final int objectIndex;

    protected T pagedObject = null;

    protected PagedObjectButton(ItemBuilder buttonItem, SoundWrapper clickSound, List<String> commands,
                                String requiredPermission, SoundWrapper lackPermissionSound,
                                ItemBuilder nullItem, int objectIndex) {
        super(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound);
        this.nullItem = nullItem == null ? new ItemBuilder(Material.AIR) : nullItem;
        this.objectIndex = objectIndex;
    }

    public void updateObject(T pagedObject) {
        this.pagedObject = pagedObject;
    }

    public ItemBuilder getNullItem() {
        return nullItem.copy();
    }

    public int getObjectIndex() {
        return objectIndex;
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
        protected ItemBuilder nullItem = null;

        public B setNullItem(ItemBuilder nullItem) {
            this.nullItem = nullItem;
            return (B) this;
        }

        protected int getObjectIndex() {
            return objectIndex++;
        }

    }

}
