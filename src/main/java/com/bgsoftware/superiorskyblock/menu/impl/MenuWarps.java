package com.bgsoftware.superiorskyblock.menu.impl;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.warps.IslandWarp;
import com.bgsoftware.superiorskyblock.api.island.warps.WarpCategory;
import com.bgsoftware.superiorskyblock.api.menu.ISuperiorMenu;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.island.permissions.IslandPrivileges;
import com.bgsoftware.superiorskyblock.island.warps.SIslandWarp;
import com.bgsoftware.superiorskyblock.lang.Message;
import com.bgsoftware.superiorskyblock.menu.PagedSuperiorMenu;
import com.bgsoftware.superiorskyblock.menu.button.impl.DummyButton;
import com.bgsoftware.superiorskyblock.menu.button.impl.menu.WarpPagedObjectButton;
import com.bgsoftware.superiorskyblock.menu.converter.MenuConverter;
import com.bgsoftware.superiorskyblock.menu.file.MenuPatternSlots;
import com.bgsoftware.superiorskyblock.menu.pattern.SuperiorMenuPattern;
import com.bgsoftware.superiorskyblock.menu.pattern.impl.PagedMenuPattern;
import com.bgsoftware.superiorskyblock.threads.Executor;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.utils.items.TemplateItem;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class MenuWarps extends PagedSuperiorMenu<MenuWarps, IslandWarp> {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
    private static PagedMenuPattern<MenuWarps, IslandWarp> menuPattern;

    public static List<String> editLore;

    private final WarpCategory warpCategory;
    private final boolean hasManagePermission;

    private MenuWarps(SuperiorPlayer superiorPlayer, WarpCategory warpCategory) {
        super(menuPattern, superiorPlayer);
        this.warpCategory = warpCategory;
        this.hasManagePermission = warpCategory != null && warpCategory.getIsland().hasPermission(superiorPlayer, IslandPrivileges.SET_WARP);
    }

    public WarpCategory getWarpCategory() {
        return warpCategory;
    }

    public boolean hasManagePermission() {
        return hasManagePermission;
    }

    @Override
    public void cloneAndOpen(ISuperiorMenu previousMenu) {
        openInventory(inventoryViewer, previousMenu, warpCategory);
    }

    @Override
    protected List<IslandWarp> requestObjects() {
        boolean isMember = warpCategory.getIsland().isMember(inventoryViewer);
        return warpCategory.getWarps().stream()
                .filter(islandWarp -> isMember || !islandWarp.hasPrivateFlag())
                .collect(Collectors.toList());
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
        menuPattern = null;

        PagedMenuPattern.Builder<MenuWarps, IslandWarp> patternBuilder = new PagedMenuPattern.Builder<>();

        Pair<MenuPatternSlots, CommentedConfiguration> menuLoadResult = FileUtils.loadMenu(patternBuilder,
                "warps.yml", MenuWarps::convertOldGUI);

        if (menuLoadResult == null)
            return;

        MenuPatternSlots menuPatternSlots = menuLoadResult.getKey();
        CommentedConfiguration cfg = menuLoadResult.getValue();

        editLore = cfg.getStringList("edit-lore");

        menuPattern = patternBuilder
                .setPreviousPageSlots(getSlots(cfg, "previous-page", menuPatternSlots))
                .setCurrentPageSlots(getSlots(cfg, "current-page", menuPatternSlots))
                .setNextPageSlots(getSlots(cfg, "next-page", menuPatternSlots))
                .setPagedObjectSlots(getSlots(cfg, "slots", menuPatternSlots), new WarpPagedObjectButton.Builder())
                .build();

        ItemStack defaultWarpIcon = menuPattern.getButtons().stream()
                .filter(button -> button instanceof WarpPagedObjectButton)
                .findFirst().orElse(new DummyButton.Builder<MenuWarps>().build())
                .getRawButtonItem();

        SIslandWarp.DEFAULT_WARP_ICON = new TemplateItem(defaultWarpIcon == null ? new ItemBuilder(Material.AIR) :
                new ItemBuilder(defaultWarpIcon));
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, ISuperiorMenu previousMenu, WarpCategory warpCategory) {
        if (plugin.getSettings().isSkipOneItemMenus() && hasOnlyOneItem(warpCategory, superiorPlayer) &&
                !warpCategory.getIsland().hasPermission(superiorPlayer, IslandPrivileges.SET_WARP)) {
            simulateClick(superiorPlayer, warpCategory.getIsland(), getOnlyOneItem(warpCategory, superiorPlayer).getName());
        } else {
            new MenuWarps(superiorPlayer, warpCategory).open(previousMenu);
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

    private static boolean convertOldGUI(SuperiorSkyblockPlugin plugin, YamlConfiguration newMenu) {
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

        char slotsChar = SuperiorMenuPattern.BUTTON_SYMBOLS[charCounter++];

        MenuConverter.convertPagedButtons(cfg.getConfigurationSection("warps-gui"),
                cfg.getConfigurationSection("warps-gui.warp-item"),
                newMenu, patternChars,
                slotsChar, SuperiorMenuPattern.BUTTON_SYMBOLS[charCounter++],
                SuperiorMenuPattern.BUTTON_SYMBOLS[charCounter++], SuperiorMenuPattern.BUTTON_SYMBOLS[charCounter++],
                itemsSection, commandsSection, soundsSection);

        newMenu.set("pattern", MenuConverter.buildPattern(size, patternChars,
                SuperiorMenuPattern.BUTTON_SYMBOLS[charCounter]));

        return true;
    }

}
