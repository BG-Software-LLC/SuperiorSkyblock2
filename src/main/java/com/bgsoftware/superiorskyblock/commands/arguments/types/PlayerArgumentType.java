package com.bgsoftware.superiorskyblock.commands.arguments.types;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.api.commands.CommandContext;
import com.bgsoftware.superiorskyblock.api.commands.CommandSyntaxException;
import com.bgsoftware.superiorskyblock.api.commands.arguments.ArgumentsReader;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgumentType;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.messages.Message;

import java.util.function.Predicate;

public class PlayerArgumentType implements CommandArgumentType<SuperiorPlayer, CommandContext> {

    public static final PlayerArgumentType ALL_PLAYERS = new PlayerArgumentType(null);
    public static final PlayerArgumentType ONLINE_PLAYERS = new PlayerArgumentType(SuperiorPlayer::isOnline);

    @Nullable
    private final PlayerSelector selector;

    private PlayerArgumentType(@Nullable PlayerSelector selector) {
        this.selector = selector;
    }

    @Override
    public SuperiorPlayer parse(SuperiorSkyblock plugin, CommandContext context, ArgumentsReader reader) throws CommandSyntaxException {
        String name = reader.read();

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
