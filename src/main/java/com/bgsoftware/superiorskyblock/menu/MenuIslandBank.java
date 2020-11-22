package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.bank.BankTransaction;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.chat.PlayerChat;
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
                            BankTransaction bankTransaction = superiorPlayer.getIsland().getIslandBank()
                                    .withdrawMoney(superiorPlayer, newAmount, null);
                            MenuIslandBank.handleTransaction(superiorPlayer, bankTransaction, withdrawSlot);
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
            handleTransaction(superiorPlayer, bankTransaction, e.getRawSlot());
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
                        BankTransaction bankTransaction = superiorPlayer.getIsland().getIslandBank().depositMoney(superiorPlayer, newAmount);
                        MenuIslandBank.handleTransaction(superiorPlayer, bankTransaction, depositSlot);
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
                handleTransaction(superiorPlayer, bankTransaction, e.getRawSlot());
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

    public static void handleTransaction(SuperiorPlayer superiorPlayer, BankTransaction bankTransaction, int clickedSlot){
        MenuIslandBank menu = new MenuIslandBank(null, null);

        if(bankTransaction.getFailureReason().isEmpty()){
            SoundWrapper successSound = (SoundWrapper) menu.getData(clickedSlot + "-success-sound");
            if(successSound != null)
                successSound.playSound(superiorPlayer.asPlayer());
        }else{
            SoundWrapper failSound = (SoundWrapper) menu.getData(clickedSlot + "-fail-sound");
            if(failSound != null)
                failSound.playSound(superiorPlayer.asPlayer());
        }
    }

}
