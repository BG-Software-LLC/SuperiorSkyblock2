package com.bgsoftware.superiorskyblock.commands.command.admin;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.commands.ICommand;
import com.bgsoftware.superiorskyblock.hooks.EconomyHook;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public final class CmdAdminWithdraw implements ICommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("withdraw");
    }

    @Override
    public String getPermission() {
        return "superior.admin.withdraw";
    }

    @Override
    public String getUsage() {
        return "island admin withdraw <player-name> <amount>";
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
        if(!EconomyHook.isVaultEnabled()){
            Locale.sendMessage(sender, "&cServer doesn't have vault installed so island banks are disabled.");
            return;
        }

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

        double amount = -1;

        try{
            amount = Double.valueOf(args[1]);
        }catch(IllegalArgumentException ignored){}

        if(amount < 0){
            Locale.INVALID_AMOUNT.send(sender, args[1]);
            return;
        }

        if(island.getMoneyInBank() <= 0){
            Locale.ISLAND_BANK_EMPTY.send(sender);
            return;
        }

        if(island.getMoneyInBank() < amount){
            Locale.WITHDRAW_ALL_MONEY.send(sender, island.getMoneyInBank());
            amount = island.getMoneyInBank();
        }

        island.withdrawMoney(amount);
        Locale.WITHDRAWN_MONEY.send(sender, amount, targetPlayer.getName());
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        return null;
    }
}
