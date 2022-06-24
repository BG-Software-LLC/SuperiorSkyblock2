package com.bgsoftware.superiorskyblock.core.menu.impl;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.bank.BankTransaction;
import com.bgsoftware.superiorskyblock.api.menu.ISuperiorMenu;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.io.MenuParser;
import com.bgsoftware.superiorskyblock.core.menu.MenuParseResult;
import com.bgsoftware.superiorskyblock.core.menu.MenuPatternSlots;
import com.bgsoftware.superiorskyblock.core.menu.PagedSuperiorMenu;
import com.bgsoftware.superiorskyblock.core.menu.SuperiorMenu;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.menu.BankLogsPagedObjectButton;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.menu.BankLogsSortButton;
import com.bgsoftware.superiorskyblock.core.menu.converter.MenuConverter;
import com.bgsoftware.superiorskyblock.core.menu.pattern.SuperiorMenuPattern;
import com.bgsoftware.superiorskyblock.core.menu.pattern.impl.PagedMenuPattern;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class MenuBankLogs extends PagedSuperiorMenu<MenuBankLogs, BankTransaction> {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private static final UUID CONSOLE_UUID = new UUID(0, 0);
    private static PagedMenuPattern<MenuBankLogs, BankTransaction> menuPattern;

    private final Island island;

    private UUID filteredPlayer;
    private Comparator<BankTransaction> sorting;

    private MenuBankLogs(SuperiorPlayer superiorPlayer, Island island) {
        super(menuPattern, superiorPlayer);
        this.island = island;
    }

    public void setSorting(Comparator<BankTransaction> sorting) {
        this.sorting = sorting;
    }

    public void setFilteredPlayer(UUID filteredPlayer) {
        this.filteredPlayer = filteredPlayer == null ? CONSOLE_UUID : filteredPlayer;
    }

    @Override
    public void cloneAndOpen(ISuperiorMenu previousMenu) {
        openInventory(inventoryViewer, previousMenu, island);
    }

    @Override
    protected String replaceTitle(String title) {
        return title.replace("{0}", getFilteredPlayerName(filteredPlayer));
    }

    @Override
    protected List<BankTransaction> requestObjects() {
        List<BankTransaction> transactions = getTransactions();

        if (sorting == null) {
            return transactions;
        }

        transactions.sort(sorting);

        return transactions;
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

    public static void init() {
        menuPattern = null;

        PagedMenuPattern.Builder<MenuBankLogs, BankTransaction> patternBuilder = new PagedMenuPattern.Builder<>();

        MenuParseResult menuLoadResult = MenuParser.loadMenu(patternBuilder, "bank-logs.yml", MenuBankLogs::convertOldGUI);

        if (menuLoadResult == null)
            return;

        MenuPatternSlots menuPatternSlots = menuLoadResult.getPatternSlots();
        CommentedConfiguration cfg = menuLoadResult.getConfig();

        menuPattern = patternBuilder
                .setPreviousPageSlots(getSlots(cfg, "previous-page", menuPatternSlots))
                .setCurrentPageSlots(getSlots(cfg, "current-page", menuPatternSlots))
                .setNextPageSlots(getSlots(cfg, "next-page", menuPatternSlots))
                .setPagedObjectSlots(getSlots(cfg, "slots", menuPatternSlots), new BankLogsPagedObjectButton.Builder())
                .mapButtons(getSlots(cfg, "time-sort", menuPatternSlots), new BankLogsSortButton.Builder()
                        .setSortType(BankLogsSortButton.SortType.TIME))
                .mapButtons(getSlots(cfg, "money-sort", menuPatternSlots), new BankLogsSortButton.Builder()
                        .setSortType(BankLogsSortButton.SortType.MONEY))
                .build();
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, ISuperiorMenu previousMenu, Island island) {
        new MenuBankLogs(superiorPlayer, island).open(previousMenu);
    }

    public static void refreshMenus(Island island) {
        SuperiorMenu.refreshMenus(MenuBankLogs.class, superiorMenu -> superiorMenu.island.equals(island));
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

        char slotsChar = SuperiorMenuPattern.BUTTON_SYMBOLS[charCounter++];

        MenuConverter.convertPagedButtons(cfg.getConfigurationSection("members-panel"),
                cfg.getConfigurationSection("members-panel.member-item"), newMenu, patternChars,
                slotsChar, SuperiorMenuPattern.BUTTON_SYMBOLS[charCounter++],
                SuperiorMenuPattern.BUTTON_SYMBOLS[charCounter++],
                SuperiorMenuPattern.BUTTON_SYMBOLS[charCounter++],
                itemsSection, commandsSection, soundsSection);

        newMenu.set("pattern", MenuConverter.buildPattern(size, patternChars,
                SuperiorMenuPattern.BUTTON_SYMBOLS[charCounter]));

        return true;
    }

}
