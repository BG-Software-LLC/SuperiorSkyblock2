package com.bgsoftware.superiorskyblock.core.menu.button;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.menu.button.click.ButtonClickContext;
import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.menu.button.MenuViewButton;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import com.bgsoftware.superiorskyblock.core.GameSoundImpl;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;
import com.bgsoftware.superiorskyblock.core.menu.button.click.ButtonClickContextImpl;
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

    @Nullable
    public TemplateItem getButtonTemplateItem() {
        return this.templateButton.getButtonTemplateItem();
    }

    @Override
    public void onButtonClick(ButtonClickContext<V> context) {
        // Do nothing.
    }

    @Override
    @Deprecated
    public final void onButtonClick(InventoryClickEvent clickEvent) {
        try (ButtonClickContextImpl<V> ctx = ButtonClickContextImpl.obtain(getView(), clickEvent)) {
            onButtonClick(ctx);
        }
    }

    /**
     * Called when the player lacks the required permission to click this button.
     * Plays the button's lack-permission sound. Subclasses may override to add further behaviour.
     *
     * @param context The button click context.
     */
    public void onButtonClickLackPermission(ButtonClickContext<V> context) {
        GameSoundImpl.playSound(context.getPlayer(), this.templateButton.getLackPermissionSound());
    }

}
