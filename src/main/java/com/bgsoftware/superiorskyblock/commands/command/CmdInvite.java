package com.bgsoftware.superiorskyblock.commands.command;

import com.bgsoftware.superiorskyblock.wrappers.WrappedPlayer;
import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.commands.ICommand;
import com.bgsoftware.superiorskyblock.island.Island;
import com.bgsoftware.superiorskyblock.island.IslandPermission;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class CmdInvite implements ICommand {

    @Override
    public List<String> getAliases() {
        return Arrays.asList("invite", "add");
    }

    @Override
    public String getPermission() {
        return "superior.island.invite";
    }

    @Override
    public String getUsage() {
        return "island invite <player-name>";
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
    public void execute(SuperiorSkyblock plugin, CommandSender sender, String[] args) {
        WrappedPlayer wrappedPlayer = WrappedPlayer.of(sender);
        Island island = wrappedPlayer.getIsland();

        if(island == null){
            Locale.INVALID_ISLAND.send(wrappedPlayer);
            return;
        }

        if(!wrappedPlayer.hasPermission(IslandPermission.INVITE_MEMBER)){
            Locale.NO_INVITE_PERMISSION.send(wrappedPlayer, island.getRequiredRole(IslandPermission.INVITE_MEMBER));
            return;
        }

        WrappedPlayer targetPlayer = WrappedPlayer.of(args[1]);

        if(targetPlayer == null){
            Locale.INVALID_PLAYER.send(wrappedPlayer, args[1]);
            return;
        }

        if(island.isMember(targetPlayer)){
            Locale.ALREADY_IN_ISLAND_OTHER.send(wrappedPlayer);
            return;
        }

        if(island.isBanned(targetPlayer)){
            Locale.INVITE_BANNED_PLAYER.send(wrappedPlayer);
            return;
        }

        String message;

        if(island.isInvited(targetPlayer)){
            island.revokeInvite(targetPlayer);
            message = Locale.REVOKE_INVITE_ANNOUNCEMENT.getMessage(wrappedPlayer.getName(), targetPlayer.getName());
            if(targetPlayer.asOfflinePlayer().isOnline())
                Locale.GOT_REVOKED.send(targetPlayer, wrappedPlayer.getName());
        }
        else {
            if(island.getTeamLimit() >= 0 && island.getAllMembers().size() >= island.getTeamLimit()){
                Locale.INVITE_TO_FULL_ISLAND.send(wrappedPlayer);
                return;
            }

            island.inviteMember(targetPlayer);
            message = Locale.INVITE_ANNOUNCEMENT.getMessage(wrappedPlayer.getName(), targetPlayer.getName());
            if(targetPlayer.asOfflinePlayer().isOnline())
                Locale.GOT_INVITE.send(targetPlayer, wrappedPlayer.getName());
        }

        for (UUID uuid : island.getAllMembers()) {
            if (Bukkit.getOfflinePlayer(uuid).isOnline())
                Locale.sendMessage(Bukkit.getPlayer(uuid), message);
        }
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblock plugin, CommandSender sender, String[] args) {
        WrappedPlayer wrappedPlayer = WrappedPlayer.of(sender);
        Island island = wrappedPlayer.getIsland();

        if(args.length == 2 && island != null && wrappedPlayer.hasPermission(IslandPermission.INVITE_MEMBER)){
            List<String> list = new ArrayList<>();

            for(Player player : Bukkit.getOnlinePlayers()){
                if(!island.isMember(WrappedPlayer.of(player)) &&
                        player.getName().toLowerCase().startsWith(args[1].toLowerCase())){
                    list.add(player.getName());
                }
            }

            return list;
        }
        return new ArrayList<>();
    }
}
