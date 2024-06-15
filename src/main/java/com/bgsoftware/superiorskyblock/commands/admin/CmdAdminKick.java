package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.common.collections.Lists;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.BaseCommand;
import com.bgsoftware.superiorskyblock.commands.IAdminPlayerCommand;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.island.IslandUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class CmdAdminKick extends BaseCommand implements IAdminPlayerCommand {

    @Override
    protected List<String> aliases() {
        return Lists.singleton("kick");
    }

    @Override
    protected String permission() {
        return "superior.admin.kick";
    }

    @Override
    protected String usage(java.util.Locale locale) {
        return "admin kick <" + Message.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + ">";
    }

    @Override
    protected String description(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_ADMIN_KICK.getMessage(locale);
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
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, SuperiorPlayer targetPlayer, String[] args) {
        Island targetIsland = targetPlayer.getIsland();

        if (targetIsland == null) {
            Message.INVALID_ISLAND_OTHER.send(sender, targetPlayer.getName());
            return;
        }

        if (targetIsland.getOwner() == targetPlayer) {
            Message.KICK_ISLAND_LEADER.send(sender);
            return;
        }

        IslandUtils.handleKickPlayer(sender instanceof Player ? plugin.getPlayers().getSuperiorPlayer(sender) : null,
                sender.getName(), targetIsland, targetPlayer);
    }

}
