package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.bukkit.Sound;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class SuperiorMenu implements InventoryHolder {

    private static Map<UUID, SuperiorMenu> previousMenus = new HashMap<>();

    private String identifier;
    private Sound openSound = null, closeSound = null;
    protected Inventory inventory;

    public SuperiorMenu(String identifier){
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }

    public Sound getOpenSound() {
        return openSound;
    }

    public Sound getCloseSound() {
        return closeSound;
    }

    public void setOpenSound(Sound openSound) {
        this.openSound = openSound;
    }

    public void setCloseSound(Sound closeSound) {
        this.closeSound = closeSound;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void openInventory(SuperiorPlayer superiorPlayer, boolean savePrevious){
        if(openSound != null)
            superiorPlayer.asPlayer().playSound(superiorPlayer.getLocation(), openSound, 1, 1);

        superiorPlayer.asPlayer().openInventory(inventory);
        if(savePrevious)
            previousMenus.put(superiorPlayer.getUniqueId(), this);
    }

    public void closeInventory(SuperiorPlayer superiorPlayer){
        SuperiorMenu previousMenu = previousMenus.get(superiorPlayer.getUniqueId());

        if(closeSound != null && (previousMenu == null || previousMenu.openSound == null))
            superiorPlayer.asPlayer().playSound(superiorPlayer.getLocation(), closeSound, 1, 1);

        if(previousMenu != null)
            previousMenu.openInventory(superiorPlayer, false);
        else
            superiorPlayer.asPlayer().closeInventory();
    }

    public abstract void onClick(InventoryClickEvent e);

}
