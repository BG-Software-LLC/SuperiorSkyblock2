package com.bgsoftware.superiorskyblock.core.menu.impl;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.bank.BankTransaction;
import com.bgsoftware.superiorskyblock.api.menu.Menu;
import com.bgsoftware.superiorskyblock.api.menu.layout.MenuLayout;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.io.MenuParserImpl;
import com.bgsoftware.superiorskyblock.core.menu.AbstractPagedMenu;
import com.bgsoftware.superiorskyblock.core.menu.MenuIdentifiers;
import com.bgsoftware.superiorskyblock.core.menu.MenuParseResult;
import com.bgsoftware.superiorskyblock.core.menu.MenuPatternSlots;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.BankLogsPagedObjectButton;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.BankLogsSortButton;
import com.bgsoftware.superiorskyblock.core.menu.converter.MenuConverter;
import com.bgsoftware.superiorskyblock.core.menu.layout.AbstractMenuLayout;
import com.bgsoftware.superiorskyblock.core.menu.view.AbstractPagedMenuView;
import com.bgsoftware.superiorskyblock.core.menu.view.args.IslandViewArgs;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class MenuBankLogs extends AbstractPagedMenu<MenuBankLogs.View, IslandViewArgs, BankTransaction> {

    private static final UUID CONSOLE_UUID = new UUID(0, 0);

    private MenuBankLogs(MenuParseResult<View> parseResult) {
        super(MenuIdentifiers.MENU_BANK_LOGS, parseResult, false);
    }

    @Override
    protected View createViewInternal(SuperiorPlayer superiorPlayer, IslandViewArgs args,
                                      @Nullable MenuView<?, ?> previousMenuView) {
        return new View(superiorPlayer, previousMenuView, this, args);
    }

    public void refreshViews(Island island) {
        this.refreshViews(view -> Objects.equals(view.island, island));
    }

    @Nullable
    public static MenuBankLogs createInstance() {
        MenuParseResult<View> menuParseResult = MenuParserImpl.getInstance().loadMenu("bank-logs.yml",
                MenuBankLogs::convertOldGUI, new BankLogsPagedObjectButton.Builder());

        if (menuParseResult == null)
            return null;

        MenuPatternSlots menuPatternSlots = menuParseResult.getPatternSlots();
        YamlConfiguration cfg = menuParseResult.getConfig();
        MenuLayout.Builder<View> patternBuilder = menuParseResult.getLayoutBuilder();

        patternBuilder.mapButtons(MenuParserImpl.getInstance().parseButtonSlots(cfg, "time-sort", menuPatternSlots),
                new BankLogsSortButton.Builder().setSortType(BankLogsSortButton.SortType.TIME));
        patternBuilder.mapButtons(MenuParserImpl.getInstance().parseButtonSlots(cfg, "money-sort", menuPatternSlots),
                new BankLogsSortButton.Builder().setSortType(BankLogsSortButton.SortType.MONEY));

        return new MenuBankLogs(menuParseResult);
    }

    public static class View extends AbstractPagedMenuView<View, IslandViewArgs, BankTransaction> {

        private final Island island;

        private Comparator<BankTransaction> sorting;
        private UUID filteredPlayer;

        View(SuperiorPlayer inventoryViewer, @Nullable MenuView<?, ?> previousMenuView,
             Menu<View, IslandViewArgs> menu, IslandViewArgs args) {
            super(inventoryViewer, previousMenuView, menu);
            this.island = args.getIsland();
        }

        public void setSorting(Comparator<BankTransaction> sorting) {
            this.sorting = sorting;
        }

        public void setFilteredPlayer(UUID filteredPlayer) {
            this.filteredPlayer = filteredPlayer == null ? CONSOLE_UUID : filteredPlayer;
        }

        @Override
        public String replaceTitle(String title) {
            return title.replace("{0}", getFilteredPlayerName(filteredPlayer));
        }

        @Override
        protected List<BankTransaction> requestObjects() {
            List<BankTransaction> transactions = getTransactions();

            if (sorting == null) {
                return transactions;
            }

            transactions = new LinkedList<>(transactions);

            transactions.sort(sorting);

            return Collections.unmodifiableList(transactions);
        }

        private List<BankTransaction> getTransactions() {
            if (filteredPlayer == null) {
                return island.getIslandBank().getAllTransactions();
            } else if (filteredPlayer.equals(CONSOLE_UUID)) {
                return island.getIslandBank().getConsoleTransactions();
            } else {
                return island.getIslandBank().getTransactions(plugin.getPlayers().getSuperiorPlayer(filteredPlayer));
            }
        }

        private static String getFilteredPlayerName(UUID filteredPlayer) {
            if (filteredPlayer == null) {
                return "";
            } else if (filteredPlayer.equals(CONSOLE_UUID)) {
                return "Console";
            } else {
                return plugin.getPlayers().getSuperiorPlayer(filteredPlayer).getName();
            }
        }

    }

    private static boolean convertOldGUI(SuperiorSkyblockPlugin plugin, YamlConfiguration newMenu) {
        File oldFile = new File(plugin.getDataFolder(), "guis/panel-gui.yml");

        if (!oldFile.exists())
            return false;

        //We want to reset the items of newMenu.
        ConfigurationSection itemsSection = newMenu.createSection("items");
        ConfigurationSection soundsSection = newMenu.createSection("sounds");
        ConfigurationSection commandsSection = newMenu.createSection("commands");

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(oldFile);

        newMenu.set("title", cfg.getString("members-panel.title"));

        int size = cfg.getInt("members-panel.size");

        char[] patternChars = new char[size * 9];
        Arrays.fill(patternChars, '\n');

        int charCounter = 0;

        if (cfg.contains("members-panel.fill-items")) {
            charCounter = MenuConverter.convertFillItems(cfg.getConfigurationSection("members-panel.fill-items"),
                    charCounter, patternChars, itemsSection, commandsSection, soundsSection);
        }

        char slotsChar = AbstractMenuLayout.BUTTON_SYMBOLS[charCounter++];

        MenuConverter.convertPagedButtons(cfg.getConfigurationSection("members-panel"),
                cfg.getConfigurationSection("members-panel.member-item"), newMenu, patternChars,
                slotsChar, AbstractMenuLayout.BUTTON_SYMBOLS[charCounter++],
                AbstractMenuLayout.BUTTON_SYMBOLS[charCounter++],
                AbstractMenuLayout.BUTTON_SYMBOLS[charCounter++],
                itemsSection, commandsSection, soundsSection);

        newMenu.set("pattern", MenuConverter.buildPattern(size, patternChars,
                AbstractMenuLayout.BUTTON_SYMBOLS[charCounter]));

        return true;
    }

}
