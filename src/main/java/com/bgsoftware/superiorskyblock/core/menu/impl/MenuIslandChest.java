package com.bgsoftware.superiorskyblock.core.menu.impl;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandChest;
import com.bgsoftware.superiorskyblock.api.menu.Menu;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.SequentialListBuilder;
import com.bgsoftware.superiorskyblock.core.io.MenuParserImpl;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.menu.AbstractPagedMenu;
import com.bgsoftware.superiorskyblock.core.menu.MenuIdentifiers;
import com.bgsoftware.superiorskyblock.core.menu.MenuParseResult;
import com.bgsoftware.superiorskyblock.core.menu.MenuPatternSlots;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.IslandChestPagedObjectButton;
import com.bgsoftware.superiorskyblock.core.menu.layout.PagedMenuLayoutImpl;
import com.bgsoftware.superiorskyblock.core.menu.view.AbstractPagedMenuView;
import com.bgsoftware.superiorskyblock.core.menu.view.MenuViewWrapper;
import com.bgsoftware.superiorskyblock.core.menu.view.args.IslandViewArgs;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.Arrays;
import java.util.List;

public class MenuIslandChest extends AbstractPagedMenu<MenuIslandChest.View, IslandViewArgs, IslandChest> {

    private MenuIslandChest(MenuParseResult<View> parseResult) {
        super(MenuIdentifiers.MENU_ISLAND_CHEST, parseResult, false);
    }

    @Override
    protected View createViewInternal(SuperiorPlayer superiorPlayer, IslandViewArgs args,
                                      @Nullable MenuView<?, ?> previousMenuView) {
        return new View(superiorPlayer, previousMenuView, this, args);
    }

    public void refreshViews(Island island) {
        refreshViews(view -> view.island.equals(island));
    }

    public void openMenu(SuperiorPlayer superiorPlayer, @Nullable MenuView<?, ?> previousMenu, Island island) {
        if (isSkipOneItem()) {
            IslandChest[] islandChest = island.getChest();
            if (islandChest.length == 1) {
                islandChest[0].openChest(superiorPlayer);
                return;
            }
        }

        plugin.getMenus().openIslandChest(superiorPlayer, MenuViewWrapper.fromView(previousMenu), island);
    }

    @Nullable
    public static MenuIslandChest createInstance() {
        MenuParseResult<View> menuParseResult = MenuParserImpl.getInstance().loadMenu("island-chest.yml",
                null, new IslandChestPagedObjectButton.Builder());

        if (menuParseResult == null) {
            return null;
        }

        MenuPatternSlots menuPatternSlots = menuParseResult.getPatternSlots();
        YamlConfiguration cfg = menuParseResult.getConfig();
        PagedMenuLayoutImpl.Builder<View, IslandChest> patternBuilder = (PagedMenuLayoutImpl.Builder<View, IslandChest>) menuParseResult.getLayoutBuilder();

        if (cfg.isString("slots")) {
            for (char slotChar : cfg.getString("slots", "").toCharArray()) {
                List<Integer> slots = menuPatternSlots.getSlots(slotChar);

                ConfigurationSection validPageSection = cfg.getConfigurationSection("items." + slotChar + ".valid-page");
                ConfigurationSection invalidPageSection = cfg.getConfigurationSection("items." + slotChar + ".invalid-page");

                if (validPageSection == null) {
                    Log.warnFromFile("island-chest.yml", "The slot char ", slotChar, " is missing the valid-page section.");
                    continue;
                }

                if (invalidPageSection == null) {
                    Log.warnFromFile("island-chest.yml", "&cThe slot char ", slotChar, " is missing the invalid-page section.");
                    continue;
                }

                IslandChestPagedObjectButton.Builder buttonBuilder = new IslandChestPagedObjectButton.Builder();
                buttonBuilder.setButtonItem(MenuParserImpl.getInstance().getItemStack("island-chest.yml", validPageSection));
                buttonBuilder.setNullItem(MenuParserImpl.getInstance().getItemStack("island-chest.yml", invalidPageSection));

                patternBuilder.mapButtons(slots, buttonBuilder);
            }
        }

        return new MenuIslandChest(menuParseResult);
    }

    public static class View extends AbstractPagedMenuView<MenuIslandChest.View, IslandViewArgs, IslandChest> {

        private final Island island;

        View(SuperiorPlayer inventoryViewer, @Nullable MenuView<?, ?> previousMenuView,
             Menu<View, IslandViewArgs> menu, IslandViewArgs args) {
            super(inventoryViewer, previousMenuView, menu);
            this.island = args.getIsland();
        }

        @Override
        protected List<IslandChest> requestObjects() {
            return new SequentialListBuilder<IslandChest>()
                    .build(Arrays.asList(island.getChest()));
        }

    }

}
