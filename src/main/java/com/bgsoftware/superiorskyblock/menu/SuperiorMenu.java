package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import org.bukkit.Bukkit;
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

    public void addCommands(int slot, List<String> commands) {
        if(commands != null && !commands.isEmpty()) {
            Map<Integer, List<String>> commandMap = SuperiorMenu.commands.getOrDefault(identifier, new HashMap<>());
            commandMap.put(slot, commands);
            SuperiorMenu.commands.put(identifier, commandMap);
        }
    }

    public void resetData(){
        sounds.clear();
        commands.clear();
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
        if(commands != null)
            commands.forEach(command ->
                    Bukkit.dispatchCommand(command.startsWith("PLAYER:") ? player : Bukkit.getConsoleSender(),
                            command.replace("PLAYER:", "").replace("%player%", player.getName())));
    }

    public void open(SuperiorPlayer superiorPlayer, SuperiorMenu previousMenu){
        if(Bukkit.isPrimaryThread()){
            Executor.async(() -> open(superiorPlayer, previousMenu));
            return;
        }

        Inventory inventory = getInventory();

        Executor.sync(() -> {
            SuperiorMenu currentMenu = null;
            if(superiorPlayer.asPlayer().getOpenInventory().getTopInventory().getHolder() instanceof SuperiorMenu)
                currentMenu = (SuperiorMenu) superiorPlayer.asPlayer().getOpenInventory().getTopInventory().getHolder();

            superiorPlayer.asPlayer().openInventory(inventory);

            this.previousMenu = previousMenu == null && currentMenu != null ? currentMenu : previousMenu;
        });
    }

    public void closeInventory(SuperiorPlayer superiorPlayer){
        if(previousMenu != null) {
            Executor.sync(() -> {
                if(previousMove)
                    previousMenu.open(superiorPlayer, previousMenu.previousMenu);
                else
                    previousMove = true;
            });
        }
    }

}
