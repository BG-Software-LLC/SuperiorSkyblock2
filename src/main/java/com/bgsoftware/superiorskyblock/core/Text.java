package com.bgsoftware.superiorskyblock.core;

import javax.annotation.Nullable;

public class Text {

    private Text() {

    }

    public static boolean isBlank(@Nullable String string) {
        return string == null || string.isEmpty();
    }

}
