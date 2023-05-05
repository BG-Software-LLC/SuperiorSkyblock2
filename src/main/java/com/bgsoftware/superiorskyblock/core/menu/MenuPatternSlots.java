package com.bgsoftware.superiorskyblock.core.menu;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MenuPatternSlots {

    private final Map<Character, List<Integer>> charSlots = new HashMap<>();

    public MenuPatternSlots() {
    }

    public Collection<Character> getChars() {
        return this.charSlots.keySet();
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
