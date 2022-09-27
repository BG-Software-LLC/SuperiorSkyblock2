package com.bgsoftware.superiorskyblock.api.island.bank;

import com.bgsoftware.superiorskyblock.api.enums.BankAction;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.UUID;

public class DelegateBankTransaction implements BankTransaction {

    private final BankTransaction handle;

    protected DelegateBankTransaction(BankTransaction handle) {
        this.handle = handle;
    }

    @Nullable
    @Override
    public UUID getPlayer() {
        return this.handle.getPlayer();
    }

    @Override
    public BankAction getAction() {
        return this.handle.getAction();
    }

    @Override
    public int getPosition() {
        return this.handle.getPosition();
    }

    @Override
    public long getTime() {
        return this.handle.getTime();
    }

    @Override
    public String getDate() {
        return this.handle.getDate();
    }

    @Override
    public String getFailureReason() {
        return this.handle.getFailureReason();
    }

    @Override
    public BigDecimal getAmount() {
        return this.handle.getAmount();
    }

}
