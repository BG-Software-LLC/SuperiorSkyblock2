package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.events.IslandJoinEvent;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.CommandTabCompletes;
import com.bgsoftware.superiorskyblock.commands.IAdminIslandCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.island.IslandUtils;
import com.bgsoftware.superiorskyblock.island.role.SPlayerRole;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class CmdAdminAdd implements IAdminIslandCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("add");
    }

    @Override
    public String getPermission() {
        return "superior.admin.add";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "admin add <" +
                Message.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "/" +
                Message.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + "> <" +
                Message.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_ADMIN_ADD.getMessage(locale);
    }

    @Override
    public int getMinArgs() {
        return 4;
    }

    @Override
    public int getMaxArgs() {
        return 4;
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return true;
    }

    @Override
    public boolean supportMultipleIslands() {
        return false;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, @Nullable SuperiorPlayer superiorPlayer, Island island, String[] args) {
        SuperiorPlayer targetPlayer = CommandArguments.getPlayer(plugin, sender, args[3]);

        if (targetPlayer == null)
            return;

        if (targetPlayer.getIsland() != null) {
            Message.PLAYER_ALREADY_IN_ISLAND.send(sender);
            return;
        }

        if (!plugin.getEventsBus().callIslandJoinEvent(targetPlayer, island, IslandJoinEvent.Cause.ADMIN))
            return;

        IslandUtils.sendMessage(island, Message.JOIN_ANNOUNCEMENT, Collections.emptyList(), targetPlayer.getName());

        island.revokeInvite(targetPlayer);
        island.addMember(targetPlayer, SPlayerRole.defaultRole());

        if (superiorPlayer == null) {
            Message.JOINED_ISLAND_NAME.send(targetPlayer, island.getName());
            Message.ADMIN_ADD_PLAYER_NAME.send(sender, targetPlayer.getName(), island.getName());
        } else {
            Message.JOINED_ISLAND.send(targetPlayer, superiorPlayer.getName());
            Message.ADMIN_ADD_PLAYER.send(sender, targetPlayer.getName(), superiorPlayer.getName());
        }

        if (plugin.getSettings().isTeleportOnJoin() && targetPlayer.isOnline())
            targetPlayer.teleport(island);
        if (plugin.getSettings().isClearOnJoin())
            plugin.getNMSPlayers().clearInventory(targetPlayer.asOfflinePlayer());
    }

    @Override
    public List<String> adminTabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, Island island, String[] args) {
        return args.length == 4 ? CommandTabCompletes.getOnlinePlayers(plugin, args[2], false,
                superiorPlayer -> superiorPlayer.getIsland() == null) : Collections.emptyList();
    }

}
