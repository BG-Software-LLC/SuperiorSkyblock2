package com.ome_r.superiorskyblock.gui;

import com.google.common.collect.Maps;
import com.ome_r.superiorskyblock.wrappers.WrappedPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

public class GUIInventory {

    private ItemStack[] contents;
    private String title;
    Sound openSound = null, closeSound = null;
    Map<String, Object> data = Maps.newHashMap();

    public GUIInventory(Inventory inventory){
        contents = inventory.getContents();
        title = inventory.getTitle();
    }

    public GUIInventory withSounds(Sound openSound, Sound closeSound){
        this.openSound = openSound;
        this.closeSound = closeSound;
        return this;
    }

    public void openInventory(WrappedPlayer wrappedPlayer){
        playOpenSound(wrappedPlayer);
        wrappedPlayer.asPlayer().openInventory(getInventory());
    }

    public Inventory getInventory(){
        Inventory inventory = Bukkit.createInventory(null, contents.length, title);
        inventory.setContents(contents);
        return inventory;
    }

    public ItemStack[] getContents() {
        return contents;
    }

    public String getTitle() {
        return title;
    }

    public int getSize(){
        return contents.length;
    }

    public void playOpenSound(WrappedPlayer wrappedPlayer){
        if(openSound != null)
            wrappedPlayer.asPlayer().playSound(wrappedPlayer.getLocation(), openSound, 1, 1);
    }

    public void playCloseSound(WrappedPlayer wrappedPlayer){
        if(closeSound != null)
            wrappedPlayer.asPlayer().playSound(wrappedPlayer.getLocation(), closeSound, 1, 1);
    }

    public <T> T get(String key, Class<T> classType){
        return classType.cast(data.get(key));
    }

    public void put(String key, Object value){
        data.put(key, value);
    }

    public boolean contains(String key){
        return data.containsKey(key);
    }

    public void setItem(int slot, ItemStack itemStack){
        contents[slot] = itemStack;
    }

    public SyncGUIInventory toSyncGUI(){
        return new SyncGUIInventory(this);
    }

}
