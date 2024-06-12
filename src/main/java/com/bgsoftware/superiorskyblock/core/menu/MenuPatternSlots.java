package com.bgsoftware.superiorskyblock.core.menu;

import com.bgsoftware.superiorskyblock.core.collections.CollectionsFactory;
import com.bgsoftware.superiorskyblock.core.collections.view.Char2ObjectMapView;
import com.bgsoftware.superiorskyblock.core.collections.view.CharIterator;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class MenuPatternSlots {

    private final Char2ObjectMapView<List<Integer>> charSlots = CollectionsFactory.createChar2ObjectArrayMap();

    public MenuPatternSlots() {
    }

    public CharIterator getChars() {
        return this.charSlots.keyIterator();
    }

    public void addSlot(char character, int slot) {
        this.charSlots.computeIfAbsent(character, ch -> new LinkedList<>()).add(slot);
    }

    public List<Integer> getSlots(char character) {
        List<Integer> slots = this.charSlots.get(character);
        return slots == null ? Collections.emptyList() : slots;
    }

    public List<Integer> getSlots(String str) {
        for (char ch : str.toCharArray()) {
            List<Integer> slots = getSlots(ch);
            if (!slots.isEmpty())
                return slots;
        }

        return Collections.emptyList();
    }

}
