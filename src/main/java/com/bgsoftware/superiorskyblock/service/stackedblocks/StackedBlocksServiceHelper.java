package com.bgsoftware.superiorskyblock.service.stackedblocks;

import com.bgsoftware.superiorskyblock.api.service.stackedblocks.InteractionResult;

public class StackedBlocksServiceHelper {

    private StackedBlocksServiceHelper() {

    }

    public static boolean shouldCancelOriginalEvent(InteractionResult result) {
        switch (result) {
            case STACKED_BLOCK_PROTECTED:
            case EVENT_CANCELLED:
            case SUCCESS:
                return true;
            default:
                return false;
        }
    }

}
