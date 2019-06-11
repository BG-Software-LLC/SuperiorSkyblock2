package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.threads.SuperiorThread;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public abstract class SuperiorMenu implements InventoryHolder {

    protected static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private String identifier;
    protected Sound openSound = null, closeSound = null;
    protected SuperiorMenu previousMenu;
    protected boolean previousMove = true;

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
    public abstract Inventory getInventory();

    public abstract void onClick(InventoryClickEvent e);

    public void openInventory(SuperiorPlayer superiorPlayer, SuperiorMenu previousMenu){
        if(Bukkit.isPrimaryThread()){
            new SuperiorThread(() -> openInventory(superiorPlayer, previousMenu)).start();
            return;
        }

        Inventory inventory = getInventory();

        Bukkit.getScheduler().runTask(plugin, () -> {
            superiorPlayer.asPlayer().openInventory(inventory);

            if(openSound != null)
                superiorPlayer.asPlayer().playSound(superiorPlayer.getLocation(), openSound, 1, 1);

            this.previousMenu = previousMenu;
        });
    }

    public void closeInventory(SuperiorPlayer superiorPlayer){
        if(closeSound != null && (previousMenu == null || !previousMove || previousMenu.openSound == null))
            superiorPlayer.asPlayer().playSound(superiorPlayer.getLocation(), closeSound, 1, 1);

        if(previousMenu != null) {
            Bukkit.getScheduler().runTask(plugin, () -> {
                if(previousMove)
                    previousMenu.openInventory(superiorPlayer, previousMenu.previousMenu);
                else
                    previousMove = true;
            });
        }
    }

    protected static Sound getSound(String name){
        try{
            return Sound.valueOf(name);
        }catch(Exception ex){
            return null;
        }
    }

}
