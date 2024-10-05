package com.bgsoftware.superiorskyblock.island.bank;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.bank.BankTransaction;
import com.bgsoftware.superiorskyblock.api.world.GameSound;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.GameSoundImpl;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.island.privilege.IslandPrivileges;

import java.math.BigDecimal;

public class BankManager {

    public static void handleDeposit(SuperiorPlayer superiorPlayer, Island island, BankTransaction bankTransaction,
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

    public static void handleWithdraw(SuperiorPlayer superiorPlayer, Island island, BankTransaction bankTransaction,
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

}
