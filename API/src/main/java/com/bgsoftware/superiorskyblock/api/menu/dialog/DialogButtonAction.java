package com.bgsoftware.superiorskyblock.api.menu.dialog;

import com.bgsoftware.common.annotations.Nullable;

public interface DialogButtonAction {

    Type getType();

    @Nullable
    String getActionData();

    enum Type {

        CUSTOM,
        RUN_COMMAND,
        OPEN_URL,
        SUGGEST_COMMAND

    }

}
