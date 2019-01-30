package com.bgsoftware.superiorskyblock.commands.command.admin;

import com.bgsoftware.superiorskyblock.wrappers.WrappedPlayer;
import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.commands.ICommand;
import com.bgsoftware.superiorskyblock.island.Island;
import com.bgsoftware.superiorskyblock.island.IslandRole;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class CmdAdminDemote implements ICommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("demote");
    }

    @Override
    public String getPermission() {
        return "superior.admin.demote";
    }

    @Override
    public String getUsage() {
        return "island admin demote <player-name>";
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
    public void execute(SuperiorSkyblock plugin, CommandSender sender, String[] args) {
        WrappedPlayer targetPlayer = WrappedPlayer.of(args[2]);

        if(targetPlayer == null){
            Locale.INVALID_PLAYER.send(sender, args[2]);
            return;
        }

        Island island = targetPlayer.getIsland();

        if(island == null){
            Locale.INVALID_ISLAND_OTHER.send(sender, targetPlayer.getName());
            return;
        }

        if(targetPlayer.getIslandRole().getPreviousRole() == IslandRole.GUEST){
            Locale.LAST_ROLE_DEMOTE.send(sender);
            return;
        }

        targetPlayer.setIslandRole(targetPlayer.getIslandRole().getPreviousRole());

        Locale.DEMOTED_MEMBER.send(sender, targetPlayer.getName(), targetPlayer.getIslandRole());
        Locale.GOT_DEMOTED.send(targetPlayer, targetPlayer.getIslandRole());
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblock plugin, CommandSender sender, String[] args) {
        return null;
    }
}
