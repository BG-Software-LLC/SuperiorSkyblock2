package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"unused", "WeakerAccess"})
public abstract class PagedMappedSuperiorMenu<K, V> extends PagedSuperiorMenu<K> {

    private Map<K, V> objects;

    public PagedMappedSuperiorMenu(String identifier, SuperiorPlayer superiorPlayer){
        super(identifier, superiorPlayer);
    }

    @Override
    protected void onPlayerClick(InventoryClickEvent event, K clickedObject) {
        onPlayerClick(event, clickedObject, objects.get(clickedObject));
    }

    @Override
    protected ItemStack getObjectItem(ItemStack clickedItem, K value) {
        return getObjectItem(clickedItem, value, objects.get(value));
    }

    @Override
    protected List<K> requestObjects() {
        this.objects = requestMappedObjects();
        return new ArrayList<>(objects.keySet());
    }
    
    protected abstract void onPlayerClick(InventoryClickEvent event, K clickedObjectKey, V clickedObjectValue);

    protected abstract ItemStack getObjectItem(ItemStack clickedItem, K itemKey, V itemValue);

    protected abstract Map<K, V> requestMappedObjects();

}
