package com.bgsoftware.superiorskyblock.commands.command;

import com.bgsoftware.superiorskyblock.wrappers.WrappedPlayer;
import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.commands.ICommand;
import com.bgsoftware.superiorskyblock.hooks.EconomyHook;
import com.bgsoftware.superiorskyblock.island.Island;
import com.bgsoftware.superiorskyblock.island.IslandPermission;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class CmdWithdraw implements ICommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("withdraw");
    }

    @Override
    public String getPermission() {
        return "superior.island.withdraw";
    }

    @Override
    public String getUsage() {
        return "island withdraw <amount>";
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

        if(!EconomyHook.isVaultEnabled()){
            Locale.sendMessage(wrappedPlayer, "&cServer doesn't have vault installed so island banks are disabled.");
            return;
        }

        if(island == null){
            Locale.INVALID_ISLAND.send(wrappedPlayer);
            return;
        }

        if(!wrappedPlayer.hasPermission(IslandPermission.WITHDRAW_MONEY)){
            Locale.NO_WITHDRAW_PERMISSION.send(wrappedPlayer, island.getRequiredRole(IslandPermission.WITHDRAW_MONEY));
            return;
        }

        double amount = -1;

        try{
            amount = Double.valueOf(args[1]);
        }catch(IllegalArgumentException ignored){}

        if(amount < 0){
            Locale.INVALID_AMOUNT.send(wrappedPlayer, args[1]);
            return;
        }

        if(island.getMoneyInBank() <= 0){
            Locale.ISLAND_BANK_EMPTY.send(wrappedPlayer);
            return;
        }

        if(island.getMoneyInBank() < amount){
            Locale.WITHDRAW_ALL_MONEY.send(wrappedPlayer, island.getMoneyInBank());
            amount = island.getMoneyInBank();
        }

        island.withdrawMoney(amount);
        EconomyHook.depositMoney(wrappedPlayer.asPlayer(), amount);

        for(UUID uuid : island.getAllMembers()){
            if(Bukkit.getOfflinePlayer(uuid).isOnline()){
                Locale.WITHDRAW_ANNOUNCEMENT.send(Bukkit.getPlayer(uuid), wrappedPlayer.getName(), amount);
            }
        }
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblock plugin, CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}
