package com.bgsoftware.superiorskyblock.api.enums;

import java.util.Arrays;

public enum Rating {

    UNKNOWN,
    ONE_STAR,
    TWO_STARS,
    THREE_STARS,
    FOUR_STARS,
    FIVE_STARS;

    public int getValue(){
        return ordinal();
    }

    public static String getValuesString(){
        StringBuilder stringBuilder = new StringBuilder();
        Arrays.stream(values()).forEach(rating -> stringBuilder.append(", ").append(rating.toString().toLowerCase()));
        return stringBuilder.toString().substring(2);
    }

    public static Rating valueOf(int value){
        return values()[value];
    }

}
