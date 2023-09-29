package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgument;
import com.bgsoftware.superiorskyblock.api.events.IslandJoinEvent;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.InternalIslandCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArgumentsBuilder;
import com.bgsoftware.superiorskyblock.commands.arguments.types.IslandArgumentType;
import com.bgsoftware.superiorskyblock.commands.arguments.types.PlayerArgumentType;
import com.bgsoftware.superiorskyblock.commands.context.IslandCommandContext;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.island.IslandUtils;
import com.bgsoftware.superiorskyblock.island.role.SPlayerRole;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class CmdAdminAdd implements InternalIslandCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("add");
    }

    @Override
    public String getPermission() {
        return "superior.admin.add";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_ADMIN_ADD.getMessage(locale);
    }

    @Override
    public List<CommandArgument<?>> getArguments() {
        return new CommandArgumentsBuilder()
                .add(CommandArguments.required("island", IslandArgumentType.INCLUDE_PLAYERS, Message.COMMAND_ARGUMENT_PLAYER_NAME, Message.COMMAND_ARGUMENT_ISLAND_NAME))
                .add(CommandArguments.required("player", PlayerArgumentType.ALL_PLAYERS, Message.COMMAND_ARGUMENT_PLAYER_NAME))
                .build();
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return true;
    }

    @Override
    public boolean isSelfIsland() {
        return false;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, IslandCommandContext context) {
        CommandSender dispatcher = context.getDispatcher();

        SuperiorPlayer targetPlayer = context.getRequiredArgument("player", SuperiorPlayer.class);

        if (targetPlayer.getIsland() != null) {
            Message.PLAYER_ALREADY_IN_ISLAND.send(dispatcher);
            return;
        }
        Island island = context.getIsland();

        if (!plugin.getEventsBus().callIslandJoinEvent(targetPlayer, island, IslandJoinEvent.Cause.ADMIN))
            return;

        IslandUtils.sendMessage(island, Message.JOIN_ANNOUNCEMENT, Collections.emptyList(), targetPlayer.getName());

        island.revokeInvite(targetPlayer);
        island.addMember(targetPlayer, SPlayerRole.defaultRole());

        SuperiorPlayer superiorPlayer = context.getTargetPlayer();

        if (superiorPlayer == null) {
            Message.JOINED_ISLAND_NAME.send(targetPlayer, island.getName());
            Message.ADMIN_ADD_PLAYER_NAME.send(dispatcher, targetPlayer.getName(), island.getName());
        } else {
            Message.JOINED_ISLAND.send(targetPlayer, dispatcher.getName());
            Message.ADMIN_ADD_PLAYER.send(dispatcher, targetPlayer.getName(), superiorPlayer.getName());
        }

        if (plugin.getSettings().isTeleportOnJoin() && targetPlayer.isOnline())
            targetPlayer.teleport(island);
        if (plugin.getSettings().isClearOnJoin())
            plugin.getNMSPlayers().clearInventory(targetPlayer.asOfflinePlayer());
    }

}
