package com.bgsoftware.superiorskyblock.api.menu.dialog;

/**
 * Represents the type of the dialog UI.
 * The plugin only supports the types of this enum.
 */
public enum DialogMenuType {

    /**
     * A dialog screen with a scrollable list of action buttons arranged in columns.
     */
    MULTI_ACTION,

    /**
     * A dialog screen with two action buttons in footer.
     */
    CONFIRMATION,

    /**
     * A dialog screen with a single action button in footer.
     */
    NOTICE

}
