package com.bgsoftware.superiorskyblock.commands.command.admin;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandRole;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.commands.ICommand;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public final class CmdAdminPromote implements ICommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("promote");
    }

    @Override
    public String getPermission() {
        return "superior.admin.promote";
    }

    @Override
    public String getUsage() {
        return "island admin promote <player-name>";
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
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        SuperiorPlayer targetPlayer = SSuperiorPlayer.of(args[2]);

        if(targetPlayer == null){
            Locale.INVALID_PLAYER.send(sender, args[2]);
            return;
        }

        Island island = targetPlayer.getIsland();

        if(island == null){
            Locale.INVALID_ISLAND_OTHER.send(sender, targetPlayer.getName());
            return;
        }

        if(targetPlayer.getIslandRole().getNextRole() == IslandRole.LEADER){
            Locale.LAST_ROLE_PROMOTE.send(sender);
            return;
        }

        targetPlayer.setIslandRole(targetPlayer.getIslandRole().getNextRole());

        Locale.PROMOTED_MEMBER.send(sender, targetPlayer.getName(), targetPlayer.getIslandRole());
        Locale.GOT_PROMOTED.send(targetPlayer, targetPlayer.getIslandRole());
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        return null;
    }
}
