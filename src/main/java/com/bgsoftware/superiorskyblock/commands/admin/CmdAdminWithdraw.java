package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.events.EventsCaller;
import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.commands.ISuperiorCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CmdAdminWithdraw implements ISuperiorCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("withdraw");
    }

    @Override
    public String getPermission() {
        return "superior.admin.withdraw";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "admin withdraw <" +
                Locale.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "/" +
                Locale.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + "> <" +
                Locale.COMMAND_ARGUMENT_AMOUNT.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Locale.COMMAND_DESCRIPTION_ADMIN_WITHDRAW.getMessage(locale);
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
        SuperiorPlayer targetPlayer = plugin.getPlayers().getSuperiorPlayer(args[2]);
        Island island = targetPlayer == null ? plugin.getGrid().getIsland(args[2]) : targetPlayer.getIsland();

        if(island == null){
            if(args[2].equalsIgnoreCase(sender.getName()))
                Locale.INVALID_ISLAND.send(sender);
            else if(targetPlayer == null)
                Locale.INVALID_ISLAND_OTHER_NAME.send(sender, StringUtils.stripColors(args[2]));
            else
                Locale.INVALID_ISLAND_OTHER.send(sender, targetPlayer.getName());
            return;
        }

        BigDecimal amount = BigDecimal.valueOf(-1);

        if(args[3].equalsIgnoreCase("all") || args[3].equals("*")){
            amount = island.getIslandBank().getBalance();
        }

        else try{
            amount = new BigDecimal(args[3]);
        }catch(IllegalArgumentException ignored){}

        if(amount.compareTo(BigDecimal.ZERO) < 0){
            Locale.INVALID_AMOUNT.send(sender, args[3]);
            return;
        }

        if(island.getIslandBank().getBalance().compareTo(BigDecimal.ZERO) == 0){
            Locale.ISLAND_BANK_EMPTY.send(sender);
            return;
        }

        if(island.getIslandBank().getBalance().compareTo(amount) < 0){
            Locale.WITHDRAW_ALL_MONEY.send(sender, island.getIslandBank().getBalance().toString());
            amount = island.getIslandBank().getBalance();
        }

        EventsCaller.callIslandBankWithdrawEvent(sender instanceof Player ? plugin.getPlayers().getSuperiorPlayer(sender) : null, island, amount);

        island.getIslandBank().withdrawAdminMoney(sender, amount);

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
                SuperiorPlayer onlinePlayer = plugin.getPlayers().getSuperiorPlayer(player);
                Island playerIsland = onlinePlayer.getIsland();
                if (playerIsland != null) {
                    if (player.getName().toLowerCase().contains(args[2].toLowerCase()))
                        list.add(player.getName());
                    if(!playerIsland.getName().isEmpty() && playerIsland.getName().toLowerCase().contains(args[2].toLowerCase()))
                        list.add(playerIsland.getName());
                }
            }
        }

        return list;
    }
}
