package com.bgsoftware.superiorskyblock.island.bank;

import com.bgsoftware.superiorskyblock.api.enums.BankAction;
import com.bgsoftware.superiorskyblock.api.island.bank.BankTransaction;
import com.bgsoftware.superiorskyblock.database.DatabaseResult;
import com.bgsoftware.superiorskyblock.utils.StringUtils;

import java.math.BigDecimal;
import java.util.UUID;

public final class SBankTransaction implements BankTransaction {

    private final UUID player;
    private final BankAction bankAction;
    private final int position;
    private final long time;
    private final String date;
    private final String failureReason;
    private final BigDecimal amount;

    public SBankTransaction(UUID player, BankAction bankAction, int position, long time, String failureReason, BigDecimal amount){
        this.player = player;
        this.bankAction = bankAction;
        this.position = position;
        this.time = time;
        this.date = StringUtils.formatDate(time);
        this.failureReason = failureReason == null ? "" : failureReason;
        this.amount = amount.setScale(2, BigDecimal.ROUND_HALF_EVEN);
    }

    public SBankTransaction(DatabaseResult resultSet) {
        String player = resultSet.getString("player");
        this.player = player == null || player.isEmpty() ? null  : UUID.fromString(player);
        this.bankAction = BankAction.valueOf(resultSet.getString("bank_action"));
        this.position = resultSet.getInt("position");
        this.time = resultSet.getLong("time");
        this.date = StringUtils.formatDate(time);
        this.failureReason = resultSet.getString("failure_reason");
        this.amount = new BigDecimal(resultSet.getString("amount")).setScale(2, BigDecimal.ROUND_HALF_EVEN);
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
    public BigDecimal getAmount() {
        return amount;
    }

}
