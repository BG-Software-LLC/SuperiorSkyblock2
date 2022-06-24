package com.bgsoftware.superiorskyblock.core.menu;

import java.util.Arrays;
import java.util.Collection;
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

    public List<Integer> getSlots(char character, Integer... defaultSlots) {
        List<Integer> slots = this.charSlots.get(character);
        return slots == null ? Arrays.asList(defaultSlots) : slots;
    }

    public List<Integer> getSlots(String str, Integer... defaultSlots) {
        for (char ch : str.toCharArray()) {
            List<Integer> slots = getSlots(ch);
            if (!slots.isEmpty())
                return slots;
        }

        return Arrays.asList(defaultSlots);
    }

    public int getSlot(char character, int defaultSlot) {
        List<Integer> slots = getSlots(character);
        return slots.isEmpty() ? defaultSlot : slots.get(0);
    }

    public int getSlot(char character) {
        return getSlot(character, -1);
    }

    public int getSlot(String str, int defaultSlot) {
        List<Integer> slots = getSlots(str);
        return slots.isEmpty() ? defaultSlot : slots.get(0);
    }

    public int getSlot(String str) {
        return getSlot(str, -1);
    }

}
