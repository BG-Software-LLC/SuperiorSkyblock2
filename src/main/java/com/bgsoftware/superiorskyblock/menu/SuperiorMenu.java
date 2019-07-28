package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.threads.SuperiorThread;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"unused", "WeakerAccess"})
public abstract class SuperiorMenu implements InventoryHolder {

    protected static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private String identifier;
    protected SuperiorMenu previousMenu;
    protected boolean previousMove = true;

    private static final Map<String, Map<Integer, SoundWrapper>> sounds = new HashMap<>();
    private static final Map<String, Map<Integer, List<String>>> commands = new HashMap<>();

    public SuperiorMenu(String identifier){
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }

    void addSound(int slot, SoundWrapper sound) {
        if(sound != null) {
            Map<Integer, SoundWrapper> soundMap = sounds.getOrDefault(identifier, new HashMap<>());
            soundMap.put(slot, sound);
            sounds.put(identifier, soundMap);
        }
    }

    void addCommands(int slot, List<String> commands) {
        if(commands != null && !commands.isEmpty()) {
            Map<Integer, List<String>> commandMap = SuperiorMenu.commands.getOrDefault(identifier, new HashMap<>());
            commandMap.put(slot, commands);
            SuperiorMenu.commands.put(identifier, commandMap);
        }
    }

    SoundWrapper getSound(int slot){
        return sounds.containsKey(identifier) ? sounds.get(identifier).get(slot) : null;
    }

    List<String> getCommands(int slot){
        return SuperiorMenu.commands.containsKey(identifier) ? SuperiorMenu.commands.get(identifier).get(slot) : null;
    }

    @Override
    public abstract Inventory getInventory();

    public void onClick(InventoryClickEvent e){
        if(!(e.getWhoClicked() instanceof Player))
            return;

        Player player = (Player) e.getWhoClicked();

        SoundWrapper sound = getSound(e.getRawSlot());
        if(sound != null)
            sound.playSound(player);

        List<String> commands = getCommands(e.getRawSlot());
        if(commands != null && !commands.isEmpty())
            commands.forEach(command -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", player.getName())));
    }

    public void open(SuperiorPlayer superiorPlayer, SuperiorMenu previousMenu){
        if(Bukkit.isPrimaryThread()){
            new SuperiorThread(() -> open(superiorPlayer, previousMenu)).start();
            return;
        }

        Inventory inventory = getInventory();

        Bukkit.getScheduler().runTask(plugin, () -> {
            superiorPlayer.asPlayer().openInventory(inventory);

            this.previousMenu = previousMenu;
        });
    }

    public void closeInventory(SuperiorPlayer superiorPlayer){
        if(previousMenu != null) {
            Bukkit.getScheduler().runTask(plugin, () -> {
                if(previousMove)
                    previousMenu.open(superiorPlayer, previousMenu.previousMenu);
                else
                    previousMove = true;
            });
        }
    }

    protected static SoundWrapper getSound(ConfigurationSection section){
        Sound sound = null;

        try{
            sound = Sound.valueOf(section.getString("type"));
        }catch(Exception ignored){}

        if(sound == null)
            return null;

        return new SoundWrapper(sound, (float) section.getDouble("volume"), (float) section.getDouble("pitch"));
    }

}
