package com.bgsoftware.superiorskyblock.core.menu.button.impl.menu;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.core.GameSound;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;
import com.bgsoftware.superiorskyblock.core.menu.SuperiorMenuIconEdit;
import com.bgsoftware.superiorskyblock.core.menu.button.SuperiorMenuButton;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class IconDisplayButton<M extends SuperiorMenuIconEdit<M, T>, T> extends SuperiorMenuButton<M> {

    private IconDisplayButton(TemplateItem buttonItem, GameSound clickSound, List<String> commands,
                              String requiredPermission, GameSound lackPermissionSound) {
        super(buttonItem, clickSound, commands, requiredPermission, lackPermissionSound);
    }

    @Override
    public void onButtonClick(SuperiorSkyblockPlugin plugin, M superiorMenu, InventoryClickEvent clickEvent) {
        // Dummy button
    }

    @Nullable
    @Override
    public ItemStack getButtonItem(M superiorMenu) {
        TemplateItem iconTemplate = superiorMenu.getIconTemplate();
        return iconTemplate == null ? null : iconTemplate.build();
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
