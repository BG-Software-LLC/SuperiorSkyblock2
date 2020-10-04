package com.bgsoftware.superiorskyblock.commands;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.bank.BankTransaction;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.commands.CommandArguments;
import com.bgsoftware.superiorskyblock.utils.islands.IslandPrivileges;
import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import org.bukkit.command.CommandSender;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CmdDeposit implements ISuperiorCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("deposit");
    }

    @Override
    public String getPermission() {
        return "superior.island.deposit";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "deposit <" + Locale.COMMAND_ARGUMENT_AMOUNT.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Locale.COMMAND_DESCRIPTION_DEPOSIT.getMessage(locale);
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

        BigDecimal moneyInBank = plugin.getProviders().getBalance(superiorPlayer);
        BigDecimal amount = BigDecimal.valueOf(-1);

        if(args[1].equalsIgnoreCase("all") || args[1].equals("*")){
            amount = moneyInBank;
        }

        else try{
            amount = BigDecimal.valueOf(Double.parseDouble(args[1]));
        }catch(IllegalArgumentException ignored){}

        BankTransaction transaction = island.getIslandBank().depositMoney(superiorPlayer, amount);

        String failureReason = transaction.getFailureReason();

        if(!failureReason.isEmpty()) {
            switch (failureReason) {
                case "No permission":
                    Locale.NO_DEPOSIT_PERMISSION.send(superiorPlayer, island.getRequiredPlayerRole(IslandPrivileges.DEPOSIT_MONEY));
                    break;
                case "Invalid amount":
                    Locale.INVALID_AMOUNT.send(superiorPlayer, args[1]);
                    break;
                case "Not enough money":
                    Locale.NOT_ENOUGH_MONEY_TO_DEPOSIT.send(superiorPlayer, args[1]);
                    break;
                case "Exceed bank limit":
                    Locale.BANK_LIMIT_EXCEED.send(superiorPlayer);
                    break;
                default:
                    Locale.DEPOSIT_ERROR.send(sender, failureReason);
                    break;
            }
        }
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        return new ArrayList<>();
    }

}
