package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.warps.IslandWarp;
import com.bgsoftware.superiorskyblock.api.island.warps.WarpCategory;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.island.warps.SIslandWarp;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.islands.IslandPrivileges;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.utils.menus.MenuConverter;
import com.bgsoftware.superiorskyblock.utils.registry.Registry;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
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

    private MenuWarps(SuperiorPlayer superiorPlayer, WarpCategory warpCategory){
        super("menuWarps", superiorPlayer);
        this.warpCategory = warpCategory;
        this.hasManagePermission = warpCategory != null && warpCategory.getIsland().hasPermission(superiorPlayer, IslandPrivileges.SET_WARP);
    }

    @Override
    public void onPlayerClick(InventoryClickEvent event, IslandWarp islandWarp) {
        if(hasManagePermission && event != null && event.getClick().isRightClick()){
            previousMove = false;
            MenuWarpManage.openInventory(superiorPlayer, this, islandWarp);
        }
        else {
            if(!superiorPlayer.hasBypassModeEnabled() && plugin.getSettings().chargeOnWarp > 0) {
                if(plugin.getProviders().getBalance(superiorPlayer).compareTo(BigDecimal.valueOf(plugin.getSettings().chargeOnWarp)) < 0){
                    Locale.NOT_ENOUGH_MONEY_TO_WARP.send(superiorPlayer);
                    return;
                }

                plugin.getProviders().withdrawMoney(superiorPlayer, plugin.getSettings().chargeOnWarp);
            }

            Executor.sync(() -> {
                previousMove = false;
                superiorPlayer.runIfOnline(Player::closeInventory);
                warpCategory.getIsland().warpPlayer(superiorPlayer, islandWarp.getName());
            }, 1L);
        }
    }

    @Override
    protected void cloneAndOpen(SuperiorMenu previousMenu) {
        openInventory(superiorPlayer, previousMenu, warpCategory);
    }

    @Override
    protected ItemStack getObjectItem(ItemStack clickedItem, IslandWarp islandWarp) {
        try {
            ItemStack icon = islandWarp.getIcon(superiorPlayer);
            ItemBuilder itemBuilder = new ItemBuilder(icon == null ? clickedItem : icon);

            if(hasManagePermission && !editLore.isEmpty())
                itemBuilder.appendLore(editLore);

            return itemBuilder.replaceAll("{0}", islandWarp.getName())
                    .replaceAll("{1}", SBlockPosition.of(islandWarp.getLocation()).toString())
                    .replaceAll("{2}", islandWarp.hasPrivateFlag() ?
                            ensureNotNull(Locale.ISLAND_WARP_PRIVATE.getMessage(superiorPlayer.getUserLocale())) :
                            ensureNotNull(Locale.ISLAND_WARP_PUBLIC.getMessage(superiorPlayer.getUserLocale())))
                    .build(superiorPlayer);
        }catch(Exception ex){
            SuperiorSkyblockPlugin.log("Failed to load menu because of warp: " + islandWarp.getName());
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

    public static void init(){
        MenuWarps menuWarps = new MenuWarps(null, null);

        File file = new File(plugin.getDataFolder(), "menus/warps.yml");

        if(!file.exists())
            FileUtils.saveResource("menus/warps.yml");

        CommentedConfiguration cfg = CommentedConfiguration.loadConfiguration(file);

        try {
            cfg.syncWithConfig(file, FileUtils.getResource("menus/warps.yml"), MENU_IGNORED_SECTIONS);
        }catch (Exception ex){
            ex.printStackTrace();
        }

        if(convertOldGUI(cfg)){
            try {
                cfg.save(file);
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }

        Registry<Character, List<Integer>> charSlots = FileUtils.loadGUI(menuWarps, "warps.yml", cfg);

        menuWarps.setPreviousSlot(getSlots(cfg, "previous-page", charSlots));
        menuWarps.setCurrentSlot(getSlots(cfg, "current-page", charSlots));
        menuWarps.setNextSlot(getSlots(cfg, "next-page", charSlots));

        List<Integer> slots = getSlots(cfg, "slots", charSlots);
        menuWarps.setSlots(slots);

        SIslandWarp.DEFAULT_WARP_ICON = menuWarps.getFillItem(slots.get(0));

        editLore = cfg.getStringList("edit-lore");

        charSlots.delete();

        menuWarps.markCompleted();
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, SuperiorMenu previousMenu, WarpCategory warpCategory){
        MenuWarps menuWarps = new MenuWarps(superiorPlayer, warpCategory);
        if(plugin.getSettings().skipOneItemMenus && hasOnlyOneItem(warpCategory, superiorPlayer) &&
                !warpCategory.getIsland().hasPermission(superiorPlayer, IslandPrivileges.SET_WARP)){
            menuWarps.onPlayerClick(null, getOnlyOneItem(warpCategory, superiorPlayer));
        }
        else {
            menuWarps.open(previousMenu);
        }
    }

    public static void refreshMenus(WarpCategory warpCategory){
        refreshMenus(MenuWarps.class, superiorMenu -> superiorMenu.warpCategory.equals(warpCategory));
    }

    public static void destroyMenus(WarpCategory warpCategory){
        destroyMenus(MenuWarps.class, superiorMenu -> superiorMenu.warpCategory.equals(warpCategory));
    }

    private String ensureNotNull(String check){
        return check == null ? "" : check;
    }

    private static boolean hasOnlyOneItem(WarpCategory warpCategory, SuperiorPlayer superiorPlayer){
        boolean isMember = warpCategory.getIsland().isMember(superiorPlayer);
        return warpCategory.getWarps().stream()
                .filter(islandWarp -> isMember || !islandWarp.hasPrivateFlag())
                .count() == 1;
    }

    private static IslandWarp getOnlyOneItem(WarpCategory warpCategory, SuperiorPlayer superiorPlayer){
        boolean isMember = warpCategory.getIsland().isMember(superiorPlayer);
        return warpCategory.getWarps().stream()
                .filter(islandWarp -> isMember || !islandWarp.hasPrivateFlag())
                .findFirst().orElse(null);
    }

    private static boolean convertOldGUI(YamlConfiguration newMenu){
        File oldFile = new File(plugin.getDataFolder(), "guis/warps-gui.yml");

        if(!oldFile.exists())
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

        if(cfg.contains("warps-gui.fill-items")) {
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

}
