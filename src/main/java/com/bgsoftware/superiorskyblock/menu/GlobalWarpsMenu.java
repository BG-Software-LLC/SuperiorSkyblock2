package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.config.GlobalSectionComments;
import com.bgsoftware.superiorskyblock.utils.FileUtil;
import com.bgsoftware.superiorskyblock.utils.ItemBuilder;
import com.bgsoftware.superiorskyblock.utils.threads.SuperiorThread;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class GlobalWarpsMenu extends SuperiorMenu {

    private static Inventory inventory = null;
    private static String title = "";

    private static ItemStack previousButton, currentButton, nextButton, warpItem;
    private static int previousSlot, currentSlot, nextSlot;
    private static List<Integer> slots;

    private GlobalWarpsMenu(){
        super("globalWarpsPage");
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

            List<String> islands = plugin.getGrid().getListIslands().stream()
                    .filter(island -> !island.getAllWarps().isEmpty())
                    .map(island -> island.getOwner().getName())
                    .sorted(String::compareTo)
                    .collect(Collectors.toList());

            int indexOf = slots.indexOf(e.getRawSlot());

            if(indexOf >= islands.size() || indexOf == -1)
                return;

            String ownerName = islands.get(indexOf);
            Island island = Objects.requireNonNull(SSuperiorPlayer.of(ownerName)).getIsland();

            SoundWrapper sound = getSound(-1);
            if(sound != null)
                sound.playSound(e.getWhoClicked());
            List<String> commands = getCommands(-1);
            if(commands != null)
                commands.forEach(command ->
                        Bukkit.dispatchCommand(command.startsWith("PLAYER:") ? superiorPlayer.asPlayer() : Bukkit.getConsoleSender(),
                                command.replace("PLAYER:", "").replace("%player%", superiorPlayer.getName())));

            if(island == null)
                GlobalWarpsMenu.openInventory(superiorPlayer, null);
            else
                IslandWarpsMenu.openInventory(superiorPlayer, this, island);
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
            new SuperiorThread(() -> open(superiorPlayer, page, previousMenu)).start();
            return;
        }

        Inventory inv = Bukkit.createInventory(this, inventory.getSize(), title);
        inv.setContents(inventory.getContents());

        List<String> islands = plugin.getGrid().getListIslands().stream()
                .filter(island -> !island.getAllWarps().isEmpty())
                .map(island -> island.getOwner().getName())
                .sorted(String::compareTo)
                .collect(Collectors.toList());

        for(int i = 0; i < slots.size() && (i + (slots.size() * (page - 1))) < islands.size(); i++){
            String ownerName = islands.get(i + (slots.size() * (page - 1)));
            inv.setItem(slots.get(i), new ItemBuilder(warpItem).asSkullOf(SSuperiorPlayer.of(ownerName))
                    .replaceAll("{0}", ownerName).build());
        }

        inv.setItem(previousSlot, new ItemBuilder(previousButton)
                .replaceName("{0}", (page == 1 ? "&c" : "&a")).build());

        inv.setItem(currentSlot, new ItemBuilder(currentButton)
                .replaceLore("{0}", page + "").build());

        inv.setItem(nextSlot, new ItemBuilder(nextButton)
                .replaceName("{0}", (islands.size() > page * slots.size() ? "&a" : "&c")).build());

        this.previousMenu = previousMenu;

        Bukkit.getScheduler().runTask(plugin, () -> superiorPlayer.asPlayer().openInventory(inv));
    }

    public static void init(){
        GlobalWarpsMenu globalWarpsMenu = new GlobalWarpsMenu();
        File file = new File(plugin.getDataFolder(), "guis/warps-gui.yml");

        if(!file.exists())
            FileUtil.saveResource("guis/warps-gui.yml");

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        if(!cfg.contains("global-gui")){
            createGlobalSection(file);
            cfg = YamlConfiguration.loadConfiguration(file);
        }

        inventory = FileUtil.loadGUI(globalWarpsMenu, cfg.getConfigurationSection("global-gui"), 6, "&lIslands Warps");
        title = ChatColor.translateAlternateColorCodes('&', cfg.getString("global-gui.title"));

        ItemStack previousButton = FileUtil.getItemStack(cfg.getConfigurationSection("global-gui.previous-page"));
        ItemStack currentButton = FileUtil.getItemStack(cfg.getConfigurationSection("global-gui.current-page"));
        ItemStack nextButton = FileUtil.getItemStack(cfg.getConfigurationSection("global-gui.next-page"));
        ItemStack warpItem = FileUtil.getItemStack(cfg.getConfigurationSection("global-gui.warp-item"));
        int previousSlot = cfg.getInt("global-gui.previous-page.slot");
        int currentSlot = cfg.getInt("global-gui.current-page.slot");
        int nextSlot = cfg.getInt("global-gui.next-page.slot");

        globalWarpsMenu.addSound(previousSlot, getSound(cfg.getConfigurationSection("global-gui.previous-page.sound")));
        globalWarpsMenu.addSound(currentSlot, getSound(cfg.getConfigurationSection("global-gui.current-page.sound")));
        globalWarpsMenu.addSound(nextSlot, getSound(cfg.getConfigurationSection("global-gui.next-page.sound")));
        globalWarpsMenu.addSound(-1, getSound(cfg.getConfigurationSection("global-gui.warp-item.sound")));
        globalWarpsMenu.addCommands(previousSlot, cfg.getStringList("global-gui.previous-page.commands"));
        globalWarpsMenu.addCommands(currentSlot, cfg.getStringList("global-gui.current-page.commands"));
        globalWarpsMenu.addCommands(nextSlot, cfg.getStringList("global-gui.next-page.commands"));
        globalWarpsMenu.addCommands(-1, cfg.getStringList("global-gui.warp-item.commands"));

        List<Integer> slots = new ArrayList<>();
        Arrays.stream(cfg.getString("global-gui.warp-item.slots").split(","))
                .forEach(slot -> slots.add(Integer.valueOf(slot)));
        slots.sort(Integer::compareTo);

        GlobalWarpsMenu.previousButton = previousButton;
        GlobalWarpsMenu.currentButton = currentButton;
        GlobalWarpsMenu.nextButton = nextButton;
        GlobalWarpsMenu.warpItem = warpItem;
        GlobalWarpsMenu.previousSlot = previousSlot;
        GlobalWarpsMenu.currentSlot = currentSlot;
        GlobalWarpsMenu.nextSlot = nextSlot;
        GlobalWarpsMenu.slots = slots;
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, SuperiorMenu previousMenu){
        new GlobalWarpsMenu().open(superiorPlayer, previousMenu);
    }

    private static void createGlobalSection(File file){
        CommentedConfiguration cfg = new CommentedConfiguration(GlobalSectionComments.class, file);
        cfg.resetYamlFile(plugin, "guis/warps-gui.yml");
    }

}
