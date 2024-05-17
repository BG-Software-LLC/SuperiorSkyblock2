package com.bgsoftware.superiorskyblock.core.database.loader.sql.upgrade.v0.attributes;

public class StackedBlockAttributes extends AttributesRegistry<StackedBlockAttributes.Field> {

    public StackedBlockAttributes() {
        super(Field.class);
    }

    @Override
    public StackedBlockAttributes setValue(Field field, Object value) {
        return (StackedBlockAttributes) super.setValue(field, value);
    }

    public enum Field {

        LOCATION,
        BLOCK_TYPE,
        AMOUNT

    }

}
