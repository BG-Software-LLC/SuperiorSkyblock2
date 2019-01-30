package com.bgsoftware.superiorskyblock.api.island;

import java.util.Arrays;

public enum IslandRole {

    GUEST, MEMBER, MODERATOR, ADMIN, LEADER;

    public boolean isHigherThan(IslandRole role){
        return ordinal() > role.ordinal();
    }

    public boolean isLessThan(IslandRole role){
        return ordinal() < role.ordinal();
    }

    public IslandRole getNextRole(){
        return ordinal() + 1 >= IslandRole.values().length ? IslandRole.values()[ordinal()] : IslandRole.values()[ordinal() + 1];
    }

    public IslandRole getPreviousRole(){
        return ordinal() - 1 < 0 ? IslandRole.values()[0] : IslandRole.values()[ordinal() - 1];
    }

    public static String getValuesString(){
        StringBuilder stringBuilder = new StringBuilder();
        Arrays.stream(values()).forEach(islandRole -> stringBuilder.append(", ").append(islandRole.toString().toLowerCase()));
        return stringBuilder.toString().substring(2);
    }

}
