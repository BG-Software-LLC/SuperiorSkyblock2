package com.bgsoftware.superiorskyblock.player.container;

import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class DefaultPlayersContainer implements PlayersContainer {

    private final Map<UUID, SuperiorPlayer> players = new ConcurrentHashMap<>();
    private final Map<String, SuperiorPlayer> playersByNames = new ConcurrentHashMap<>();

    @Nullable
    @Override
    public SuperiorPlayer getSuperiorPlayer(String name) {
        SuperiorPlayer superiorPlayer = this.playersByNames.get(name.toLowerCase());

        if(superiorPlayer == null) {
            superiorPlayer = players.values().stream()
                    .filter(player -> player.getName().equalsIgnoreCase(name))
                    .findFirst().orElse(null);
            if(superiorPlayer != null)
                this.playersByNames.put(name.toLowerCase(), superiorPlayer);
        }

        return superiorPlayer;
    }

    @Nullable
    @Override
    public SuperiorPlayer getSuperiorPlayer(UUID uuid) {
        return this.players.get(uuid);
    }

    @Override
    public List<SuperiorPlayer> getAllPlayers() {
        return Collections.unmodifiableList(new ArrayList<>(this.players.values()));
    }

    @Override
    public void addPlayer(SuperiorPlayer superiorPlayer) {
        this.players.put(superiorPlayer.getUniqueId(), superiorPlayer);
        String playerName = superiorPlayer.getName();
        if(!playerName.equals("null"))
            this.playersByNames.put(playerName.toLowerCase(), superiorPlayer);
    }

    @Override
    public void removePlayer(SuperiorPlayer superiorPlayer) {
        this.players.remove(superiorPlayer.getUniqueId());
        this.playersByNames.remove(superiorPlayer.getName().toLowerCase());
    }

}
