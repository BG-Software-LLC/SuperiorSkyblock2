package com.ome_r.superiorskyblock.utils.legacy;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public enum Materials {

    PLAYER_HEAD("SKULL_ITEM", 3),
    GOLDEN_AXE("GOLD_AXE"),
    SPAWNER("MOB_SPAWNER");

    Materials(String bukkitType){
        this(bukkitType, 0);
    }

    Materials(String bukkitType, int bukkitData){
        this.bukkitType = bukkitType;
        this.bukkitData = (short) bukkitData;
    }

    private String bukkitType;
    private short bukkitData;

    public Material toBukkitType(){
        try {
            try {
                return Material.valueOf(bukkitType);
            } catch (IllegalArgumentException ex) {
                return Material.valueOf(name());
            }
        }catch(Exception ex){
            throw new IllegalArgumentException("Couldn't cast " + name() + " into a bukkit enum. Contact Ome_R!");
        }
    }

    public ItemStack toBukkitItem(){
        return toBukkitItem(1);
    }

    public ItemStack toBukkitItem(int amount){
        return bukkitData == 0 ? new ItemStack(toBukkitType(), amount) : new ItemStack(toBukkitType(), amount, bukkitData);
    }
}