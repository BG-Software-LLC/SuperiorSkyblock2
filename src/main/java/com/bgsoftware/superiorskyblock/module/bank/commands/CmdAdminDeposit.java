package com.bgsoftware.superiorskyblock.module.bank.commands;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.common.collections.Lists;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.BaseCommand;
import com.bgsoftware.superiorskyblock.commands.IAdminIslandCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import org.bukkit.command.CommandSender;

import java.math.BigDecimal;
import java.util.List;

public class CmdAdminDeposit extends BaseCommand implements IAdminIslandCommand {

    @Override
    protected List<String> aliases() {
        return Lists.singleton("deposit");
    }

    @Override
    protected String permission() {
        return "superior.admin.deposit";
    }

    @Override
    protected String usage(java.util.Locale locale) {
        return "admin deposit <" +
                Message.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "/" +
                Message.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + "/" +
                Message.COMMAND_ARGUMENT_ALL_ISLANDS.getMessage(locale) + "> <" +
                Message.COMMAND_ARGUMENT_AMOUNT.getMessage(locale) + ">";
    }

    @Override
    protected String description(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_ADMIN_DEPOSIT.getMessage(locale);
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
        return true;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, @Nullable SuperiorPlayer targetPlayer, List<Island> islands, String[] args) {
        BigDecimal amount = CommandArguments.getBigDecimalAmount(sender, args[3]);

        if (amount == null)
            return;

        BukkitExecutor.data(() -> islands.forEach(island -> island.getIslandBank().depositAdminMoney(sender, amount)));

        if (targetPlayer == null)
            Message.ADMIN_DEPOSIT_MONEY_NAME.send(sender, Formatters.NUMBER_FORMATTER.format(amount), islands.size() == 1 ? islands.get(0).getName() : "all");
        else
            Message.ADMIN_DEPOSIT_MONEY.send(sender, Formatters.NUMBER_FORMATTER.format(amount), targetPlayer.getName());
    }

}
