package com.bgsoftware.superiorskyblock.commands;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.bank.BankTransaction;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.menu.MenuIslandBank;
import com.bgsoftware.superiorskyblock.utils.commands.CommandArguments;
import com.bgsoftware.superiorskyblock.Locale;
import org.bukkit.command.CommandSender;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CmdWithdraw implements ISuperiorCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("withdraw");
    }

    @Override
    public String getPermission() {
        return "superior.island.withdraw";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "withdraw <" + Locale.COMMAND_ARGUMENT_AMOUNT.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Locale.COMMAND_DESCRIPTION_WITHDRAW.getMessage(locale);
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
        Pair<Island, SuperiorPlayer> arguments = CommandArguments.getSenderIsland(plugin, sender);

        Island island = arguments.getKey();

        if(island == null)
            return;

        SuperiorPlayer superiorPlayer = arguments.getValue();

        BigDecimal amount = BigDecimal.valueOf(-1);

        if(args[1].equalsIgnoreCase("all") || args[1].equals("*")){
            amount = island.getIslandBank().getBalance();
        }

        else try{
            amount = new BigDecimal(args[1]);
        }catch(IllegalArgumentException ignored){}

        BankTransaction transaction = island.getIslandBank().withdrawMoney(superiorPlayer, amount, null);
        MenuIslandBank.handleWithdraw(superiorPlayer, island, null, transaction, -1, amount);
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        return new ArrayList<>();
    }

}
