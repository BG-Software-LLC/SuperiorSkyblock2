package com.bgsoftware.superiorskyblock.menu.file;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class MenuPatternSlots {

    private final Map<Character, List<Integer>> charSlots = new HashMap<>();

    public MenuPatternSlots() {
    }

    public Collection<Character> getChars() {
        return this.charSlots.keySet();
    }

    public void addSlot(char character, int slot) {
        this.charSlots.computeIfAbsent(character, ch -> new ArrayList<>()).add(slot);
    }

    public List<Integer> getSlots(char character, Integer... defaultSlots) {
        List<Integer> slots = this.charSlots.get(character);
        return slots == null ? Arrays.asList(defaultSlots) : slots;
    }

    public List<Integer> getSlots(String str, Integer... defaultSlots) {
        for(char ch : str.toCharArray()) {
            List<Integer> slots = getSlots(ch);
            if(!slots.isEmpty())
                return slots;
        }

        return Arrays.asList(defaultSlots);
    }

    public int getSlot(char character, int defaultSlot) {
        return getSlots(character, defaultSlot).get(0);
    }

    public int getSlot(char character) {
        return getSlots(character).get(0);
    }

    public int getSlot(String str, int defaultSlot) {
        return getSlots(str, defaultSlot).get(0);
    }

    public int getSlot(String str) {
        return getSlots(str).get(0);
    }

}
