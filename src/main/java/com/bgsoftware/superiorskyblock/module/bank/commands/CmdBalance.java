package com.bgsoftware.superiorskyblock.module.bank.commands;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.CommandContext;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgument;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.InternalSuperiorCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArgumentsBuilder;
import com.bgsoftware.superiorskyblock.commands.arguments.types.IslandArgumentType;
import com.bgsoftware.superiorskyblock.core.messages.Message;

import java.util.Arrays;
import java.util.List;

public class CmdBalance implements InternalSuperiorCommand {

    @Override
    public List<String> getAliases() {
        return Arrays.asList("balance", "bal", "money");
    }

    @Override
    public String getPermission() {
        return "superior.island.balance";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_BALANCE.getMessage(locale);
    }

    @Override
    public List<CommandArgument<?>> getArguments() {
        return new CommandArgumentsBuilder()
                .add(CommandArgument.optional("island", IslandArgumentType.INCLUDE_PLAYERS, Message.COMMAND_ARGUMENT_PLAYER_NAME, Message.COMMAND_ARGUMENT_ISLAND_NAME))
                .build();
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return false;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, CommandContext context) {
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(context.getDispatcher());

        IslandArgumentType.Result islandResult = context.getOptionalArgument("island", IslandArgumentType.Result.class)
                .orElseGet(() -> {
                    Island locationIsland = plugin.getGrid().getIslandAt(superiorPlayer.getLocation());
                    return new IslandArgumentType.Result(locationIsland, null);
                });

        Island island = islandResult.getIsland();

        if (island == null) {
            Message.INVALID_ISLAND.send(superiorPlayer);
            return;
        }

        SuperiorPlayer targetPlayer = islandResult.getTargetPlayer();

        if (island == superiorPlayer.getIsland())
            Message.ISLAND_BANK_SHOW.send(superiorPlayer, island.getIslandBank().getBalance());
        else if (targetPlayer == null)
            Message.ISLAND_BANK_SHOW_OTHER_NAME.send(superiorPlayer, island.getName(), island.getIslandBank().getBalance());
        else
            Message.ISLAND_BANK_SHOW_OTHER.send(superiorPlayer, targetPlayer.getName(), island.getIslandBank().getBalance());

    }

}
