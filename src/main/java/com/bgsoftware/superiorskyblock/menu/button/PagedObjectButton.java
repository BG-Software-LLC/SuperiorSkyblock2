package com.bgsoftware.superiorskyblock.menu.button;

import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class PagedObjectButton<T> extends SuperiorMenuButton {

    private final ItemBuilder nullItem;

    protected T pagedObject = null;
    protected SuperiorPlayer inventoryViewer;

    protected PagedObjectButton(ItemBuilder buttonItem, SoundWrapper clickSound, List<String> commands,
                                String requiredPermission, SoundWrapper lackPermissionSound,
                                ItemBuilder nullItem) {
        super(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound);
        this.nullItem = nullItem == null ? new ItemBuilder(Material.AIR) : nullItem;
    }

    public void updateViewer(T pagedObject, SuperiorPlayer inventoryViewer) {
        this.pagedObject = pagedObject;
        this.inventoryViewer = inventoryViewer;
    }

    public ItemBuilder getNullItem() {
        return nullItem.clone();
    }

    @Nullable
    @Override
    public ItemStack getButtonItem(SuperiorPlayer inventoryViewer, SuperiorPlayer targetPlayer) {
        return modifyButtonItem(super.getButtonItem(inventoryViewer, targetPlayer),
                inventoryViewer, targetPlayer, pagedObject);
    }

    public abstract ItemStack modifyButtonItem(ItemStack buttonItem, SuperiorPlayer inventoryViewer,
                                               SuperiorPlayer targetPlayer, T pagedObject);

    @SuppressWarnings("unchecked")
    public static abstract class PagedObjectBuilder<B extends AbstractBuilder<B, T>, T extends SuperiorMenuButton>
            extends AbstractBuilder<B, T> {

        protected ItemBuilder nullItem = null;

        public B setNullItem(ItemBuilder nullItem) {
            this.nullItem = nullItem;
            return (B) this;
        }

    }

}
