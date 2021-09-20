package com.bgsoftware.superiorskyblock.player;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.handlers.PlayersManager;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.database.DatabaseResult;
import com.bgsoftware.superiorskyblock.database.bridge.PlayersDatabaseBridge;
import com.bgsoftware.superiorskyblock.handler.AbstractHandler;
import com.bgsoftware.superiorskyblock.player.container.PlayersContainer;
import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@SuppressWarnings("WeakerAccess")
public final class PlayersHandler extends AbstractHandler implements PlayersManager {

    private final PlayersContainer playersContainer;

    public PlayersHandler(SuperiorSkyblockPlugin plugin, PlayersContainer playersContainer) {
        super(plugin);
        this.playersContainer = playersContainer;
    }

    @Override
    public void loadData() {

    }

    @Override
    public SuperiorPlayer getSuperiorPlayer(String name) {
        Preconditions.checkNotNull(name, "name parameter cannot be null.");
        return this.playersContainer.getSuperiorPlayer(name);
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
        SuperiorPlayer superiorPlayer = this.playersContainer.getSuperiorPlayer(uuid);

        if (superiorPlayer == null) {
            superiorPlayer = plugin.getFactory().createPlayer(uuid);
            this.playersContainer.addPlayer(superiorPlayer);
            PlayersDatabaseBridge.insertPlayer(superiorPlayer);
        }

        return superiorPlayer;
    }

    public List<SuperiorPlayer> matchAllPlayers(Predicate<? super SuperiorPlayer> predicate) {
        return Collections.unmodifiableList(this.playersContainer.getAllPlayers().stream()
                .filter(predicate)
                .collect(Collectors.toList())
        );
    }

    @Override
    public List<SuperiorPlayer> getAllPlayers() {
        return this.playersContainer.getAllPlayers();
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

    public void loadPlayer(DatabaseResult resultSet) {
        SuperiorPlayer superiorPlayer = plugin.getFactory().createPlayer(resultSet);
        this.playersContainer.addPlayer(superiorPlayer);
    }

    public void replacePlayers(SuperiorPlayer originPlayer, SuperiorPlayer newPlayer) {
        this.playersContainer.removePlayer(originPlayer);

        for (Island island : plugin.getGrid().getIslands())
            island.replacePlayers(originPlayer, newPlayer);

        newPlayer.merge(originPlayer);
    }

    // Updating last time status
    public void savePlayers() {
        Bukkit.getOnlinePlayers().stream().map(this::getSuperiorPlayer).forEach(PlayersDatabaseBridge::saveLastTimeStatus);
    }

}
