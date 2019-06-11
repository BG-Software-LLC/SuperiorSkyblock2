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

public final class IslandMembersMenu extends SuperiorMenu {

    private static Inventory inventory = null;
    private static ItemStack previousButton, currentButton, nextButton, memberItem;
    private static int previousSlot, currentSlot, nextSlot;
    private static Sound previousSound, currentSound, nextSound, memberSound;
    private static List<Integer> slots = new ArrayList<>();

    private List<UUID> members;
    private int page;

    private IslandMembersMenu(Island island){
        super("membersPage");
        if(island != null) {
            this.members = island.getAllMembers();
            members.sort(Comparator.comparing(o -> SSuperiorPlayer.of(o).getName()));
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
                nextPage = members.size() > page * slots.size() ? page + 1 : -1;
            }
            else return;

            if(nextPage == -1)
                return;

            open(superiorPlayer, nextPage, previousMenu);
        }

        else{
            if(e.getCurrentItem() == null)
                return;

            int indexOf = slots.indexOf(e.getRawSlot());

            if(indexOf < 0 || indexOf >= members.size())
                return;

            SuperiorPlayer targetPlayer = SSuperiorPlayer.of(members.get(indexOf));

            if (targetPlayer != null) {
                previousMove = false;
                MemberManageMenu.createInventory(targetPlayer).open(superiorPlayer, this);
            }
        }
    }

    @Override
    public Inventory getInventory() {
        return null;
    }

    @Override
    public void open(SuperiorPlayer superiorPlayer, SuperiorMenu previousMenu) {
        open(superiorPlayer, 1, previousMenu);
    }

    private void open(SuperiorPlayer superiorPlayer, int page, SuperiorMenu previousMenu){
        if(Bukkit.isPrimaryThread()){
            new SuperiorThread(() -> open(superiorPlayer, page, previousMenu)).start();
            return;
        }

        this.page = page;

        Inventory inv = Bukkit.createInventory(this, inventory.getSize(), inventory.getTitle());
        inv.setContents(inventory.getContents());

        for(int i = 0; i < slots.size() && (i + (slots.size() * (page - 1))) < members.size(); i++){
            SuperiorPlayer _superiorPlayer = SSuperiorPlayer.of(members.get(i + (slots.size() * (page - 1))));
            inv.setItem(slots.get(i), new ItemBuilder(memberItem)
                    .replaceAll("{0}", _superiorPlayer.getName())
                    .asSkullOf(_superiorPlayer).build());
        }

        inv.setItem(previousSlot, new ItemBuilder(previousButton)
                .replaceName("{0}", (page == 1 ? "&c" : "&a")).build());

        inv.setItem(currentSlot, new ItemBuilder(currentButton)
                .replaceLore("{0}", page + "").build());

        inv.setItem(nextSlot, new ItemBuilder(nextButton)
                .replaceName("{0}", (members.size() > page * slots.size() ? "&a" : "&c")).build());


        Bukkit.getScheduler().runTask(plugin, () -> {
            superiorPlayer.asPlayer().openInventory(inv);

            if(openSound != null)
                superiorPlayer.asPlayer().playSound(superiorPlayer.getLocation(), openSound, 1, 1);

            this.previousMenu = previousMenu;
        });
    }

    public static void init(){
        IslandMembersMenu islandMembersMenu = new IslandMembersMenu(null);

        File file = new File(plugin.getDataFolder(), "guis/panel-gui.yml");

        if(!file.exists())
            FileUtil.saveResource("guis/panel-gui.yml");

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        inventory = FileUtil.loadGUI(islandMembersMenu, cfg.getConfigurationSection("members-panel"), 6, "&lIsland Members");

        previousButton = FileUtil.getItemStack(cfg.getConfigurationSection("members-panel.previous-page"));
        currentButton = FileUtil.getItemStack(cfg.getConfigurationSection("members-panel.current-page"));
        nextButton = FileUtil.getItemStack(cfg.getConfigurationSection("members-panel.next-page"));
        memberItem = FileUtil.getItemStack(cfg.getConfigurationSection("members-panel.member-item"));

        previousSound = getSound(cfg.getString("members-panel.previous-page.sound", ""));
        currentSound = getSound(cfg.getString("members-panel.current-page.sound", ""));
        nextSound = getSound(cfg.getString("members-panel.next-page.sound", ""));
        memberSound = getSound(cfg.getString("members-panel.member-item.sound", ""));

        previousSlot = cfg.getInt("members-panel.previous-page.slot");
        currentSlot = cfg.getInt("members-panel.current-page.slot");
        nextSlot = cfg.getInt("members-panel.next-page.slot");

        inventory.setItem(previousSlot, previousButton);
        inventory.setItem(currentSlot, currentButton);
        inventory.setItem(nextSlot, nextButton);

        Arrays.stream(cfg.getString("members-panel.member-item.slots").split(","))
                .forEach(slot -> slots.add(Integer.valueOf(slot)));
        slots.sort(Integer::compareTo);
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, SuperiorMenu previousMenu, Island island){
        new IslandMembersMenu(island).open(superiorPlayer, previousMenu);
    }

}
