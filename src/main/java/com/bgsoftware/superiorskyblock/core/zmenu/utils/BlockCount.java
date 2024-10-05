package com.bgsoftware.superiorskyblock.core.zmenu.utils;

import com.bgsoftware.superiorskyblock.api.key.Key;

import java.math.BigInteger;

public class BlockCount {

    private final Key blockKey;
    private final BigInteger amount;

    public BlockCount(Key blockKey, BigInteger amount) {
        this.blockKey = blockKey;
        this.amount = amount;
    }

    public Key getBlockKey() {
        return blockKey;
    }

    public BigInteger getAmount() {
        return amount;
    }

}