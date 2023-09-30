package com.bgsoftware.superiorskyblock.commands.arguments;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.api.commands.CommandContext;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

public class CommandSuggestions {

    private CommandSuggestions() {

    }

    public static List<String> getIslandSuggestions(SuperiorSkyblock plugin, String argument, @Nullable Predicate<Island> selector, List<String> suggestions) {
        for (Island island : plugin.getGrid().getIslands()) {
            if (selector == null || selector.test(island)) {
                String islandName = island.getName();
                if (islandName.toLowerCase(Locale.ENGLISH).contains(argument)) suggestions.add(islandName);
            }
        }

        return suggestions;
    }

    public static List<String> getPlayerSuggestions(SuperiorSkyblock plugin, CommandContext context, String argument, @Nullable SuggestionsSelector<SuperiorPlayer> selector, List<String> suggestions) {
        Collection<SuperiorPlayer> playerList = selector == null ? plugin.getPlayers().getAllPlayers() : selector.getAllPossibilities(plugin, context);

        for (SuperiorPlayer superiorPlayer : playerList) {
            if (selector == null || selector.check(plugin, context, superiorPlayer)) {
                String playerName = superiorPlayer.getName();
                if (playerName.toLowerCase(Locale.ENGLISH).contains(argument)) suggestions.add(playerName);
            }
        }

        return suggestions;
    }

}
