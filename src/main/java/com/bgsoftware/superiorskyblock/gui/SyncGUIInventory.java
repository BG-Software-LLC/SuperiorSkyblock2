package com.bgsoftware.superiorskyblock.gui;

import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.google.common.collect.Maps;
import org.bukkit.Sound;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

@SuppressWarnings({"WeakerAccess", "unused"})
public final class SyncGUIInventory {

    private Inventory inventory;
    private Sound openSound, closeSound;
    private Map<String, Object> data;

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

    public void openInventory(SuperiorPlayer superiorPlayer){
        playOpenSound(superiorPlayer);
        superiorPlayer.asPlayer().openInventory(inventory);
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

    public void playOpenSound(SuperiorPlayer superiorPlayer){
        if(openSound != null)
            superiorPlayer.asPlayer().playSound(superiorPlayer.getLocation(), openSound, 1, 1);
    }

    public void playCloseSound(SuperiorPlayer superiorPlayer){
        if(closeSound != null)
            superiorPlayer.asPlayer().playSound(superiorPlayer.getLocation(), closeSound, 1, 1);
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setItem(int slot, ItemStack itemStack){
        inventory.setItem(slot, itemStack);
    }

}
