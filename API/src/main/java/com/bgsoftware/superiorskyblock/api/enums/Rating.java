package com.bgsoftware.superiorskyblock.api.enums;

import java.util.Arrays;

/**
 * Used to determine what rating has a player given to an island.
 */
public enum Rating {

    UNKNOWN,
    ONE_STAR,
    TWO_STARS,
    THREE_STARS,
    FOUR_STARS,
    FIVE_STARS;

    /**
     * Get the integer value of the rating.
     */
    public int getValue(){
        return ordinal();
    }

    /**
     * Get a string of all the rating names.
     */
    public static String getValuesString(){
        StringBuilder stringBuilder = new StringBuilder();
        Arrays.stream(values()).forEach(rating -> stringBuilder.append(", ").append(rating.toString().toLowerCase()));
        return stringBuilder.toString().substring(2);
    }

    /**
     * Get a rating by it's value.
     */
    public static Rating valueOf(int value){
        return values()[value];
    }

}
