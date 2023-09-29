package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgument;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandFlag;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.InternalIslandsCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArgumentsBuilder;
import com.bgsoftware.superiorskyblock.commands.arguments.types.BoolArgumentType;
import com.bgsoftware.superiorskyblock.commands.arguments.types.IslandFlagArgumentType;
import com.bgsoftware.superiorskyblock.commands.arguments.types.MultipleIslandsArgumentType;
import com.bgsoftware.superiorskyblock.commands.context.IslandsCommandContext;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class CmdAdminSetSettings implements InternalIslandsCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("setsettings");
    }

    @Override
    public String getPermission() {
        return "superior.admin.setsettings";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_ADMIN_SET_SETTINGS.getMessage(locale);
    }

    @Override
    public List<CommandArgument<?>> getArguments() {
        return new CommandArgumentsBuilder()
                .add(CommandArgument.required("islands", MultipleIslandsArgumentType.INCLUDE_PLAYERS, Message.COMMAND_ARGUMENT_PLAYER_NAME, Message.COMMAND_ARGUMENT_ISLAND_NAME, Message.COMMAND_ARGUMENT_ALL_ISLANDS))
                .add(CommandArgument.required("island-flag", IslandFlagArgumentType.INSTANCE, Message.COMMAND_ARGUMENT_SETTINGS))
                .add(CommandArgument.required("value", BoolArgumentType.INSTANCE, Message.COMMAND_ARGUMENT_VALUE))
                .build();
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return true;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, IslandsCommandContext context) {
        CommandSender dispatcher = context.getDispatcher();

        List<Island> islands = context.getIslands();
        IslandFlag islandFlag = context.getRequiredArgument("island-flag", IslandFlag.class);
        boolean value = context.getRequiredArgument("value", boolean.class);

        boolean anyIslandChanged = false;

        for (Island island : islands) {
            if (island.hasSettingsEnabled(islandFlag) == value) {
                anyIslandChanged = true;
                continue;
            }

            if (value) {
                if (plugin.getEventsBus().callIslandEnableFlagEvent(dispatcher, island, islandFlag)) {
                    anyIslandChanged = true;
                    island.enableSettings(islandFlag);
                }
            } else if (plugin.getEventsBus().callIslandDisableFlagEvent(dispatcher, island, islandFlag)) {
                anyIslandChanged = true;
                island.disableSettings(islandFlag);
            }
        }

        if (!anyIslandChanged)
            return;

        SuperiorPlayer targetPlayer = context.getTargetPlayer();

        if (islands.size() != 1)
            Message.SETTINGS_UPDATED_ALL.send(dispatcher, Formatters.CAPITALIZED_FORMATTER.format(islandFlag.getName()));
        else if (targetPlayer == null)
            Message.SETTINGS_UPDATED_NAME.send(dispatcher, Formatters.CAPITALIZED_FORMATTER.format(islandFlag.getName()), islands.get(0).getName());
        else
            Message.SETTINGS_UPDATED.send(dispatcher, Formatters.CAPITALIZED_FORMATTER.format(islandFlag.getName()), targetPlayer.getName());
    }

}
