package com.bgsoftware.superiorskyblock.player.container;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.player.container.PlayersContainer;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.SequentialListBuilder;
import com.bgsoftware.superiorskyblock.core.collections.CollectionsFactory;
import com.bgsoftware.superiorskyblock.core.collections.view.Int2ObjectMapView;
import com.bgsoftware.superiorskyblock.core.threads.Synchronized;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultPlayersContainer implements PlayersContainer {

    private final Map<UUID, SuperiorPlayer> players = new ConcurrentHashMap<>();
    private final Synchronized<Int2ObjectMapView<SuperiorPlayer>> playersByIds = Synchronized.of(CollectionsFactory.createInt2ObjectLinkedHashMap());
    private final Map<String, SuperiorPlayer> playersByNames = new ConcurrentHashMap<>();

    @Nullable
    @Override
    public SuperiorPlayer getSuperiorPlayer(String name) {
        SuperiorPlayer superiorPlayer = this.playersByNames.get(name.toLowerCase(Locale.ENGLISH));

        if (superiorPlayer == null) {
            superiorPlayer = players.values().stream()
                    .filter(player -> player.getName().equalsIgnoreCase(name))
                    .findFirst().orElse(null);
            if (superiorPlayer != null)
                this.playersByNames.put(name.toLowerCase(Locale.ENGLISH), superiorPlayer);
        }

        return superiorPlayer;
    }

    @Nullable
    @Override
    public SuperiorPlayer getSuperiorPlayer(UUID uuid) {
        return this.players.get(uuid);
    }

    @Nullable
    @Override
    public SuperiorPlayer getSuperiorPlayer(int id) {
        return this.playersByIds.readAndGet(playersByIds -> playersByIds.get(id));
    }

    @Override
    public void setPlayerId(SuperiorPlayer superiorPlayer, int id) {
        this.playersByIds.write(playersByIds -> playersByIds.put(id, superiorPlayer));
    }

    @Override
    public List<SuperiorPlayer> getAllPlayers() {
        return new SequentialListBuilder<SuperiorPlayer>().build(this.players.values());
    }

    @Override
    public void addPlayer(SuperiorPlayer superiorPlayer) {
        this.players.put(superiorPlayer.getUniqueId(), superiorPlayer);
        String playerName = superiorPlayer.getName();
        if (!playerName.equals("null"))
            this.playersByNames.put(playerName.toLowerCase(Locale.ENGLISH), superiorPlayer);
    }

    @Override
    public void removePlayer(SuperiorPlayer superiorPlayer) {
        this.players.remove(superiorPlayer.getUniqueId());
        this.playersByNames.remove(superiorPlayer.getName().toLowerCase(Locale.ENGLISH));
    }

}
