package com.bgsoftware.superiorskyblock.module.bank.commands;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.IAdminIslandCommand;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import org.bukkit.command.CommandSender;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

public class CmdAdminWithdraw implements IAdminIslandCommand {

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
                Message.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "/" +
                Message.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + "> <" +
                Message.COMMAND_ARGUMENT_AMOUNT.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_ADMIN_WITHDRAW.getMessage(locale);
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
    public boolean supportMultipleIslands() {
        return false;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, @Nullable SuperiorPlayer targetPlayer, Island island, String[] args) {
        BigDecimal amount = BigDecimal.valueOf(-1);

        if (args[3].equalsIgnoreCase("all") || args[3].equals("*")) {
            amount = island.getIslandBank().getBalance();
        } else try {
            amount = new BigDecimal(args[3]);
        } catch (IllegalArgumentException ignored) {
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            Message.INVALID_AMOUNT.send(sender, args[3]);
            return;
        }

        if (island.getIslandBank().getBalance().compareTo(BigDecimal.ZERO) == 0) {
            Message.ISLAND_BANK_EMPTY.send(sender);
            return;
        }

        if (island.getIslandBank().getBalance().compareTo(amount) < 0) {
            Message.WITHDRAW_ALL_MONEY.send(sender, island.getIslandBank().getBalance().toString());
            amount = island.getIslandBank().getBalance();
        }

        island.getIslandBank().withdrawAdminMoney(sender, amount);

        if (targetPlayer == null)
            Message.WITHDRAWN_MONEY_NAME.send(sender, Formatters.NUMBER_FORMATTER.format(amount), island.getName());
        else
            Message.WITHDRAWN_MONEY.send(sender, Formatters.NUMBER_FORMATTER.format(amount), targetPlayer.getName());
    }

}
