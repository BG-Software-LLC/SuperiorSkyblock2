package com.bgsoftware.superiorskyblock.island.bank;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.module.BuiltinModules;
import com.bgsoftware.superiorskyblock.player.PlayerLocales;

import java.math.BigDecimal;
import java.util.Locale;

public class BankInterestStatus {

    private BankInterestStatus() {

    }

    public static String getStatus(Island island, SuperiorPlayer superiorPlayer) {
        return getStatus(island, superiorPlayer == null ? null : superiorPlayer.getUserLocale());
    }

    public static String getStatus(Island island, Locale locale) {
        if (locale == null)
            locale = PlayerLocales.getDefaultLocale();

        if (!BuiltinModules.BANK.getConfiguration().isBankInterestEnabled())
            return Message.BANK_INTEREST_STATUS_DISABLED.getMessage(locale);

        if (island.getNextInterest() > 0)
            return Message.BANK_INTEREST_STATUS_WAITING.getMessage(locale);

        SuperiorPlayer owner = island.getOwner();
        long currentTime = System.currentTimeMillis() / 1000L;
        int bankInterestRecentActive = BuiltinModules.BANK.getConfiguration().getBankInterestRecentActive();
        if (bankInterestRecentActive > 0 && !owner.isOnline() &&
                currentTime - owner.getLastTimeStatus() > bankInterestRecentActive)
            return Message.BANK_INTEREST_STATUS_WAITING_OWNER_ACTIVE.getMessage(locale);

        int bankInterestPercentage = BuiltinModules.BANK.getConfiguration().getBankInterestPercentage();
        BigDecimal balance = island.getIslandBank().getBalance().max(BigDecimal.ONE);
        BigDecimal balanceToGive = balance.multiply(new BigDecimal(bankInterestPercentage / 100D));
        if (!island.getIslandBank().canDepositMoney(balanceToGive))
            return Message.BANK_INTEREST_STATUS_BANK_LIMIT_REACHED.getMessage(locale);

        return Message.BANK_INTEREST_STATUS_READY.getMessage(locale);
    }

}
