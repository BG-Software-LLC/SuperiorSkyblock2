package com.bgsoftware.superiorskyblock.commands.command.admin;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.commands.ICommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CmdAdminDemote implements ICommand {

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
    public String getDescription() {
        return Locale.COMMAND_DESCRIPTION_ADMIN_DEMOTE.getMessage();
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

        if(targetPlayer.getPlayerRole().isFirstRole()){
            Locale.LAST_ROLE_DEMOTE.send(sender);
            return;
        }

        targetPlayer.setPlayerRole(targetPlayer.getPlayerRole().getPreviousRole());

        Locale.DEMOTED_MEMBER.send(sender, targetPlayer.getName(), targetPlayer.getPlayerRole());
        Locale.GOT_DEMOTED.send(targetPlayer, targetPlayer.getPlayerRole());
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        List<String> list = new ArrayList<>();

        if(args.length == 3){
            for(Player player : Bukkit.getOnlinePlayers()){
                SuperiorPlayer onlinePlayer = SSuperiorPlayer.of(player);
                Island playerIsland = onlinePlayer.getIsland();
                if (playerIsland != null) {
                    if (player.getName().toLowerCase().startsWith(args[2].toLowerCase()))
                        list.add(player.getName());
                    if(!playerIsland.getName().isEmpty() && playerIsland.getName().toLowerCase().startsWith(args[2].toLowerCase()))
                        list.add(playerIsland.getName());
                }
            }
        }

        return list;
    }
}
