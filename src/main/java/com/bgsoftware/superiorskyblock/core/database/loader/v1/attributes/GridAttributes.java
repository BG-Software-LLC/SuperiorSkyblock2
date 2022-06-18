package com.bgsoftware.superiorskyblock.core.database.loader.v1.attributes;

public class GridAttributes extends AttributesRegistry<GridAttributes.Field> {

    public GridAttributes() {
        super(Field.class);
    }

    @Override
    public GridAttributes setValue(Field field, Object value) {
        return (GridAttributes) super.setValue(field, value);
    }

    public enum Field {

        LAST_ISLAND,
        MAX_ISLAND_SIZE,
        WORLD

    }

}
