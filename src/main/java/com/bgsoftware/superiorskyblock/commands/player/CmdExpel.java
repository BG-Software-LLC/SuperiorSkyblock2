package com.bgsoftware.superiorskyblock.commands.player;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgument;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.InternalPermissibleCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArgumentsBuilder;
import com.bgsoftware.superiorskyblock.commands.arguments.types.PlayerArgumentType;
import com.bgsoftware.superiorskyblock.commands.context.IslandCommandContext;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.island.privilege.IslandPrivileges;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class CmdExpel implements InternalPermissibleCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("expel");
    }

    @Override
    public String getPermission() {
        return "superior.island.expel";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_EXPEL.getMessage(locale);
    }

    @Override
    public List<CommandArgument<?>> getArguments() {
        return new CommandArgumentsBuilder()
                .add(CommandArgument.required("player", PlayerArgumentType.ONLINE_PLAYERS, Message.COMMAND_ARGUMENT_PLAYER_NAME))
                .build();
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return true;
    }

    @Override
    public IslandPrivilege getPrivilege() {
        return IslandPrivileges.EXPEL_PLAYERS;
    }

    @Override
    public Message getPermissionLackMessage() {
        return Message.NO_EXPEL_PERMISSION;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, IslandCommandContext context) {
        CommandSender dispatcher = context.getDispatcher();

        SuperiorPlayer targetPlayer = context.getRequiredArgument("player", SuperiorPlayer.class);

        Location targetLocation = targetPlayer.getLocation();
        Island targetIsland = plugin.getGrid().getIslandAt(targetLocation);

        if (targetIsland == null) {
            Message.PLAYER_NOT_INSIDE_ISLAND.send(dispatcher);
            return;
        }

        // Checking requirements for players
        if (dispatcher instanceof Player) {
            Island playerIsland = context.getIsland();

            if (!targetIsland.equals(playerIsland)) {
                Message.PLAYER_NOT_INSIDE_ISLAND.send(dispatcher);
                return;
            }

            if (targetIsland.hasPermission(targetPlayer, IslandPrivileges.EXPEL_BYPASS)) {
                Message.PLAYER_EXPEL_BYPASS.send(dispatcher);
                return;
            }
        }

        targetPlayer.teleport(plugin.getGrid().getSpawnIsland());
        targetLocation.setDirection(plugin.getGrid().getSpawnIsland()
                .getCenter(plugin.getSettings().getWorlds().getDefaultWorld()).getDirection());
        Message.EXPELLED_PLAYER.send(dispatcher, targetPlayer.getName());
        Message.GOT_EXPELLED.send(targetPlayer, dispatcher.getName());
    }

}
