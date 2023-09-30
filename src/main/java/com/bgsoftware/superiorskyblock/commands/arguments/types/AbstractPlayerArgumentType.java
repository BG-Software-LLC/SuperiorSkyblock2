package com.bgsoftware.superiorskyblock.commands.arguments.types;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.api.commands.CommandContext;
import com.bgsoftware.superiorskyblock.api.commands.CommandSyntaxException;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgumentType;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.arguments.SuggestionsSelector;
import com.bgsoftware.superiorskyblock.core.messages.Message;

import java.util.function.Predicate;

public abstract class AbstractPlayerArgumentType<E> implements CommandArgumentType<E> {

    @Nullable
    protected final PlayerSelector selector;
    @Nullable
    protected final SuggestionsSelector<SuperiorPlayer> suggestionsSelector;

    protected AbstractPlayerArgumentType(@Nullable PlayerSelector selector, @Nullable SuggestionsSelector<SuperiorPlayer> suggestionsSelector) {
        this.selector = selector;
        this.suggestionsSelector = suggestionsSelector;
    }

    protected final SuperiorPlayer parsePlayer(SuperiorSkyblock plugin, CommandContext context, String name) throws CommandSyntaxException {
        SuperiorPlayer targetPlayer = plugin.getPlayers().getSuperiorPlayer(name);

        if (targetPlayer == null || (this.selector != null && !this.selector.test(targetPlayer))) {
            Message.INVALID_PLAYER.send(context.getDispatcher(), name);
            throw new CommandSyntaxException("Invalid player");
        }

        return targetPlayer;
    }

    public interface PlayerSelector extends Predicate<SuperiorPlayer> {

    }

}
