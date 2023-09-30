package com.bgsoftware.superiorskyblock.commands.arguments;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;

import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

public class CommandSuggestions {

    private CommandSuggestions() {

    }

    public static List<String> getIslandSuggestions(SuperiorSkyblock plugin, String argument,
                                                    @Nullable Predicate<Island> selector, List<String> suggestions) {
        for (Island island : plugin.getGrid().getIslands()) {
            if (selector == null || selector.test(island)) {
                String islandName = island.getName();
                if (islandName.toLowerCase(Locale.ENGLISH).contains(argument))
                    suggestions.add(islandName);
            }
        }

        return suggestions;
    }

    public static List<String> getPlayerSuggestions(SuperiorSkyblock plugin, String argument,
                                                    @Nullable Predicate<SuperiorPlayer> selector,
                                                    List<String> suggestions) {
        for (SuperiorPlayer superiorPlayer : plugin.getPlayers().getAllPlayers()) {
            if (selector == null || selector.test(superiorPlayer)) {
                String playerName = superiorPlayer.getName();
                if (playerName.toLowerCase(Locale.ENGLISH).contains(argument))
                    suggestions.add(playerName);
            }
        }

        return suggestions;
    }

}
