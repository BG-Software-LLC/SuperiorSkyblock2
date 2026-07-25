package com.bgsoftware.superiorskyblock.core.menu.button.click;

import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.menu.button.click.ButtonClickContext;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import com.bgsoftware.superiorskyblock.core.ObjectsPool;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;

import static com.bgsoftware.superiorskyblock.platform.event.args.GameEventArgs.DialogClickEvent;

public class ButtonClickContextImpl<V extends MenuView<V, ?>> implements ButtonClickContext<V>, ObjectsPool.Releasable, AutoCloseable {

    public static final ObjectsPool<ButtonClickContextImpl> POOL = new ObjectsPool<>(ButtonClickContextImpl::new);

    private V menuView;
    private Player player;
    private int clickedSlot = -1;
    private MenuTemplateButton<V> clickedButton;
    private ClickType clickType;

    private ButtonClickContextImpl() {
    }

    public ButtonClickContextImpl(Player player, V menuView, int clickedSlot, ClickType clickType) {
        this.menuView = menuView;
        this.player = player;
        this.clickedSlot = clickedSlot;
        this.clickedButton = menuView.getMenu().getLayout().getButton(clickedSlot);
        this.clickType = clickType;
    }

    public static <V extends MenuView<V, ?>> ButtonClickContextImpl<V> obtain(
            V menuView, InventoryClickEvent clickEvent) {
        ButtonClickContextImpl<V> buttonClickContext = POOL.obtain();
        buttonClickContext.menuView = menuView;
        buttonClickContext.clickedSlot = clickEvent.getRawSlot();
        buttonClickContext.player = (Player) clickEvent.getWhoClicked();
        buttonClickContext.clickedButton = menuView.getMenu().getLayout().getButton(clickEvent.getRawSlot());
        buttonClickContext.clickType = clickEvent.getClick();
        return buttonClickContext;
    }

    public static <V extends MenuView<V, ?>> ButtonClickContextImpl<V> obtain(
            V menuView, DialogClickEvent clickEvent) {
        ButtonClickContextImpl<V> buttonClickContext = POOL.obtain();
        buttonClickContext.menuView = menuView;
        buttonClickContext.clickedSlot = clickEvent.clickedSlot;
        buttonClickContext.player = clickEvent.superiorPlayer.asPlayer();
        buttonClickContext.clickedButton = menuView.getMenu().getLayout().getButton(clickEvent.clickedSlot);
        buttonClickContext.clickType = ClickType.LEFT;
        return buttonClickContext;
    }

    @Override
    public Player getPlayer() {
        return this.player;
    }

    @Override
    public V getMenuView() {
        return this.menuView;
    }

    @Override
    public int getClickedSlot() {
        return this.clickedSlot;
    }

    @Override
    public MenuTemplateButton<V> getClickedButton() {
        return this.clickedButton;
    }

    @Override
    public ClickType getClickType() {
        return this.clickType;
    }

    @Override
    public void release() {
        this.player = null;
        this.menuView = null;
        this.clickedSlot = -1;
        this.clickedButton = null;
        this.clickType = null;
        POOL.release(this);
    }

    @Override
    public void close() {
        release();
    }

}
