package com.bgsoftware.superiorskyblock.commands.command.admin;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandRole;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.commands.ICommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public final class CmdAdminJoin implements ICommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("join");
    }

    @Override
    public String getPermission() {
        return "superior.admin.join";
    }

    @Override
    public String getUsage() {
        return "island admin join <player-name>";
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
        return false;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(sender);

        if(superiorPlayer.getIsland() != null){
            Locale.ALREADY_IN_ISLAND.send(superiorPlayer);
            return;
        }

        SuperiorPlayer targetPlayer = SSuperiorPlayer.of(args[2]);

        if(targetPlayer == null){
            Locale.INVALID_PLAYER.send(superiorPlayer, args[2]);
            return;
        }

        Island island = targetPlayer.getIsland();

        if(island == null){
            Locale.INVALID_ISLAND_OTHER.send(superiorPlayer, targetPlayer.getName());
            return;
        }

        for(UUID uuid : island.getAllMembers()){
            if(Bukkit.getOfflinePlayer(uuid).isOnline()){
                Locale.JOIN_ANNOUNCEMENT.send(Bukkit.getPlayer(uuid), superiorPlayer.getName());
            }
        }

        island.addMember(superiorPlayer, IslandRole.MEMBER);

        Locale.JOINED_ISLAND.send(superiorPlayer, island.getOwner().getName());
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        return null;
    }
}
