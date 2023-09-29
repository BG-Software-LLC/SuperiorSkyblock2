package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgument;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandChunkFlags;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.InternalIslandsCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArgumentsBuilder;
import com.bgsoftware.superiorskyblock.commands.arguments.types.EnumArgumentType;
import com.bgsoftware.superiorskyblock.commands.arguments.types.MultipleIslandsArgumentType;
import com.bgsoftware.superiorskyblock.commands.context.IslandsCommandContext;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;

public class CmdAdminResetWorld implements InternalIslandsCommand {

    @Override
    public List<String> getAliases() {
        return Arrays.asList("resetworld", "rworld");
    }

    @Override
    public String getPermission() {
        return "superior.admin.resetworld";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_ADMIN_RESET_WORLD.getMessage(locale);
    }

    @Override
    public List<CommandArgument<?>> getArguments()

    {
        return new CommandArgumentsBuilder()
                .add(CommandArguments.required("islands", MultipleIslandsArgumentType.INCLUDE_PLAYERS, Message.COMMAND_ARGUMENT_PLAYER_NAME, Message.COMMAND_ARGUMENT_ISLAND_NAME, Message.COMMAND_ARGUMENT_ALL_ISLANDS))
                .add(CommandArgument.required("environment", "normal/nether/the_end", EnumArgumentType.WORLD_ENVIRONMENT))
                .build();
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return true;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, IslandsCommandContext context) {
        CommandSender dispatcher = context.getDispatcher();

        World.Environment environment = context.getRequiredArgument("environment", World.Environment.class);
        String environmentName = context.getInputArgument("environment");

        if (environment == plugin.getSettings().getWorlds().getDefaultWorld()) {
            Message.INVALID_ENVIRONMENT.send(dispatcher, environmentName);
            return;
        }

        List<Island> islands = context.getIslands();

        boolean anyIslandChanged = false;

        for (Island island : islands) {
            World world;

            try {
                world = island.getCenter(environment).getWorld();
            } catch (NullPointerException error) {
                Log.entering("ENTER", island.getOwner().getName(), environment);
                Log.error(error, "An unexpected error occurred while resetting world:");
                return;
            }

            if (!plugin.getEventsBus().callIslandWorldResetEvent(dispatcher, island, environment))
                continue;

            anyIslandChanged = true;

            // Sending the players that are in that world to the main island.
            // If the world that will be reset is the normal world, they will be teleported to spawn.
            for (SuperiorPlayer superiorPlayer : island.getAllPlayersInside()) {
                assert superiorPlayer.getWorld() != null;
                if (superiorPlayer.getWorld().equals(world))
                    superiorPlayer.teleport(island);
            }

            // Resetting the chunks
            island.resetChunks(environment, IslandChunkFlags.ONLY_PROTECTED, () -> island.calcIslandWorth(null));

            island.setSchematicGenerate(environment, false);
        }

        if (!anyIslandChanged)
            return;

        SuperiorPlayer targetPlayer = context.getTargetPlayer();

        if (islands.size() > 1)
            Message.RESET_WORLD_SUCCEED_ALL.send(dispatcher, Formatters.CAPITALIZED_FORMATTER.format(environmentName));
        else if (targetPlayer == null)
            Message.RESET_WORLD_SUCCEED_NAME.send(dispatcher, Formatters.CAPITALIZED_FORMATTER.format(environmentName), islands.get(0).getName());
        else
            Message.RESET_WORLD_SUCCEED.send(dispatcher, Formatters.CAPITALIZED_FORMATTER.format(environmentName), targetPlayer.getName());
    }

}
