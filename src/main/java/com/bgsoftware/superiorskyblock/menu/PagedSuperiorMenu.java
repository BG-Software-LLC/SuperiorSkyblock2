package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.function.Function;

@SuppressWarnings({"unused", "WeakerAccess"})
public abstract class PagedSuperiorMenu<T> extends SuperiorMenu {

    private List<T> objects;
    protected int currentPage = 1;
    private boolean acceptNull;

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

        int previousSlot = getPreviousSlot(), nextSlot = getNextSlot(), currentSlot = getCurrentSlot();
        List<Integer> slots = getSlots();

        for(int i = 0; i < slots.size(); i++){
            int objectIndex = i + (slots.size() * (currentPage - 1));

            if(objectIndex < objects.size()) {
                inventory.setItem(slots.get(i), getObjectItem(inventory.getItem(slots.get(i)), objects.get(objectIndex)));
            }
            else{
                inventory.setItem(slots.get(i), getNullItem());
            }
        }

        if(previousSlot >= 0)
            inventory.setItem(previousSlot, new ItemBuilder(inventory.getItem(previousSlot))
                    .replaceAll("{0}", (currentPage == 1 ? "&c" : "&a")).build(superiorPlayer));

        if(currentSlot >= 0)
            inventory.setItem(currentSlot, new ItemBuilder(inventory.getItem(currentSlot))
                    .replaceAll("{0}", currentPage + "").build(superiorPlayer));

        if(nextSlot >= 0)
            inventory.setItem(nextSlot, new ItemBuilder(inventory.getItem(nextSlot))
                    .replaceAll("{0}", (objects.size() > currentPage * slots.size() ? "&a" : "&c")).build(superiorPlayer));

        return inventory;
    }

    @Override
    protected final void onPlayerClick(InventoryClickEvent e) {
        int previousSlot = getPreviousSlot(), nextSlot = getNextSlot(), currentSlot = getCurrentSlot();
        List<Integer> slots = getSlots();

        if(e.getRawSlot() == previousSlot || e.getRawSlot() == nextSlot || e.getRawSlot() == currentSlot){
            if(e.getRawSlot() == currentSlot)
                return;

            boolean nextPage = slots.size() * currentPage < objects.size();

            if((!nextPage && e.getRawSlot() == nextSlot) || (currentPage == 1 && e.getRawSlot() == previousSlot))
                return;

            currentPage = e.getRawSlot() == nextSlot ? currentPage + 1 : currentPage - 1;

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

    public void setCurrentSlot(int currentSlot){
        addData("currentSlot", currentSlot);
    }

    public void setNextSlot(int nextSlot) {
        addData("nextSlot", nextSlot);
    }

    public void setPreviousSlot(int previousSlot) {
        addData("previousSlot", previousSlot);
    }

    public void setSlots(List<Integer> slots) {
        addData("slots", slots);
        slots.sort(Integer::compareTo);
    }

    private int getCurrentSlot(){
        return (Integer) getData("currentSlot");
    }

    private int getNextSlot(){
        return (Integer) getData("nextSlot");
    }

    private int getPreviousSlot(){
        return (Integer) getData("previousSlot");
    }

    private List<Integer> getSlots(){
        //noinspection unchecked
        return (List<Integer>) getData("slots");
    }

}
