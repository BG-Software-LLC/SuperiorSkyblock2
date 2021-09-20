package com.bgsoftware.superiorskyblock.commands.player;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.ISuperiorCommand;
import com.bgsoftware.superiorskyblock.island.SPlayerRole;
import com.bgsoftware.superiorskyblock.commands.CommandTabCompletes;
import com.bgsoftware.superiorskyblock.utils.events.EventsCaller;
import com.bgsoftware.superiorskyblock.utils.islands.IslandUtils;
import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class CmdAccept implements ISuperiorCommand {

    @Override
    public List<String> getAliases() {
        return Arrays.asList("accept", "join");
    }

    @Override
    public String getPermission() {
        return "superior.island.accept";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "accept <" +
                Locale.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "/" +
                Locale.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Locale.COMMAND_DESCRIPTION_ACCEPT.getMessage(locale);
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
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(sender);
        SuperiorPlayer targetPlayer = plugin.getPlayers().getSuperiorPlayer(args[1]);
        Island island;

        if(targetPlayer == null){
            if((island = plugin.getGrid().getIsland(args[1])) == null || !island.isInvited(superiorPlayer)){
                Locale.NO_ISLAND_INVITE.send(superiorPlayer);
                return;
            }
        }
        else{
            if((island = plugin.getGrid().getIsland(targetPlayer)) == null || !island.isInvited(superiorPlayer)) {
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

        if(!EventsCaller.callIslandJoinEvent(superiorPlayer, island))
            return;

        IslandUtils.sendMessage(island, Locale.JOIN_ANNOUNCEMENT, new ArrayList<>(), superiorPlayer.getName());

        island.revokeInvite(superiorPlayer);
        island.addMember(superiorPlayer, SPlayerRole.defaultRole());

        if(targetPlayer == null)
            Locale.JOINED_ISLAND_NAME.send(superiorPlayer, island.getName());
        else
            Locale.JOINED_ISLAND.send(superiorPlayer, targetPlayer.getName());

        if(plugin.getSettings().isTeleportOnJoin())
            superiorPlayer.teleport(island);
        if(plugin.getSettings().isClearOnJoin())
            plugin.getNMSPlayers().clearInventory(superiorPlayer.asPlayer());
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(sender);
        return args.length == 2 ? CommandTabCompletes.getOnlinePlayersWithIslands(plugin, args[1],
                plugin.getSettings().isTabCompleteHideVanished(), (onlinePlayer, onlineIsland) ->
                        onlineIsland != null && onlineIsland.isInvited(superiorPlayer)) : new ArrayList<>();
    }

}
