package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class MenuMembers extends PagedSuperiorMenu<SuperiorPlayer> {

    private Island island;

    private MenuMembers(SuperiorPlayer superiorPlayer, Island island){
        super("menuMembers", superiorPlayer);
        this.island = island;
    }

    @Override
    protected void onPlayerClick(InventoryClickEvent event, SuperiorPlayer targetPlayer) {
        previousMove = false;
        MenuMemberManage.openInventory(superiorPlayer, this, targetPlayer);
    }

    @Override
    protected ItemStack getObjectItem(ItemStack clickedItem, SuperiorPlayer superiorPlayer) {
        return new ItemBuilder(clickedItem)
                .replaceAll("{0}", superiorPlayer.getName())
                .replaceAll("{1}", superiorPlayer.getPlayerRole() + "")
                .asSkullOf(superiorPlayer).build(super.superiorPlayer);
    }

    @Override
    protected List<SuperiorPlayer> requestObjects() {
        return island.getIslandMembers(true);
    }

    public static void init(){
        MenuMembers menuMembers = new MenuMembers(null, null);

        File file = new File(plugin.getDataFolder(), "menus/members.yml");

        if(!file.exists())
            FileUtils.saveResource("menus/members.yml");

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        Map<Character, List<Integer>> charSlots = FileUtils.loadGUI(menuMembers, "members.yml", cfg);

        menuMembers.setPreviousSlot(charSlots.getOrDefault(cfg.getString("previous-page", "%").charAt(0), Collections.singletonList(-1)).get(0));
        menuMembers.setCurrentSlot(charSlots.getOrDefault(cfg.getString("current-page", "*").charAt(0), Collections.singletonList(-1)).get(0));
        menuMembers.setNextSlot(charSlots.getOrDefault(cfg.getString("next-page", "^").charAt(0), Collections.singletonList(-1)).get(0));
        menuMembers.setSlots(charSlots.getOrDefault(cfg.getString("slots", "@").charAt(0), Collections.singletonList(-1)));
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, SuperiorMenu previousMenu, Island island){
        new MenuMembers(superiorPlayer, island).open(previousMenu);
    }

    public static void refreshMenus(){
        SuperiorMenu.refreshMenus(MenuMembers.class);
    }

}
