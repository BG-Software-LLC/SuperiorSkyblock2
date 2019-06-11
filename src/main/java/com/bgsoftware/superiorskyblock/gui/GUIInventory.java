package com.bgsoftware.superiorskyblock.gui;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class GUIInventory {

    public static final String ROLE_PAGE_IDENTIFIER = "rolePage";
    public static final String UPGRADES_PAGE_IDENTIFIER = "upgradesPage";

    private static Map<UUID, GUIInventory> openedInventories = Maps.newHashMap();
    private static SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
    private static Map<GUIInventory, Set<UUID>> recentlyOpened = Maps.newHashMap();

    private Inventory inventory;
    private Map<String, Object> data = Maps.newHashMap();

    private GUIInventory(String identifier, Inventory inventory){
        if(inventory.getType() == InventoryType.CHEST)
            this.inventory = Bukkit.createInventory(new GUIIdentifier(identifier), inventory.getSize(), inventory.getTitle());
        else
            this.inventory = Bukkit.createInventory(new GUIIdentifier(identifier), inventory.getType(), inventory.getTitle());
        this.inventory.setContents(inventory.getContents());
        put("identifier", identifier);
        recentlyOpened.put(this, Sets.newHashSet());
    }

    public GUIInventory withSounds(Sound openSound, Sound closeSound){
        put("openSound", openSound);
        put("closeSound", closeSound);
        return this;
    }

    public void openInventory(SuperiorPlayer superiorPlayer, boolean cloned){
        openInventory(superiorPlayer, cloned ? clonedInventory() : getInventory());
    }

    public void openInventory(SuperiorPlayer superiorPlayer, Inventory inventory){
        if(!Bukkit.isPrimaryThread()){
            Bukkit.getScheduler().runTask(plugin, () -> openInventory(superiorPlayer, inventory));
            return;
        }
        if(!recentlyOpened.get(this).contains(superiorPlayer.getUniqueId())) {
            recentlyOpened(superiorPlayer.getUniqueId());
            Bukkit.getScheduler().runTaskLater(plugin, () -> recentlyOpened.get(this).remove(superiorPlayer.getUniqueId()), 20L);
            playOpenSound(superiorPlayer);
            superiorPlayer.asPlayer().openInventory(inventory);
            openedInventories.put(superiorPlayer.getUniqueId(), this);
        }
    }

    public void closeInventory(SuperiorPlayer superiorPlayer){
        openedInventories.remove(superiorPlayer.getUniqueId());
        playCloseSound(superiorPlayer);
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

    public String getIdentifier(){
        return get("identifier", String.class);
    }

    private void playOpenSound(SuperiorPlayer superiorPlayer){
        if(contains("openSound")) {
            Sound openSound = get("openSound", Sound.class);
            Player player = superiorPlayer.asPlayer();
            if(player != null)
                player.playSound(player.getLocation(), openSound, 1, 1);
        }
    }

    private void playCloseSound(SuperiorPlayer superiorPlayer){
        if(contains("closeSound")) {
            Sound closeSound = get("closeSound", Sound.class);
            superiorPlayer.asPlayer().playSound(superiorPlayer.getLocation(), closeSound, 1, 1);
        }
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
        inventory.setItem(slot, itemStack);
    }

    public Inventory clonedInventory(){
        Inventory inventory = Bukkit.createInventory(new GUIIdentifier(getIdentifier()), this.inventory.getSize(), this.inventory.getTitle());
        inventory.setContents(this.inventory.getContents());
        return inventory;
    }

    private Inventory getInventory(){
        return inventory;
    }

    private void recentlyOpened(UUID uuid){
        for(GUIInventory gui : recentlyOpened.keySet()){
            recentlyOpened.get(gui).remove(uuid);
        }
        recentlyOpened.get(this).add(uuid);
    }

    public static GUIInventory from(SuperiorPlayer superiorPlayer){
        return openedInventories.get(superiorPlayer.getUniqueId());
    }

    public static GUIInventory from(String identifier, Inventory inventory){
        return new GUIInventory(identifier, inventory);
    }

}
