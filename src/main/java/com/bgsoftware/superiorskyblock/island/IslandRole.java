package com.bgsoftware.superiorskyblock.island;

import com.bgsoftware.superiorskyblock.Locale;

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

    @Override
    public String toString() {
        switch (this){
            case GUEST:
                return Locale.ROLE_GUEST.getMessage();
            case MEMBER:
                return Locale.ROLE_MEMBER.getMessage();
            case MODERATOR:
                return Locale.ROLE_MOD.getMessage();
            case ADMIN:
                return Locale.ROLE_ADMIN.getMessage();
            case LEADER:
                return Locale.ROLE_LEADER.getMessage();
        }

        return "";
    }

    public static String getValuesString(){
        StringBuilder stringBuilder = new StringBuilder();
        Arrays.stream(values()).forEach(islandRole -> stringBuilder.append(", ").append(islandRole.toString().toLowerCase()));
        return stringBuilder.toString().substring(2);
    }

}
