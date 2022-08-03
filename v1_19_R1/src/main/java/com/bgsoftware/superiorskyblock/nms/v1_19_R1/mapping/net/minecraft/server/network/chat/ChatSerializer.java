package com.bgsoftware.superiorskyblock.nms.v1_19_R1.mapping.net.minecraft.server.network.chat;

import com.bgsoftware.superiorskyblock.nms.mapping.Remap;
import net.minecraft.network.chat.IChatBaseComponent;

public final class ChatSerializer {

    private ChatSerializer() {

    }

    @Remap(classPath = "net.minecraft.network.chat.Component$Serializer",
            name = "toJson",
            type = Remap.Type.METHOD,
            remappedName = "a")
    public static String toJson(IChatBaseComponent component) {
        return IChatBaseComponent.ChatSerializer.a(component);
    }

}
