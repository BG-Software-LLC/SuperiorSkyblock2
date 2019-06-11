package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.FileUtil;
import com.bgsoftware.superiorskyblock.utils.ItemBuilder;
import com.bgsoftware.superiorskyblock.utils.threads.SuperiorThread;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public final class IslandVisitorsMenu extends SuperiorMenu {

    private static Inventory inventory = null;
    private static ItemStack previousButton, currentButton, nextButton, visitorItem;
    private static int previousSlot, currentSlot, nextSlot;
    private static Sound previousSound, currentSound, nextSound, visitorSound;
    private static List<Integer> slots = new ArrayList<>();

    private Island island;
    private List<UUID> visitors;
    private int page;

    private IslandVisitorsMenu(Island island){
        super("visitorsPage");
        if(island != null) {
            this.island = island;
            this.visitors = island.getVisitors();
            visitors.sort(Comparator.comparing(o -> SSuperiorPlayer.of(o).getName()));
        }
    }

    @Override
    public void onClick(InventoryClickEvent e) {
        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(e.getWhoClicked());
        int clickedSlot = e.getRawSlot();

        if(clickedSlot == previousSlot || clickedSlot == nextSlot || clickedSlot == currentSlot){
            int nextPage;

            if(clickedSlot == previousSlot){
                nextPage = page == 1 ? -1 : page - 1;
            }
            else if(clickedSlot == nextSlot){
                nextPage = visitors.size() > page * slots.size() ? page + 1 : -1;
            }
            else return;

            if(nextPage == -1)
                return;

            openInventory(superiorPlayer, nextPage, previousMenus.get(superiorPlayer.getUniqueId()));
        }

        else{
            if(e.getCurrentItem() == null)
                return;

            int indexOf = slots.indexOf(e.getRawSlot());

            if(indexOf < 0)
                return;

            SuperiorPlayer targetPlayer = SSuperiorPlayer.of(visitors.get(indexOf));

            if (targetPlayer != null) {
                if (e.getClick().name().contains("RIGHT")) {
                    Bukkit.dispatchCommand(superiorPlayer.asPlayer(), "island invite " + targetPlayer.getName());
                } else if (e.getClick().name().contains("LEFT")) {
                    Bukkit.dispatchCommand(superiorPlayer.asPlayer(), "island expel " + targetPlayer.getName());
                }
            }
        }
    }

    @Override
    public Inventory getInventory() {
        return null;
    }

    @Override
    public void openInventory(SuperiorPlayer superiorPlayer, SuperiorMenu previousMenu) {
        openInventory(superiorPlayer, 1, previousMenu);
    }

    private void openInventory(SuperiorPlayer superiorPlayer, int page, SuperiorMenu previousMenu){
        if(Bukkit.isPrimaryThread()){
            new SuperiorThread(() -> openInventory(superiorPlayer, page, previousMenu)).start();
            return;
        }

        this.page = page;

        Inventory inv = Bukkit.createInventory(this, inventory.getSize(), inventory.getTitle());
        inv.setContents(inventory.getContents());

        for(int i = 0; i < slots.size() && (i + (slots.size() * (page - 1))) < visitors.size(); i++){
            SuperiorPlayer _superiorPlayer = SSuperiorPlayer.of(visitors.get(i + (slots.size() * (page - 1))));
            String islandOwner = "None";
            if(_superiorPlayer.getIsland() != null)
                islandOwner = _superiorPlayer.getIsland().getOwner().getName();
            inv.setItem(slots.get(i), new ItemBuilder(visitorItem)
                    .replaceAll("{0}", _superiorPlayer.getName())
                    .replaceAll("{1}", islandOwner)
                    .asSkullOf(_superiorPlayer).build());
        }

        inv.setItem(previousSlot, new ItemBuilder(previousButton)
                .replaceName("{0}", (page == 1 ? "&c" : "&a")).build());

        inv.setItem(currentSlot, new ItemBuilder(currentButton)
                .replaceLore("{0}", page + "").build());

        inv.setItem(nextSlot, new ItemBuilder(nextButton)
                .replaceName("{0}", (visitors.size() > page * slots.size() ? "&a" : "&c")).build());

        Bukkit.getScheduler().runTask(plugin, () -> {
            superiorPlayer.asPlayer().openInventory(inv);

            if(openSound != null)
                superiorPlayer.asPlayer().playSound(superiorPlayer.getLocation(), openSound, 1, 1);

            if(previousMenu != null)
                previousMenus.put(superiorPlayer.getUniqueId(), previousMenu);
        });
    }

    public static void init(){
        IslandVisitorsMenu islandVisitorsMenu = new IslandVisitorsMenu(null);

        File file = new File(plugin.getDataFolder(), "guis/panel-gui.yml");

        if(!file.exists())
            FileUtil.saveResource("guis/panel-gui.yml");

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        inventory = FileUtil.loadGUI(islandVisitorsMenu, cfg.getConfigurationSection("visitors-panel"), 6, "&lIsland Visitors");

        previousButton = FileUtil.getItemStack(cfg.getConfigurationSection("visitors-panel.previous-page"));
        currentButton = FileUtil.getItemStack(cfg.getConfigurationSection("visitors-panel.current-page"));
        nextButton = FileUtil.getItemStack(cfg.getConfigurationSection("visitors-panel.next-page"));
        visitorItem = FileUtil.getItemStack(cfg.getConfigurationSection("visitors-panel.visitor-item"));
        previousSlot = cfg.getInt("visitors-panel.previous-page.slot");
        currentSlot = cfg.getInt("visitors-panel.current-page.slot");
        nextSlot = cfg.getInt("visitors-panel.next-page.slot");
        previousSound = getSound(cfg.getString("visitors-panel.previous-page.sound", ""));
        currentSound = getSound(cfg.getString("visitors-panel.current-page.sound", ""));
        nextSound = getSound(cfg.getString("visitors-panel.next-page.sound", ""));
        visitorSound = getSound(cfg.getString("visitors-panel.visitor-item.sound", ""));

        Arrays.stream(cfg.getString("visitors-panel.visitor-item.slots").split(","))
                .forEach(slot -> slots.add(Integer.valueOf(slot)));
        slots.sort(Integer::compareTo);
    }

    public static IslandVisitorsMenu createInventory(Island island){
        return new IslandVisitorsMenu(island);
    }

}
