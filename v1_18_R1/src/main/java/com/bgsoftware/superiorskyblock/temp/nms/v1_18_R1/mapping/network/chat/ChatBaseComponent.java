package com.bgsoftware.superiorskyblock.temp.nms.v1_18_R1.mapping.network.chat;

import com.bgsoftware.superiorskyblock.temp.nms.v1_18_R1.mapping.MappedObject;
import net.minecraft.network.chat.IChatBaseComponent;

public class ChatBaseComponent extends MappedObject<IChatBaseComponent> {

    public ChatBaseComponent(IChatBaseComponent handle) {
        super(handle);
    }

    public static class ChatSerializer {

        public static String toJson(IChatBaseComponent component) {
            return IChatBaseComponent.ChatSerializer.a(component);
        }

    }

}
