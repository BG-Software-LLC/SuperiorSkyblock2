package com.bgsoftware.superiorskyblock.api.island;

import java.util.Arrays;

public enum IslandPermission {

    ALL,
    ANIMAL_BREED,
    ANIMAL_DAMAGE,
    BAN_MEMBER,
    BREAK,
    BUILD,
    CHANGE_NAME,
    CHECK_PERMISSION,
    CHEST_ACCESS,
    CLOSE_BYPASS,
    CLOSE_ISLAND,
    DELETE_WARP,
    DEMOTE_MEMBERS,
    DEPOSIT_MONEY,
    DISBAND_ISLAND,
    DISCORD_SHOW,
    DROP_ITEMS,
    EXPEL_BYPASS,
    EXPEL_PLAYERS,
    FARM_TRAMPING,
    INTERACT,
    INVITE_MEMBER,
    ITEM_FRAME,
    KICK_MEMBER,
    OPEN_ISLAND,
    PAINTING,
    PAYPAL_SHOW,
    PICKUP_DROPS,
    PROMOTE_MEMBERS,
    RANKUP,
    RATINGS_SHOW,
    SET_BIOME,
    SET_DISCORD,
    SET_HOME,
    SET_PAYPAL,
    SET_PERMISSION,
    SET_ROLE,
    SET_WARP,
    SIGN_INTERACT,
    USE,
    WITHDRAW_MONEY;

    public static String getValuesString(){
        StringBuilder stringBuilder = new StringBuilder();
        Arrays.stream(values()).forEach(islandPermission -> stringBuilder.append(", ").append(islandPermission.name().toLowerCase()));
        return stringBuilder.toString().substring(2);
    }


}
