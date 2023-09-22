package com.bgsoftware.superiorskyblock.core.menu.impl;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.warps.IslandWarp;
import com.bgsoftware.superiorskyblock.api.island.warps.WarpCategory;
import com.bgsoftware.superiorskyblock.api.menu.Menu;
import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.SequentialListBuilder;
import com.bgsoftware.superiorskyblock.core.io.MenuParserImpl;
import com.bgsoftware.superiorskyblock.core.itemstack.ItemBuilder;
import com.bgsoftware.superiorskyblock.core.menu.AbstractPagedMenu;
import com.bgsoftware.superiorskyblock.core.menu.MenuIdentifiers;
import com.bgsoftware.superiorskyblock.core.menu.MenuParseResult;
import com.bgsoftware.superiorskyblock.core.menu.TemplateItem;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.WarpPagedObjectButton;
import com.bgsoftware.superiorskyblock.core.menu.converter.MenuConverter;
import com.bgsoftware.superiorskyblock.core.menu.layout.AbstractMenuLayout;
import com.bgsoftware.superiorskyblock.core.menu.view.AbstractPagedMenuView;
import com.bgsoftware.superiorskyblock.core.menu.view.MenuViewWrapper;
import com.bgsoftware.superiorskyblock.core.menu.view.args.IslandViewArgs;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.island.privilege.IslandPrivileges;
import com.bgsoftware.superiorskyblock.island.warp.WarpIcons;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MenuWarps extends AbstractPagedMenu<MenuWarps.View, MenuWarps.Args, IslandWarp> {

    private final List<String> editLore;

    private MenuWarps(MenuParseResult<View> parseResult, List<String> editLore) {
        super(MenuIdentifiers.MENU_WARPS, parseResult, false);
        this.editLore = editLore;
    }

    public List<String> getEditLore() {
        return editLore;
    }

    @Override
    protected View createViewInternal(SuperiorPlayer superiorPlayer, Args args,
                                      @Nullable MenuView<?, ?> previousMenuView) {
        return new View(superiorPlayer, previousMenuView, this, args);
    }

    public void refreshViews(WarpCategory warpCategory) {
        refreshViews(view -> view.warpCategory.equals(warpCategory));
    }

    public void closeViews(WarpCategory warpCategory) {
        closeViews(view -> view.getWarpCategory().equals(warpCategory));
    }

    public void simulateClick(SuperiorPlayer superiorPlayer, Island island, IslandWarp islandWarp) {
        if (!superiorPlayer.hasBypassModeEnabled() && plugin.getSettings().getChargeOnWarp() > 0) {
            if (plugin.getProviders().getEconomyProvider().getBalance(superiorPlayer)
                    .compareTo(BigDecimal.valueOf(plugin.getSettings().getChargeOnWarp())) < 0) {
                Message.NOT_ENOUGH_MONEY_TO_WARP.send(superiorPlayer);
                return;
            }

            plugin.getProviders().getEconomyProvider().withdrawMoney(superiorPlayer,
                    plugin.getSettings().getChargeOnWarp());
        }

        BukkitExecutor.sync(() -> {
            superiorPlayer.runIfOnline(player -> {
                MenuView<?, ?> currentView = superiorPlayer.getOpenedView();
                if (currentView == null) {
                    player.closeInventory();
                } else {
                    currentView.closeView();
                }
                island.warpPlayer(superiorPlayer, islandWarp.getName());
            });
        }, 1L);
    }

    public void openMenu(SuperiorPlayer superiorPlayer, @Nullable MenuView<?, ?> previousMenu, WarpCategory warpCategory) {
        // We want skip one item to only work if the player can't edit warps, otherwise he
        // won't be able to edit them as the menu will get skipped if only one warp exists.
        if (isSkipOneItem() && !warpCategory.getIsland().hasPermission(superiorPlayer, IslandPrivileges.SET_WARP)) {
            List<IslandWarp> availableWarps = warpCategory.getIsland().isMember(superiorPlayer) ? warpCategory.getWarps() :
                    warpCategory.getWarps().stream()
                            .filter(islandWarp -> !islandWarp.hasPrivateFlag())
                            .collect(Collectors.toList());

            if (availableWarps.size() == 1) {
                simulateClick(superiorPlayer, warpCategory.getIsland(), availableWarps.get(0));
                return;
            }
        }

        plugin.getMenus().openWarps(superiorPlayer, MenuViewWrapper.fromView(previousMenu), warpCategory);
    }

    @Nullable
    public static MenuWarps createInstance() {
        MenuParseResult<View> menuParseResult = MenuParserImpl.getInstance().loadMenu("warps.yml",
                MenuWarps::convertOldGUI, new WarpPagedObjectButton.Builder());

        if (menuParseResult == null)
            return null;

        YamlConfiguration cfg = menuParseResult.getConfig();

        List<String> editLore = cfg.getStringList("edit-lore");

        ItemStack defaultWarpIcon = menuParseResult.getLayoutBuilder().build().getButtons().stream()
                .filter(button -> button.getViewButtonType().equals(WarpPagedObjectButton.class))
                .findFirst().map(MenuTemplateButton::getButtonItem)
                .orElse(null);
        WarpIcons.DEFAULT_WARP_ICON = new TemplateItem(defaultWarpIcon == null ? new ItemBuilder(Material.AIR) : new ItemBuilder(defaultWarpIcon));

        return new MenuWarps(menuParseResult, editLore);
    }

    public static class Args extends IslandViewArgs {

        private final WarpCategory warpCategory;

        public Args(WarpCategory warpCategory) {
            super(warpCategory.getIsland());
            this.warpCategory = warpCategory;
        }

    }

    public static class View extends AbstractPagedMenuView<View, Args, IslandWarp> {

        private final Island island;
        private final WarpCategory warpCategory;
        private final boolean hasManagePerms;

        protected View(SuperiorPlayer inventoryViewer, @Nullable MenuView<?, ?> previousMenuView,
                       Menu<View, Args> menu, Args args) {
            super(inventoryViewer, previousMenuView, menu);
            this.island = args.getIsland();
            this.warpCategory = args.warpCategory;
            this.hasManagePerms = warpCategory.getIsland().hasPermission(inventoryViewer, IslandPrivileges.SET_WARP);
        }

        @Override
        protected List<IslandWarp> requestObjects() {
            boolean isMember = warpCategory.getIsland().isMember(getInventoryViewer());
            return new SequentialListBuilder<IslandWarp>()
                    .filter(islandWarp -> isMember || !islandWarp.hasPrivateFlag())
                    .build(warpCategory.getWarps());
        }

        public Island getIsland() {
            return island;
        }

        public WarpCategory getWarpCategory() {
            return warpCategory;
        }

        public boolean hasManagePerms() {
            return hasManagePerms;
        }

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

        char slotsChar = AbstractMenuLayout.BUTTON_SYMBOLS[charCounter++];

        MenuConverter.convertPagedButtons(cfg.getConfigurationSection("warps-gui"),
                cfg.getConfigurationSection("warps-gui.warp-item"),
                newMenu, patternChars,
                slotsChar, AbstractMenuLayout.BUTTON_SYMBOLS[charCounter++],
                AbstractMenuLayout.BUTTON_SYMBOLS[charCounter++], AbstractMenuLayout.BUTTON_SYMBOLS[charCounter++],
                itemsSection, commandsSection, soundsSection);

        newMenu.set("pattern", MenuConverter.buildPattern(size, patternChars,
                AbstractMenuLayout.BUTTON_SYMBOLS[charCounter]));

        return true;
    }

}
