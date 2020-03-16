package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.commands.CommandUtils;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.utils.registry.Registry;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.Collections;
import java.util.List;

public final class MenuUniqueVisitors extends PagedSuperiorMenu<SuperiorPlayer> {

    private Island island;

    private MenuUniqueVisitors(SuperiorPlayer superiorPlayer, Island island){
        super("menuUniqueVisitors", superiorPlayer);
        this.island = island;
    }

    @Override
    public Inventory getInventory() {
        return buildInventory(title -> title.replace("{0}", island.getUniqueVisitors().size() + ""));
    }

    @Override
    protected void onPlayerClick(InventoryClickEvent event, SuperiorPlayer targetPlayer) {
        if (event.getClick().name().contains("RIGHT")) {
            CommandUtils.dispatchSubCommand(superiorPlayer.asPlayer(), "invite " + targetPlayer.getName());
        } else if (event.getClick().name().contains("LEFT")) {
            CommandUtils.dispatchSubCommand(superiorPlayer.asPlayer(), "expel " + targetPlayer.getName());
        }
    }

    @Override
    protected ItemStack getObjectItem(ItemStack clickedItem, SuperiorPlayer superiorPlayer) {
        Island island = superiorPlayer.getIsland();
        String islandOwner = island != null ? island.getOwner().getName() : "None";
        String islandName = island != null ? island.getName().isEmpty() ? islandOwner : island.getName() : "None";
        return new ItemBuilder(clickedItem)
                .replaceAll("{0}", superiorPlayer.getName())
                .replaceAll("{1}", islandOwner)
                .replaceAll("{2}", islandName)
                .asSkullOf(superiorPlayer).build(super.superiorPlayer);
    }

    @Override
    protected List<SuperiorPlayer> requestObjects() {
        return island.getUniqueVisitors();
    }

    public static void init(){
        MenuUniqueVisitors menuUniqueVisitors = new MenuUniqueVisitors(null, null);

        File file = new File(plugin.getDataFolder(), "menus/unique-visitors.yml");

        if(!file.exists())
            FileUtils.saveResource("menus/unique-visitors.yml");

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        Registry<Character, List<Integer>> charSlots = FileUtils.loadGUI(menuUniqueVisitors, "unique-visitors.yml", cfg);

        menuUniqueVisitors.setPreviousSlot(charSlots.get(cfg.getString("previous-page", " ").charAt(0), Collections.singletonList(-1)).get(0));
        menuUniqueVisitors.setCurrentSlot(charSlots.get(cfg.getString("current-page", " ").charAt(0), Collections.singletonList(-1)).get(0));
        menuUniqueVisitors.setNextSlot(charSlots.get(cfg.getString("next-page", " ").charAt(0), Collections.singletonList(-1)).get(0));
        menuUniqueVisitors.setSlots(charSlots.get(cfg.getString("slots", " ").charAt(0), Collections.singletonList(-1)));

        charSlots.delete();
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, SuperiorMenu previousMenu, Island island){
        new MenuUniqueVisitors(superiorPlayer, island).open(previousMenu);
    }

    public static void refreshMenus(){
        refreshMenus(MenuUniqueVisitors.class);
    }

}
