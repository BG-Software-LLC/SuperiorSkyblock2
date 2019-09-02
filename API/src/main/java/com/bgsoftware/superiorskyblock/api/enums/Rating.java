package com.bgsoftware.superiorskyblock.api.enums;

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

    public static Rating valueOf(int value){
        return values()[value];
    }

}
