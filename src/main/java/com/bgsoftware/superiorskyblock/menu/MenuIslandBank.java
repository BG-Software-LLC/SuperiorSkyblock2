package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.bank.BankTransaction;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.chat.PlayerChat;
import com.bgsoftware.superiorskyblock.utils.islands.IslandPrivileges;
import com.bgsoftware.superiorskyblock.utils.registry.Registry;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;

public final class MenuIslandBank extends SuperiorMenu {

    private static List<Integer> logsSlot;

    private final Island island;

    private MenuIslandBank(SuperiorPlayer superiorPlayer, Island island){
        super("menuIslandBank", superiorPlayer);
        this.island = island;
    }

    @Override
    protected void onPlayerClick(InventoryClickEvent e) {
        if (logsSlot.contains(e.getRawSlot())) {
            previousMove = false;
            MenuBankLogs.openInventory(superiorPlayer, this, island);
        }

        else if(containsData(e.getRawSlot() + "-withdraw")){
            Object withdrawValue = getData(e.getRawSlot() + "-withdraw");
            List<String> commandsToExecute = null;
            BigDecimal amount = island.getIslandBank().getBalance();

            if(withdrawValue instanceof Double){
                if((double) withdrawValue <= 0){
                    int withdrawSlot = e.getRawSlot();
                    previousMove = false;
                    e.getWhoClicked().closeInventory();
                    Locale.BANK_WITHDRAW_CUSTOM.send(superiorPlayer);

                    PlayerChat.listen((Player) e.getWhoClicked(), message -> {
                        try{
                            BigDecimal newAmount = BigDecimal.valueOf(Double.parseDouble(message));
                            BankTransaction bankTransaction = island.getIslandBank().withdrawMoney(superiorPlayer, newAmount, null);
                            handleWithdraw(superiorPlayer, island, this, bankTransaction, withdrawSlot, newAmount);
                        }catch (IllegalArgumentException ex){
                            Locale.INVALID_AMOUNT.send(superiorPlayer, message);
                        }

                        MenuIslandBank.openInventory(superiorPlayer, null, superiorPlayer.getIsland());
                        PlayerChat.remove((Player) e.getWhoClicked());

                        return true;
                    });

                    return;
                }
                amount = amount.multiply(BigDecimal.valueOf(((double) withdrawValue) / 100D));
            }
            else{
                //noinspection all
                commandsToExecute = (List<String>) withdrawValue;
            }

            BankTransaction bankTransaction = island.getIslandBank().withdrawMoney(superiorPlayer, amount, commandsToExecute);
            handleWithdraw(superiorPlayer, island, this, bankTransaction, e.getRawSlot(), amount);
        }

        else if(containsData(e.getRawSlot() + "-deposit")){
            double depositPercentage = (Double) getData(e.getRawSlot() + "-deposit");
            if(depositPercentage <= 0){
                int depositSlot = e.getRawSlot();
                previousMove = false;
                e.getWhoClicked().closeInventory();
                Locale.BANK_DEPOSIT_CUSTOM.send(superiorPlayer);

                PlayerChat.listen((Player) e.getWhoClicked(), message -> {
                    try{
                        BigDecimal newAmount = BigDecimal.valueOf(Double.parseDouble(message));
                        BankTransaction bankTransaction = island.getIslandBank().depositMoney(superiorPlayer, newAmount);
                        handleDeposit(superiorPlayer, island, this, bankTransaction, depositSlot, newAmount);
                    }catch(IllegalArgumentException ex){
                        Locale.INVALID_AMOUNT.send(superiorPlayer, message);
                    }

                    MenuIslandBank.openInventory(superiorPlayer, null, superiorPlayer.getIsland());
                    PlayerChat.remove((Player) e.getWhoClicked());

                    return true;
                });

            }
            else {
                BigDecimal amount = plugin.getProviders().getBalanceForBanks(superiorPlayer).multiply(BigDecimal.valueOf(depositPercentage / 100D));
                BankTransaction bankTransaction = island.getIslandBank().depositMoney(superiorPlayer, amount);
                handleDeposit(superiorPlayer, island, this, bankTransaction, e.getRawSlot(), amount);
            }
        }
    }

    @Override
    protected void cloneAndOpen(SuperiorMenu previousMenu) {
        openInventory(superiorPlayer, previousMenu, island);
    }

    public static void init(){
        MenuIslandBank menuIslandBank = new MenuIslandBank(null, null);

        File file = new File(plugin.getDataFolder(), "menus/island-bank.yml");

        if(!file.exists())
            FileUtils.saveResource("menus/island-bank.yml");

        CommentedConfiguration cfg = CommentedConfiguration.loadConfiguration(file);

        Registry<Character, List<Integer>> charSlots = FileUtils.loadGUI(menuIslandBank, "island-bank.yml", cfg);

        logsSlot = getSlots(cfg, "logs", charSlots);

        for(String itemChar : cfg.getConfigurationSection("items").getKeys(false)){
            if(cfg.contains("items." + itemChar + ".bank-action")){
                List<Integer> slots = charSlots.get(itemChar.toCharArray()[0]);

                if(slots == null){
                    SuperiorSkyblockPlugin.log("&cThe item '" + itemChar.toCharArray()[0] + "' in island bank has no slots, skipping...");
                    continue;
                }

                SoundWrapper successSound = FileUtils.getSound(cfg.getConfigurationSection("sounds." + itemChar + ".success-sound"));
                SoundWrapper failSound = FileUtils.getSound(cfg.getConfigurationSection("sounds." + itemChar + ".fail-sound"));

                if(cfg.isDouble("items." + itemChar + ".bank-action.withdraw")){
                    double withdrawPercentage = cfg.getDouble("items." + itemChar + ".bank-action.withdraw");
                    slots.forEach(i -> {
                        menuIslandBank.addData(i + "-withdraw", withdrawPercentage);
                        menuIslandBank.addData(i + "-success-sound", successSound);
                        menuIslandBank.addData(i + "-fail-sound", failSound);
                    });
                }

                else if(cfg.isList("items." + itemChar + ".bank-action.withdraw")){
                    List<String> withdrawCommands = cfg.getStringList("items." + itemChar + ".bank-action.withdraw");
                    slots.forEach(i -> {
                        menuIslandBank.addData(i + "-withdraw", withdrawCommands);
                        menuIslandBank.addData(i + "-success-sound", successSound);
                        menuIslandBank.addData(i + "-fail-sound", failSound);
                    });
                }

                else if(cfg.contains("items." + itemChar + ".bank-action.deposit")){
                    double depositPercentage = cfg.getDouble("items." + itemChar + ".bank-action.deposit");
                    slots.forEach(i -> {
                        menuIslandBank.addData(i + "-deposit", depositPercentage);
                        menuIslandBank.addData(i + "-success-sound", successSound);
                        menuIslandBank.addData(i + "-fail-sound", failSound);
                    });
                }
            }
        }

        charSlots.delete();

        menuIslandBank.markCompleted();
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, SuperiorMenu previousMenu, Island island){
        new MenuIslandBank(superiorPlayer, island).open(previousMenu);
    }

    public static void refreshMenus(Island island){
        SuperiorMenu.refreshMenus(MenuIslandBank.class, superiorMenu -> superiorMenu.island.equals(island));
    }

    public static void handleDeposit(SuperiorPlayer superiorPlayer, Island island, MenuIslandBank menuIslandBank, BankTransaction bankTransaction, int clickedSlot, BigDecimal amount){
        if(bankTransaction.getFailureReason().isEmpty()){
            if(menuIslandBank != null) {
                SoundWrapper successSound = (SoundWrapper) menuIslandBank.getData(clickedSlot + "-success-sound");
                if (successSound != null)
                    superiorPlayer.runIfOnline(successSound::playSound);
            }
        }
        else{
            if(menuIslandBank != null) {
                SoundWrapper failSound = (SoundWrapper) menuIslandBank.getData(clickedSlot + "-fail-sound");
                if (failSound != null)
                    superiorPlayer.runIfOnline(failSound::playSound);
            }

            String failureReason = bankTransaction.getFailureReason();

            if(!failureReason.isEmpty()) {
                switch (failureReason) {
                    case "No permission":
                        Locale.NO_DEPOSIT_PERMISSION.send(superiorPlayer, island.getRequiredPlayerRole(IslandPrivileges.DEPOSIT_MONEY));
                        break;
                    case "Invalid amount":
                        Locale.INVALID_AMOUNT.send(superiorPlayer, StringUtils.format(amount));
                        break;
                    case "Not enough money":
                        Locale.NOT_ENOUGH_MONEY_TO_DEPOSIT.send(superiorPlayer, StringUtils.format(amount));
                        break;
                    case "Exceed bank limit":
                        Locale.BANK_LIMIT_EXCEED.send(superiorPlayer);
                        break;
                    default:
                        Locale.DEPOSIT_ERROR.send(superiorPlayer, failureReason);
                        break;
                }
            }
        }
    }

    public static void handleWithdraw(SuperiorPlayer superiorPlayer, Island island, MenuIslandBank menuIslandBank, BankTransaction bankTransaction, int clickedSlot, BigDecimal amount){
        if(bankTransaction.getFailureReason().isEmpty()){
            if(menuIslandBank != null) {
                SoundWrapper successSound = (SoundWrapper) menuIslandBank.getData(clickedSlot + "-success-sound");
                if (successSound != null)
                    superiorPlayer.runIfOnline(successSound::playSound);
            }
        }
        else{
            if(menuIslandBank != null) {
                SoundWrapper failSound = (SoundWrapper) menuIslandBank.getData(clickedSlot + "-fail-sound");
                if (failSound != null)
                    superiorPlayer.runIfOnline(failSound::playSound);
            }

            String failureReason = bankTransaction.getFailureReason();

            if(!failureReason.isEmpty()){
                switch (failureReason){
                    case "No permission":
                        Locale.NO_WITHDRAW_PERMISSION.send(superiorPlayer, island.getRequiredPlayerRole(IslandPrivileges.WITHDRAW_MONEY));
                        break;
                    case "Invalid amount":
                        Locale.INVALID_AMOUNT.send(superiorPlayer, StringUtils.format(amount));
                        break;
                    case "Bank is empty":
                        Locale.ISLAND_BANK_EMPTY.send(superiorPlayer);
                        break;
                    default:
                        Locale.WITHDRAW_ERROR.send(superiorPlayer, failureReason);
                        break;
                }
            }
        }
    }

}
