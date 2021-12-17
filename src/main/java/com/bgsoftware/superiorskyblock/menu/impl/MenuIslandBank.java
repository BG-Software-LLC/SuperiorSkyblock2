package com.bgsoftware.superiorskyblock.menu.impl;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.bank.BankTransaction;
import com.bgsoftware.superiorskyblock.api.menu.ISuperiorMenu;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.menu.SuperiorMenu;
import com.bgsoftware.superiorskyblock.menu.button.impl.menu.BankCustomDepositButton;
import com.bgsoftware.superiorskyblock.menu.button.impl.menu.BankCustomWithdrawButton;
import com.bgsoftware.superiorskyblock.menu.button.impl.menu.BankDepositButton;
import com.bgsoftware.superiorskyblock.menu.button.impl.menu.BankWithdrawButton;
import com.bgsoftware.superiorskyblock.menu.button.impl.menu.OpenBankLogsButton;
import com.bgsoftware.superiorskyblock.menu.file.MenuPatternSlots;
import com.bgsoftware.superiorskyblock.menu.pattern.impl.RegularMenuPattern;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.islands.IslandPrivileges;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;

import java.math.BigDecimal;
import java.util.List;

public final class MenuIslandBank extends SuperiorMenu<MenuIslandBank> {

    private static RegularMenuPattern<MenuIslandBank> menuPattern;

    private final Island island;

    private MenuIslandBank(SuperiorPlayer superiorPlayer, Island island) {
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

    public static void init() {
        menuPattern = null;

        RegularMenuPattern.Builder<MenuIslandBank> patternBuilder = new RegularMenuPattern.Builder<>();

        Pair<MenuPatternSlots, CommentedConfiguration> menuLoadResult = FileUtils.loadMenu(patternBuilder,
                "island-bank.yml", null);

        if (menuLoadResult == null)
            return;

        MenuPatternSlots menuPatternSlots = menuLoadResult.getKey();
        CommentedConfiguration cfg = menuLoadResult.getValue();

        if(cfg.isConfigurationSection("items")) {
            for (String itemChar : cfg.getConfigurationSection("items").getKeys(false)) {
                if (cfg.contains("items." + itemChar + ".bank-action")) {
                    List<Integer> slots = menuPatternSlots.getSlots(itemChar);

                    if (slots.isEmpty()) {
                        continue;
                    }

                    SoundWrapper successSound = FileUtils.getSound(cfg.getConfigurationSection("sounds." + itemChar + ".success-sound"));
                    SoundWrapper failSound = FileUtils.getSound(cfg.getConfigurationSection("sounds." + itemChar + ".fail-sound"));

                    if (cfg.isDouble("items." + itemChar + ".bank-action.withdraw")) {
                        double withdrawPercentage = cfg.getDouble("items." + itemChar + ".bank-action.withdraw");
                        if (withdrawPercentage <= 0) {
                            patternBuilder.mapButtons(slots, new BankCustomWithdrawButton.Builder()
                                    .setFailSound(failSound).setSuccessSound(successSound));
                        } else {
                            patternBuilder.mapButtons(slots, new BankWithdrawButton.Builder(withdrawPercentage)
                                    .setFailSound(failSound).setSuccessSound(successSound));
                        }
                    } else if (cfg.isList("items." + itemChar + ".bank-action.withdraw")) {
                        List<String> withdrawCommands = cfg.getStringList("items." + itemChar + ".bank-action.withdraw");
                        patternBuilder.mapButtons(slots, new BankWithdrawButton.Builder(withdrawCommands)
                                .setFailSound(failSound).setSuccessSound(successSound));
                    } else if (cfg.contains("items." + itemChar + ".bank-action.deposit")) {
                        double depositPercentage = cfg.getDouble("items." + itemChar + ".bank-action.deposit");
                        if (depositPercentage <= 0) {
                            patternBuilder.mapButtons(slots, new BankCustomDepositButton.Builder()
                                    .setFailSound(failSound).setSuccessSound(successSound));
                        } else {
                            patternBuilder.mapButtons(slots, new BankDepositButton.Builder(depositPercentage)
                                    .setFailSound(failSound).setSuccessSound(successSound));
                        }
                    }
                }
            }
        }

        menuPattern = patternBuilder
                .mapButtons(getSlots(cfg, "logs", menuPatternSlots), new OpenBankLogsButton.Builder())
                .build();
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, ISuperiorMenu previousMenu, Island island) {
        new MenuIslandBank(superiorPlayer, island).open(previousMenu);
    }

    public static void refreshMenus(Island island) {
        SuperiorMenu.refreshMenus(MenuIslandBank.class, superiorMenu -> superiorMenu.island.equals(island));
    }

    public static void handleDeposit(SuperiorPlayer superiorPlayer, Island island, MenuIslandBank menuIslandBank,
                                     BankTransaction bankTransaction, SoundWrapper successSound,
                                     SoundWrapper failSound, BigDecimal amount) {
        if (bankTransaction.getFailureReason().isEmpty()) {
            if (menuIslandBank != null) {
                if (successSound != null)
                    superiorPlayer.runIfOnline(successSound::playSound);
            }
        } else {
            if (menuIslandBank != null) {
                if (failSound != null)
                    superiorPlayer.runIfOnline(failSound::playSound);
            }

            String failureReason = bankTransaction.getFailureReason();

            if (!failureReason.isEmpty()) {
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

    public static void handleWithdraw(SuperiorPlayer superiorPlayer, Island island, MenuIslandBank menuIslandBank,
                                      BankTransaction bankTransaction, SoundWrapper successSound,
                                      SoundWrapper failSound, BigDecimal amount) {
        if (bankTransaction.getFailureReason().isEmpty()) {
            if (menuIslandBank != null) {
                if (successSound != null)
                    superiorPlayer.runIfOnline(successSound::playSound);
            }
        } else {
            if (menuIslandBank != null) {
                if (failSound != null)
                    superiorPlayer.runIfOnline(failSound::playSound);
            }

            String failureReason = bankTransaction.getFailureReason();

            if (!failureReason.isEmpty()) {
                switch (failureReason) {
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
