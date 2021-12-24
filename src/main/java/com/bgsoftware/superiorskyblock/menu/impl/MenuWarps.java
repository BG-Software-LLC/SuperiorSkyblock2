package com.bgsoftware.superiorskyblock.menu.impl;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.lang.Message;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.warps.IslandWarp;
import com.bgsoftware.superiorskyblock.api.island.warps.WarpCategory;
import com.bgsoftware.superiorskyblock.api.menu.ISuperiorMenu;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.island.warps.SIslandWarp;
import com.bgsoftware.superiorskyblock.menu.PagedSuperiorMenu;
import com.bgsoftware.superiorskyblock.menu.converter.MenuConverter;
import com.bgsoftware.superiorskyblock.menu.file.MenuPatternSlots;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.island.permissions.IslandPrivileges;
import com.bgsoftware.superiorskyblock.utils.debug.PluginDebugger;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.threads.Executor;
import com.bgsoftware.superiorskyblock.wrappers.SBlockPosition;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class MenuWarps extends PagedSuperiorMenu<IslandWarp> {

    private static List<String> editLore;

    private final WarpCategory warpCategory;
    private final boolean hasManagePermission;

    private MenuWarps(SuperiorPlayer superiorPlayer, WarpCategory warpCategory) {
        super("menuWarps", superiorPlayer);
        this.warpCategory = warpCategory;
        this.hasManagePermission = warpCategory != null && warpCategory.getIsland().hasPermission(superiorPlayer, IslandPrivileges.SET_WARP);
    }

    public static void simulateClick(SuperiorPlayer superiorPlayer, Island island, String warpName) {

        if (!superiorPlayer.hasBypassModeEnabled() && plugin.getSettings().getChargeOnWarp() > 0) {
            if (plugin.getProviders().getEconomyProvider().getBalance(superiorPlayer)
                    .compareTo(BigDecimal.valueOf(plugin.getSettings().getChargeOnWarp())) < 0) {
                Message.NOT_ENOUGH_MONEY_TO_WARP.send(superiorPlayer);
                return;
            }

            plugin.getProviders().getEconomyProvider().withdrawMoney(superiorPlayer,
                    plugin.getSettings().getChargeOnWarp());
        }

        Executor.sync(() -> {
            superiorPlayer.runIfOnline(Player::closeInventory);
            island.warpPlayer(superiorPlayer, warpName);
        }, 1L);

    }

    public static void init() {
        MenuWarps menuWarps = new MenuWarps(null, null);

        File file = new File(plugin.getDataFolder(), "menus/warps.yml");

        if (!file.exists())
            FileUtils.saveResource("menus/warps.yml");

        CommentedConfiguration cfg = CommentedConfiguration.loadConfiguration(file);

        try {
            cfg.syncWithConfig(file, FileUtils.getResource("menus/warps.yml"), MENU_IGNORED_SECTIONS);
        } catch (Exception ex) {
            ex.printStackTrace();
            PluginDebugger.debug(ex);
        }

        if (convertOldGUI(cfg)) {
            try {
                cfg.save(file);
            } catch (Exception ex) {
                ex.printStackTrace();
                PluginDebugger.debug(ex);
            }
        }

        MenuPatternSlots menuPatternSlots = FileUtils.loadGUI(menuWarps, "warps.yml", cfg);

        menuWarps.setPreviousSlot(getSlots(cfg, "previous-page", menuPatternSlots));
        menuWarps.setCurrentSlot(getSlots(cfg, "current-page", menuPatternSlots));
        menuWarps.setNextSlot(getSlots(cfg, "next-page", menuPatternSlots));

        List<Integer> slots = getSlots(cfg, "slots", menuPatternSlots);
        menuWarps.setSlots(slots);

        SIslandWarp.DEFAULT_WARP_ICON = menuWarps.getFillItem(slots.get(0));

        editLore = cfg.getStringList("edit-lore");

        menuWarps.markCompleted();
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, ISuperiorMenu previousMenu, WarpCategory warpCategory) {
        MenuWarps menuWarps = new MenuWarps(superiorPlayer, warpCategory);
        if (plugin.getSettings().isSkipOneItemMenus() && hasOnlyOneItem(warpCategory, superiorPlayer) &&
                !warpCategory.getIsland().hasPermission(superiorPlayer, IslandPrivileges.SET_WARP)) {
            menuWarps.onPlayerClick(null, getOnlyOneItem(warpCategory, superiorPlayer));
        } else {
            menuWarps.open(previousMenu);
        }
    }

    public static void refreshMenus(WarpCategory warpCategory) {
        refreshMenus(MenuWarps.class, superiorMenu -> superiorMenu.warpCategory.equals(warpCategory));
    }

    public static void destroyMenus(WarpCategory warpCategory) {
        destroyMenus(MenuWarps.class, superiorMenu -> superiorMenu.warpCategory.equals(warpCategory));
    }

    private static boolean hasOnlyOneItem(WarpCategory warpCategory, SuperiorPlayer superiorPlayer) {
        boolean isMember = warpCategory.getIsland().isMember(superiorPlayer);
        return warpCategory.getWarps().stream()
                .filter(islandWarp -> isMember || !islandWarp.hasPrivateFlag())
                .count() == 1;
    }

    private static IslandWarp getOnlyOneItem(WarpCategory warpCategory, SuperiorPlayer superiorPlayer) {
        boolean isMember = warpCategory.getIsland().isMember(superiorPlayer);
        return warpCategory.getWarps().stream()
                .filter(islandWarp -> isMember || !islandWarp.hasPrivateFlag())
                .findFirst().orElse(null);
    }

    private static boolean convertOldGUI(YamlConfiguration newMenu) {
        File oldFile = new File(plugin.getDataFolder(), "guis/warps-gui.yml");

        if (!oldFile.exists())
            return false;

        //We want to reset the items of newMenu.
        ConfigurationSection itemsSection = newMenu.createSection("items");
        ConfigurationSection soundsSection = newMenu.createSection("sounds");
        ConfigurationSection commandsSection = newMenu.createSection("commands");

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(oldFile);

        newMenu.set("title", cfg.getString("warps-gui.title"));

        int size = cfg.getInt("warps-gui.size");

        char[] patternChars = new char[size * 9];
        Arrays.fill(patternChars, '\n');

        int charCounter = 0;

        if (cfg.contains("warps-gui.fill-items")) {
            charCounter = MenuConverter.convertFillItems(cfg.getConfigurationSection("warps-gui.fill-items"),
                    charCounter, patternChars, itemsSection, commandsSection, soundsSection);
        }

        char slotsChar = itemChars[charCounter++];

        MenuConverter.convertPagedButtons(cfg.getConfigurationSection("warps-gui"),
                cfg.getConfigurationSection("warps-gui.warp-item"),
                newMenu, patternChars,
                slotsChar, itemChars[charCounter++], itemChars[charCounter++], itemChars[charCounter++],
                itemsSection, commandsSection, soundsSection);

        newMenu.set("pattern", MenuConverter.buildPattern(size, patternChars, itemChars[charCounter]));

        return true;
    }

    @Override
    public void onPlayerClick(InventoryClickEvent event, IslandWarp islandWarp) {
        if (hasManagePermission && event != null && event.getClick().isRightClick()) {
            previousMove = false;
            plugin.getMenus().openWarpManage(superiorPlayer, this, islandWarp);
        } else {

            simulateClick(superiorPlayer, warpCategory.getIsland(), islandWarp.getName());

            Executor.sync(() -> {
                previousMove = false;
            }, 1L);
        }
    }

    @Override
    protected ItemStack getObjectItem(ItemStack clickedItem, IslandWarp islandWarp) {
        try {
            ItemStack icon = islandWarp.getIcon(superiorPlayer);
            ItemBuilder itemBuilder = new ItemBuilder(icon == null ? clickedItem : icon);

            if (hasManagePermission && !editLore.isEmpty())
                itemBuilder.appendLore(editLore);

            return itemBuilder.replaceAll("{0}", islandWarp.getName())
                    .replaceAll("{1}", SBlockPosition.of(islandWarp.getLocation()).toString())
                    .replaceAll("{2}", islandWarp.hasPrivateFlag() ?
                            ensureNotNull(Message.ISLAND_WARP_PRIVATE.getMessage(superiorPlayer.getUserLocale())) :
                            ensureNotNull(Message.ISLAND_WARP_PUBLIC.getMessage(superiorPlayer.getUserLocale())))
                    .build(superiorPlayer);
        } catch (Exception ex) {
            SuperiorSkyblockPlugin.log("Failed to load menu because of warp: " + islandWarp.getName());
            PluginDebugger.debug(ex);
            throw ex;
        }
    }

    @Override
    protected List<IslandWarp> requestObjects() {
        boolean isMember = warpCategory.getIsland().isMember(superiorPlayer);
        return warpCategory.getWarps().stream()
                .filter(islandWarp -> isMember || !islandWarp.hasPrivateFlag())
                .collect(Collectors.toList());
    }

    @Override
    public void cloneAndOpen(ISuperiorMenu previousMenu) {
        openInventory(superiorPlayer, previousMenu, warpCategory);
    }

    private String ensureNotNull(String check) {
        return check == null ? "" : check;
    }

}
