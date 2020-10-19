package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

@SuppressWarnings({"unused", "WeakerAccess"})
public abstract class PagedSuperiorMenu<T> extends SuperiorMenu {

    private final boolean acceptNull;
    private List<T> objects;
    protected int currentPage = 1;
    protected Consumer<SuperiorPlayer> onPageMove = null;

    public PagedSuperiorMenu(String identifier, SuperiorPlayer superiorPlayer){
        this(identifier, superiorPlayer, false);
    }

    public PagedSuperiorMenu(String identifier, SuperiorPlayer superiorPlayer, boolean acceptNull){
        super(identifier, superiorPlayer);
        this.acceptNull = acceptNull;
    }

    @Override
    protected Inventory buildInventory(Function<String, String> titleReplacer) {
        Inventory inventory = super.buildInventory(titleReplacer);

        objects = requestObjects();

        List<Integer> previousSlot = getPreviousSlot(), nextSlot = getNextSlot(), currentSlot = getCurrentSlot(), slots = getSlots();

        for(int i = 0; i < slots.size(); i++){
            int objectIndex = i + (slots.size() * (currentPage - 1));

            int slot = slots.get(i);

            if(slot >= 0) {
                if (objectIndex < objects.size()) {
                    inventory.setItem(slot, getObjectItem(inventory.getItem(slots.get(i)), objects.get(objectIndex)));
                    if(inventory.getItem(slot) == null)
                        SuperiorSkyblockPlugin.log("Warning: Cannot get item-type of " + objects.get(objectIndex));
                } else {
                    inventory.setItem(slot, getNullItem());
                }

            }
        }

        for(int _previousSlot : previousSlot) {
            if (_previousSlot >= 0)
                inventory.setItem(_previousSlot, new ItemBuilder(inventory.getItem(_previousSlot))
                        .replaceAll("{0}", (currentPage == 1 ? "&c" : "&a")).build(superiorPlayer));
        }

        for(int _currentSlot : currentSlot) {
            if (_currentSlot >= 0)
                inventory.setItem(_currentSlot, new ItemBuilder(inventory.getItem(_currentSlot))
                        .replaceAll("{0}", currentPage + "").build(superiorPlayer));
        }

        for(int _nextSlot : nextSlot) {
            if (_nextSlot >= 0)
                inventory.setItem(_nextSlot, new ItemBuilder(inventory.getItem(_nextSlot))
                        .replaceAll("{0}", (objects.size() > currentPage * slots.size() ? "&a" : "&c")).build(superiorPlayer));
        }

        return inventory;
    }

    @Override
    protected final void onPlayerClick(InventoryClickEvent e) {
        List<Integer> previousSlot = getPreviousSlot(), nextSlot = getNextSlot(), currentSlot = getCurrentSlot(), slots = getSlots();

        boolean isPreviousSlot = previousSlot.contains(e.getRawSlot()),
                isNextSlot = nextSlot.contains(e.getRawSlot()),
                isCurrentSlot = currentSlot.contains(e.getRawSlot());

        if(isPreviousSlot || isNextSlot || isCurrentSlot){
            if(isCurrentSlot)
                return;

            boolean nextPage = slots.size() * currentPage < objects.size();

            if((!nextPage && isNextSlot) || (currentPage == 1 && isPreviousSlot))
                return;

            currentPage = isNextSlot ? currentPage + 1 : currentPage - 1;

            if(onPageMove != null)
                onPageMove.accept(superiorPlayer);

            previousMove = false;
            open(previousMenu);
        }
        else{
            if(e.getCurrentItem() == null)
                return;

            objects = requestObjects();

            int indexOf = getSlots().indexOf(e.getRawSlot());
            int objectIndex = indexOf + (slots.size() * (currentPage - 1));

            if(objectIndex >= objects.size() || indexOf == -1){
                if(acceptNull){
                    onPlayerClick(e, null);
                }

                return;
            }

            onPlayerClick(e, objects.get(objectIndex));
        }
    }

    protected abstract void onPlayerClick(InventoryClickEvent event, T clickedObject);

    protected abstract ItemStack getObjectItem(ItemStack clickedItem, T value);

    protected ItemStack getNullItem(){
        return new ItemStack(Material.AIR);
    }

    protected abstract List<T> requestObjects();

    protected void setPageMoveRunnable(Consumer<SuperiorPlayer> onPageMove){
        this.onPageMove = onPageMove;
    }

    public void setCurrentSlot(List<Integer> currentSlot){
        addData("currentSlot", currentSlot);
    }

    public void setNextSlot(List<Integer> nextSlot) {
        addData("nextSlot", nextSlot);
    }

    public void setPreviousSlot(List<Integer> previousSlot) {
        addData("previousSlot", previousSlot);
    }

    public void setSlots(List<Integer> slots) {
        if(slots == null)
            throw new IllegalArgumentException("The menu " + getIdentifier() + " doesn't have any available slots.");

        addData("slots", slots);
        slots.sort(Integer::compareTo);
    }

    private List<Integer> getCurrentSlot(){
        //noinspection unchecked
        return (List<Integer>) getData("currentSlot");
    }

    private List<Integer> getNextSlot(){
        //noinspection unchecked
        return (List<Integer>) getData("nextSlot");
    }

    private List<Integer> getPreviousSlot(){
        //noinspection unchecked
        return (List<Integer>) getData("previousSlot");
    }

    private List<Integer> getSlots(){
        //noinspection unchecked
        return (List<Integer>) getData("slots");
    }

}
