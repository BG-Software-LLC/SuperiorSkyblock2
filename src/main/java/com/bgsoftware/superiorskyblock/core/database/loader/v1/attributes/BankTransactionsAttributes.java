package com.bgsoftware.superiorskyblock.core.database.loader.v1.attributes;

public class BankTransactionsAttributes extends AttributesRegistry<BankTransactionsAttributes.Field> {

    public BankTransactionsAttributes() {
        super(Field.class);
    }

    @Override
    public BankTransactionsAttributes setValue(Field field, Object value) {
        return (BankTransactionsAttributes) super.setValue(field, value);
    }

    public enum Field {

        ISLAND,
        PLAYER,
        BANK_ACTION,
        POSITION,
        TIME,
        FAILURE_REASON,
        AMOUNT

    }

}
