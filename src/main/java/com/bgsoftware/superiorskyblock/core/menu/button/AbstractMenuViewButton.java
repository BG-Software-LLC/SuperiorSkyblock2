package com.bgsoftware.superiorskyblock.core.menu.button;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.menu.button.MenuViewButton;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import com.bgsoftware.superiorskyblock.core.GameSoundImpl;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public abstract class AbstractMenuViewButton<V extends MenuView<V, ?>> implements MenuViewButton<V> {

    protected static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    protected final V menuView;
    private final AbstractMenuTemplateButton<V> templateButton;

    protected AbstractMenuViewButton(MenuTemplateButton<V> templateButton, V menuView) {
        this.templateButton = (AbstractMenuTemplateButton<V>) templateButton;
        this.menuView = menuView;
    }

    @Override
    public MenuTemplateButton<V> getTemplate() {
        return this.templateButton;
    }

    @Override
    public V getView() {
        return this.menuView;
    }

    @Override
    public ItemStack createViewItem() {
        TemplateItem templateItem = this.templateButton.getButtonTemplateItem();
        return templateItem == null ? null : templateItem.getBuilder().build(menuView.getInventoryViewer());
    }

    @Override
    public abstract void onButtonClick(InventoryClickEvent clickEvent);

    public void onButtonClickLackPermission(InventoryClickEvent clickEvent) {
        GameSoundImpl.playSound(clickEvent.getWhoClicked(), this.templateButton.getLackPermissionSound());
    }

}
