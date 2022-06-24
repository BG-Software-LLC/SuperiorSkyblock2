package com.bgsoftware.superiorskyblock.core.menu.impl;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandChest;
import com.bgsoftware.superiorskyblock.api.menu.ISuperiorMenu;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.SequentialListBuilder;
import com.bgsoftware.superiorskyblock.core.io.MenuParser;
import com.bgsoftware.superiorskyblock.core.menu.MenuParseResult;
import com.bgsoftware.superiorskyblock.core.menu.MenuPatternSlots;
import com.bgsoftware.superiorskyblock.core.menu.PagedSuperiorMenu;
import com.bgsoftware.superiorskyblock.core.menu.SuperiorMenu;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.menu.IslandChestPagedObjectButton;
import com.bgsoftware.superiorskyblock.core.menu.pattern.impl.PagedMenuPattern;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Arrays;
import java.util.List;

public class MenuIslandChest extends PagedSuperiorMenu<MenuIslandChest, IslandChest> {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private static PagedMenuPattern<MenuIslandChest, IslandChest> menuPattern;

    private final Island island;

    private MenuIslandChest(SuperiorPlayer superiorPlayer, Island island) {
        super(menuPattern, superiorPlayer);
        this.island = island;
    }

    public Island getTargetIsland() {
        return island;
    }

    @Override
    public void cloneAndOpen(ISuperiorMenu previousMenu) {
        openInventory(inventoryViewer, previousMenu, island);
    }

    @Override
    protected List<IslandChest> requestObjects() {
        return new SequentialListBuilder<IslandChest>()
                .build(Arrays.asList(island.getChest()));
    }

    public static void init() {
        menuPattern = null;

        PagedMenuPattern.Builder<MenuIslandChest, IslandChest> patternBuilder = new PagedMenuPattern.Builder<>();

        MenuParseResult menuLoadResult = MenuParser.loadMenu(patternBuilder, "island-chest.yml", null);

        if (menuLoadResult == null)
            return;

        MenuPatternSlots menuPatternSlots = menuLoadResult.getPatternSlots();
        CommentedConfiguration cfg = menuLoadResult.getConfig();

        if (cfg.isString("slots")) {
            for (char slotChar : cfg.getString("slots", "").toCharArray()) {
                List<Integer> slots = menuPatternSlots.getSlots(slotChar);

                ConfigurationSection validPageSection = cfg.getConfigurationSection("items." + slotChar + ".valid-page");
                ConfigurationSection invalidPageSection = cfg.getConfigurationSection("items." + slotChar + ".invalid-page");

                if (validPageSection == null) {
                    SuperiorSkyblockPlugin.log("&cThe slot char " + slotChar + " is missing the valid-page section.");
                    continue;
                }

                if (invalidPageSection == null) {
                    SuperiorSkyblockPlugin.log("&cThe slot char " + slotChar + " is missing the invalid-page section.");
                    continue;
                }

                patternBuilder.mapButtons(slots, new IslandChestPagedObjectButton.Builder()
                        .setButtonItem(MenuParser.getItemStack("island-chest.yml", validPageSection))
                        .setNullItem(MenuParser.getItemStack("island-chest.yml", invalidPageSection)));
            }
        }

        menuPattern = patternBuilder
                .setPreviousPageSlots(getSlots(cfg, "previous-page", menuPatternSlots))
                .setCurrentPageSlots(getSlots(cfg, "current-page", menuPatternSlots))
                .setNextPageSlots(getSlots(cfg, "next-page", menuPatternSlots))
                .build();
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, ISuperiorMenu previousMenu, Island island) {
        MenuIslandChest menuIslandChest = new MenuIslandChest(superiorPlayer, island);
        if (plugin.getSettings().isSkipOneItemMenus() && island.getChest().length == 1) {
            island.getChest()[0].openChest(superiorPlayer);
        } else {
            menuIslandChest.open(previousMenu);
        }
    }

    public static void refreshMenus(Island island) {
        SuperiorMenu.refreshMenus(MenuIslandChest.class, superiorMenu -> superiorMenu.island.equals(island));
    }

}
