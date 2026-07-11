package com.bgsoftware.superiorskyblock.api.menu.dialog;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;

/**
 * Represents a button in a dialog UI
 */
public interface DialogButton {

    /**
     * Get the label of the button
     */
    String getLabel();

    /**
     * Get the width of the button.
     */
    int getWidth();

    /**
     * Get the action to be done once clicking on the button.
     * See {@link DialogButtonAction} for more information.
     */
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
