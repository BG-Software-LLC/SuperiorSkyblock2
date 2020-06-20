package com.bgsoftware.superiorskyblock.commands;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.island.SIsland;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.islands.IslandPrivileges;
import com.bgsoftware.superiorskyblock.wrappers.player.SSuperiorPlayer;
import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.hooks.EconomyHook;
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

        if(!superiorPlayer.hasPermission(IslandPrivileges.DEPOSIT_MONEY)){
            Locale.NO_DEPOSIT_PERMISSION.send(superiorPlayer, island.getRequiredPlayerRole(IslandPrivileges.DEPOSIT_MONEY));
            return;
        }

        BigDecimal moneyInBank = BigDecimal.valueOf(EconomyHook.getMoneyInBank(superiorPlayer.asPlayer()));
        BigDecimal amount = BigDecimal.valueOf(-1);

        if(args[1].equalsIgnoreCase("all") || args[1].equals("*")){
            amount = moneyInBank;
        }

        else try{
            amount = BigDecimal.valueOf(Double.parseDouble(args[1]));
        }catch(IllegalArgumentException ignored){}

        if(amount.compareTo(BigDecimal.ZERO) <= 0){
            Locale.INVALID_AMOUNT.send(superiorPlayer, args[1]);
            return;
        }

        if(moneyInBank.compareTo(amount) < 0){
            Locale.NOT_ENOUGH_MONEY_TO_DEPOSIT.send(superiorPlayer, amount);
            return;
        }

        island.depositMoney(amount);
        EconomyHook.withdrawMoney(superiorPlayer.asPlayer(), amount);

        ((SIsland) island).sendMessage(Locale.DEPOSIT_ANNOUNCEMENT, new ArrayList<>(), superiorPlayer.getName(), StringUtils.format(amount));
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}
