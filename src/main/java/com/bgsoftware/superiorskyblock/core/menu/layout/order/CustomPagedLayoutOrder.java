package com.bgsoftware.superiorskyblock.core.menu.layout.order;

import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;

import java.util.ArrayList;
import java.util.List;

public class CustomPagedLayoutOrder<T extends MenuView<T, ?>> implements PagedLayoutOrder<T> {

    private final List<Integer> slotsOrder;

    public CustomPagedLayoutOrder(List<Integer> slotsOrder) {
        this.slotsOrder = new ArrayList<>(slotsOrder);
    }

    @Override
    public int getObjectsPerPageCount() {
        return this.slotsOrder.size();
    }

    @Override
    public MenuButtonsIterator<T> createIterator(MenuTemplateButton<T>[] buttons) {
        return new IteratorImpl(buttons);
    }

    private class IteratorImpl implements MenuButtonsIterator<T> {

        private final MenuTemplateButton<T>[] buttons;
        private int cursor = 0;
        private int currentSlot;

        private IteratorImpl(MenuTemplateButton<T>[] buttons) {
            this.buttons = buttons;
        }

        @Override
        public int getSlot() {
            return this.currentSlot;
        }

        @Override
        public boolean hasNext() {
            if (this.cursor >= slotsOrder.size())
                return false;

            int slot = slotsOrder.get(this.cursor);
            return slot >= 0 && slot < this.buttons.length;
        }

        @Override
        public MenuTemplateButton<T> next() {
            this.currentSlot = slotsOrder.get(this.cursor++);
            return this.buttons[this.currentSlot];
        }

    }

}
