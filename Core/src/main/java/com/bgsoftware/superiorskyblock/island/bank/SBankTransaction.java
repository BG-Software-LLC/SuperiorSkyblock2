package com.bgsoftware.superiorskyblock.island.bank;

import com.bgsoftware.superiorskyblock.api.enums.BankAction;
import com.bgsoftware.superiorskyblock.api.island.bank.BankTransaction;
import com.bgsoftware.superiorskyblock.core.database.DatabaseResult;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.logging.Log;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

public class SBankTransaction implements BankTransaction {

    private final UUID player;
    private final BankAction bankAction;
    private final int position;
    private final long time;
    private final String date;
    private final String failureReason;
    private final BigDecimal amount;

    public SBankTransaction(UUID player, BankAction bankAction, int position, long time, String failureReason, BigDecimal amount) {
        this.player = player;
        this.bankAction = bankAction;
        this.position = position;
        this.time = time;
        this.date = Formatters.DATE_FORMATTER.format(new Date(time));
        this.failureReason = failureReason == null ? "" : failureReason;
        this.amount = amount.setScale(2, RoundingMode.HALF_EVEN);
    }

    public static Optional<BankTransaction> fromDatabase(DatabaseResult resultSet) {
        Optional<BankAction> bankAction = resultSet.getEnum("bank_action", BankAction.class);
        if (!bankAction.isPresent()) {
            Log.warn("Cannot load bank transaction with invalid bank action, skipping...");
            return Optional.empty();
        }

        Optional<BigDecimal> amount = resultSet.getBigDecimal("amount");
        if (!amount.isPresent()) {
            Log.warn("Cannot load bank transaction with null amount, skipping...");
            return Optional.empty();
        }

        return Optional.of(new SBankTransaction(
                resultSet.getUUID("player").orElse(null),
                bankAction.get(),
                resultSet.getInt("position").orElse(1),
                resultSet.getLong("time").orElse(System.currentTimeMillis()),
                resultSet.getString("failure_reason").orElse(""),
                amount.get()
        ));
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
