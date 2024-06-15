package com.bgsoftware.superiorskyblock.core.menu;

import com.bgsoftware.common.collections.Lists;
import com.bgsoftware.common.collections.Maps;
import com.bgsoftware.common.collections.ints.Int2ObjectMap;
import com.bgsoftware.common.collections.ints.IntIterator;
import com.bgsoftware.common.collections.ints.IntList;

public class MenuPatternSlots {

    private final Int2ObjectMap<IntList> charSlots = Maps.newInt2ObjectArrayMap();

    public MenuPatternSlots() {
    }

    public IntIterator getChars() {
        return this.charSlots.keysIterator();
    }

    public void addSlot(char character, int slot) {
        this.charSlots.computeIfAbsent(character, ch -> Lists.newIntArrayList()).add(slot);
    }

    public IntList getSlots(char character) {
        IntList slots = this.charSlots.get(character);
        return slots == null ? Lists.emptyIntList() : slots;
    }

    public IntList getSlots(String str) {
        for (char ch : str.toCharArray()) {
            IntList slots = getSlots(ch);
            if (!slots.isEmpty())
                return slots;
        }

        return Lists.emptyIntList();
    }

}
