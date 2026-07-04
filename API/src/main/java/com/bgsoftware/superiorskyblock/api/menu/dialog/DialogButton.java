package com.bgsoftware.superiorskyblock.api.menu.dialog;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;

public interface DialogButton {

    String getLabel();

    int getWidth();

    @Nullable
    DialogButtonAction getAction();

    static Builder newBuilder() {
        return SuperiorSkyblockAPI.getMenus().createDialogButtonBuilder();
    }

    interface Builder {

        Builder setLabel(@Nullable String label);

        Builder setWidth(int width);

        Builder setAction(DialogButtonAction.Type type, @Nullable String data);

        DialogButton build();

    }

}
