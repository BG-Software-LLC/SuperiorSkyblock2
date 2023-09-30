package com.bgsoftware.superiorskyblock.commands.arguments.types;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.api.commands.CommandContext;
import com.bgsoftware.superiorskyblock.api.commands.CommandSyntaxException;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgumentType;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

public abstract class AbstractPlayerArgumentType<E> implements CommandArgumentType<E> {

    @Nullable
    protected final PlayerSelector selector;

    protected AbstractPlayerArgumentType(@Nullable PlayerSelector selector) {
        this.selector = selector;
    }

    protected final SuperiorPlayer parsePlayer(SuperiorSkyblock plugin, CommandContext context, String name) throws CommandSyntaxException {
        SuperiorPlayer targetPlayer = plugin.getPlayers().getSuperiorPlayer(name);

        if (targetPlayer == null || (this.selector != null && !this.selector.test(targetPlayer))) {
            Message.INVALID_PLAYER.send(context.getDispatcher(), name);
            throw new CommandSyntaxException("Invalid player");
        }

        return targetPlayer;
    }

    protected final List<String> getPlayerSuggestions(SuperiorSkyblock plugin, String argument, List<String> suggestions) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            SuperiorPlayer onlinePlayer = plugin.getPlayers().getSuperiorPlayer(player);
            if (onlinePlayer.isShownAsOnline() && (this.selector == null || this.selector.test(onlinePlayer))) {
                String playerName = onlinePlayer.getName();
                if (playerName.toLowerCase(Locale.ENGLISH).contains(argument))
                    suggestions.add(playerName);
            }
        }

        return suggestions;
    }

    public interface PlayerSelector extends Predicate<SuperiorPlayer> {

    }

}
