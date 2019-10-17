package com.bgsoftware.superiorskyblock.commands.command;

import com.bgsoftware.superiorskyblock.api.events.IslandJoinEvent;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.island.SPlayerRole;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import com.google.common.collect.Lists;
import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.commands.ICommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public final class CmdAccept implements ICommand {

    @Override
    public List<String> getAliases() {
        return Arrays.asList("accept", "join");
    }

    @Override
    public String getPermission() {
        return "superior.island.accept";
    }

    @Override
    public String getUsage() {
        return "island accept <player-name/island-name>";
    }

    @Override
    public String getDescription() {
        return Locale.COMMAND_DESCRIPTION_ACCEPT.getMessage();
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
        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(sender);
        SuperiorPlayer targetPlayer = SSuperiorPlayer.of(args[1]);
        Island island;

        if(targetPlayer == null){
            if((island = plugin.getGrid().getIsland(args[1])) == null || !island.isInvited(superiorPlayer)){
                Locale.NO_ISLAND_INVITE.send(superiorPlayer);
                return;
            }
        }
        else{
            if(!(island = plugin.getGrid().getIsland(targetPlayer)).isInvited(superiorPlayer)) {
                Locale.NO_ISLAND_INVITE.send(superiorPlayer);
                return;
            }
        }

        if(superiorPlayer.getIsland() != null){
            Locale.JOIN_WHILE_IN_ISLAND.send(superiorPlayer);
            return;
        }

        if(island.getTeamLimit() >= 0 && island.getIslandMembers(true).size() >= island.getTeamLimit()){
            Locale.JOIN_FULL_ISLAND.send(superiorPlayer);
            island.revokeInvite(superiorPlayer);
            return;
        }

        IslandJoinEvent islandJoinEvent = new IslandJoinEvent(superiorPlayer, island);
        Bukkit.getPluginManager().callEvent(islandJoinEvent);

        if(islandJoinEvent.isCancelled())
            return;

        if(!Locale.JOIN_ANNOUNCEMENT.isEmpty())
            island.sendMessage(Locale.JOIN_ANNOUNCEMENT.getMessage(superiorPlayer.getName()));

        island.revokeInvite(superiorPlayer);
        island.addMember(superiorPlayer, SPlayerRole.defaultRole());

        if(targetPlayer == null)
            Locale.JOINED_ISLAND_NAME.send(superiorPlayer, island.getName());
        else
            Locale.JOINED_ISLAND.send(superiorPlayer, targetPlayer.getName());

        if(plugin.getSettings().teleportOnJoin)
            superiorPlayer.asPlayer().teleport(island.getTeleportLocation());
        if(plugin.getSettings().clearOnJoin)
            plugin.getNMSAdapter().clearInventory(superiorPlayer.asPlayer());
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        if(args.length == 2){
            SuperiorPlayer superiorPlayer = SSuperiorPlayer.of((Player) sender);
            List<String> list = Lists.newArrayList();
            Island island;

            for(UUID uuid : plugin.getGrid().getAllIslands()){
                island = plugin.getGrid().getIsland(SSuperiorPlayer.of(uuid));
                if(island != null && island.isInvited(superiorPlayer)) {
                    if(island.getOwner().getName().toLowerCase().startsWith(args[1].toLowerCase()))
                        list.add(island.getOwner().getName());
                    if(!island.getName().isEmpty() && island.getName().toLowerCase().startsWith(args[1].toLowerCase()))
                        list.add(island.getName());
                }
            }

            return list;
        }

        return Lists.newArrayList();
    }
}
