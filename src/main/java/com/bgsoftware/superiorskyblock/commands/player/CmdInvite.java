package com.bgsoftware.superiorskyblock.commands.player;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.api.commands.CommandContext;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgument;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.InternalPermissibleCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArgumentsBuilder;
import com.bgsoftware.superiorskyblock.commands.arguments.SuggestionsSelector;
import com.bgsoftware.superiorskyblock.commands.arguments.types.PlayerArgumentType;
import com.bgsoftware.superiorskyblock.commands.context.IslandCommandContext;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.island.IslandUtils;
import com.bgsoftware.superiorskyblock.island.privilege.IslandPrivileges;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CmdInvite implements InternalPermissibleCommand {

    @Override
    public List<String> getAliases() {
        return Arrays.asList("invite", "add");
    }

    @Override
    public String getPermission() {
        return "superior.island.invite";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_INVITE.getMessage(locale);
    }

    @Override
    public List<CommandArgument<?>> getArguments() {
        return new CommandArgumentsBuilder()
                .add(CommandArgument.required("player", PlayerArgumentType.allOf(Selector.INSTANCE), Message.COMMAND_ARGUMENT_PLAYER_NAME))
                .build();
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return false;
    }

    @Override
    public IslandPrivilege getPrivilege() {
        return IslandPrivileges.INVITE_MEMBER;
    }

    @Override
    public Message getPermissionLackMessage() {
        return Message.NO_INVITE_PERMISSION;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, IslandCommandContext context) {
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(context.getDispatcher());
        Island island = context.getIsland();

        SuperiorPlayer targetPlayer = context.getRequiredArgument("player", SuperiorPlayer.class);

        if (island.isMember(targetPlayer)) {
            Message.ALREADY_IN_ISLAND_OTHER.send(superiorPlayer);
            return;
        }

        if (island.isBanned(targetPlayer)) {
            Message.INVITE_BANNED_PLAYER.send(superiorPlayer);
            return;
        }

        Message announcementMessage;

        if (island.isInvited(targetPlayer)) {
            island.revokeInvite(targetPlayer);
            announcementMessage = Message.REVOKE_INVITE_ANNOUNCEMENT;
            Message.GOT_REVOKED.send(targetPlayer, superiorPlayer.getName());
        } else {
            if (island.getTeamLimit() >= 0 && island.getIslandMembers(true).size() >= island.getTeamLimit()) {
                Message.INVITE_TO_FULL_ISLAND.send(superiorPlayer);
                return;
            }

            if (!plugin.getEventsBus().callIslandInviteEvent(superiorPlayer, targetPlayer, island))
                return;

            island.inviteMember(targetPlayer);
            announcementMessage = Message.INVITE_ANNOUNCEMENT;

            Message.GOT_INVITE.send(targetPlayer, superiorPlayer.getName());
        }

        IslandUtils.sendMessage(island, announcementMessage, Collections.emptyList(), superiorPlayer.getName(), targetPlayer.getName());
    }

    private static class Selector implements SuggestionsSelector<SuperiorPlayer> {

        private static final Selector INSTANCE = new Selector();

        @Override
        public List<SuperiorPlayer> getAllPossibilities(SuperiorSkyblock plugin, CommandContext context) {
            SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer((Player) context.getDispatcher());
            Island island = superiorPlayer.getIsland();
            return island == null ? Collections.emptyList() : plugin.getPlayers().getAllPlayers();
        }

        @Override
        public boolean check(SuperiorSkyblock plugin, CommandContext context, SuperiorPlayer targetPlayer) {
            SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer((Player) context.getDispatcher());
            Island island = superiorPlayer.getIsland();
            return !island.isMember(targetPlayer) && !island.isBanned(targetPlayer);
        }
    }

}
