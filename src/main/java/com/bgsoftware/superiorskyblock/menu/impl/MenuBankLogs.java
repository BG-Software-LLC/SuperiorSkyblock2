package com.bgsoftware.superiorskyblock.menu.impl;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.enums.BankAction;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.bank.BankTransaction;
import com.bgsoftware.superiorskyblock.api.menu.ISuperiorMenu;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.menu.PagedSuperiorMenu;
import com.bgsoftware.superiorskyblock.menu.SuperiorMenu;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.menu.converter.MenuConverter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class MenuBankLogs extends PagedSuperiorMenu<BankTransaction> {

    private static final UUID CONSOLE_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");
    private static List<Integer> timeSortSlots, moneySortSlots;

    private final Island island;

    private UUID filteredPlayer;
    private Comparator<BankTransaction> sorting;

    private MenuBankLogs(SuperiorPlayer superiorPlayer, Island island) {
        super("menuBankLogs", superiorPlayer, true);
        this.island = island;
    }

    @Override
    protected void onPlayerClick(InventoryClickEvent e, BankTransaction transaction) {
        boolean reopenMenu = false;

        if (transaction == null) {
            if (timeSortSlots.contains(e.getRawSlot())) {
                sorting = Comparator.comparingLong(BankTransaction::getTime);
                reopenMenu = true;
            } else if (moneySortSlots.contains(e.getRawSlot())) {
                sorting = (o1, o2) -> o2.getAmount().compareTo(o1.getAmount());
                reopenMenu = true;
            }
        } else if (e.getClick().name().contains("RIGHT")) {
            filteredPlayer = transaction.getPlayer() == null ? CONSOLE_UUID : transaction.getPlayer();
            reopenMenu = true;
        }

        if (reopenMenu) {
            previousMove = false;
            open(previousMenu);
        }
    }

    @Override
    public void cloneAndOpen(ISuperiorMenu previousMenu) {
        openInventory(superiorPlayer, previousMenu, island);
    }

    @Override
    protected ItemStack getObjectItem(ItemStack clickedItem, BankTransaction transaction) {
        try {
            return new ItemBuilder(clickedItem)
                    .replaceAll("{0}", transaction.getPosition() + "")
                    .replaceAll("{1}", getFilteredPlayerName(transaction.getPlayer() == null ? CONSOLE_UUID : transaction.getPlayer()))
                    .replaceAll("{2}", (transaction.getAction() == BankAction.WITHDRAW_COMPLETED ?
                            Locale.BANK_WITHDRAW_COMPLETED : Locale.BANK_DEPOSIT_COMPLETED).getMessage(superiorPlayer.getUserLocale()))
                    .replaceAll("{3}", transaction.getDate())
                    .replaceAll("{4}", transaction.getAmount() + "")
                    .replaceAll("{5}", StringUtils.format(transaction.getAmount()))
                    .replaceAll("{6}", StringUtils.fancyFormat(transaction.getAmount(), superiorPlayer.getUserLocale()))
                    .asSkullOf(superiorPlayer).build(superiorPlayer);
        } catch (Exception ex) {
            SuperiorSkyblockPlugin.log("Failed to load menu because of player: " + superiorPlayer.getName());
            throw ex;
        }
    }

    @Override
    protected Inventory buildInventory(Function<String, String> titleReplacer) {
        return super.buildInventory(title -> title.replace("{0}", getFilteredPlayerName(filteredPlayer)));
    }

    @Override
    protected List<BankTransaction> requestObjects() {
        List<BankTransaction> transactions = getTransactions();

        if (sorting == null) {
            return transactions;
        }

        return transactions.stream().sorted(sorting).collect(Collectors.toList());
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
        MenuBankLogs menuMembers = new MenuBankLogs(null, null);

        File file = new File(plugin.getDataFolder(), "menus/bank-logs.yml");

        if (!file.exists())
            FileUtils.saveResource("menus/bank-logs.yml");

        CommentedConfiguration cfg = CommentedConfiguration.loadConfiguration(file);

        if (convertOldGUI(cfg)) {
            try {
                cfg.save(file);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        Map<Character, List<Integer>> charSlots = FileUtils.loadGUI(menuMembers, "bank-logs.yml", cfg);

        menuMembers.setPreviousSlot(getSlots(cfg, "previous-page", charSlots));
        menuMembers.setCurrentSlot(getSlots(cfg, "current-page", charSlots));
        menuMembers.setNextSlot(getSlots(cfg, "next-page", charSlots));
        menuMembers.setSlots(getSlots(cfg, "slots", charSlots));

        timeSortSlots = getSlots(cfg, "time-sort", charSlots);
        moneySortSlots = getSlots(cfg, "money-sort", charSlots);

        menuMembers.markCompleted();
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, ISuperiorMenu previousMenu, Island island) {
        new MenuBankLogs(superiorPlayer, island).open(previousMenu);
    }

    public static void refreshMenus(Island island) {
        SuperiorMenu.refreshMenus(MenuBankLogs.class, superiorMenu -> superiorMenu.island.equals(island));
    }

    private static boolean convertOldGUI(YamlConfiguration newMenu) {
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

        char slotsChar = itemChars[charCounter++];

        MenuConverter.convertPagedButtons(cfg.getConfigurationSection("members-panel"),
                cfg.getConfigurationSection("members-panel.member-item"), newMenu, patternChars,
                slotsChar, itemChars[charCounter++], itemChars[charCounter++], itemChars[charCounter++],
                itemsSection, commandsSection, soundsSection);

        newMenu.set("pattern", MenuConverter.buildPattern(size, patternChars, itemChars[charCounter]));

        return true;
    }

}
