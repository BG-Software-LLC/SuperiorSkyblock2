package com.bgsoftware.superiorskyblock.core;

import com.bgsoftware.common.annotations.Nullable;

public class Text {

    private Text() {

    }

    public static boolean isBlank(@Nullable String string) {
        return string == null || string.isEmpty();
    }

}
