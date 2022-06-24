package com.bgsoftware.superiorskyblock.core.menu.pattern;

import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.menu.ISuperiorMenu;
import com.bgsoftware.superiorskyblock.api.service.placeholders.PlaceholdersService;
import com.bgsoftware.superiorskyblock.core.GameSound;
import com.bgsoftware.superiorskyblock.core.SequentialListBuilder;
import com.bgsoftware.superiorskyblock.core.menu.button.SuperiorMenuButton;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.DummyButton;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public abstract class SuperiorMenuPattern<M extends ISuperiorMenu> {

    public static final char[] BUTTON_SYMBOLS = new char[]{
            '!', '@', '#', '$', '%', '^', '&', '*', '-', '_', '+', '=',
            '~', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '>',
            '<', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K',
            'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W',
            'X', 'Y', 'Z'
    };

    private static final ReflectField<Object> INVENTORY = new ReflectField<>(
            "org.bukkit.craftbukkit.VERSION.inventory.CraftInventory", Object.class, "inventory")
            .removeFinal();
    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    protected final String title;
    protected final InventoryType inventoryType;
    protected final SuperiorMenuButton<M>[] buttons;
    protected final GameSound openingSound;
    protected final boolean isPreviousMoveAllowed;

    protected SuperiorMenuPattern(String title, InventoryType inventoryType, SuperiorMenuButton<M>[] buttons,
                                  GameSound openingSound, boolean isPreviousMoveAllowed) {
        this.title = title;
        this.inventoryType = inventoryType;
        this.buttons = buttons;
        this.openingSound = openingSound;
        this.isPreviousMoveAllowed = isPreviousMoveAllowed;
    }

    @SuppressWarnings("unchecked")
    public SuperiorMenuButton<M> getButton(int slot) {
        return slot < 0 || slot >= this.buttons.length ? DummyButton.EMPTY_BUTTON : this.buttons[slot];
    }

    public Collection<SuperiorMenuButton<M>> getButtons() {
        return new SequentialListBuilder<SuperiorMenuButton<M>>().build(Arrays.asList(buttons));
    }

    public int getRowsSize() {
        return this.buttons.length / 9;
    }

    public GameSound getOpeningSound() {
        return openingSound;
    }

    public boolean isPreviousMoveAllowed() {
        return isPreviousMoveAllowed;
    }

    public Inventory buildInventory(M superiorMenu, Function<String, String> titleReplacer) {
        String title = titleReplacer.apply(this.title);

        PlaceholdersService placeholdersService = plugin.getServices().getPlaceholdersService();

        Inventory inventory = createInventory(superiorMenu, placeholdersService
                .parsePlaceholders(superiorMenu.getInventoryViewer().asOfflinePlayer(), title));

        setupInventory(inventory, superiorMenu);

        return inventory;
    }

    public abstract void setupInventory(Inventory inventory, M superiorMenu);

    private Inventory createInventory(InventoryHolder holder, String title) {
        Inventory inventory;

        if (this.inventoryType != InventoryType.CHEST) {
            inventory = Bukkit.createInventory(holder, this.inventoryType, title);
        } else {
            inventory = Bukkit.createInventory(holder, this.buttons.length, title);
        }

        if (inventory.getHolder() == null) {
            Object menuHolder = plugin.getNMSAlgorithms().createMenuInventoryHolder(this.inventoryType, holder, title);
            if (menuHolder != null)
                INVENTORY.set(inventory, menuHolder);
        }

        return inventory;
    }

    @SuppressWarnings("unchecked")
    public static abstract class AbstractBuilder<B extends AbstractBuilder<B, T, M>, T, M extends ISuperiorMenu> {

        protected String title = "";
        protected InventoryType inventoryType = InventoryType.CHEST;
        protected SuperiorMenuButton<M>[] buttons;
        protected GameSound openingSound;
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

        public B setOpeningSound(GameSound openingSound) {
            this.openingSound = openingSound;
            return (B) this;
        }

        public B setPreviousMoveAllowed(boolean previousMoveAllowed) {
            isPreviousMoveAllowed = previousMoveAllowed;
            return (B) this;
        }

        public B setButtons(SuperiorMenuButton<M>[] buttons) {
            if (buttons != null) {
                setRowsSize(buttons.length / 9);

                for (int slot = 0; slot < this.buttons.length && slot < buttons.length; ++slot)
                    this.buttons[slot] = buttons[slot];
            }

            return (B) this;
        }

        public B setButton(int slot, SuperiorMenuButton.AbstractBuilder<?, ?, M> buttonBuilder) {
            if (buttonBuilder != null && slot >= 0 && slot < this.buttons.length)
                this.buttons[slot] = buttonBuilder.build();
            return (B) this;
        }

        public B setButtons(List<Integer> slots, SuperiorMenuButton.AbstractBuilder<?, ?, M> buttonBuilder) {
            if (buttonBuilder != null) {
                for (int slot : slots) {
                    if (slot >= 0 && slot < this.buttons.length)
                        this.buttons[slot] = buttonBuilder.build();
                }
            }
            return (B) this;
        }

        public B mapButton(int slot, SuperiorMenuButton.AbstractBuilder<?, ?, M> buttonBuilder) {
            if (slot >= 0 && slot < this.buttons.length) {
                if (this.buttons[slot] == null) {
                    this.buttons[slot] = buttonBuilder.build();
                } else {
                    this.buttons[slot] = this.buttons[slot].applyToBuilder(buttonBuilder).build();
                }
            }

            return (B) this;
        }

        public B mapButtons(List<Integer> slots, SuperiorMenuButton.AbstractBuilder<?, ?, M> buttonBuilder) {
            for (int slot : slots) {
                if (slot >= 0 && slot < this.buttons.length) {
                    if (this.buttons[slot] == null) {
                        this.buttons[slot] = buttonBuilder.build();
                    } else {
                        this.buttons[slot] = this.buttons[slot].applyToBuilder(buttonBuilder).build();
                    }
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
