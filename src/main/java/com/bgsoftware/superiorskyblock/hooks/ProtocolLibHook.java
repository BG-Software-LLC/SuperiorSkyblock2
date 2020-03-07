package com.bgsoftware.superiorskyblock.hooks;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.utils.ServerVersion;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class ProtocolLibHook {

    public static void init(SuperiorSkyblockPlugin plugin){
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(plugin, PacketType.Play.Server.ENTITY_METADATA) {
            @Override
            public void onPacketSending(PacketEvent event) {
                PacketContainer packet = event.getPacket();

                if (packet.getType() == PacketType.Play.Server.ENTITY_METADATA) {
                    WrappedDataWatcher watcher = new WrappedDataWatcher(packet.getWatchableCollectionModifier().read(0));
                    List<WrappedWatchableObject> newWatchableObjects = new ArrayList<>();
                    for (WrappedWatchableObject watchableObject : watcher.getWatchableObjects()) {
                        if (watchableObject.getIndex() == 2)
                            replacePlaceholders(watchableObject);

                        newWatchableObjects.add(watchableObject);
                    }
                    packet.getWatchableCollectionModifier().write(0, newWatchableObjects);
                }
            }
        });
    }

    private static void replacePlaceholders(WrappedWatchableObject customNameWatchableObject) {
        if (customNameWatchableObject == null) return;

        Object customNameWatchableObjectValue = customNameWatchableObject.getValue();
        String customName;

        if (ServerVersion.isAtLeast(ServerVersion.v1_13)) {
            if (!(customNameWatchableObjectValue instanceof Optional)) {
                return;
            }

            Optional<?> customNameOptional = (Optional<?>) customNameWatchableObjectValue;
            if (!customNameOptional.isPresent()) {
                return;
            }

            WrappedChatComponent componentWrapper = WrappedChatComponent.fromHandle(customNameOptional.get());
            customName = componentWrapper.getJson();

        } else {
            customName = (String) customNameWatchableObjectValue;
        }

        customName = IslandsTopHook.parsePlaceholders(customName);

        if (ServerVersion.isAtLeast(ServerVersion.v1_13)) {
            customNameWatchableObject.setValue(Optional.of(WrappedChatComponent.fromJson(customName).getHandle()));
        } else {
            customNameWatchableObject.setValue(customName);
        }
    }

    public static void disable(SuperiorSkyblockPlugin plugin){
        ProtocolLibrary.getProtocolManager().removePacketListeners(plugin);
    }

}
