package com.bgsoftware.superiorskyblock.commands.player;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.CommandContext;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgument;
import com.bgsoftware.superiorskyblock.api.events.IslandJoinEvent;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.InternalSuperiorCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArgumentsBuilder;
import com.bgsoftware.superiorskyblock.commands.arguments.types.IslandArgumentType;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.island.IslandUtils;
import com.bgsoftware.superiorskyblock.island.role.SPlayerRole;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CmdAccept implements InternalSuperiorCommand {

    @Override
    public List<String> getAliases() {
        return Arrays.asList("accept", "join");
    }

    @Override
    public String getPermission() {
        return "superior.island.accept";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_ACCEPT.getMessage(locale);
    }

    @Override
    public List<CommandArgument<?>> getArguments() {
        return new CommandArgumentsBuilder()
                .add(CommandArgument.optional("island", IslandArgumentType.INSTANCE, Message.COMMAND_ARGUMENT_PLAYER_NAME, Message.COMMAND_ARGUMENT_ISLAND_NAME))
                .build();
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return false;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, CommandContext context) {
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(context.getDispatcher());

        if (superiorPlayer.getIsland() != null) {
            Message.JOIN_WHILE_IN_ISLAND.send(superiorPlayer);
            return;
        }

        IslandArgumentType.Result result = context.getOptionalArgument("island", IslandArgumentType.Result.class).orElseGet(() -> {
            List<Island> playerPendingInvites = superiorPlayer.getInvites();
            return new IslandArgumentType.Result(playerPendingInvites.isEmpty() ? null : playerPendingInvites.get(0), null);
        });

        Island island = result.getIsland();
        if (island == null || !island.isInvited(superiorPlayer)) {
            Message.NO_ISLAND_INVITE.send(superiorPlayer);
            return;
        }

        if (island.getTeamLimit() >= 0 && island.getIslandMembers(true).size() >= island.getTeamLimit()) {
            Message.JOIN_FULL_ISLAND.send(superiorPlayer);
            island.revokeInvite(superiorPlayer);
            return;
        }

        if (!plugin.getEventsBus().callIslandJoinEvent(superiorPlayer, island, IslandJoinEvent.Cause.INVITE))
            return;

        IslandUtils.sendMessage(island, Message.JOIN_ANNOUNCEMENT, Collections.emptyList(), superiorPlayer.getName());

        island.revokeInvite(superiorPlayer);
        island.addMember(superiorPlayer, SPlayerRole.defaultRole());

        SuperiorPlayer targetPlayer = result.getTargetPlayer();
        if (targetPlayer == null)
            Message.JOINED_ISLAND_NAME.send(superiorPlayer, island.getName());
        else
            Message.JOINED_ISLAND.send(superiorPlayer, targetPlayer.getName());

        if (plugin.getSettings().isTeleportOnJoin())
            superiorPlayer.teleport(island);
        if (plugin.getSettings().isClearOnJoin())
            plugin.getNMSPlayers().clearInventory(superiorPlayer.asPlayer());
    }

}
