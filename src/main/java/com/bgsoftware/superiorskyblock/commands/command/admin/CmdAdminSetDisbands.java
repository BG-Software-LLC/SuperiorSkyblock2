package com.bgsoftware.superiorskyblock.commands.command.admin;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.ICommand;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class CmdAdminSetDisbands implements ICommand {
    @Override
    public List<String> getAliases() {
        return Collections.singletonList("setdisbands");
    }

    @Override
    public String getPermission() {
        return "superior.admin.setdisbands";
    }

    @Override
    public String getUsage() {
        return "is admin setdisbands <player-name> <amount>";
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

        targetPlayer.setDisbands(amount);

        if (!sender.equals(targetPlayer.asPlayer()))
            Locale.DISBAND_SET_OTHER.send(sender, targetPlayer.getName(), targetPlayer.getDisbands());
        Locale.DISBAND_SET.send(targetPlayer, amount);
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        return null;
    }
}
