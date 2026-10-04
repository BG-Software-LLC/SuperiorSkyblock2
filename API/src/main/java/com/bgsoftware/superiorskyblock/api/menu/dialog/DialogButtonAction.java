package com.bgsoftware.superiorskyblock.api.menu.dialog;

import com.bgsoftware.common.annotations.Nullable;

/**
 * Represents the action to be performed when clicking a button in a dialog UI.
 */
public interface DialogButtonAction {

    /**
     * Get the type of the action.
     */
    Type getType();

    /**
     * Get the data for the action.
     * This depends on the type of the action.
     */
    @Nullable
    String getActionData();

    /**
     * The type of action to be performed when clicking a button.
     */
    enum Type {

        /**
         * Custom action to be done when clicking the button.
         * This is the default action type for most buttons that do something when clicked in a menu.
         */
        CUSTOM,

        /**
         * Run a command when clicking the button.
         * The plugin handles commands by itself, so this is identical to {@link #CUSTOM} in its implementation.
         */
        RUN_COMMAND,

        /**
         * Opens a URL for the player when clicking the button.
         */
        OPEN_URL,

        /**
         * Suggest a command through a tooltip when clicking the button.
         * Unlike {@link #RUN_COMMAND}, this does not actually executes the command.
         */
        SUGGEST_COMMAND

    }

}
