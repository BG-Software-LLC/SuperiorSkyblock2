package com.bgsoftware.superiorskyblock.commands.command.admin;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.commands.ICommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

public final class CmdAdminDisband implements ICommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("disband");
    }

    @Override
    public String getPermission() {
        return "superior.admin.disband";
    }

    @Override
    public String getUsage() {
        return "island admin disband <player-name>";
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

        for(UUID uuid : island.getAllMembers()){
            if(Bukkit.getOfflinePlayer(uuid).isOnline()){
                Locale.DISBAND_ANNOUNCEMENT.send(Bukkit.getPlayer(uuid), sender.getName());
            }
        }

        Locale.DISBANDED_ISLAND.send(sender, targetPlayer.getName());

        island.disbandIsland();
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        return null;
    }
}
