package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.bank.BankTransaction;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.utils.menus.MenuConverter;
import com.bgsoftware.superiorskyblock.utils.registry.Registry;
import com.bgsoftware.superiorskyblock.wrappers.player.SSuperiorPlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public final class MenuBankLogs extends PagedSuperiorMenu<BankTransaction> {

    private static final UUID CONSOLE_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    private final Island island;

    private UUID filteredPlayer;
    private Comparator<BankTransaction> sorting;

    private MenuBankLogs(SuperiorPlayer superiorPlayer, Island island){
        super("menuBankLogs", superiorPlayer);
        this.island = island;
    }

    @Override
    protected void onPlayerClick(InventoryClickEvent event, BankTransaction transaction) {
        if(event.getAction().name().contains("RIGHT")){
            filteredPlayer = transaction.getPlayer() == null ? CONSOLE_UUID : transaction.getPlayer();
        }
    }

    @Override
    protected void cloneAndOpen(SuperiorMenu previousMenu) {
        openInventory(superiorPlayer, previousMenu, island);
    }

    @Override
    protected ItemStack getObjectItem(ItemStack clickedItem, BankTransaction transaction) {
        try {
            return new ItemBuilder(clickedItem)
                    .replaceAll("{0}", transaction.getPosition() + "")
                    .replaceAll("{1}", transaction.getPlayer() ==  null ? "Console" : SSuperiorPlayer.of(transaction.getPlayer()).getName())
                    .replaceAll("{2}", StringUtils.format(transaction.getAction().name()))
                    .replaceAll("{3}", transaction.getDate())
                    .replaceAll("{4}", transaction.getAmount() + "")
                    .replaceAll("{5}", StringUtils.format(transaction.getAmount()))
                    .replaceAll("{6}", StringUtils.fancyFormat(transaction.getAmount(), superiorPlayer.getUserLocale()))
                    .asSkullOf(superiorPlayer).build(superiorPlayer);
        }catch(Exception ex){
            SuperiorSkyblockPlugin.log("Failed to load menu because of player: " + superiorPlayer.getName());
            throw ex;
        }
    }

    @Override
    protected List<BankTransaction> requestObjects() {
        return filteredPlayer != null ? filteredPlayer.equals(CONSOLE_UUID) ? island.getIslandBank().getConsoleTransactions() :
                island.getIslandBank().getTransactions(SSuperiorPlayer.of(filteredPlayer)) :
                sorting == null ? island.getIslandBank().getAllTransactions() :
                island.getIslandBank().getAllTransactions().stream().sorted(sorting).collect(Collectors.toList());
    }

    public static void init(){
        MenuBankLogs menuMembers = new MenuBankLogs(null, null);

        File file = new File(plugin.getDataFolder(), "menus/bank-logs.yml");

        if(!file.exists())
            FileUtils.saveResource("menus/bank-logs.yml");

        CommentedConfiguration cfg = CommentedConfiguration.loadConfiguration(file);

        if(convertOldGUI(cfg)){
            cfg.save(file);
        }

        Registry<Character, List<Integer>> charSlots = FileUtils.loadGUI(menuMembers, "bank-logs.yml", cfg);

        menuMembers.setPreviousSlot(getSlots(cfg, "previous-page", charSlots));
        menuMembers.setCurrentSlot(getSlots(cfg, "current-page", charSlots));
        menuMembers.setNextSlot(getSlots(cfg, "next-page", charSlots));
        menuMembers.setSlots(getSlots(cfg, "slots", charSlots));

        charSlots.delete();

        menuMembers.markCompleted();
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, SuperiorMenu previousMenu, Island island){
        new MenuBankLogs(superiorPlayer, island).open(previousMenu);
    }

    public static void refreshMenus(){
        SuperiorMenu.refreshMenus(MenuBankLogs.class);
    }

    private static boolean convertOldGUI(YamlConfiguration newMenu){
        File oldFile = new File(plugin.getDataFolder(), "guis/panel-gui.yml");

        if(!oldFile.exists())
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

        if(cfg.contains("members-panel.fill-items")) {
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
