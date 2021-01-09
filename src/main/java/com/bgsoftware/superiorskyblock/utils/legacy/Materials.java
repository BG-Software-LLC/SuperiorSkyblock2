package com.bgsoftware.superiorskyblock.utils.legacy;

import com.bgsoftware.superiorskyblock.utils.ServerVersion;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public enum Materials {

    CLOCK("WATCH"),
    PLAYER_HEAD("SKULL_ITEM", 3),
    GOLDEN_AXE("GOLD_AXE"),
    SPAWNER("MOB_SPAWNER"),
    SUNFLOWER("DOUBLE_PLANT"),
    BLACK_STAINED_GLASS_PANE("STAINED_GLASS_PANE", 15),
    BONE_MEAL("INK_SACK", 15),
    NETHER_PORTAL("PORTAL");

    Materials(String bukkitType){
        this(bukkitType, 0);
    }

    Materials(String bukkitType, int bukkitData){
        this.bukkitType = bukkitType;
        this.bukkitData = (short) bukkitData;
    }

    private final String bukkitType;
    private final short bukkitData;

    public Material toBukkitType(){
        try {
            return Material.valueOf(ServerVersion.isLegacy() ? bukkitType : name());
        }catch(Exception ex){
            throw new IllegalArgumentException("Couldn't cast " + name() + " into a bukkit enum. Contact Ome_R!");
        }
    }

    public ItemStack toBukkitItem(){
        return toBukkitItem(1);
    }

    public ItemStack toBukkitItem(int amount){
        return ServerVersion.isLegacy() ? new ItemStack(toBukkitType(), amount, bukkitData) : new ItemStack(toBukkitType(), amount);
    }

}