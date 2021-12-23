package com.bgsoftware.superiorskyblock.menu.impl;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.menu.ISuperiorMenu;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.menu.PagedSuperiorMenu;
import com.bgsoftware.superiorskyblock.menu.file.MenuPatternSlots;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.debug.PluginDebugger;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.List;

public final class MenuUniqueVisitors extends PagedSuperiorMenu<Pair<SuperiorPlayer, Long>> {

    private final Island island;

    private MenuUniqueVisitors(SuperiorPlayer superiorPlayer, Island island) {
        super("menuUniqueVisitors", superiorPlayer);
        this.island = island;
    }

    public static void init() {
        MenuUniqueVisitors menuUniqueVisitors = new MenuUniqueVisitors(null, null);

        File file = new File(plugin.getDataFolder(), "menus/unique-visitors.yml");

        if (!file.exists())
            FileUtils.saveResource("menus/unique-visitors.yml");

        CommentedConfiguration cfg = CommentedConfiguration.loadConfiguration(file);

        MenuPatternSlots menuPatternSlots = FileUtils.loadGUI(menuUniqueVisitors, "unique-visitors.yml", cfg);

        menuUniqueVisitors.setPreviousSlot(getSlots(cfg, "previous-page", menuPatternSlots));
        menuUniqueVisitors.setCurrentSlot(getSlots(cfg, "current-page", menuPatternSlots));
        menuUniqueVisitors.setNextSlot(getSlots(cfg, "next-page", menuPatternSlots));
        menuUniqueVisitors.setSlots(getSlots(cfg, "slots", menuPatternSlots));

        menuUniqueVisitors.markCompleted();
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, ISuperiorMenu previousMenu, Island island) {
        new MenuUniqueVisitors(superiorPlayer, island).open(previousMenu);
    }

    public static void refreshMenus(Island island) {
        refreshMenus(MenuUniqueVisitors.class, superiorMenu -> superiorMenu.island.equals(island));
    }

    @Override
    public Inventory getInventory() {
        return buildInventory(title -> title.replace("{0}", island.getUniqueVisitorsWithTimes().size() + ""));
    }

    @Override
    public void cloneAndOpen(ISuperiorMenu previousMenu) {
        openInventory(superiorPlayer, previousMenu, island);
    }

    @Override
    protected void onPlayerClick(InventoryClickEvent event, Pair<SuperiorPlayer, Long> pair) {
        if (event.getClick().name().contains("RIGHT")) {
            plugin.getCommands().dispatchSubCommand(superiorPlayer.asPlayer(), "invite", pair.getKey().getName());
        } else if (event.getClick().name().contains("LEFT")) {
            plugin.getCommands().dispatchSubCommand(superiorPlayer.asPlayer(), "expel", pair.getKey().getName());
        }
    }

    @Override
    protected ItemStack getObjectItem(ItemStack clickedItem, Pair<SuperiorPlayer, Long> pair) {
        try {
            Island island = pair.getKey().getIsland();
            String islandOwner = island != null ? island.getOwner().getName() : "None";
            String islandName = island != null ? island.getName().isEmpty() ? islandOwner : island.getName() : "None";
            return new ItemBuilder(clickedItem)
                    .replaceAll("{0}", pair.getKey().getName())
                    .replaceAll("{1}", islandOwner)
                    .replaceAll("{2}", islandName)
                    .replaceAll("{3}", StringUtils.formatDate(pair.getValue()))
                    .asSkullOf(pair.getKey()).build(pair.getKey());
        } catch (Exception ex) {
            SuperiorSkyblockPlugin.log("Failed to load menu because of player: " + pair.getKey().getName());
            PluginDebugger.debug(ex);
            throw ex;
        }
    }

    @Override
    protected List<Pair<SuperiorPlayer, Long>> requestObjects() {
        return island.getUniqueVisitorsWithTimes();
    }

}
