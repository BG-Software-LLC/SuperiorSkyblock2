package com.bgsoftware.superiorskyblock.menu.pattern;

import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.menu.ISuperiorMenu;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.hooks.support.PlaceholderHook;
import com.bgsoftware.superiorskyblock.menu.button.SuperiorMenuButton;
import com.bgsoftware.superiorskyblock.menu.button.impl.DummyButton;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public abstract class SuperiorMenuPattern {

    public static final char[] BUTTON_SYMBOLS = new char[]{
            '!', '@', '#', '$', '%', '^', '&', '*', '-', '_', '+', '=',
            '~', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '>',
            '<', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K',
            'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W',
            'X', 'Y', 'Z'
    };

    private static final ReflectField<Object> INVENTORY =
            new ReflectField<>("org.bukkit.craftbukkit.VERSION.inventory.CraftInventory", Object.class, "inventory");
    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    protected final String title;
    protected final InventoryType inventoryType;
    protected final SuperiorMenuButton[] buttons;
    protected final SoundWrapper openingSound;
    protected final boolean isPreviousMoveAllowed;

    protected SuperiorMenuPattern(String title, InventoryType inventoryType, SuperiorMenuButton[] buttons,
                                  SoundWrapper openingSound, boolean isPreviousMoveAllowed) {
        this.title = title;
        this.inventoryType = inventoryType;
        this.buttons = buttons;
        this.openingSound = openingSound;
        this.isPreviousMoveAllowed = isPreviousMoveAllowed;
    }

    public SuperiorMenuButton getButton(int slot) {
        return slot < 0 || slot >= this.buttons.length ? DummyButton.EMPTY_BUTTON : this.buttons[slot];
    }

    public int getRowsSize() {
        return this.buttons.length / 9;
    }

    public SoundWrapper getOpeningSound() {
        return openingSound;
    }

    public boolean isPreviousMoveAllowed() {
        return isPreviousMoveAllowed;
    }

    public Inventory buildInventory(ISuperiorMenu menu, Function<String, String> titleReplacer,
                                    SuperiorPlayer inventoryViewer, @Nullable SuperiorPlayer targetPlayer) {

        String title = titleReplacer.apply(this.title);

        Inventory inventory = createInventory(menu, PlaceholderHook.parse(inventoryViewer, title));

        setupInventory(inventory, menu, inventoryViewer, targetPlayer);

        return inventory;
    }

    public abstract void setupInventory(Inventory inventory, ISuperiorMenu superiorMenu,
                                        SuperiorPlayer inventoryViewer, @Nullable SuperiorPlayer targetPlayer);

    private Inventory createInventory(InventoryHolder holder, String title) {
        Inventory inventory;

        if (this.inventoryType != InventoryType.CHEST) {
            inventory = Bukkit.createInventory(holder, this.inventoryType, title);
        } else {
            inventory = Bukkit.createInventory(holder, this.buttons.length, title);
        }

        if (inventory.getHolder() == null)
            INVENTORY.set(inventory, plugin.getNMSAlgorithms().getCustomHolder(this.inventoryType, holder, title));

        return inventory;
    }

    @SuppressWarnings("unchecked")
    public static abstract class AbstractBuilder<B extends AbstractBuilder<B, T>, T> {

        protected String title = "";
        protected InventoryType inventoryType = InventoryType.CHEST;
        protected SuperiorMenuButton[] buttons;
        protected SoundWrapper openingSound;
        protected boolean isPreviousMoveAllowed;

        private int rowsSize = 6;

        public AbstractBuilder() {

        }

        public B setTitle(String title) {
            this.title = title;
            return (B) this;
        }

        public B setInventoryType(InventoryType inventoryType) {
            this.inventoryType = inventoryType;
            updateButtons();
            return (B) this;
        }

        public B setRowsSize(int rowsSize) {
            this.rowsSize = rowsSize;
            updateButtons();
            return (B) this;
        }

        public B setOpeningSound(SoundWrapper openingSound) {
            this.openingSound = openingSound;
            return (B) this;
        }

        public B setPreviousMoveAllowed(boolean previousMoveAllowed) {
            isPreviousMoveAllowed = previousMoveAllowed;
            return (B) this;
        }

        public B setButton(int slot, SuperiorMenuButton.AbstractBuilder<?, ?> buttonBuilder) {
            if (buttonBuilder != null && slot >= 0 && slot < this.buttons.length)
                this.buttons[slot] = buttonBuilder.build();
            return (B) this;
        }

        public B setButtons(List<Integer> slots, SuperiorMenuButton.AbstractBuilder<?, ?> buttonBuilder) {
            if(buttonBuilder != null) {
                for (int slot : slots) {
                    if (slot >= 0 && slot < this.buttons.length)
                        this.buttons[slot] = buttonBuilder.build();
                }
            }
            return (B) this;
        }

        public B mapButton(int slot, SuperiorMenuButton.AbstractBuilder<?, ?> buttonBuilder) {
            if (slot >= 0 && slot < this.buttons.length) {
                this.buttons[slot] = this.buttons[slot].applyToBuilder(buttonBuilder).build();
            }

            return (B) this;
        }

        public B mapButtons(List<Integer> slots, SuperiorMenuButton.AbstractBuilder<?, ?> buttonBuilder) {
            for (int slot : slots) {
                if (slot >= 0 && slot < this.buttons.length) {
                    this.buttons[slot] = this.buttons[slot].applyToBuilder(buttonBuilder).build();
                }
            }

            return (B) this;
        }

        public abstract T build();

        private void updateButtons() {
            switch (inventoryType) {
                case CHEST:
                    rowsSize = Math.max(Math.min(rowsSize, 6), 1);
                    this.buttons = new SuperiorMenuButton[rowsSize * 9];
                    break;
                case DROPPER:
                case DISPENSER:
                    this.buttons = new SuperiorMenuButton[inventoryType.getDefaultSize() - 1];
                    break;
                default:
                    this.buttons = new SuperiorMenuButton[inventoryType.getDefaultSize()];
                    break;
            }

            Arrays.fill(this.buttons, DummyButton.EMPTY_BUTTON);
        }

    }

}
