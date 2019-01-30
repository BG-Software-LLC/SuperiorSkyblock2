package com.bgsoftware.superiorskyblock.commands.command.admin;

import com.bgsoftware.superiorskyblock.wrappers.WrappedPlayer;
import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.commands.ICommand;
import com.bgsoftware.superiorskyblock.hooks.EconomyHook;
import com.bgsoftware.superiorskyblock.island.Island;

import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class CmdAdminDeposit implements ICommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("deposit");
    }

    @Override
    public String getPermission() {
        return "superior.admin.deposit";
    }

    @Override
    public String getUsage() {
        return "island admin deposit <player-name> <amount>";
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
    public void execute(SuperiorSkyblock plugin, CommandSender sender, String[] args) {
        if(!EconomyHook.isVaultEnabled()){
            Locale.sendMessage(sender, "&cServer doesn't have vault installed so island banks are disabled.");
            return;
        }

        WrappedPlayer targetPlayer = WrappedPlayer.of(args[2]);

        if(targetPlayer == null){
            Locale.INVALID_PLAYER.send(sender, args[2]);
            return;
        }

        Island island = targetPlayer.getIsland();

        if(island == null){
            Locale.INVALID_ISLAND_OTHER.send(sender, targetPlayer.getName());
            return;
        }

        double amount = -1;

        try{
            amount = Double.valueOf(args[3]);
        }catch(IllegalArgumentException ignored){}

        if(amount < 0){
            Locale.INVALID_AMOUNT.send(sender, args[3]);
            return;
        }

        island.depositMoney(amount);
        Locale.ADMIN_DEPOSIT_MONEY.send(sender, amount, targetPlayer.getName());
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblock plugin, CommandSender sender, String[] args) {
        return null;
    }
}
