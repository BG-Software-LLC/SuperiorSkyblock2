package com.bgsoftware.superiorskyblock.commands.command.admin;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.commands.ICommand;
import com.bgsoftware.superiorskyblock.hooks.EconomyHook;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.ArrayList;
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
        return "admin withdraw <" +
                Locale.COMMAND_ARGUMENT_PLAYER_NAME.getMessage() + "/" +
                Locale.COMMAND_ARGUMENT_ISLAND_NAME.getMessage() + "> <" +
                Locale.COMMAND_ARGUMENT_AMOUNT.getMessage() + ">";
    }

    @Override
    public String getDescription() {
        return Locale.COMMAND_DESCRIPTION_ADMIN_WITHDRAW.getMessage();
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
        Island island = targetPlayer == null ? plugin.getGrid().getIsland(args[2]) : targetPlayer.getIsland();

        if(island == null){
            if(args[2].equalsIgnoreCase(sender.getName()))
                Locale.INVALID_ISLAND.send(sender);
            else if(targetPlayer == null)
                Locale.INVALID_ISLAND_OTHER_NAME.send(sender, args[2]);
            else
                Locale.INVALID_ISLAND_OTHER.send(sender, targetPlayer.getName());
            return;
        }

        double amount = -1;

        try{
            amount = Double.parseDouble(args[1]);
        }catch(IllegalArgumentException ignored){}

        if(amount < 0){
            Locale.INVALID_AMOUNT.send(sender, args[1]);
            return;
        }

        if(island.getMoneyInBank().compareTo(BigDecimal.ZERO) == 0){
            Locale.ISLAND_BANK_EMPTY.send(sender);
            return;
        }

        if(island.getMoneyInBank().compareTo(new BigDecimal(amount)) < 0){
            Locale.WITHDRAW_ALL_MONEY.send(sender, island.getMoneyInBank().toString());
            amount = island.getMoneyInBank().doubleValue();
        }

        island.withdrawMoney(amount);

        if(targetPlayer == null)
            Locale.WITHDRAWN_MONEY_NAME.send(sender, StringUtils.format(amount), island.getName());
        else
            Locale.WITHDRAWN_MONEY.send(sender, StringUtils.format(amount), targetPlayer.getName());
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        List<String> list = new ArrayList<>();

        if(args.length == 3){
            for(Player player : Bukkit.getOnlinePlayers()){
                SuperiorPlayer onlinePlayer = SSuperiorPlayer.of(player);
                Island playerIsland = onlinePlayer.getIsland();
                if (playerIsland != null) {
                    if (player.getName().toLowerCase().startsWith(args[2].toLowerCase()))
                        list.add(player.getName());
                    if(!playerIsland.getName().isEmpty() && playerIsland.getName().toLowerCase().startsWith(args[2].toLowerCase()))
                        list.add(playerIsland.getName());
                }
            }
        }

        return list;
    }
}
