package com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.network.chat;

import net.minecraft.network.chat.IChatBaseComponent;

public final class ChatSerializer {

    private ChatSerializer() {

    }

    public static String toJson(IChatBaseComponent component) {
        return IChatBaseComponent.ChatSerializer.a(component);
    }

}
