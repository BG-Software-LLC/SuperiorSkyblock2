package com.bgsoftware.superiorskyblock.commands.command;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPermission;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.commands.ICommand;
import com.bgsoftware.superiorskyblock.hooks.EconomyHook;
import org.bukkit.command.CommandSender;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CmdWithdraw implements ICommand {

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
        return "island withdraw <" + Locale.COMMAND_ARGUMENT_AMOUNT + ">";
    }

    @Override
    public String getDescription() {
        return Locale.COMMAND_DESCRIPTION_WITHDRAW.getMessage();
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
        Island island = superiorPlayer.getIsland();

        if(!EconomyHook.isVaultEnabled()){
            Locale.sendMessage(superiorPlayer, "&cServer doesn't have vault installed so island banks are disabled.");
            return;
        }

        if(island == null){
            Locale.INVALID_ISLAND.send(superiorPlayer);
            return;
        }

        if(!superiorPlayer.hasPermission(IslandPermission.WITHDRAW_MONEY)){
            Locale.NO_WITHDRAW_PERMISSION.send(superiorPlayer, island.getRequiredPlayerRole(IslandPermission.WITHDRAW_MONEY));
            return;
        }

        double amount = -1;

        try{
            amount = Double.parseDouble(args[1]);
        }catch(IllegalArgumentException ignored){}

        if(amount < 0){
            Locale.INVALID_AMOUNT.send(superiorPlayer, args[1]);
            return;
        }

        if(island.getMoneyInBankAsBigDecimal().compareTo(BigDecimal.ZERO) == 0){
            Locale.ISLAND_BANK_EMPTY.send(sender);
            return;
        }

        if(island.getMoneyInBankAsBigDecimal().compareTo(new BigDecimal(amount)) < 0){
            Locale.WITHDRAW_ALL_MONEY.send(sender, island.getMoneyInBankAsBigDecimal().toString());
            amount = island.getMoneyInBankAsBigDecimal().doubleValue();
        }

        island.withdrawMoney(amount);
        EconomyHook.depositMoney(superiorPlayer.asPlayer(), amount);

        if(!Locale.WITHDRAW_ANNOUNCEMENT.isEmpty())
            island.sendMessage(Locale.WITHDRAW_ANNOUNCEMENT.getMessage(superiorPlayer.getName(), StringUtils.format(amount)));
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}
