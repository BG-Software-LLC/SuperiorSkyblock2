package com.bgsoftware.superiorskyblock.player.inventory;

import com.bgsoftware.superiorskyblock.api.player.inventory.ClearAction;

public class ClearActions {

    public static final ClearAction ENDER_CHEST = register(new ClearAction("ENDER_CHEST") {

    });

    public static final ClearAction INVENTORY = register(new ClearAction("INVENTORY") {

    });

    private ClearActions() {

    }

    public static void registerActions() {
        // Do nothing, only trigger all the register calls
    }

    private static ClearAction register(ClearAction clearAction) {
        ClearAction.register(clearAction);
        return clearAction;
    }

}
