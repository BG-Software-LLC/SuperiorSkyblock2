package com.bgsoftware.superiorskyblock.core.menu.layout;

import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.menu.hologram.HologramMenuStyle;
import com.bgsoftware.superiorskyblock.api.menu.layout.HologramMenuLayout;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.DummyButton;
import com.bgsoftware.superiorskyblock.core.menu.hologram.HologramMenuStyleImpl;
import com.bgsoftware.superiorskyblock.core.menu.hologram.HologramMenuWrapper;
import com.bgsoftware.superiorskyblock.core.messages.MessageContent;
import com.google.common.base.Preconditions;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class RegularHologramMenuLayoutImpl<V extends MenuView<V, ?>> extends AbstractMenuLayout<V> implements HologramMenuLayout<V> {

    private final List<String> rawBodyLines;
    private final List<MessageContent> bodyLines;
    private final HologramMenuStyle style;
    private final List<List<Integer>> rows;

    protected RegularHologramMenuLayoutImpl(AbstractBuilder<V, ?> builder) {
        super(builder);
        this.rawBodyLines = Collections.unmodifiableList(new ArrayList<>(builder.bodyLines));
        List<MessageContent> bodyLines = new ArrayList<>(this.rawBodyLines.size());
        for (String bodyLine : this.rawBodyLines)
            bodyLines.add(MessageContent.parse(bodyLine));
        this.bodyLines = Collections.unmodifiableList(bodyLines);
        this.style = builder.style;

        List<List<Integer>> rows = new ArrayList<>();
        if (builder.rows.isEmpty()) {
            // Each button is in a row of its own.
            for (int slot = 0; slot < this.buttons.length; ++slot)
                rows.add(Collections.singletonList(slot));
        } else {
            for (List<Integer> row : builder.rows)
                rows.add(Collections.unmodifiableList(new ArrayList<>(row)));
        }
        this.rows = Collections.unmodifiableList(rows);
    }

    @Override
    public List<String> getBodyLines() {
        return this.rawBodyLines;
    }

    public List<String> getBodyLines(OfflinePlayer offlinePlayer) {
        List<String> bodyLines = new LinkedList<>();
        for (MessageContent bodyLine : this.bodyLines)
            bodyLines.add(bodyLine.getContent(offlinePlayer).orElse(""));
        return bodyLines;
    }

    @Override
    public HologramMenuStyle getStyle() {
        return this.style;
    }

    @Override
    public List<List<Integer>> getRows() {
        return this.rows;
    }

    @Override
    public int getRowsCount() {
        throw new IllegalStateException("Called MenuLayout#getRowsCount on HologramMenuLayout");
    }

    @Override
    public final Inventory buildInventory(V menuView) {
        throw new IllegalStateException("Called MenuLayout#buildInventory on HologramMenuLayout");
    }

    public HologramMenuWrapper<V> buildHologramMenu(V menuView) {
        Preconditions.checkState(plugin.getNMSHologramMenus().isPresent(), "Hologram menus are not supported");
        return new HologramMenuWrapper<>(menuView);
    }

    @SuppressWarnings("unchecked")
    protected static abstract class AbstractBuilder<V extends MenuView<V, ?>, B extends AbstractBuilder<V, B>> extends AbstractMenuLayout.AbstractBuilder<V, B> {

        protected final List<String> bodyLines = new LinkedList<>();
        protected final List<List<Integer>> rows = new LinkedList<>();
        protected HologramMenuStyle style = HologramMenuStyleImpl.DEFAULT;

        protected AbstractBuilder() {
            this.buttons = new MenuTemplateButton[0];
        }

        public B addBodyLine(String bodyLine) {
            Preconditions.checkNotNull(bodyLine, "bodyLine parameter cannot be null");
            this.bodyLines.add(bodyLine);
            return (B) this;
        }

        public B setStyle(HologramMenuStyle style) {
            Preconditions.checkNotNull(style, "style parameter cannot be null");
            this.style = style;
            return (B) this;
        }

        public B addRow(List<Integer> slots) {
            Preconditions.checkNotNull(slots, "slots parameter cannot be null");
            this.rows.add(new ArrayList<>(slots));
            return (B) this;
        }

        public B setInventoryType(InventoryType inventoryType) {
            throw new IllegalStateException("Called MenuLayout.Builder#setInventoryType on HologramMenuLayout.Builder");
        }

        public B setRowsCount(int rowsCount) {
            throw new IllegalStateException("Called MenuLayout.Builder#setRowsCount on HologramMenuLayout.Builder");
        }

        @Override
        public B setButton(int slot, MenuTemplateButton<V> button) {
            ensureButtonsCapacity(slot);
            return super.setButton(slot, button);
        }

        @Override
        public B setButtons(MenuTemplateButton<V>[] buttons) {
            ensureButtonsCapacity(buttons.length - 1);
            return super.setButtons(buttons);
        }

        @Override
        public B setButtons(List<Integer> slots, MenuTemplateButton<V> button) {
            for (int slot : slots) {
                ensureButtonsCapacity(slot);
            }
            return super.setButtons(slots, button);
        }

        @Override
        public B mapButton(int slot, MenuTemplateButton.Builder<V> buttonBuilder) {
            ensureButtonsCapacity(slot);
            return super.mapButton(slot, buttonBuilder);
        }

        private void ensureButtonsCapacity(int slot) {
            int oldLength = this.buttons.length;
            if (slot >= oldLength) {
                this.buttons = Arrays.copyOf(this.buttons, slot + 1);
                Arrays.fill(this.buttons, oldLength, this.buttons.length, DummyButton.EMPTY_BUTTON);
            }
        }

    }

    public static class Builder<V extends MenuView<V, ?>> extends AbstractBuilder<V, Builder<V>> implements HologramMenuLayout.Builder<V> {

        @Override
        public RegularHologramMenuLayoutImpl<V> build() {
            return new RegularHologramMenuLayoutImpl<>(this);
        }

    }

}
