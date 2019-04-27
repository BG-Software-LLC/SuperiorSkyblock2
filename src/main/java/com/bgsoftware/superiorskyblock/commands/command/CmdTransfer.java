package com.bgsoftware.superiorskyblock.commands.command;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandRole;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.ICommand;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class CmdTransfer implements ICommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("transfer");
    }

    @Override
    public String getPermission() {
        return "superior.island.transfer";
    }

    @Override
    public String getUsage() {
        return "island transfer <player-name>";
    }

    @Override
    public int getMinArgs() {
        return 2;
    }

    @Override
    public int getMaxArgs() {
        return 2;
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return false;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        SuperiorPlayer player = SSuperiorPlayer.of((Player) sender);

        if (player == null)
            return;

        Island island = player.getIsland();

        if (island == null) {
            Locale.PLAYER_NOT_INSIDE_ISLAND.send(player);
            return;
        }

        if (player.getIslandRole() != IslandRole.LEADER) {
            Locale.NO_TRANSFER_PERMISSION.send(player);
            return;
        }

        SuperiorPlayer target = SSuperiorPlayer.of(args[1]);
        if (target == null) {
            Locale.INVALID_PLAYER.send(sender);
            return;
        }

        if (!island.isMember(target)) {
            Locale.TRANSFER_NOT_A_MEMBER.send(sender);
            return;
        }

        if (island.getOwner().getUniqueId().equals(target.getUniqueId())) {
            Locale.TRANSFER_ALREADY_LEADER.send(player);
            return;
        }

        island.transfer(target);
        island.sendMessage(Locale.TRANSFER_BROADCAST.getMessage(target.getName()));
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        return null;
    }
}
