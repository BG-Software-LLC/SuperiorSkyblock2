package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.FileUtil;
import com.bgsoftware.superiorskyblock.utils.ItemBuilder;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import com.bgsoftware.superiorskyblock.wrappers.SBlockPosition;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class IslandWarpsMenu extends SuperiorMenu {

    private static Inventory inventory = null;
    private static String title = "";

    private static ItemStack previousButton, currentButton, nextButton, warpItem;
    private static int previousSlot, currentSlot, nextSlot;
    private static List<Integer> slots;

    private Island island;

    private IslandWarpsMenu(Island island){
        super("warpsPage");
        this.island = island;
    }

    @Override
    public void onClick(InventoryClickEvent e) {
        super.onClick(e);
        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(e.getWhoClicked());

        if(e.getRawSlot() == previousSlot || e.getRawSlot() == nextSlot || e.getRawSlot() == currentSlot){
            if(e.getCurrentItem().getItemMeta().getDisplayName().startsWith(ChatColor.RED + ""))
                return;

            if(e.getRawSlot() == currentSlot)
                return;

            int currentPage = Integer.valueOf(ChatColor.stripColor(e.getInventory().getItem(currentSlot)
                    .getItemMeta().getLore().get(0)).split(" ")[1]);

            open(superiorPlayer, e.getRawSlot() == nextSlot ? currentPage + 1 : currentPage - 1, null);
        }

        else{
            if(e.getCurrentItem() == null)
                return;

            List<String> warps = new ArrayList<>(island.getAllWarps());
            warps.sort(String::compareTo);

            int indexOf = slots.indexOf(e.getRawSlot());

            if(indexOf >= warps.size() || indexOf == -1)
                return;

            String warpName = warps.get(indexOf);
            Location location = island.getWarpLocation(warpName);

            if(location != null) {
                SoundWrapper sound = getSound(-1);
                if(sound != null)
                    sound.playSound(e.getWhoClicked());
                List<String> commands = getCommands(-1);
                if(commands != null)
                    commands.forEach(command ->
                            Bukkit.dispatchCommand(command.startsWith("PLAYER:") ? superiorPlayer.asPlayer() : Bukkit.getConsoleSender(),
                                    command.replace("PLAYER:", "").replace("%player%", superiorPlayer.getName())));
                this.previousMenu = null;
                island.warpPlayer(superiorPlayer, warpName);
            }
        }
    }

    @Override
    public void open(SuperiorPlayer superiorPlayer, SuperiorMenu previousMenu) {
        open(superiorPlayer, 1, previousMenu);
    }

    @Override
    public Inventory getInventory() {
        return null;
    }

    private void open(SuperiorPlayer superiorPlayer, int page, SuperiorMenu previousMenu) {
        if (Bukkit.isPrimaryThread()) {
            Executor.async(() -> open(superiorPlayer, page, previousMenu));
            return;
        }

        Inventory inv = Bukkit.createInventory(this, inventory.getSize(), title);
        inv.setContents(inventory.getContents());

        List<String> warps = new ArrayList<>(island.getAllWarps());

        warps.sort(String::compareTo);

        for(int i = 0; i < slots.size() && (i + (slots.size() * (page - 1))) < warps.size(); i++){
            String warpName = warps.get(i + (slots.size() * (page - 1)));
            inv.setItem(slots.get(i), new ItemBuilder(warpItem)
                    .replaceAll("{0}", warpName)
                    .replaceAll("{1}", SBlockPosition.of(island.getWarpLocation(warpName)).toString()).build());
        }

        inv.setItem(previousSlot, new ItemBuilder(previousButton)
                .replaceName("{0}", (page == 1 ? "&c" : "&a")).build());

        inv.setItem(currentSlot, new ItemBuilder(currentButton)
                .replaceLore("{0}", page + "").build());

        inv.setItem(nextSlot, new ItemBuilder(nextButton)
                .replaceName("{0}", (warps.size() > page * slots.size() ? "&a" : "&c")).build());

        this.previousMenu = previousMenu;

        Executor.sync(() -> superiorPlayer.asPlayer().openInventory(inv));
    }

    public static void init(){
        IslandWarpsMenu islandValuesMenu = new IslandWarpsMenu(null);
        File file = new File(plugin.getDataFolder(), "guis/warps-gui.yml");

        if(!file.exists())
            FileUtil.saveResource("guis/warps-gui.yml");

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        inventory = FileUtil.loadGUI(islandValuesMenu, cfg.getConfigurationSection("warps-gui"), 6, "&lIsland Warps");
        title = ChatColor.translateAlternateColorCodes('&', cfg.getString("warps-gui.title"));

        ItemStack previousButton = FileUtil.getItemStack(cfg.getConfigurationSection("warps-gui.previous-page"));
        ItemStack currentButton = FileUtil.getItemStack(cfg.getConfigurationSection("warps-gui.current-page"));
        ItemStack nextButton = FileUtil.getItemStack(cfg.getConfigurationSection("warps-gui.next-page"));
        ItemStack warpItem = FileUtil.getItemStack(cfg.getConfigurationSection("warps-gui.warp-item"));
        int previousSlot = cfg.getInt("warps-gui.previous-page.slot");
        int currentSlot = cfg.getInt("warps-gui.current-page.slot");
        int nextSlot = cfg.getInt("warps-gui.next-page.slot");

        islandValuesMenu.addSound(previousSlot, getSound(cfg.getConfigurationSection("warps-gui.previous-page.sound")));
        islandValuesMenu.addSound(currentSlot, getSound(cfg.getConfigurationSection("warps-gui.current-page.sound")));
        islandValuesMenu.addSound(nextSlot, getSound(cfg.getConfigurationSection("warps-gui.next-page.sound")));
        islandValuesMenu.addSound(-1, getSound(cfg.getConfigurationSection("warps-gui.warp-item.sound")));
        islandValuesMenu.addCommands(previousSlot, cfg.getStringList("warps-gui.previous-page.commands"));
        islandValuesMenu.addCommands(currentSlot, cfg.getStringList("warps-gui.current-page.commands"));
        islandValuesMenu.addCommands(nextSlot, cfg.getStringList("warps-gui.next-page.commands"));
        islandValuesMenu.addCommands(-1, cfg.getStringList("warps-gui.warp-item.commands"));

        List<Integer> slots = new ArrayList<>();
        Arrays.stream(cfg.getString("warps-gui.warp-item.slots").split(","))
                .forEach(slot -> slots.add(Integer.valueOf(slot)));
        slots.sort(Integer::compareTo);

        IslandWarpsMenu.previousButton = previousButton;
        IslandWarpsMenu.currentButton = currentButton;
        IslandWarpsMenu.nextButton = nextButton;
        IslandWarpsMenu.warpItem = warpItem;
        IslandWarpsMenu.previousSlot = previousSlot;
        IslandWarpsMenu.currentSlot = currentSlot;
        IslandWarpsMenu.nextSlot = nextSlot;
        IslandWarpsMenu.slots = slots;
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, SuperiorMenu previousMenu, Island island){
        new IslandWarpsMenu(island).open(superiorPlayer, previousMenu);
    }

}
