package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.ISuperiorCommand;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CmdAdminSetDisbands implements ISuperiorCommand {
    @Override
    public List<String> getAliases() {
        return Collections.singletonList("setdisbands");
    }

    @Override
    public String getPermission() {
        return "superior.admin.setdisbands";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "admin setdisbands <" +
                Locale.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "/" +
                Locale.COMMAND_ARGUMENT_ALL_PLAYERS.getMessage(locale) + "> <" +
                Locale.COMMAND_ARGUMENT_AMOUNT.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Locale.COMMAND_DESCRIPTION_ADMIN_SET_DISBANDS.getMessage(locale);
    }

    @Override
    public int getMinArgs() {
        return 4;
    }

    @Override
    public int getMaxArgs() {
        return 4;
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return true;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        List<SuperiorPlayer> superiorPlayers = new ArrayList<>();

        if(args[2].equalsIgnoreCase("*")){
            superiorPlayers.addAll(plugin.getPlayers().getAllPlayers());
        }

        else {
            SuperiorPlayer targetPlayer = SSuperiorPlayer.of(args[2]);

            if (targetPlayer == null) {
                Locale.INVALID_PLAYER.send(sender, args[2]);
                return;
            }

            superiorPlayers.add(targetPlayer);
        }

        int amount;
        try {
            amount = Integer.parseInt(args[3]);
        } catch (Exception e) {
            Locale.INVALID_AMOUNT.send(sender);
            return;
        }

        Executor.data(() -> superiorPlayers.forEach(superiorPlayer -> superiorPlayer.setDisbands(amount)));

        if(superiorPlayers.size() > 1){
            Locale.DISBAND_SET_ALL.send(sender, amount);
        }
        else if (!sender.equals(superiorPlayers.get(0).asPlayer()))
            Locale.DISBAND_SET_OTHER.send(sender, superiorPlayers.get(0).getName(), amount);

        superiorPlayers.forEach(superiorPlayer -> {
            if(superiorPlayer.isOnline()){
                Locale.DISBAND_SET.send(superiorPlayer, amount);
            }
        });
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
