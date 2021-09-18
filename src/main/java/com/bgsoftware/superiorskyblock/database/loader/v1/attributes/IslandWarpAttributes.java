package com.bgsoftware.superiorskyblock.database.loader.v1.attributes;

public final class IslandWarpAttributes extends AttributesRegistry<IslandWarpAttributes.Field> {

    public IslandWarpAttributes(){
        super(Field.class);
    }

    @Override
    public IslandWarpAttributes setValue(Field field, Object value) {
        return (IslandWarpAttributes) super.setValue(field, value);
    }

    public enum Field {

        NAME,
        CATEGORY,
        LOCATION,
        PRIVATE_STATUS,
        ICON

    }

}
