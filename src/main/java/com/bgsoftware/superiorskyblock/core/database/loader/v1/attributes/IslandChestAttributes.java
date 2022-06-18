package com.bgsoftware.superiorskyblock.core.database.loader.v1.attributes;

public class IslandChestAttributes extends AttributesRegistry<IslandChestAttributes.Field> {

    public IslandChestAttributes() {
        super(Field.class);
    }

    @Override
    public IslandChestAttributes setValue(Field field, Object value) {
        return (IslandChestAttributes) super.setValue(field, value);
    }

    public enum Field {

        INDEX,
        CONTENTS

    }

}
