package com.bgsoftware.superiorskyblock.module.generators.commands;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.CommandSyntaxException;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgument;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.InternalIslandsCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArgumentsBuilder;
import com.bgsoftware.superiorskyblock.commands.arguments.types.EnumArgumentType;
import com.bgsoftware.superiorskyblock.commands.arguments.types.MultipleIslandsArgumentType;
import com.bgsoftware.superiorskyblock.commands.context.IslandsCommandContext;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;

public class CmdAdminClearGenerator implements InternalIslandsCommand {

    @Override
    public List<String> getAliases() {
        return Arrays.asList("cleargenerator", "cg");
    }

    @Override
    public String getPermission() {
        return "superior.admin.cleargenerator";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_ADMIN_CLEAR_GENERATOR.getMessage(locale);
    }

    @Override
    public List<CommandArgument<?>> getArguments() {
        return new CommandArgumentsBuilder()
                .add(CommandArgument.required("islands", MultipleIslandsArgumentType.INCLUDE_PLAYERS, Message.COMMAND_ARGUMENT_PLAYER_NAME, Message.COMMAND_ARGUMENT_ISLAND_NAME, Message.COMMAND_ARGUMENT_ALL_ISLANDS))
                .add(CommandArgument.optional("environment", EnumArgumentType.WORLD_ENVIRONMENT, "normal/nether/the_end"))
                .build();
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return true;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, IslandsCommandContext context) throws CommandSyntaxException {
        CommandSender dispatcher = context.getDispatcher();

        List<Island> islands = context.getIslands();
        World.Environment environment = context.getOptionalArgument("environment", World.Environment.class)
                .orElse(plugin.getSettings().getWorlds().getDefaultWorld());
        boolean anyIslandChanged = false;

        for (Island island : islands) {
            if (!plugin.getEventsBus().callIslandClearGeneratorRatesEvent(dispatcher, island, environment))
                continue;

            anyIslandChanged = true;

            island.clearGeneratorAmounts(environment);
        }

        if (!anyIslandChanged)
            return;

        SuperiorPlayer targetPlayer = context.getTargetPlayer();

        if (islands.size() != 1)
            Message.GENERATOR_CLEARED_ALL.send(dispatcher);
        else if (targetPlayer == null)
            Message.GENERATOR_CLEARED_NAME.send(dispatcher, islands.get(0).getName());
        else
            Message.GENERATOR_CLEARED.send(dispatcher, targetPlayer.getName());
    }

}
