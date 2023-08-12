package com.bgsoftware.superiorskyblock.external;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.mojang.authlib.properties.Property;
import net.skinsrestorer.api.SkinsRestorer;
import net.skinsrestorer.api.SkinsRestorerProvider;
import net.skinsrestorer.api.event.SkinApplyEvent;
import net.skinsrestorer.api.exception.DataRequestException;
import net.skinsrestorer.api.property.SkinProperty;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.function.Consumer;

public class SkinsRestorer15Hook {

    private static SuperiorSkyblockPlugin plugin;
    private static SkinsRestorer skinsRestorer;

    public static boolean isCompatible() {
        try {
            SkinsRestorer skinsRestorer = SkinsRestorerProvider.get();
            return skinsRestorer != null;
        } catch (IllegalStateException error) {
            return false;
        }
    }

    public static void register(SuperiorSkyblockPlugin plugin) {
        SkinsRestorer15Hook.plugin = plugin;
        skinsRestorer = SkinsRestorerProvider.get();

        plugin.getProviders().registerSkinsListener(SkinsRestorer15Hook::setSkinTexture);
        skinsRestorer.getEventBus().subscribe(plugin, SkinApplyEvent.class, new SkinsListener());
    }

    private static void setSkinTexture(SuperiorPlayer superiorPlayer) {
        if (Bukkit.isPrimaryThread()) {
            BukkitExecutor.async(() -> setSkinTexture(superiorPlayer));
            return;
        }

        Property property = getSkin(superiorPlayer);
        if (property != null)
            BukkitExecutor.sync(() -> plugin.getNMSPlayers().setSkinTexture(superiorPlayer, property));
    }

    private static Property getSkin(SuperiorPlayer superiorPlayer) {
        SkinProperty skinProperty;

        try {
            skinProperty = skinsRestorer.getPlayerStorage().getSkinForPlayer(superiorPlayer.getUniqueId(),
                    superiorPlayer.getName()).orElse(null);
        } catch (DataRequestException error) {
            return null;
        }

        if (skinProperty == null)
            return null;

        return new Property("", skinProperty.getValue(), skinProperty.getSignature());
    }

    private static class SkinsListener implements Consumer<SkinApplyEvent> {

        @Override
        public void accept(SkinApplyEvent event) {
            Object playerObject = event.getPlayer(Object.class);

            if (!(playerObject instanceof Player))
                return;

            SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer((Player) playerObject);

            Property property = new Property("", event.getProperty().getValue(), event.getProperty().getSignature());

            plugin.getNMSPlayers().setSkinTexture(superiorPlayer, property);
        }

    }

}
