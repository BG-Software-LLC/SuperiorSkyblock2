package com.ome_r.superiorskyblock.gui;

import com.google.common.collect.Maps;
import com.ome_r.superiorskyblock.wrappers.WrappedPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class SyncGUIInventory {

    private Inventory inventory;
    private Sound openSound = null, closeSound = null;
    private Map<String, Object> data = new HashMap<>();

    public SyncGUIInventory(Inventory inventory){
        this.inventory = inventory;
    }

    public SyncGUIInventory(GUIInventory guiInventory){
        this.inventory = guiInventory.getInventory();
        openSound = guiInventory.openSound;
        closeSound = guiInventory.closeSound;
        data = Maps.newHashMap(guiInventory.data);
    }

    public SyncGUIInventory withSounds(Sound openSound, Sound closeSound){
        this.openSound = openSound;
        this.closeSound = closeSound;
        return this;
    }

    public void openInventory(WrappedPlayer wrappedPlayer){
        playOpenSound(wrappedPlayer);
        wrappedPlayer.asPlayer().openInventory(inventory);
    }

    public Inventory getInventory(){
        return inventory;
    }

    public ItemStack[] getContents() {
        return inventory.getContents();
    }

    public String getTitle() {
        return inventory.getTitle();
    }

    public int getSize(){
        return inventory.getSize();
    }

    public void playOpenSound(WrappedPlayer wrappedPlayer){
        if(openSound != null)
            wrappedPlayer.asPlayer().playSound(wrappedPlayer.getLocation(), openSound, 1, 1);
    }

    public void playCloseSound(WrappedPlayer wrappedPlayer){
        if(closeSound != null)
            wrappedPlayer.asPlayer().playSound(wrappedPlayer.getLocation(), closeSound, 1, 1);
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setItem(int slot, ItemStack itemStack){
        inventory.setItem(slot, itemStack);
    }

}
