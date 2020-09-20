package com.bgsoftware.superiorskyblock.island.bank;

import com.bgsoftware.superiorskyblock.api.enums.BankAction;
import com.bgsoftware.superiorskyblock.api.island.bank.BankTransaction;
import com.bgsoftware.superiorskyblock.utils.BigDecimalFormatted;
import com.bgsoftware.superiorskyblock.utils.StringUtils;

import java.util.UUID;

public final class SBankTransaction implements BankTransaction {

    private final UUID player;
    private final BankAction bankAction;
    private final int position;
    private final long time;
    private final String date;
    private final String failureReason;
    private final BigDecimalFormatted amount;

    public SBankTransaction(UUID player, BankAction bankAction, int position, long time, String failureReason, BigDecimalFormatted amount){
        this.player = player;
        this.bankAction = bankAction;
        this.position = position;
        this.time = time;
        this.date = StringUtils.formatDate(time);
        this.failureReason = failureReason == null ? "" : failureReason;
        this.amount = amount;
    }

    @Override
    public UUID getPlayer() {
        return player;
    }

    @Override
    public BankAction getAction() {
        return bankAction;
    }

    @Override
    public int getPosition() {
        return position;
    }

    @Override
    public long getTime() {
        return time;
    }

    @Override
    public String getDate() {
        return date;
    }

    @Override
    public String getFailureReason() {
        return failureReason;
    }

    @Override
    public BigDecimalFormatted getAmount() {
        return amount;
    }

}
