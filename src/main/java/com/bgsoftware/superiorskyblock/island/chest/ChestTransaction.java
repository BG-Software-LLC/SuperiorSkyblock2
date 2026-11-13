package com.bgsoftware.superiorskyblock.island.chest;

import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.IntUnaryOperator;

public final class ChestTransaction {

    private final ItemStack[] topContents;
    private final ItemStack[] playerContents;
    private final List<ItemStack> drops = new ArrayList<>();
    private ItemStack cursor;

    public ChestTransaction(ItemStack[] top, ItemStack[] playerContents, ItemStack cursor) {
        if (top == null || playerContents == null || playerContents.length < 36)
            throw new IllegalArgumentException("Chest and player contents are required");
        this.topContents = copy(top);
        this.playerContents = copy(playerContents);
        this.cursor = copy(cursor);
    }

    public boolean click(int rawSlot, int convertedSlot, InventoryAction action,
                         int hotbarButton, boolean offhand, boolean creative) {
        if (action == null)
            return false;
        if (action == InventoryAction.DROP_ALL_CURSOR || action == InventoryAction.DROP_ONE_CURSOR) {
            if (cursor == null)
                return false;
            int amount = action == InventoryAction.DROP_ONE_CURSOR ? 1 : cursor.getAmount();
            drops.add(withAmount(cursor, amount));
            cursor = withAmount(cursor, cursor.getAmount() - amount);
            return true;
        }
        if (action == InventoryAction.COLLECT_TO_CURSOR)
            return collect();
        int slot = resolve(rawSlot, convertedSlot);
        if (slot < 0)
            return false;
        ItemStack current = get(slot);
        switch (action) {
            case PICKUP_ALL:
            case PICKUP_SOME:
                return pickup(slot, current == null ? 0 : current.getAmount());
            case PICKUP_HALF:
                return pickup(slot, current == null ? 0 : (current.getAmount() + 1) / 2);
            case PICKUP_ONE:
                return pickup(slot, 1);
            case PLACE_ALL:
            case PLACE_SOME:
                return place(slot, cursor == null ? 0 : cursor.getAmount());
            case PLACE_ONE:
                return place(slot, 1);
            case SWAP_WITH_CURSOR:
                if (same(current, cursor))
                    return false;
                set(slot, cursor);
                cursor = current;
                return true;
            case MOVE_TO_OTHER_INVENTORY:
                return shift(slot);
            case HOTBAR_SWAP:
            case HOTBAR_MOVE_AND_READD:
                int selected = offhand ? 40 : hotbarButton;
                if ((!offhand && (selected < 0 || selected > 8)) || selected >= playerContents.length)
                    return false;
                return hotbar(slot, topContents.length + selected,
                        action == InventoryAction.HOTBAR_MOVE_AND_READD);
            case CLONE_STACK:
                if (!creative || current == null || cursor != null)
                    return false;
                cursor = withAmount(current, limit(current));
                return true;
            case DROP_ALL_SLOT:
            case DROP_ONE_SLOT:
                if (current == null)
                    return false;
                int amount = action == InventoryAction.DROP_ONE_SLOT ? 1 : current.getAmount();
                drops.add(withAmount(current, amount));
                set(slot, withAmount(current, current.getAmount() - amount));
                return true;
            default:
                return false;
        }
    }

    public boolean drag(Map<Integer, ItemStack> newItems, ItemStack newCursor,
                        IntUnaryOperator convertSlot, boolean creative) {
        if (cursor == null || newItems == null || newItems.isEmpty() || convertSlot == null)
            return false;
        ItemStack resultCursor;
        try {
            resultCursor = copy(newCursor);
        } catch (IllegalArgumentException ex) {
            return false;
        }
        if (resultCursor != null && !cursor.isSimilar(resultCursor))
            return false;
        long added = 0;
        Set<Integer> seenSlots = new HashSet<>();
        List<Integer> slots = new ArrayList<>();
        List<ItemStack> contents = new ArrayList<>();
        for (Map.Entry<Integer, ItemStack> entry : newItems.entrySet()) {
            if (entry.getKey() == null)
                return false;
            int slot;
            ItemStack next;
            try {
                slot = resolve(entry.getKey(), convertSlot.applyAsInt(entry.getKey()));
                next = copy(entry.getValue());
            } catch (RuntimeException ex) {
                return false;
            }
            if (slot < 0 || !seenSlots.add(slot) || next == null || !cursor.isSimilar(next))
                return false;
            ItemStack previous = get(slot);
            if (previous != null && !previous.isSimilar(cursor))
                return false;
            int increase = next.getAmount() - (previous == null ? 0 : previous.getAmount());
            if (increase <= 0)
                return false;
            added += increase;
            slots.add(slot);
            contents.add(next);
        }
        if (!creative && added + (resultCursor == null ? 0 : resultCursor.getAmount()) != cursor.getAmount())
            return false;
        for (int i = 0; i < slots.size(); ++i)
            set(slots.get(i), contents.get(i));
        cursor = resultCursor;
        return true;
    }

    public ItemStack[] getTopContents() {
        return copy(topContents);
    }

    public ItemStack[] getPlayerContents() {
        return copy(playerContents);
    }

    public ItemStack getCursor() {
        return copy(cursor);
    }

    public List<ItemStack> getDrops() {
        List<ItemStack> result = new ArrayList<>(drops.size());
        for (ItemStack item : drops)
            result.add(copy(item));
        return result;
    }

    private boolean pickup(int slot, int requested) {
        ItemStack current = get(slot);
        if (current == null || requested <= 0 || cursor != null && !cursor.isSimilar(current))
            return false;
        int currentAmount = cursor == null ? 0 : cursor.getAmount();
        int moved = Math.min(Math.min(requested, current.getAmount()), limit(current) - currentAmount);
        if (moved <= 0)
            return false;
        cursor = withAmount(current, currentAmount + moved);
        set(slot, withAmount(current, current.getAmount() - moved));
        return true;
    }

    private boolean place(int slot, int requested) {
        ItemStack current = get(slot);
        if (cursor == null || requested <= 0 || current != null && !current.isSimilar(cursor))
            return false;
        int currentAmount = current == null ? 0 : current.getAmount();
        int moved = Math.min(Math.min(requested, cursor.getAmount()), limit(cursor) - currentAmount);
        if (moved <= 0)
            return false;
        set(slot, withAmount(cursor, currentAmount + moved));
        cursor = withAmount(cursor, cursor.getAmount() - moved);
        return true;
    }

    private boolean shift(int slot) {
        ItemStack current = get(slot);
        if (current == null)
            return false;
        int remaining = insert(current, slot < topContents.length ? playerOrder(true) : topOrder(), -1);
        if (remaining == current.getAmount())
            return false;
        set(slot, withAmount(current, remaining));
        return true;
    }

    private boolean hotbar(int slot, int selected, boolean readd) {
        if (slot == selected)
            return false;
        ItemStack current = get(slot);
        ItemStack picked = get(selected);
        if (!readd) {
            if (same(current, picked))
                return false;
            set(slot, picked);
            set(selected, current);
            return true;
        }
        if (current == null && picked == null)
            return false;
        set(slot, null);
        set(selected, current);
        if (picked != null) {
            int remaining = insert(picked, playerOrder(false), selected);
            if (remaining > 0)
                drops.add(withAmount(picked, remaining));
        }
        return true;
    }

    private boolean collect() {
        if (cursor == null)
            return false;
        int original = cursor.getAmount();
        int remaining = limit(cursor) - original;
        int[] playerOrder = playerOrder(false);
        for (int pass = 0; pass < 2 && remaining > 0; ++pass) {
            for (int i = 0; i < topContents.length + playerOrder.length && remaining > 0; ++i) {
                int slot = i < topContents.length ? i : playerOrder[i - topContents.length];
                ItemStack current = get(slot);
                if (current == null || !cursor.isSimilar(current) || pass == 0 && current.getAmount() >= limit(current))
                    continue;
                int moved = Math.min(remaining, current.getAmount());
                set(slot, withAmount(current, current.getAmount() - moved));
                remaining -= moved;
            }
        }
        int amount = limit(cursor) - remaining;
        if (amount == original)
            return false;
        cursor = withAmount(cursor, amount);
        return true;
    }

    private int insert(ItemStack item, int[] slots, int excluded) {
        int remaining = item.getAmount();
        for (int pass = 0; pass < 2 && remaining > 0; ++pass) {
            for (int slot : slots) {
                if (slot == excluded)
                    continue;
                ItemStack current = get(slot);
                if (pass == 0 ? current == null || !current.isSimilar(item) : current != null)
                    continue;
                int previous = current == null ? 0 : current.getAmount();
                int moved = Math.min(remaining, limit(item) - previous);
                if (moved <= 0)
                    continue;
                set(slot, withAmount(item, previous + moved));
                remaining -= moved;
                if (remaining == 0)
                    break;
            }
        }
        return remaining;
    }

    private int[] topOrder() {
        int[] result = new int[topContents.length];
        for (int i = 0; i < result.length; ++i)
            result[i] = i;
        return result;
    }

    private int[] playerOrder(boolean reverse) {
        int[] result = new int[36];
        for (int i = 0; i < result.length; ++i) {
            int index = reverse ? 35 - i : i;
            result[i] = topContents.length + (index < 27 ? index + 9 : index - 27);
        }
        return result;
    }

    private int resolve(int rawSlot, int convertedSlot) {
        if (rawSlot >= 0 && rawSlot < topContents.length)
            return rawSlot;
        if (rawSlot >= topContents.length && rawSlot < topContents.length + 36
                && convertedSlot >= 0 && convertedSlot < 36)
            return topContents.length + convertedSlot;
        return -1;
    }

    private ItemStack get(int slot) {
        return slot < topContents.length ? topContents[slot] : playerContents[slot - topContents.length];
    }

    private void set(int slot, ItemStack item) {
        if (slot < topContents.length)
            topContents[slot] = item;
        else
            playerContents[slot - topContents.length] = item;
    }

    private static boolean same(ItemStack first, ItemStack second) {
        return first == null ? second == null : second != null && first.getAmount() == second.getAmount()
                && first.isSimilar(second);
    }

    private static int limit(ItemStack item) {
        return Math.min(64, item.getMaxStackSize());
    }

    private static ItemStack withAmount(ItemStack item, int amount) {
        if (amount == 0)
            return null;
        ItemStack result = item.clone();
        result.setAmount(amount);
        return result;
    }

    private static ItemStack copy(ItemStack item) {
        if (item == null)
            return null;
        if (item.getAmount() < 0 || item.getAmount() > limit(item))
            throw new IllegalArgumentException("Invalid item stack amount");
        if (item.getAmount() == 0 || item.getType() == Material.AIR)
            return null;
        return item.clone();
    }

    private static ItemStack[] copy(ItemStack[] items) {
        ItemStack[] result = new ItemStack[items.length];
        for (int i = 0; i < items.length; ++i)
            result[i] = copy(items[i]);
        return result;
    }
}