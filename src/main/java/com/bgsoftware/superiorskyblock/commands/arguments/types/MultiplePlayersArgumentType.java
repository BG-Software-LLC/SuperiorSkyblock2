package com.bgsoftware.superiorskyblock.commands.arguments.types;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.api.commands.CommandContext;
import com.bgsoftware.superiorskyblock.api.commands.CommandSyntaxException;
import com.bgsoftware.superiorskyblock.api.commands.arguments.ArgumentsReader;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgumentType;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.SequentialListBuilder;
import com.bgsoftware.superiorskyblock.core.messages.Message;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public class MultiplePlayersArgumentType implements CommandArgumentType<List<SuperiorPlayer>, CommandContext> {

    public static final MultiplePlayersArgumentType ALL_PLAYERS = new MultiplePlayersArgumentType(null);
    public static final MultiplePlayersArgumentType ONLINE_PLAYERS = new MultiplePlayersArgumentType(SuperiorPlayer::isOnline);

    @Nullable
    private final PlayerSelector selector;

    private MultiplePlayersArgumentType(@Nullable PlayerSelector selector) {
        this.selector = selector;
    }

    @Override
    public List<SuperiorPlayer> parse(SuperiorSkyblock plugin, CommandContext context, ArgumentsReader reader) throws CommandSyntaxException {
        String name = reader.read();

        if (name.equals("*")) {
            return new SequentialListBuilder<SuperiorPlayer>()
                    .filter(this.selector)
                    .build(plugin.getPlayers().getAllPlayers());
        }

        SuperiorPlayer targetPlayer = plugin.getPlayers().getSuperiorPlayer(name);

        if (targetPlayer == null || (this.selector != null && !this.selector.test(targetPlayer))) {
            Message.INVALID_PLAYER.send(context.getDispatcher(), name);
            throw new CommandSyntaxException("Invalid player");
        }

        return Collections.singletonList(targetPlayer);
    }

    public interface PlayerSelector extends Predicate<SuperiorPlayer> {

    }

}
