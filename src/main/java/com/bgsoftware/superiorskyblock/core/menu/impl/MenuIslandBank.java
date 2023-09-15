package com.bgsoftware.superiorskyblock.core.menu.impl;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.bank.BankTransaction;
import com.bgsoftware.superiorskyblock.api.menu.layout.MenuLayout;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import com.bgsoftware.superiorskyblock.api.world.GameSound;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.GameSoundImpl;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.io.MenuParserImpl;
import com.bgsoftware.superiorskyblock.core.menu.AbstractMenu;
import com.bgsoftware.superiorskyblock.core.menu.MenuIdentifiers;
import com.bgsoftware.superiorskyblock.core.menu.MenuParseResult;
import com.bgsoftware.superiorskyblock.core.menu.MenuPatternSlots;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.BankCustomDepositButton;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.BankCustomWithdrawButton;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.BankDepositButton;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.BankWithdrawButton;
import com.bgsoftware.superiorskyblock.core.menu.button.impl.OpenBankLogsButton;
import com.bgsoftware.superiorskyblock.core.menu.view.IslandMenuView;
import com.bgsoftware.superiorskyblock.core.menu.view.args.IslandViewArgs;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.island.privilege.IslandPrivileges;
import org.bukkit.configuration.file.YamlConfiguration;

import java.math.BigDecimal;
import java.util.List;

public class MenuIslandBank extends AbstractMenu<IslandMenuView, IslandViewArgs> {

    private MenuIslandBank(MenuParseResult<IslandMenuView> parseResult) {
        super(MenuIdentifiers.MENU_ISLAND_BANK, parseResult);
    }

    @Override
    protected IslandMenuView createViewInternal(SuperiorPlayer superiorPlayer, IslandViewArgs args,
                                                @Nullable MenuView<?, ?> previousMenuView) {
        return new IslandMenuView(superiorPlayer, previousMenuView, this, args);
    }

    public void refreshViews(Island island) {
        refreshViews(view -> view.getIsland().equals(island));
    }

    public void handleDeposit(SuperiorPlayer superiorPlayer, Island island, BankTransaction bankTransaction,
                              @Nullable GameSound successSound, @Nullable GameSound failSound, BigDecimal amount) {
        if (bankTransaction.getFailureReason().isEmpty()) {
            superiorPlayer.runIfOnline(player -> GameSoundImpl.playSound(player, successSound));
        } else {
            superiorPlayer.runIfOnline(player -> GameSoundImpl.playSound(player, failSound));

            String failureReason = bankTransaction.getFailureReason();

            if (!failureReason.isEmpty()) {
                switch (failureReason) {
                    case "No permission":
                        Message.NO_DEPOSIT_PERMISSION.send(superiorPlayer, island.getRequiredPlayerRole(IslandPrivileges.DEPOSIT_MONEY));
                        break;
                    case "Invalid amount":
                        Message.INVALID_AMOUNT.send(superiorPlayer, Formatters.NUMBER_FORMATTER.format(amount));
                        break;
                    case "Not enough money":
                        Message.NOT_ENOUGH_MONEY_TO_DEPOSIT.send(superiorPlayer, Formatters.NUMBER_FORMATTER.format(amount));
                        break;
                    case "Exceed bank limit":
                        Message.BANK_LIMIT_EXCEED.send(superiorPlayer);
                        break;
                    default:
                        Message.DEPOSIT_ERROR.send(superiorPlayer, failureReason);
                        break;
                }
            }
        }
    }

    public void handleWithdraw(SuperiorPlayer superiorPlayer, Island island, BankTransaction bankTransaction,
                               GameSound successSound, GameSound failSound, BigDecimal amount) {
        if (bankTransaction.getFailureReason().isEmpty()) {
            superiorPlayer.runIfOnline(player -> GameSoundImpl.playSound(player, successSound));
        } else {
            superiorPlayer.runIfOnline(player -> GameSoundImpl.playSound(player, failSound));

            String failureReason = bankTransaction.getFailureReason();

            if (!failureReason.isEmpty()) {
                switch (failureReason) {
                    case "No permission":
                        Message.NO_WITHDRAW_PERMISSION.send(superiorPlayer, island.getRequiredPlayerRole(IslandPrivileges.WITHDRAW_MONEY));
                        break;
                    case "Invalid amount":
                        Message.INVALID_AMOUNT.send(superiorPlayer, Formatters.NUMBER_FORMATTER.format(amount));
                        break;
                    case "Bank is empty":
                        Message.ISLAND_BANK_EMPTY.send(superiorPlayer);
                        break;
                    default:
                        Message.WITHDRAW_ERROR.send(superiorPlayer, failureReason);
                        break;
                }
            }
        }
    }

    @Nullable
    public static MenuIslandBank createInstance() {
        MenuParseResult<IslandMenuView> menuParseResult = MenuParserImpl.getInstance().loadMenu("island-bank.yml",
                null);

        if (menuParseResult == null) {
            return null;
        }

        MenuPatternSlots menuPatternSlots = menuParseResult.getPatternSlots();
        YamlConfiguration cfg = menuParseResult.getConfig();
        MenuLayout.Builder<IslandMenuView> patternBuilder = menuParseResult.getLayoutBuilder();

        if (cfg.isConfigurationSection("items")) {
            for (String itemChar : cfg.getConfigurationSection("items").getKeys(false)) {
                if (cfg.contains("items." + itemChar + ".bank-action")) {
                    List<Integer> slots = menuPatternSlots.getSlots(itemChar);

                    if (slots.isEmpty()) {
                        continue;
                    }

                    GameSound successSound = MenuParserImpl.getInstance().getSound(cfg.getConfigurationSection("sounds." + itemChar + ".success-sound"));
                    GameSound failSound = MenuParserImpl.getInstance().getSound(cfg.getConfigurationSection("sounds." + itemChar + ".fail-sound"));

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

        patternBuilder.mapButtons(MenuParserImpl.getInstance().parseButtonSlots(cfg, "logs", menuPatternSlots),
                new OpenBankLogsButton.Builder());

        return new MenuIslandBank(menuParseResult);
    }

}
