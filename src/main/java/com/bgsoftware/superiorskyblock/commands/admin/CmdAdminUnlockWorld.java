package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgument;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.commands.InternalIslandsCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArgumentsBuilder;
import com.bgsoftware.superiorskyblock.commands.arguments.types.BoolArgumentType;
import com.bgsoftware.superiorskyblock.commands.arguments.types.EnumArgumentType;
import com.bgsoftware.superiorskyblock.commands.arguments.types.IslandArgumentType;
import com.bgsoftware.superiorskyblock.commands.context.IslandsCommandContext;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;

public class CmdAdminUnlockWorld implements InternalIslandsCommand {

    @Override
    public List<String> getAliases() {
        return Arrays.asList("unlockworld", "world", "uworld");
    }

    @Override
    public String getPermission() {
        return "superior.admin.world";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_ADMIN_UNLOCK_WORLD.getMessage(locale);
    }

    @Override
    public List<CommandArgument<?>> getArguments()

    {
        return new CommandArgumentsBuilder()
                .add(CommandArguments.required("island", IslandArgumentType.INCLUDE_PLAYERS, Message.COMMAND_ARGUMENT_PLAYER_NAME, Message.COMMAND_ARGUMENT_ISLAND_NAME, Message.COMMAND_ARGUMENT_ALL_ISLANDS))
                .add(CommandArguments.required("environment", EnumArgumentType.WORLD_ENVIRONMENT, "normal/nether/the_end"))
                .add(CommandArguments.required("unlock", BoolArgumentType.INSTANCE, "true/false"))
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

        if (environment == plugin.getSettings().getWorlds().getDefaultWorld()) {
            Message.INVALID_ENVIRONMENT.send(dispatcher, context.getInputArgument("environment"));
            return;
        }

        List<Island> islands = context.getIslands();
        boolean unlock = context.getRequiredArgument("unlock", boolean.class);

        boolean anyWorldsChanged = false;

        for (Island island : islands) {
            if (unlock ? !plugin.getEventsBus().callIslandUnlockWorldEvent(island, environment) :
                    !plugin.getEventsBus().callIslandLockWorldEvent(island, environment))
                continue;

            anyWorldsChanged = true;

            switch (environment) {
                case NORMAL:
                    island.setNormalEnabled(unlock);
                    break;
                case NETHER:
                    island.setNetherEnabled(unlock);
                    break;
                case THE_END:
                    island.setEndEnabled(unlock);
                    break;
            }
        }

        if (anyWorldsChanged)
            Message.UNLOCK_WORLD_ANNOUNCEMENT.send(dispatcher, Formatters.CAPITALIZED_FORMATTER.format(environment.name()));
    }

}
