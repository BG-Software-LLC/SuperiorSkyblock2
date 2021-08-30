package com.bgsoftware.superiorskyblock.player;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.handlers.PlayersManager;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.data.DatabaseResult;
import com.bgsoftware.superiorskyblock.data.bridge.PlayersDatabaseBridge;
import com.bgsoftware.superiorskyblock.handlers.AbstractHandler;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@SuppressWarnings("WeakerAccess")
public final class PlayersHandler extends AbstractHandler implements PlayersManager {

    private final Map<UUID, SuperiorPlayer> players = new ConcurrentHashMap<>();

    public PlayersHandler(SuperiorSkyblockPlugin plugin) {
        super(plugin);
    }

    @Override
    public void loadData() {

    }

    @Override
    public SuperiorPlayer getSuperiorPlayer(String name) {
        if (name == null)
            return null;

        for (SuperiorPlayer superiorPlayer : players.values()) {
            if (superiorPlayer.getName().equalsIgnoreCase(name))
                return superiorPlayer;
        }

        return null;
    }

    public SuperiorPlayer getSuperiorPlayer(CommandSender commandSender) {
        return getSuperiorPlayer((Player) commandSender);
    }

    @Override
    public SuperiorPlayer getSuperiorPlayer(Player player) {
        Preconditions.checkNotNull(player, "player parameter cannot be null.");
        return player.hasMetadata("NPC") ? new SuperiorNPCPlayer(player) : getSuperiorPlayer(player.getUniqueId());
    }

    @Override
    public SuperiorPlayer getSuperiorPlayer(UUID uuid) {
        Preconditions.checkNotNull(uuid, "uuid parameter cannot be null.");
        if (!players.containsKey(uuid)) {
            SuperiorPlayer superiorPlayer = plugin.getFactory().createPlayer(uuid);
            players.put(uuid, superiorPlayer);
            Executor.async(() -> PlayersDatabaseBridge.insertPlayer(superiorPlayer), 1L);
        }
        return players.get(uuid);
    }

    public List<SuperiorPlayer> matchAllPlayers(Predicate<? super SuperiorPlayer> predicate) {
        return players.values().stream().filter(predicate).collect(Collectors.toList());
    }

    @Override
    public List<SuperiorPlayer> getAllPlayers() {
        return new ArrayList<>(players.values());
    }

    @Override
    @Deprecated
    public PlayerRole getPlayerRole(int index) {
        return plugin.getRoles().getPlayerRole(index);
    }

    @Override
    @Deprecated
    public PlayerRole getPlayerRoleFromId(int id) {
        return plugin.getRoles().getPlayerRoleFromId(id);
    }

    @Override
    @Deprecated
    public PlayerRole getPlayerRole(String name) {
        return plugin.getRoles().getPlayerRole(name);
    }

    @Override
    @Deprecated
    public PlayerRole getDefaultRole() {
        return plugin.getRoles().getDefaultRole();
    }

    @Override
    @Deprecated
    public PlayerRole getLastRole() {
        return plugin.getRoles().getLastRole();
    }

    @Override
    @Deprecated
    public PlayerRole getGuestRole() {
        return plugin.getRoles().getGuestRole();
    }

    @Override
    @Deprecated
    public PlayerRole getCoopRole() {
        return plugin.getRoles().getCoopRole();
    }

    @Override
    @Deprecated
    public List<PlayerRole> getRoles() {
        return plugin.getRoles().getRoles();
    }

    public SuperiorPlayer loadPlayer(DatabaseResult resultSet) {
        UUID player = UUID.fromString(resultSet.getString("uuid"));
        SuperiorPlayer superiorPlayer = plugin.getFactory().createPlayer(resultSet);
        players.put(player, superiorPlayer);
        return superiorPlayer;
    }

    public void replacePlayers(SuperiorPlayer originPlayer, SuperiorPlayer newPlayer) {
        players.remove(originPlayer.getUniqueId());

        for (Island island : plugin.getGrid().getIslands())
            island.replacePlayers(originPlayer, newPlayer);

        newPlayer.merge(originPlayer);
    }

    // Updating last time status
    public void savePlayers() {
        Bukkit.getOnlinePlayers().stream().map(this::getSuperiorPlayer).forEach(PlayersDatabaseBridge::saveLastTimeStatus);
    }

}
