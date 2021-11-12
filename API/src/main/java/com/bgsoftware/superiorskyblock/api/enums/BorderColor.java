package com.bgsoftware.superiorskyblock.api.enums;

/**
 * Used to determine what's the color of the border of players.
 */
public enum BorderColor {

    RED,
    GREEN,
    BLUE;

    public static BorderColor safeValue(String name, BorderColor def) {
        try {
            return BorderColor.valueOf(name);
        } catch (IllegalArgumentException ex) {
            return def;
        }
    }

}
