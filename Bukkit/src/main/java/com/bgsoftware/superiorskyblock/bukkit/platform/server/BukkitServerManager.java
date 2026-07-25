package com.bgsoftware.superiorskyblock.bukkit.platform.server;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.platform.server.IServerManager;
import com.mojang.authlib.properties.Property;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class BukkitServerManager implements IServerManager {

    // The plugin is resolved lazily, as the platform is created before the plugin itself.
    private SuperiorSkyblockPlugin plugin;

    @Override
    public void shutdown() {
        Bukkit.shutdown();
    }

    @Override
    public int getOnlinePlayersCount() {
        return Bukkit.getOnlinePlayers().size();
    }

    @Override
    public List<SuperiorPlayer> getOnlinePlayers() {
        Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
        List<SuperiorPlayer> superiorPlayers = new ArrayList<>(onlinePlayers.size());
        for (Player player : onlinePlayers)
            superiorPlayers.add(plugin().getPlayers().getSuperiorPlayer(player));
        return superiorPlayers;
    }

    @Override
    public void dispatchConsoleCommand(String command) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
    }

    @Override
    public boolean isPlayerOnline(UUID uuid) {
        return Bukkit.getPlayer(uuid) != null;
    }

    @Override
    public void registerCommand(BukkitCommand command) {
        plugin().getNMSAlgorithms().registerCommand(command);
    }

    @Override
    public double getCurrentTps() {
        return plugin().getNMSAlgorithms().getCurrentTps();
    }

    @Override
    public int getDataVersion() {
        return plugin().getNMSAlgorithms().getDataVersion();
    }

    @Override
    public int getMaxWorldSize() {
        return plugin().getNMSAlgorithms().getMaxWorldSize();
    }

    @Override
    public void setSkinTexture(SuperiorPlayer superiorPlayer) {
        plugin().getNMSPlayers().setSkinTexture(superiorPlayer);
    }

    @Override
    public void setSkinTexture(SuperiorPlayer superiorPlayer, Property property) {
        plugin().getNMSPlayers().setSkinTexture(superiorPlayer, property);
    }

    @Override
    public boolean wasThrownByPlayer(Item item, SuperiorPlayer superiorPlayer) {
        return plugin().getNMSPlayers().wasThrownByPlayer(item, superiorPlayer);
    }

    @Override
    public Object createOfflinePlayerData(OfflinePlayer offlinePlayer) {
        return plugin().getNMSPlayers().createOfflinePlayerData(offlinePlayer);
    }

    @Nullable
    @Override
    public Locale getPlayerLocale(Player player) {
        return plugin().getNMSPlayers().getPlayerLocale(player);
    }

    private SuperiorSkyblockPlugin plugin() {
        if (this.plugin == null)
            this.plugin = SuperiorSkyblockPlugin.getPlugin();
        return this.plugin;
    }

}
