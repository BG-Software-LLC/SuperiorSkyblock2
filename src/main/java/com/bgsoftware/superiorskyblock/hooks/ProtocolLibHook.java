package com.bgsoftware.superiorskyblock.hooks;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.utils.ServerVersion;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import com.gmail.filoghost.holographicdisplays.bridge.protocollib.current.packet.WrapperPlayServerEntityMetadata;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;

public final class ProtocolLibHook {

    public static void init(SuperiorSkyblockPlugin plugin){
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(plugin, PacketType.Play.Server.ENTITY_METADATA) {
            @Override
            public void onPacketSending(PacketEvent event) {
                PacketContainer packet = event.getPacket();

                if (packet.getType() == PacketType.Play.Server.ENTITY_METADATA) {

                    WrapperPlayServerEntityMetadata entityMetadataPacket = new WrapperPlayServerEntityMetadata(packet.deepClone());
                    List<WrappedWatchableObject> dataWatcherValues = entityMetadataPacket.getEntityMetadata();

                    for (WrappedWatchableObject watchableObject : dataWatcherValues) {
                        if (watchableObject.getIndex() == 2) {
                            if (replacePlaceholders(watchableObject, event.getPlayer()))
                                event.setPacket(entityMetadataPacket.getHandle());

                            return;
                        }
                    }
                }
            }
        });
    }

    private static boolean replacePlaceholders(WrappedWatchableObject customNameWatchableObject, Player player) {
        if (customNameWatchableObject == null) return true;

        Object customNameWatchableObjectValue = customNameWatchableObject.getValue();
        String customName;

        if (ServerVersion.isAtLeast(ServerVersion.v1_13)) {
            if (!(customNameWatchableObjectValue instanceof Optional)) {
                return false;
            }

            Optional<?> customNameOptional = (Optional<?>) customNameWatchableObjectValue;
            if (!customNameOptional.isPresent()) {
                return false;
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

        return true;
    }

    public static void disable(SuperiorSkyblockPlugin plugin){
        ProtocolLibrary.getProtocolManager().removePacketListeners(plugin);
    }

}
