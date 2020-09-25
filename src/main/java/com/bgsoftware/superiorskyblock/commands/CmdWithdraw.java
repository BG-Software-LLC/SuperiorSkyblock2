package com.bgsoftware.superiorskyblock.commands;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.bank.BankTransaction;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.events.EventsCaller;
import com.bgsoftware.superiorskyblock.utils.islands.IslandPrivileges;
import com.bgsoftware.superiorskyblock.utils.islands.IslandUtils;
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
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(sender);
        Island island = superiorPlayer.getIsland();

        if(island == null){
            Locale.INVALID_ISLAND.send(superiorPlayer);
            return;
        }

        if(!superiorPlayer.hasPermission(IslandPrivileges.WITHDRAW_MONEY)){
            Locale.NO_WITHDRAW_PERMISSION.send(superiorPlayer, island.getRequiredPlayerRole(IslandPrivileges.WITHDRAW_MONEY));
            return;
        }

        BigDecimal amount = BigDecimal.valueOf(-1);

        if(args[1].equalsIgnoreCase("all") || args[1].equals("*")){
            amount = island.getIslandBank().getBalance();
        }

        else try{
            amount = new BigDecimal(args[1]);
        }catch(IllegalArgumentException ignored){}

        if(amount.compareTo(BigDecimal.ZERO) < 0){
            Locale.INVALID_AMOUNT.send(superiorPlayer, args[1]);
            return;
        }

        if(island.getIslandBank().getBalance().compareTo(BigDecimal.ZERO) == 0){
            Locale.ISLAND_BANK_EMPTY.send(sender);
            return;
        }

        BankTransaction transaction = island.getIslandBank().withdrawMoney(superiorPlayer, amount, null);

        if(!transaction.getFailureReason().isEmpty()){
            Locale.WITHDRAW_ERROR.send(sender, transaction.getFailureReason());
        }
        else{
            IslandUtils.sendMessage(island, Locale.WITHDRAW_ANNOUNCEMENT, new ArrayList<>(), superiorPlayer.getName(), StringUtils.format(transaction.getAmount()));
        }

        EventsCaller.callIslandBankWithdrawEvent(superiorPlayer, island, amount);
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}
