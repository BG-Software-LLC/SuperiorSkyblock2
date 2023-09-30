package com.bgsoftware.superiorskyblock.commands.arguments;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.api.commands.CommandContext;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.SequentialListBuilder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class SuggestionsSelectors {

    public static final SuggestionsSelector<SuperiorPlayer> ONLINE_PLAYERS = new SuggestionsSelector<SuperiorPlayer>() {
        @Override
        public List<SuperiorPlayer> getAllPossibilities(SuperiorSkyblock plugin, CommandContext context) {
            return new SequentialListBuilder<SuperiorPlayer>()
                    .build(Bukkit.getOnlinePlayers(), plugin.getPlayers()::getSuperiorPlayer);
        }

        @Override
        public boolean check(SuperiorSkyblock plugin, CommandContext context, SuperiorPlayer superiorPlayer) {
            return true;
        }
    };

    public static final SuggestionsSelector<SuperiorPlayer> PLAYERS_WITH_ISLAND = new SuggestionsSelector<SuperiorPlayer>() {
        @Override
        public List<SuperiorPlayer> getAllPossibilities(SuperiorSkyblock plugin, CommandContext context) {
            return plugin.getPlayers().getAllPlayers();
        }

        @Override
        public boolean check(SuperiorSkyblock plugin, CommandContext context, SuperiorPlayer superiorPlayer) {
            return superiorPlayer.getIsland() != null;
        }
    };

    public static final SuggestionsSelector<SuperiorPlayer> PLAYERS_WITH_NO_ISLAND = new SuggestionsSelector<SuperiorPlayer>() {
        @Override
        public List<SuperiorPlayer> getAllPossibilities(SuperiorSkyblock plugin, CommandContext context) {
            return plugin.getPlayers().getAllPlayers();
        }

        @Override
        public boolean check(SuperiorSkyblock plugin, CommandContext context, SuperiorPlayer superiorPlayer) {
            return superiorPlayer.getIsland() == null;
        }
    };

    public static final SuggestionsSelector<SuperiorPlayer> NON_LEADER_PLAYERS = new SuggestionsSelector<SuperiorPlayer>() {
        @Override
        public List<SuperiorPlayer> getAllPossibilities(SuperiorSkyblock plugin, CommandContext context) {
            return plugin.getPlayers().getAllPlayers();
        }

        @Override
        public boolean check(SuperiorSkyblock plugin, CommandContext context, SuperiorPlayer superiorPlayer) {
            Island island = superiorPlayer.getIsland();
            return island != null && island.getOwner() != superiorPlayer;
        }
    };

    public static final SuggestionsSelector<SuperiorPlayer> MEMBERS_WITH_LOWER_ROLE = new SuggestionsSelector<SuperiorPlayer>() {
        @Override
        public List<SuperiorPlayer> getAllPossibilities(SuperiorSkyblock plugin, CommandContext context) {
            SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer((Player) context.getDispatcher());
            Island island = superiorPlayer.getIsland();
            return island == null ? Collections.emptyList() : island.getIslandMembers(false);
        }

        @Override
        public boolean check(SuperiorSkyblock plugin, CommandContext context, SuperiorPlayer targetPlayer) {
            SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer((Player) context.getDispatcher());
            return targetPlayer.getPlayerRole().isLessThan(superiorPlayer.getPlayerRole());
        }
    };

    private SuggestionsSelectors() {

    }

}
