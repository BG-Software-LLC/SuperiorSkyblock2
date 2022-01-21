package com.bgsoftware.superiorskyblock.menu.button.impl.menu;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.menu.SuperiorMenuIconEdit;
import com.bgsoftware.superiorskyblock.menu.button.SuperiorMenuButton;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class IconDisplayButton<M extends SuperiorMenuIconEdit<M, T>, T> extends SuperiorMenuButton<M> {

    private IconDisplayButton(ItemBuilder buttonItem, SoundWrapper clickSound, List<String> commands,
                              String requiredPermission, SoundWrapper lackPermissionSound) {
        super(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound);
    }

    @Override
    public void onButtonClick(SuperiorSkyblockPlugin plugin, M superiorMenu, InventoryClickEvent clickEvent) {
        // Dummy button
    }

    @Nullable
    @Override
    public ItemStack getButtonItem(M superiorMenu) {
        ItemBuilder iconBuilder = superiorMenu.getIconBuilder();
        return iconBuilder == null ? null : iconBuilder.copy().build();
    }

    public static class Builder<M extends SuperiorMenuIconEdit<M, T>, T> extends
            AbstractBuilder<Builder<M, T>, IconDisplayButton<M, T>, M> {

        @Override
        public IconDisplayButton<M, T> build() {
            return new IconDisplayButton<>(buttonItem, clickSound, commands, requiredPermission,
                    lackPermissionSound);
        }

    }

}
