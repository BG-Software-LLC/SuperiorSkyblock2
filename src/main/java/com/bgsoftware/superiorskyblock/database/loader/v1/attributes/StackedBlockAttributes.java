package com.bgsoftware.superiorskyblock.database.loader.v1.attributes;

public final class StackedBlockAttributes extends AttributesRegistry<StackedBlockAttributes.Field> {

    public StackedBlockAttributes(){
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
