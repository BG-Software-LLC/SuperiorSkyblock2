package com.bgsoftware.superiorskyblock.commands.arguments;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.api.commands.CommandContext;

import java.util.Collection;
import java.util.List;

public interface SuggestionsSelector<E> {

    Collection<E> getAllPossibilities(SuperiorSkyblock plugin, CommandContext context);

    boolean check(SuperiorSkyblock plugin, CommandContext context, E superiorPlayer);

}
