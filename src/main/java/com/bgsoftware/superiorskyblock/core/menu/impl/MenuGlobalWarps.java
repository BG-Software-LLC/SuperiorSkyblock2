package com.bgsoftware.superiorskyblock.core.menu.impl;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.menu.Menu;
import com.bgsoftware.superiorskyblock.api.menu.layout.PagedMenuLayout;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.SequentialListBuilder;
import com.bgsoftware.superiorskyblock.core.io.MenuParserImpl;
import com.bgsoftware.superiorskyblock.core.menu.AbstractPagedMenu;
import com.bgsoftware.superiorskyblock.core.menu.MenuIdentifiers;
import com.bgsoftware.superiorskyblock.core.menu.MenuParseResult;
import com.bgsoftware.superiorskyblock.core.menu.MenuPatternSlots;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.GlobalWarpsPagedObjectButton;
import com.bgsoftware.superiorskyblock.core.menu.converter.MenuConverter;
import com.bgsoftware.superiorskyblock.core.menu.layout.AbstractMenuLayout;
import com.bgsoftware.superiorskyblock.core.menu.view.AbstractPagedMenuView;
import com.bgsoftware.superiorskyblock.core.menu.view.args.EmptyViewArgs;
import com.bgsoftware.superiorskyblock.island.top.SortingComparators;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

public class MenuGlobalWarps extends AbstractPagedMenu<MenuGlobalWarps.View, EmptyViewArgs, Island> {

    private final boolean visitorWarps;

    private MenuGlobalWarps(MenuParseResult<View> parseResult, boolean visitorWarps) {
        super(MenuIdentifiers.MENU_GLOBAL_WARPS, parseResult, false);
        this.visitorWarps = visitorWarps;
    }

    public boolean isVisitorWarps() {
        return visitorWarps;
    }

    @Override
    protected View createViewInternal(SuperiorPlayer superiorPlayer, EmptyViewArgs unused,
                                      @Nullable MenuView<?, ?> previousMenuView) {
        return new View(superiorPlayer, previousMenuView, this);
    }

    @Nullable
    public static MenuGlobalWarps createInstance() {
        MenuParseResult<View> menuParseResult = MenuParserImpl.getInstance().loadMenu("global-warps.yml",
                MenuGlobalWarps::convertOldGUI, new GlobalWarpsPagedObjectButton.Builder());

        if (menuParseResult == null)
            return null;

        MenuPatternSlots menuPatternSlots = menuParseResult.getPatternSlots();
        YamlConfiguration cfg = menuParseResult.getConfig();
        PagedMenuLayout.Builder<View, Island> patternBuilder = (PagedMenuLayout.Builder<View, Island>) menuParseResult.getLayoutBuilder();

        boolean visitorWarps = cfg.getBoolean("visitor-warps", false);

        List<Integer> slots = new LinkedList<>();

        if (cfg.contains("warps"))
            slots.addAll(MenuParserImpl.getInstance().parseButtonSlots(cfg, "warps", menuPatternSlots));
        if (cfg.contains("slots"))
            slots.addAll(MenuParserImpl.getInstance().parseButtonSlots(cfg, "slots", menuPatternSlots));
        if (slots.isEmpty())
            slots.add(-1);

        patternBuilder.setPagedObjectSlots(slots, new GlobalWarpsPagedObjectButton.Builder());

        return new MenuGlobalWarps(menuParseResult, visitorWarps);
    }

    public class View extends AbstractPagedMenuView<MenuGlobalWarps.View, EmptyViewArgs, Island> {

        View(SuperiorPlayer inventoryViewer, @Nullable MenuView<?, ?> previousMenuView,
             Menu<View, EmptyViewArgs> menu) {
            super(inventoryViewer, previousMenuView, menu);
        }

        @Override
        protected List<Island> requestObjects() {
            return new SequentialListBuilder<Island>()
                    .sorted(SortingComparators.WORTH_COMPARATOR)
                    .filter(ISLANDS_FILTER)
                    .build(plugin.getGrid().getIslands());
        }

        private final Predicate<Island> ISLANDS_FILTER = island -> {
            if (visitorWarps)
                return island.getVisitorsLocation(null /* unused */) != null;
            else if (island.equals(getInventoryViewer().getIsland()))
                return !island.getIslandWarps().isEmpty();
            else
                return island.getIslandWarps().values().stream().anyMatch(islandWarp -> !islandWarp.hasPrivateFlag());
        };

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

        newMenu.set("title", cfg.getString("global-gui.title"));

        int size = cfg.getInt("global-gui.size");

        char[] patternChars = new char[size * 9];
        Arrays.fill(patternChars, '\n');

        int charCounter = 0;

        if (cfg.contains("global-gui.fill-items")) {
            charCounter = MenuConverter.convertFillItems(cfg.getConfigurationSection("global-gui.fill-items"),
                    charCounter, patternChars, itemsSection, commandsSection, soundsSection);
        }

        char slotsChar = AbstractMenuLayout.BUTTON_SYMBOLS[charCounter++];

        MenuConverter.convertPagedButtons(cfg.getConfigurationSection("global-gui"),
                cfg.getConfigurationSection("global-gui.warp-item"),
                newMenu, patternChars,
                slotsChar, AbstractMenuLayout.BUTTON_SYMBOLS[charCounter++],
                AbstractMenuLayout.BUTTON_SYMBOLS[charCounter++],
                AbstractMenuLayout.BUTTON_SYMBOLS[charCounter++],
                itemsSection, commandsSection, soundsSection);

        newMenu.set("visitor-warps", cfg.getConfigurationSection("global-gui.visitor-warps"));
        newMenu.set("warps", newMenu.getString("slots"));
        newMenu.set("slots", null);

        newMenu.set("pattern", MenuConverter.buildPattern(size, patternChars,
                AbstractMenuLayout.BUTTON_SYMBOLS[charCounter]));

        return true;
    }

}
