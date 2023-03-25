package com.bgsoftware.superiorskyblock.external;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.player.PlayerLocales;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;

import java.util.Locale;

public class ProtocolLibHook {

    private static SuperiorSkyblockPlugin plugin;

    public static void register(SuperiorSkyblockPlugin plugin) {
        ProtocolLibHook.plugin = plugin;
        ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
        protocolManager.addPacketListener(new ChangePlayerLanguageListener(plugin));
    }

    private static class ChangePlayerLanguageListener extends PacketAdapter {

        private ChangePlayerLanguageListener(SuperiorSkyblockPlugin plugin) {
            super(plugin, PacketType.Play.Client.SETTINGS);
        }

        @Override
        public void onPacketReceiving(PacketEvent event) {
            if (!ProtocolLibHook.plugin.getSettings().isAutoLanguageDetection() || event.getPlayer() == null ||
                    event.isPlayerTemporary())
                return;

            PacketContainer packetContainer = event.getPacket();
            Locale newPlayerLocale;

            try {
                newPlayerLocale = PlayerLocales.getLocale(packetContainer.getStrings().read(0));
            } catch (Throwable error) {
                return;
            }

            SuperiorPlayer superiorPlayer = ProtocolLibHook.plugin.getPlayers().getPlayersContainer().getSuperiorPlayer(event.getPlayer().getUniqueId());
            if (superiorPlayer != null && PlayerLocales.isValidLocale(newPlayerLocale) &&
                    !superiorPlayer.getUserLocale().equals(newPlayerLocale)) {
                if (ProtocolLibHook.plugin.getEventsBus().callPlayerChangeLanguageEvent(superiorPlayer, newPlayerLocale))
                    superiorPlayer.setUserLocale(newPlayerLocale);
            }
        }

    }

}
