package com.bgsoftware.superiorskyblock.commands.command;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPermission;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.island.SPlayerRole;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.commands.ICommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public final class CmdSetRole implements ICommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("setrole");
    }

    @Override
    public String getPermission() {
        return "superior.island.setrole";
    }

    @Override
    public String getUsage() {
        return "island setrole <player-name> <island-role>";
    }

    @Override
    public String getDescription() {
        return Locale.COMMAND_DESCRIPTION_SET_ROLE.getMessage();
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
        SuperiorPlayer targetPlayer = SSuperiorPlayer.of(args[1]);

        if(targetPlayer == null){
            Locale.INVALID_PLAYER.send(sender, args[1]);
            return;
        }

        if(targetPlayer.getName().equals(sender.getName())){
            Locale.SELF_ROLE_CHANGE.send(sender);
            return;
        }

        PlayerRole playerRole = null;

        try{
            playerRole = SPlayerRole.of(args[2]);
        }catch(IllegalArgumentException ignored){}

        if(playerRole == null || !playerRole.isRoleLadder()){
            Locale.INVALID_ROLE.send(sender, args[2], SPlayerRole.getValuesString());
            return;
        }

        Island island = targetPlayer.getIsland();

        if(sender instanceof Player){
            SuperiorPlayer superiorPlayer = SSuperiorPlayer.of((Player) sender);
            island = superiorPlayer.getIsland();

            if(island == null){
                Locale.INVALID_ISLAND.send(superiorPlayer);
                return;
            }

            if(!superiorPlayer.hasPermission(IslandPermission.SET_ROLE)){
                Locale.NO_SET_ROLE_PERMISSION.send(superiorPlayer, island.getRequiredPlayerRole(IslandPermission.SET_ROLE));
                return;
            }

            if(!island.isMember(targetPlayer)){
                Locale.PLAYER_NOT_INSIDE_ISLAND.send(sender);
                return;
            }

            if(targetPlayer.getPlayerRole().isHigherThan(superiorPlayer.getPlayerRole()) ||
                    !playerRole.isLessThan(superiorPlayer.getPlayerRole())){
                Locale.CANNOT_SET_ROLE.send(sender, playerRole);
                return;
            }
        }else {
            if (island == null) {
                Locale.INVALID_ISLAND_OTHER.send(sender, targetPlayer.getName());
                return;
            }

            if(playerRole.isLastRole()){
                Locale.CANNOT_SET_ROLE.send(sender, playerRole);
                return;
            }
        }

        if(targetPlayer.getPlayerRole().equals(playerRole)){
            Locale.PLAYER_ALREADY_IN_ROLE.send(sender, targetPlayer.getName(), playerRole);
            return;
        }

        PlayerRole currentRole = targetPlayer.getPlayerRole();
        targetPlayer.setPlayerRole(playerRole);

        if(currentRole.isLessThan(playerRole)){
            Locale.PROMOTED_MEMBER.send(sender, targetPlayer.getName(), targetPlayer.getPlayerRole());
            Locale.GOT_PROMOTED.send(targetPlayer, targetPlayer.getPlayerRole());
        }else{
            Locale.DEMOTED_MEMBER.send(sender, targetPlayer.getName(), targetPlayer.getPlayerRole());
            Locale.GOT_DEMOTED.send(targetPlayer, targetPlayer.getPlayerRole());
        }
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(sender);
        Island island = superiorPlayer.getIsland();

        if(island != null && superiorPlayer.hasPermission(IslandPermission.SET_ROLE)){
            List<String> list = new ArrayList<>();
            SuperiorPlayer targetPlayer;

            if(args.length == 2) {
                for(UUID uuid : island.getAllMembers()){
                    targetPlayer = SSuperiorPlayer.of(uuid);
                    if(targetPlayer.getName().toLowerCase().startsWith(args[1].toLowerCase()))
                        list.add(targetPlayer.getName());
                }
            }
            else if(args.length == 3){
                for(PlayerRole playerRole : plugin.getPlayers().getRoles()) {
                    String roleName = playerRole.toString().trim().toLowerCase();
                    if(roleName.startsWith(args[2].toLowerCase()))
                        list.add(roleName);
                }
            }

            return list;
        }

        return new ArrayList<>();
    }
}
