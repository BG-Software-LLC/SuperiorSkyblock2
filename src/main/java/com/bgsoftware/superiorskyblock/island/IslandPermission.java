package com.bgsoftware.superiorskyblock.island;

import java.util.Arrays;

public enum IslandPermission {

    ALL,
    ANIMAL_BREED,
    ANIMAL_DAMAGE,
    BAN_MEMBER,
    BREAK,
    BUILD,
    CHECK_PERMISSION,
    CHEST_ACCESS,
    DELETE_WARP,
    DEMOTE_MEMBERS,
    DEPOSIT_MONEY,
    DISBAND_ISLAND,
    DISCORD_SHOW,
    DROP_ITEMS,
    EXPEL_BYPASS,
    EXPEL_PLAYERS,
    INTERACT,
    INVITE_MEMBER,
    ITEM_FRAME,
    KICK_MEMBER,
    PAINTING,
    PAYPAL_SHOW,
    PICKUP_DROPS,
    PROMOTE_MEMBERS,
    SET_BIOME,
    SET_DISCORD,
    SET_PAYPAL,
    SET_PERMISSION,
    SET_ROLE,
    SET_WARP,
    USE,
    WITHDRAW_MONEY;

    public static String getValuesString(){
        StringBuilder stringBuilder = new StringBuilder();
        Arrays.stream(values()).forEach(islandPermission -> stringBuilder.append(", ").append(islandPermission.name().toLowerCase()));
        return stringBuilder.toString().substring(2);
    }


}
