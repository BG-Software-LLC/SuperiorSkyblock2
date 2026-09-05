package com.bgsoftware.superiorskyblock.core.menu.layout.custom;

import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;

import java.util.ArrayList;
import java.util.List;

public class CustomPagedLayout<T extends MenuView<T, ?>> implements PagedLayout<T> {

    private final List<List<Integer>> slotsOrder;

    public CustomPagedLayout(List<List<Integer>> slotsOrder) {
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
        private List<Integer> currentSlots;

        private IteratorImpl(MenuTemplateButton<T>[] buttons) {
            this.buttons = buttons;
        }

        @Override
        public List<Integer> getSlots() {
            return this.currentSlots;
        }

        @Override
        public boolean hasNext() {
            if (this.cursor >= slotsOrder.size()) {
                return false;
            }

            List<Integer> slots = slotsOrder.get(this.cursor);

            for (int slot : slots) {
                if (slot < 0 || slot >= this.buttons.length) {
                    return false;
                }
            }

            return true;
        }

        @Override
        public MenuTemplateButton<T> next() {
            this.currentSlots = slotsOrder.get(this.cursor++);

            return this.buttons[this.currentSlots.get(0)];
        }

    }

}
