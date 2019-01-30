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

public class CmdDeposit implements ICommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("deposit");
    }

    @Override
    public String getPermission() {
        return "superior.island.deposit";
    }

    @Override
    public String getUsage() {
        return "island deposit <amount>";
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

        if(!wrappedPlayer.hasPermission(IslandPermission.DEPOSIT_MONEY)){
            Locale.NO_DEPOSIT_PERMISSION.send(wrappedPlayer, island.getRequiredRole(IslandPermission.DEPOSIT_MONEY));
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

        if(EconomyHook.getMoneyInBank(wrappedPlayer.asPlayer()) < amount){
            Locale.NOT_ENOUGH_MONEY_TO_DEPOSIT.send(wrappedPlayer, amount);
            return;
        }

        island.depositMoney(amount);
        EconomyHook.withdrawMoney(wrappedPlayer.asPlayer(), amount);

        for(UUID uuid : island.getAllMembers()){
            if(Bukkit.getOfflinePlayer(uuid).isOnline()){
                Locale.DEPOSIT_ANNOUNCEMENT.send(Bukkit.getPlayer(uuid), wrappedPlayer.getName(), amount);
            }
        }
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblock plugin, CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}
