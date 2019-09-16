package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.island.IslandRegistry;
import com.bgsoftware.superiorskyblock.utils.FileUtil;
import com.bgsoftware.superiorskyblock.utils.ItemBuilder;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public final class IslandsTopMenu extends SuperiorMenu {


    private static IslandsTopMenu instance = null;
    private static Inventory inventory = null;
    private static String title;

    private static Integer[] slots;
    private static int playerIslandSlot;
    private static ItemStack noIslandItem, islandItem;

    private IslandsTopMenu(){
        super("islandTop");
        instance = this;
    }

    @Override
    public void onClick(InventoryClickEvent e) {
        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(e.getWhoClicked());

        if(e.getRawSlot() == playerIslandSlot){
            clickItem(superiorPlayer.getIsland(), superiorPlayer, e.getAction());
        }
        else {
            for (int i = 0; i < slots.length; i++) {
                if (slots[i] == e.getRawSlot()) {
                    Island island = plugin.getGrid().getIsland(i);
                    if(clickItem(island, superiorPlayer, e.getAction()))
                        break;
                }
            }
        }
    }

    private boolean clickItem(Island island, SuperiorPlayer superiorPlayer, InventoryAction inventoryAction){
        if(island != null) {
            superiorPlayer.asPlayer().closeInventory();
            SoundWrapper sound = getSound(-1);
            if(sound != null)
                sound.playSound(superiorPlayer.asPlayer());
            List<String> commands = getCommands(-1);
            if(commands != null)
                commands.forEach(command ->
                        Bukkit.dispatchCommand(command.startsWith("PLAYER:") ? superiorPlayer.asPlayer() : Bukkit.getConsoleSender(),
                                command.replace("PLAYER:", "").replace("%player%", superiorPlayer.getName())));
            if(inventoryAction == InventoryAction.PICKUP_HALF){
                IslandWarpsMenu.openInventory(superiorPlayer, this, island);
            } else {
                IslandValuesMenu.openInventory(superiorPlayer, this, island);
            }
            return true;
        }

        SoundWrapper sound = getSound(-2);
        if(sound != null)
            sound.playSound(superiorPlayer.asPlayer());
        List<String> commands = getCommands(-2);
        if(commands != null)
            commands.forEach(command ->
                    Bukkit.dispatchCommand(command.startsWith("PLAYER:") ? superiorPlayer.asPlayer() : Bukkit.getConsoleSender(),
                            command.replace("PLAYER:", "").replace("%player%", superiorPlayer.getName())));

        return false;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    @Override
    public void open(SuperiorPlayer superiorPlayer, SuperiorMenu previousMenu) {
        if(Bukkit.isPrimaryThread()){
            Executor.async(() -> open(superiorPlayer, previousMenu));
            return;
        }

        reloadGUI();

        Inventory inv = Bukkit.createInventory(this, inventory.getSize(), title);
        reloadInventory(inv, superiorPlayer);

        Executor.sync(() -> {
            superiorPlayer.asPlayer().openInventory(inv);
            this.previousMenu = previousMenu;
        });
    }

    private void reloadInventory(Inventory inv, SuperiorPlayer superiorPlayer){
        inv.setContents(inventory.getContents());

        if(playerIslandSlot != -1){
            IslandRegistry islands = plugin.getGrid().getIslandRegistry();
            Island island = superiorPlayer.getIsland();
            int i = island == null ? -1 : islands.indexOf(island) + 1;
            inv.setItem(playerIslandSlot, getTopItem(island, i));
        }
    }

    private void reloadGUI(){
        if(Bukkit.isPrimaryThread()){
            Executor.async(this::reloadGUI);
            return;
        }

        IslandRegistry islands = plugin.getGrid().getIslandRegistry();
        islands.sort();

        for(int i = 0; i < slots.length; i++){
            Island island = i >= islands.size() ? null : islands.get(i);
            ItemStack itemStack = getTopItem(island, i + 1);
            inventory.setItem(slots[i], itemStack);
        }

        if(playerIslandSlot != -1)
            inventory.setItem(playerIslandSlot, getTopItem(null, -1));

        Executor.async(() -> {
            for(Player player : Bukkit.getOnlinePlayers()){
                Inventory topInventory = player.getOpenInventory().getTopInventory();
                if(topInventory != null && topInventory.getHolder() instanceof IslandsTopMenu)
                    reloadInventory(topInventory, SSuperiorPlayer.of(player));
            }
        }, 2L);
    }

    private ItemStack getTopItem(Island island, int place){
        SuperiorPlayer islandOwner = island == null ? null : island.getOwner();

        ItemStack itemStack;

        if(islandOwner == null){
            itemStack = noIslandItem.clone();
        }

        else{
            itemStack = islandItem.clone();
        }

        ItemBuilder itemBuilder = new ItemBuilder(itemStack).asSkullOf(islandOwner);

        if(island != null && islandOwner != null) {
            String islandName = !plugin.getSettings().islandNamesIslandTop || island.getName().isEmpty() ?
                    islandOwner.getName() : plugin.getSettings().islandNamesColorSupport ?
                    ChatColor.translateAlternateColorCodes('&', island.getName()) : island.getName();

            itemBuilder.replaceName("{0}", islandName)
                    .replaceName("{1}", String.valueOf(place))
                    .replaceName("{2}", island.getIslandLevelAsBigDecimal().toString())
                    .replaceName("{3}", island.getWorthAsBigDecimal().toString());

            if(itemStack.getItemMeta().hasLore()){
                List<String> lore = new ArrayList<>();

                for(String line : itemStack.getItemMeta().getLore()){
                    if(line.contains("{4}")){
                        List<UUID> members = plugin.getSettings().islandTopIncludeLeader ? island.getAllMembers() : island.getMembers();
                        String memberFormat = line.split("\\{4}:")[1];
                        if(members.size() == 0){
                            lore.add(memberFormat.replace("{}", "None"));
                        }
                        else {
                            for (UUID memberUUID : members) {
                                lore.add(memberFormat.replace("{}", SSuperiorPlayer.of(memberUUID).getName()));
                            }
                        }
                    }else{
                        lore.add(line
                                .replace("{0}", island.getOwner().getName())
                                .replace("{1}", String.valueOf(place))
                                .replace("{2}", island.getIslandLevelAsBigDecimal().toString())
                                .replace("{3}", island.getWorthAsBigDecimal().toString()));
                    }
                }

                itemBuilder.withLore(lore);
            }
        }

        return itemBuilder.build();
    }

    public static void init(){
        IslandsTopMenu islandsTopMenu = new IslandsTopMenu();

        File file = new File(plugin.getDataFolder(), "guis/top-islands.yml");

        if(!file.exists())
            FileUtil.saveResource("guis/top-islands.yml");

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        inventory = FileUtil.loadGUI(islandsTopMenu, cfg.getConfigurationSection("top-islands"), 6, "&lTop Islands");
        title = ChatColor.translateAlternateColorCodes('&', cfg.getString("top-islands.title"));

        ItemStack islandItem = FileUtil.getItemStack(cfg.getConfigurationSection("top-islands.island-item"));
        ItemStack noIslandItem = FileUtil.getItemStack(cfg.getConfigurationSection("top-islands.no-island-item"));

        islandsTopMenu.addSound(-1, FileUtil.getSound(cfg.getConfigurationSection("top-islands.island-item.sound")));
        islandsTopMenu.addSound(-2, FileUtil.getSound(cfg.getConfigurationSection("top-islands.no-island-item.sound")));
        islandsTopMenu.addCommands(-1, cfg.getStringList("top-islands.island-item.commands"));
        islandsTopMenu.addCommands(-2, cfg.getStringList("top-islands.no-island-item.commands"));

        List<Integer> slots = new ArrayList<>();
        Arrays.stream(cfg.getString("top-islands.slots").split(","))
                .forEach(slot -> slots.add(Integer.valueOf(slot)));

        IslandsTopMenu.islandItem = islandItem;
        IslandsTopMenu.noIslandItem = noIslandItem;
        IslandsTopMenu.slots = slots.toArray(new Integer[0]);
        IslandsTopMenu.playerIslandSlot = cfg.getInt("top-islands.player-island-slot", -1);

        islandsTopMenu.reloadGUI();
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, SuperiorMenu previousMenu){
        instance.reloadGUI();
        instance.open(superiorPlayer, previousMenu);
    }

}
