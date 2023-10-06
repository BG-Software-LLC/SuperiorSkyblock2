package com.bgsoftware.superiorskyblock.player;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.handlers.PlayersManager;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.player.container.PlayersContainer;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.Manager;
import com.bgsoftware.superiorskyblock.core.SequentialListBuilder;
import com.bgsoftware.superiorskyblock.core.database.bridge.PlayersDatabaseBridge;
import com.bgsoftware.superiorskyblock.core.logging.Debug;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Predicate;

public class PlayersManagerImpl extends Manager implements PlayersManager {

    private PlayersContainer playersContainer;

    public PlayersManagerImpl(SuperiorSkyblockPlugin plugin) {
        super(plugin);
    }

    @Override
    public void loadData() {
        // Data is loaded by the database bridge.
        if (this.playersContainer == null)
            throw new RuntimeException("PlayersManager was not initialized correctly. Contact Ome_R regarding this!");
    }

    @Override
    public SuperiorPlayer getSuperiorPlayer(String name) {
        Preconditions.checkNotNull(name, "name parameter cannot be null.");
        return this.playersContainer.getSuperiorPlayer(name);
    }

    @Override
    public SuperiorPlayer getSuperiorPlayer(Player player) {
        Preconditions.checkNotNull(player, "player parameter cannot be null.");
        return player.hasMetadata("NPC") ? new SuperiorNPCPlayer(player) : getSuperiorPlayer(player.getUniqueId());
    }

    @Override
    public SuperiorPlayer getSuperiorPlayer(UUID uuid) {
        return Objects.requireNonNull(getSuperiorPlayer(uuid, true));
    }

    @Nullable
    public SuperiorPlayer getSuperiorPlayer(UUID uuid, boolean createIfNotExists) {
        return this.getSuperiorPlayer(uuid, createIfNotExists, true);
    }

    @Nullable
    public SuperiorPlayer getSuperiorPlayer(UUID uuid, boolean createIfNotExists, boolean saveToDatabase) {
        Preconditions.checkNotNull(uuid, "uuid parameter cannot be null.");
        SuperiorPlayer superiorPlayer = this.playersContainer.getSuperiorPlayer(uuid);

        if (createIfNotExists && superiorPlayer == null) {
            superiorPlayer = plugin.getFactory().createPlayer(uuid);
            this.playersContainer.addPlayer(superiorPlayer);
            if (saveToDatabase)
                PlayersDatabaseBridge.insertPlayer(superiorPlayer);
        }

        return superiorPlayer;
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

    @Override
    public PlayersContainer getPlayersContainer() {
        return this.playersContainer;
    }

    @Override
    public void setPlayersContainer(PlayersContainer playersContainer) {
        Preconditions.checkNotNull(playersContainer, "playersContainer parameter cannot be null");
        this.playersContainer = playersContainer;
    }

    public SuperiorPlayer getSuperiorPlayer(CommandSender commandSender) {
        return getSuperiorPlayer((Player) commandSender);
    }

    public List<SuperiorPlayer> matchAllPlayers(Predicate<? super SuperiorPlayer> predicate) {
        return new SequentialListBuilder<SuperiorPlayer>()
                .filter(predicate)
                .build(this.playersContainer.getAllPlayers());
    }

    public void replacePlayers(SuperiorPlayer originPlayer, @Nullable SuperiorPlayer newPlayer) {
        Log.debug(Debug.REPLACE_PLAYER, originPlayer, newPlayer);

        // We first want to replace the player for his own island
        Island playerIsland = originPlayer.getIsland();
        if (playerIsland != null) {
            playerIsland.replacePlayers(originPlayer, newPlayer);
            if (playerIsland.getOwner() != newPlayer)
                Log.debugResult(Debug.REPLACE_PLAYER, "Owner was not replaced", "Curr owner: " + playerIsland.getOwner().getUniqueId());
        }

        for (Island island : plugin.getGrid().getIslands()) {
            if (island != playerIsland)
                island.replacePlayers(originPlayer, newPlayer);
        }

        if (newPlayer == null) {
            PlayersDatabaseBridge.deletePlayer(originPlayer);
        } else {
            newPlayer.merge(originPlayer);
            plugin.getEventsBus().callPlayerReplaceEvent(originPlayer, newPlayer);
        }
    }

    // Updating last time status
    public void savePlayers() {
        for (Player player : Bukkit.getOnlinePlayers())
            PlayersDatabaseBridge.saveLastTimeStatus(getSuperiorPlayer(player));

        List<SuperiorPlayer> modifiedPlayers = new SequentialListBuilder<SuperiorPlayer>()
                .filter(PlayersDatabaseBridge::isModified)
                .build(getAllPlayers());

        if (!modifiedPlayers.isEmpty())
            modifiedPlayers.forEach(PlayersDatabaseBridge::executeFutureSaves);
    }

}
