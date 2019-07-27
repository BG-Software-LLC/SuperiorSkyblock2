package com.bgsoftware.superiorskyblock.commands.command.admin;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.ICommand;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CmdAdminGiveDisbands implements ICommand {
    @Override
    public List<String> getAliases() {
        return Collections.singletonList("givedisbands");
    }

    @Override
    public String getPermission() {
        return "superior.admin.givedisbands";
    }

    @Override
    public String getUsage() {
        return "is admin givedisbands <player-name> <amount>";
    }

    @Override
    public String getDescription() {
        return Locale.COMMAND_DESCRIPTION_ADMIN_GIVE_DISBANDS.getMessage();
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
        SuperiorPlayer targetPlayer = SSuperiorPlayer.of(args[2]);

        if(targetPlayer == null){
            Locale.INVALID_PLAYER.send(sender, args[2]);
            return;
        }

        int amount;
        try {
            amount = Integer.valueOf(args[3]);
        } catch (Exception e) {
            Locale.INVALID_AMOUNT.send(sender);
            return;
        }

        targetPlayer.setDisbands(targetPlayer.getDisbands() + amount);

        if (!sender.equals(targetPlayer.asPlayer()))
            Locale.DISBAND_GIVE_OTHER.send(sender, targetPlayer.getName(), targetPlayer.getDisbands());
        Locale.DISBAND_GIVE.send(targetPlayer, amount);
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        List<String> list = new ArrayList<>();

        if(args.length == 3){
            for(Player player : Bukkit.getOnlinePlayers()){
                if(!player.equals(sender) && player.getName().toLowerCase().startsWith(args[2].toLowerCase()) &&
                        SSuperiorPlayer.of(player).getIsland() != null){
                    list.add(player.getName());
                }
            }
        }

        return list;
    }
}
