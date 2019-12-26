package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class MenuMembers extends SuperiorMenu {

    private static int previousSlot, currentSlot, nextSlot;
    private static List<Integer> slots = new ArrayList<>();

    private List<SuperiorPlayer> members;

    private Island island;
    private int currentPage;

    private MenuMembers(SuperiorPlayer superiorPlayer, Island island, int currentPage){
        super("menuMembers", superiorPlayer);
        this.island = island;
        this.currentPage = currentPage;
    }

    @Override
    public void onPlayerClick(InventoryClickEvent e) {
        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(e.getWhoClicked());
        int clickedSlot = e.getRawSlot();

        if(clickedSlot == previousSlot || clickedSlot == nextSlot || clickedSlot == currentSlot){
            if(clickedSlot == currentSlot)
                return;

            boolean nextPage = slots.size() * currentPage < members.size();

            if((!nextPage && e.getRawSlot() == nextSlot) || (currentPage == 1 && e.getRawSlot() == previousSlot))
                return;

            currentPage = e.getRawSlot() == nextSlot ? currentPage + 1 : currentPage - 1;

            previousMove = false;
            open(previousMenu);
        }

        else{
            if(e.getCurrentItem() == null)
                return;

            int indexOf = slots.indexOf(e.getRawSlot());

            if(indexOf < 0 || indexOf >= members.size())
                return;

            SuperiorPlayer targetPlayer = members.get(indexOf);

            if (targetPlayer != null) {
                previousMove = false;
                MenuMemberManage.openInventory(superiorPlayer, this, targetPlayer);
            }
        }
    }

    @Override
    public Inventory getInventory() {
        Inventory inventory = super.getInventory();

        //Update members list.
        members = island.getIslandMembers(true);

        for(int i = 0; i < slots.size(); i++){
            int memberIndex = i + (slots.size() * (currentPage - 1));

            if(memberIndex < members.size()) {
                SuperiorPlayer _superiorPlayer = members.get(memberIndex);
                inventory.setItem(slots.get(i), new ItemBuilder(inventory.getItem(slots.get(i)))
                        .replaceAll("{0}", _superiorPlayer.getName())
                        .replaceAll("{1}", _superiorPlayer.getPlayerRole() + "")
                        .asSkullOf(_superiorPlayer).build(superiorPlayer));
            }
            else{
                inventory.setItem(slots.get(i), new ItemStack(Material.AIR));
            }
        }

        inventory.setItem(previousSlot, new ItemBuilder(inventory.getItem(previousSlot))
                .replaceAll("{0}", (currentPage == 1 ? "&c" : "&a")).build(superiorPlayer));

        inventory.setItem(currentSlot, new ItemBuilder(inventory.getItem(currentSlot))
                .replaceAll("{0}", currentPage + "").build(superiorPlayer));

        inventory.setItem(nextSlot, new ItemBuilder(inventory.getItem(nextSlot))
                .replaceAll("{0}", (members.size() > currentPage * slots.size() ? "&a" : "&c")).build(superiorPlayer));

        return inventory;
    }

    public static void init(){
        MenuMembers menuMembers = new MenuMembers(null, null, 1);

        File file = new File(plugin.getDataFolder(), "menus/members.yml");

        if(!file.exists())
            FileUtils.saveResource("menus/members.yml");

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        Map<Character, List<Integer>> charSlots = FileUtils.loadGUI(menuMembers, "members.yml", cfg);

        previousSlot = charSlots.getOrDefault(cfg.getString("previous-page", "%").charAt(0), Collections.singletonList(-1)).get(0);
        currentSlot = charSlots.getOrDefault(cfg.getString("current-page", "*").charAt(0), Collections.singletonList(-1)).get(0);
        nextSlot = charSlots.getOrDefault(cfg.getString("next-page", "^").charAt(0), Collections.singletonList(-1)).get(0);

        slots = charSlots.getOrDefault(cfg.getString("slots", "@").charAt(0), Collections.singletonList(-1));
        slots.sort(Integer::compareTo);
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, SuperiorMenu previousMenu, Island island){
        new MenuMembers(superiorPlayer, island, 1).open(previousMenu);
    }

    public static void refreshMenus(){
        SuperiorMenu.refreshMenus(MenuMembers.class);
    }

}
