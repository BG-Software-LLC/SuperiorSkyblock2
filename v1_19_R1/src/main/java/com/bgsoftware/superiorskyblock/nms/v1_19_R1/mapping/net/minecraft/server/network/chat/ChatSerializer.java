package com.bgsoftware.superiorskyblock.nms.v1_19_R1.mapping.net.minecraft.server.network.chat;

import net.minecraft.network.chat.IChatBaseComponent;

public final class ChatSerializer {

    private ChatSerializer() {

    }

    public static String toJson(IChatBaseComponent component) {
        return IChatBaseComponent.ChatSerializer.a(component);
    }

}
