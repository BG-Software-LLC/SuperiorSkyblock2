package com.bgsoftware.superiorskyblock.commands;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.island.SIsland;
import com.bgsoftware.superiorskyblock.wrappers.player.SSuperiorPlayer;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class CmdTransfer implements ISuperiorCommand {

    @Override
    public List<String> getAliases() {
        return Arrays.asList("transfer", "leader", "leadership");
    }

    @Override
    public String getPermission() {
        return "superior.island.transfer";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "transfer <" + Locale.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Locale.COMMAND_DESCRIPTION_TRANSFER.getMessage(locale);
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
        SuperiorPlayer player = SSuperiorPlayer.of(sender);
        Island island = player.getIsland();

        if (island == null) {
            Locale.INVALID_ISLAND.send(player);
            return;
        }

        if (!player.getPlayerRole().isLastRole()) {
            Locale.NO_TRANSFER_PERMISSION.send(player);
            return;
        }

        SuperiorPlayer targetPlayer = SSuperiorPlayer.of(args[1]);

        if (targetPlayer == null) {
            Locale.INVALID_PLAYER.send(sender, args[1]);
            return;
        }

        if (!island.isMember(targetPlayer)) {
            Locale.PLAYER_NOT_INSIDE_ISLAND.send(sender);
            return;
        }

        if (island.getOwner().getUniqueId().equals(targetPlayer.getUniqueId())) {
            Locale.TRANSFER_ALREADY_LEADER.send(player);
            return;
        }

        if(island.transferIsland(targetPlayer))
            ((SIsland) island).sendMessage(Locale.TRANSFER_BROADCAST, new ArrayList<>(), targetPlayer.getName());
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(sender);
        Island island = superiorPlayer.getIsland();

        if(args.length == 2 && island != null && superiorPlayer.getPlayerRole().isLastRole()){
            List<String> list = new ArrayList<>();

            for(SuperiorPlayer targetPlayer : island.getIslandMembers(false)){
                if(targetPlayer.getName().toLowerCase().startsWith(args[1].toLowerCase())){
                    list.add(targetPlayer.getName());
                }
            }

            return list;
        }

        return new ArrayList<>();
    }

}
