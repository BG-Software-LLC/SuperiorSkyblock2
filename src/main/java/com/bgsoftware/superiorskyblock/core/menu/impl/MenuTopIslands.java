package com.bgsoftware.superiorskyblock.core.menu.impl;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.SortingType;
import com.bgsoftware.superiorskyblock.api.menu.Menu;
import com.bgsoftware.superiorskyblock.api.menu.layout.MenuLayout;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import com.bgsoftware.superiorskyblock.api.menu.view.ViewArgs;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.io.MenuParserImpl;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.menu.AbstractPagedMenu;
import com.bgsoftware.superiorskyblock.core.menu.MenuIdentifiers;
import com.bgsoftware.superiorskyblock.core.menu.MenuParseResult;
import com.bgsoftware.superiorskyblock.core.menu.MenuPatternSlots;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.ChangeSortingTypeButton;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.TopIslandsPagedObjectButton;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.TopIslandsSelfIslandButton;
import com.bgsoftware.superiorskyblock.core.menu.converter.MenuConverter;
import com.bgsoftware.superiorskyblock.core.menu.layout.AbstractMenuLayout;
import com.bgsoftware.superiorskyblock.core.menu.view.AbstractPagedMenuView;
import com.bgsoftware.superiorskyblock.island.top.SortingTypes;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class MenuTopIslands extends AbstractPagedMenu<MenuTopIslands.View, MenuTopIslands.Args, Island> {

    private final boolean sortGlowWhenSelected;

    private MenuTopIslands(MenuParseResult<View> parseResult, boolean sortGlowWhenSelected) {
        super(MenuIdentifiers.MENU_TOP_ISLANDS, parseResult, false);
        this.sortGlowWhenSelected = sortGlowWhenSelected;
    }

    public boolean isSortGlowWhenSelected() {
        return sortGlowWhenSelected;
    }

    @Override
    protected View createViewInternal(SuperiorPlayer superiorPlayer, Args args,
                                      @Nullable MenuView<?, ?> previousMenuView) {
        return new View(superiorPlayer, previousMenuView, this, args);
    }

    @Override
    public CompletableFuture<View> refreshView(View view) {
        CompletableFuture<View> res = new CompletableFuture<>();
        plugin.getGrid().sortIslands(view.sortingType, () -> {
            super.refreshView(view).whenComplete((v, err) -> {
                if (err != null) {
                    res.completeExceptionally(err);
                } else {
                    res.complete(v);
                }
            });
        });
        return res;
    }

    public void refreshViews(SortingType sortingType) {
        refreshViews(view -> view.sortingType.equals(sortingType));
    }

    @Nullable
    public static MenuTopIslands createInstance() {
        MenuParseResult<View> menuParseResult = MenuParserImpl.getInstance().loadMenu("top-islands.yml",
                MenuTopIslands::convertOldGUI, new TopIslandsPagedObjectButton.Builder());

        if (menuParseResult == null)
            return null;

        MenuPatternSlots menuPatternSlots = menuParseResult.getPatternSlots();
        YamlConfiguration cfg = menuParseResult.getConfig();
        MenuLayout.Builder<View> patternBuilder = menuParseResult.getLayoutBuilder();

        boolean sortGlowWhenSelected = cfg.getBoolean("sort-glow-when-selected", false);

        patternBuilder.mapButtons(MenuParserImpl.getInstance().parseButtonSlots(cfg, "worth-sort", menuPatternSlots),
                new ChangeSortingTypeButton.Builder().setSortingType(SortingTypes.BY_WORTH));

        patternBuilder.mapButtons(MenuParserImpl.getInstance().parseButtonSlots(cfg, "level-sort", menuPatternSlots),
                new ChangeSortingTypeButton.Builder().setSortingType(SortingTypes.BY_LEVEL));

        patternBuilder.mapButtons(MenuParserImpl.getInstance().parseButtonSlots(cfg, "rating-sort", menuPatternSlots),
                new ChangeSortingTypeButton.Builder().setSortingType(SortingTypes.BY_RATING));

        patternBuilder.mapButtons(MenuParserImpl.getInstance().parseButtonSlots(cfg, "players-sort", menuPatternSlots),
                new ChangeSortingTypeButton.Builder().setSortingType(SortingTypes.BY_PLAYERS));

        if (cfg.isConfigurationSection("items")) {
            for (String itemSectionName : cfg.getConfigurationSection("items").getKeys(false)) {
                ConfigurationSection itemSection = cfg.getConfigurationSection("items." + itemSectionName);

                if (!itemSection.isString("sorting-type"))
                    continue;

                SortingType sortingType = SortingType.getByName(itemSection.getString("sorting-type"));

                if (sortingType == null) {
                    Log.warnFromFile("top-islands.yml", "The sorting type is invalid for the item ", itemSectionName);
                    continue;
                }

                patternBuilder.mapButtons(menuPatternSlots.getSlots(itemSectionName),
                        new ChangeSortingTypeButton.Builder().setSortingType(sortingType));
            }
        }

        if (cfg.isString("slots")) {
            boolean configuredSelfPlayerButton = false;

            for (char slotsChar : cfg.getString("slots", "").toCharArray()) {
                ConfigurationSection itemsSection = cfg.getConfigurationSection("items." + slotsChar);

                if (itemsSection == null)
                    continue;

                TopIslandsPagedObjectButton.Builder slotsBuilder = new TopIslandsPagedObjectButton.Builder();
                slotsBuilder.setIslandItem(MenuParserImpl.getInstance().getItemStack("top-islands.yml", itemsSection.getConfigurationSection("island")));
                slotsBuilder.setNoIslandItem(MenuParserImpl.getInstance().getItemStack("top-islands.yml", itemsSection.getConfigurationSection("no-island")));
                slotsBuilder.setIslandSound(MenuParserImpl.getInstance().getSound(cfg.getConfigurationSection("sounds." + slotsChar + ".island")));
                slotsBuilder.setNoIslandSound(MenuParserImpl.getInstance().getSound(cfg.getConfigurationSection("sounds." + slotsChar + ".no-island")));
                slotsBuilder.setIslandCommands(cfg.getStringList("commands." + slotsChar + ".island"));
                slotsBuilder.setNoIslandCommands(cfg.getStringList("commands." + slotsChar + ".no-island"));

                patternBuilder.mapButtons(menuPatternSlots.getSlots(slotsChar), slotsBuilder);

                if (!configuredSelfPlayerButton) {
                    configuredSelfPlayerButton = true;

                    TopIslandsSelfIslandButton.Builder selfIslandBuilder = new TopIslandsSelfIslandButton.Builder();
                    selfIslandBuilder.setIslandItem(MenuParserImpl.getInstance().getItemStack("top-islands.yml", itemsSection.getConfigurationSection("island")));
                    selfIslandBuilder.setNoIslandItem(MenuParserImpl.getInstance().getItemStack("top-islands.yml", itemsSection.getConfigurationSection("no-island")));
                    selfIslandBuilder.setIslandSound(MenuParserImpl.getInstance().getSound(cfg.getConfigurationSection("sounds." + slotsChar + ".island")));
                    selfIslandBuilder.setNoIslandSound(MenuParserImpl.getInstance().getSound(cfg.getConfigurationSection("sounds." + slotsChar + ".no-island")));
                    selfIslandBuilder.setIslandCommands(cfg.getStringList("commands." + slotsChar + ".island"));
                    selfIslandBuilder.setNoIslandCommands(cfg.getStringList("commands." + slotsChar + ".no-island"));

                    patternBuilder.mapButtons(MenuParserImpl.getInstance().parseButtonSlots(cfg, "player-island", menuPatternSlots),
                            selfIslandBuilder);
                }
            }
        }


        return new MenuTopIslands(menuParseResult, sortGlowWhenSelected);
    }

    public static class Args implements ViewArgs {

        private final SortingType sortingType;

        public Args(SortingType sortingType) {
            this.sortingType = sortingType;
        }

    }

    public static class View extends AbstractPagedMenuView<View, Args, Island> {

        private final Set<SortingType> alreadySorted = new HashSet<>();
        private SortingType sortingType;

        View(SuperiorPlayer inventoryViewer, @Nullable MenuView<?, ?> previousMenuView,
             Menu<View, Args> menu, Args args) {
            super(inventoryViewer, previousMenuView, menu);
            this.sortingType = args.sortingType;
        }

        public SortingType getSortingType() {
            return sortingType;
        }

        public boolean setSortingType(SortingType sortingType) {
            this.sortingType = sortingType;
            this.updatePagedObjects();
            return this.alreadySorted.add(sortingType);
        }

        @Override
        protected List<Island> requestObjects() {
            return plugin.getGrid().getIslands(sortingType);
        }

    }

    private static boolean convertOldGUI(SuperiorSkyblockPlugin plugin, YamlConfiguration newMenu) {
        File oldFile = new File(plugin.getDataFolder(), "guis/top-islands.yml");

        if (!oldFile.exists())
            return false;

        //We want to reset the items of newMenu.
        ConfigurationSection itemsSection = newMenu.createSection("items");
        ConfigurationSection soundsSection = newMenu.createSection("sounds");
        ConfigurationSection commandsSection = newMenu.createSection("commands");

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(oldFile);

        newMenu.set("title", cfg.getString("top-islands.title"));

        int size = cfg.getInt("top-islands.size");

        char[] patternChars = new char[size * 9];
        Arrays.fill(patternChars, '\n');

        int charCounter = 0;

        if (cfg.contains("top-islands.fill-items")) {
            charCounter = MenuConverter.convertFillItems(cfg.getConfigurationSection("top-islands.fill-items"),
                    charCounter, patternChars, itemsSection, commandsSection, soundsSection);
        }

        char slotsChar = AbstractMenuLayout.BUTTON_SYMBOLS[charCounter++];
        char worthChar = AbstractMenuLayout.BUTTON_SYMBOLS[charCounter++];
        char levelChar = AbstractMenuLayout.BUTTON_SYMBOLS[charCounter++];
        char ratingChar = AbstractMenuLayout.BUTTON_SYMBOLS[charCounter++];
        char playersChar = AbstractMenuLayout.BUTTON_SYMBOLS[charCounter++];
        char playerIslandChar = AbstractMenuLayout.BUTTON_SYMBOLS[charCounter++];

        for (String slot : cfg.getString("top-islands.slots").split(","))
            patternChars[Integer.parseInt(slot)] = slotsChar;

        ConfigurationSection islandItemSection = cfg.getConfigurationSection("top-islands.island-item");
        newMenu.set("items." + slotsChar + ".island", islandItemSection);
        newMenu.set("sounds." + slotsChar + ".island", islandItemSection.getConfigurationSection("sound"));
        islandItemSection.set("sound", null);

        ConfigurationSection noIslandItemSection = cfg.getConfigurationSection("top-islands.no-island-item");
        newMenu.set("items." + slotsChar + ".no-island", noIslandItemSection);
        newMenu.set("sounds." + slotsChar + ".no-island", noIslandItemSection.getConfigurationSection("sound"));
        noIslandItemSection.set("sound", null);

        if (cfg.contains("top-islands.worth-sort")) {
            MenuConverter.convertItem(cfg.getConfigurationSection("top-islands.worth-sort"), patternChars, worthChar,
                    itemsSection, commandsSection, soundsSection);
        }
        if (cfg.contains("top-islands.level-sort")) {
            MenuConverter.convertItem(cfg.getConfigurationSection("top-islands.level-sort"), patternChars, levelChar,
                    itemsSection, commandsSection, soundsSection);
        }
        if (cfg.contains("top-islands.rating-sort")) {
            MenuConverter.convertItem(cfg.getConfigurationSection("top-islands.rating-sort"), patternChars, ratingChar,
                    itemsSection, commandsSection, soundsSection);
        }
        if (cfg.contains("top-islands.players-sort")) {
            MenuConverter.convertItem(cfg.getConfigurationSection("top-islands.players-sort"), patternChars, playersChar,
                    itemsSection, commandsSection, soundsSection);
        }

        if (cfg.contains("player-island-slot"))
            patternChars[cfg.getInt("player-island-slot")] = playerIslandChar;

        newMenu.set("worth-sort", worthChar);
        newMenu.set("level-sort", levelChar);
        newMenu.set("rating-sort", ratingChar);
        newMenu.set("players-sort", playersChar);
        newMenu.set("player-island", playerIslandChar);
        newMenu.set("sort-glow-when-selected", false);

        char invalidChar = AbstractMenuLayout.BUTTON_SYMBOLS[charCounter++];

        newMenu.set("slots", slotsChar);
        newMenu.set("previous-page", invalidChar);
        newMenu.set("current-page", invalidChar);
        newMenu.set("next-page", invalidChar);

        newMenu.set("pattern", MenuConverter.buildPattern(size, patternChars,
                AbstractMenuLayout.BUTTON_SYMBOLS[charCounter]));

        return true;
    }

}
