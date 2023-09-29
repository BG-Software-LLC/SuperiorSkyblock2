package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgument;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.InternalIslandsCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArgumentsBuilder;
import com.bgsoftware.superiorskyblock.commands.arguments.types.IslandPrivilegeArgumentType;
import com.bgsoftware.superiorskyblock.commands.arguments.types.IslandRoleArgumentType;
import com.bgsoftware.superiorskyblock.commands.arguments.types.MultipleIslandsArgumentType;
import com.bgsoftware.superiorskyblock.commands.context.IslandsCommandContext;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;

public class CmdAdminSetPermission implements InternalIslandsCommand {

    @Override
    public List<String> getAliases() {
        return Arrays.asList("setpermission", "setperm");
    }

    @Override
    public String getPermission() {
        return "superior.admin.setpermission";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_ADMIN_SET_PERMISSION.getMessage(locale);
    }

    @Override
    public List<CommandArgument<?>> getArguments() {
        return new CommandArgumentsBuilder()
                .add(CommandArgument.required("islands", MultipleIslandsArgumentType.INCLUDE_PLAYERS, Message.COMMAND_ARGUMENT_PLAYER_NAME, Message.COMMAND_ARGUMENT_ISLAND_NAME, Message.COMMAND_ARGUMENT_ALL_ISLANDS))
                .add(CommandArgument.required("privilege", IslandPrivilegeArgumentType.INSTANCE, Message.COMMAND_ARGUMENT_PERMISSION))
                .add(CommandArgument.required("island-role", IslandRoleArgumentType.INSTANCE, Message.COMMAND_ARGUMENT_ISLAND_ROLE))
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
        IslandPrivilege islandPrivilege = context.getRequiredArgument("privilege", IslandPrivilege.class);
        PlayerRole playerRole = context.getRequiredArgument("island-role", PlayerRole.class);

        boolean anyPrivilegesChanged = false;

        for (Island island : islands) {
            if (!plugin.getEventsBus().callIslandChangeRolePrivilegeEvent(island, playerRole))
                continue;

            anyPrivilegesChanged = true;
            island.setPermission(playerRole, islandPrivilege);
        }

        if (!anyPrivilegesChanged)
            return;

        SuperiorPlayer targetPlayer = context.getTargetPlayer();

        if (islands.size() > 1)
            Message.PERMISSION_CHANGED_ALL.send(dispatcher, Formatters.CAPITALIZED_FORMATTER.format(islandPrivilege.getName()));
        else if (targetPlayer == null)
            Message.PERMISSION_CHANGED_NAME.send(dispatcher, Formatters.CAPITALIZED_FORMATTER.format(islandPrivilege.getName()), islands.get(0).getName());
        else
            Message.PERMISSION_CHANGED.send(dispatcher, Formatters.CAPITALIZED_FORMATTER.format(islandPrivilege.getName()), targetPlayer.getName());
    }

}
