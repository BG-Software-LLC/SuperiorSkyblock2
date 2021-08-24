package com.bgsoftware.superiorskyblock.island.attributes;

import java.util.EnumMap;

public final class IslandAttributes {

    private final EnumMap<Field, Object> fieldValues = new EnumMap<>(Field.class);

    public IslandAttributes(){

    }

    public IslandAttributes setValue(Field field, Object value){
        fieldValues.put(field, value);
        return this;
    }

    public <T> T getValue(Field field, T def){
        Object value = fieldValues.get(field);
        // noinspection all
        return value == null ? def : (T) value;
    }

    public enum Field {

        UUID,
        OWNER,
        CENTER,
        CREATION_TIME,
        ISLAND_TYPE,
        DISCORD,
        PAYPAL,
        WORTH_BONUS,
        LEVELS_BONUS,
        LOCKED,
        IGNORED,
        NAME,
        DESCRIPTION,
        GENERATED_SCHEMATICS,
        UNLOCKED_WORLDS,
        LAST_TIME_UPDATED,
        DIRTY_CHUNKS,
        BLOCK_COUNTS,
        HOMES,
        MEMBERS,
        BANS,
        PLAYER_PERMISSIONS,
        ROLE_PERMISSIONS,
        UPGRADES,
        WARPS,
        BLOCK_LIMITS,
        RATINGS,
        MISSIONS,
        ISLAND_FLAGS,
        GENERATORS,
        VISITORS,
        ENTITY_LIMITS,
        EFFECTS,
        ISLAND_CHESTS,
        ROLE_LIMITS,
        WARP_CATEGORIES,
        BANK_BALANCE,
        BANK_LAST_INTEREST,
        VISITOR_HOMES,
        ISLAND_SIZE,
        TEAM_LIMIT,
        WARPS_LIMIT,
        CROP_GROWTH_MULTIPLIER,
        SPAWNER_RATES_MULTIPLIER,
        MOB_DROPS_MULTIPLIER,
        COOP_LIMIT,
        BANK_LIMIT
    }

}
