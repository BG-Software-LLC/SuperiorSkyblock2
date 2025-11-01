package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.CommandTabCompletes;
import com.bgsoftware.superiorskyblock.commands.IAdminPlayerCommand;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEventsFactory;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class CmdAdminPromote implements IAdminPlayerCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("promote");
    }

    @Override
    public String getPermission() {
        return "superior.admin.promote";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "admin promote <" + Message.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_ADMIN_PROMOTE.getMessage(locale);
    }

    @Override
    public int getMinArgs() {
        return 3;
    }

    @Override
    public int getMaxArgs() {
        return 3;
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return true;
    }

    @Override
    public boolean supportMultiplePlayers() {
        return false;
    }

    @Override
    public boolean requireIsland() {
        return true;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, SuperiorPlayer targetPlayer, String[] args) {
        Island island = targetPlayer.getIsland();

        if (island == null) {
            Message.INVALID_ISLAND_OTHER.send(sender, targetPlayer.getName());
            return;
        }

        PlayerRole currentRole = targetPlayer.getPlayerRole();

        if (currentRole.isLastRole()) {
            Message.LAST_ROLE_PROMOTE.send(sender);
            return;
        }

        PlayerRole nextRole = currentRole;
        int roleLimit;


        do {
            nextRole = nextRole.getNextRole();
            roleLimit = nextRole == null ? -1 : island.getRoleLimit(nextRole);
        } while (nextRole != null && !nextRole.isLastRole() &&
                roleLimit >= 0 && island.getIslandMembers(nextRole).size() >= roleLimit);

        if (nextRole == null || nextRole.isLastRole()) {
            Message.LAST_ROLE_PROMOTE.send(sender);
            return;
        }

        if (!PluginEventsFactory.callPlayerChangeRoleEvent(targetPlayer, nextRole))
            return;

        targetPlayer.setPlayerRole(nextRole);

        Message.PROMOTED_MEMBER.send(sender, targetPlayer.getName(), targetPlayer.getPlayerRole());
        Message.GOT_PROMOTED.send(targetPlayer, targetPlayer.getPlayerRole());
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        return args.length == 3 ? CommandTabCompletes.getOnlinePlayersWithIsland(plugin, args[2], false,
                superiorPlayer -> !superiorPlayer.getIslandLeader().equals(superiorPlayer) &&
                        !superiorPlayer.getPlayerRole().getNextRole().isLastRole()) : Collections.emptyList();
    }

}
