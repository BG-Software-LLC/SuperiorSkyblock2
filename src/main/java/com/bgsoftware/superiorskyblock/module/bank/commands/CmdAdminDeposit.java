package com.bgsoftware.superiorskyblock.module.bank.commands;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.IAdminIslandCommand;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.commands.CommandArguments;
import com.bgsoftware.superiorskyblock.utils.events.EventsCaller;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

public final class CmdAdminDeposit implements IAdminIslandCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("deposit");
    }

    @Override
    public String getPermission() {
        return "superior.admin.deposit";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "admin deposit <" +
                Locale.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "/" +
                Locale.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + "/" +
                Locale.COMMAND_ARGUMENT_ALL_ISLANDS.getMessage(locale) + "> <" +
                Locale.COMMAND_ARGUMENT_AMOUNT.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Locale.COMMAND_DESCRIPTION_ADMIN_DEPOSIT.getMessage(locale);
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
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, SuperiorPlayer targetPlayer, List<Island> islands, String[] args) {
        BigDecimal amount = CommandArguments.getBigDecimalAmount(sender, args[3]);

        if(amount == null)
            return;

        Executor.data(() -> islands.forEach(island -> {
            EventsCaller.callIslandBankDepositEvent(sender instanceof Player ? plugin.getPlayers().getSuperiorPlayer(sender) : null, island, amount);
            island.getIslandBank().depositAdminMoney(sender, amount);
        }));

        if(targetPlayer == null)
            Locale.ADMIN_DEPOSIT_MONEY_NAME.send(sender, StringUtils.format(amount), islands.size() == 1 ? islands.get(0).getName() : "all");
        else
            Locale.ADMIN_DEPOSIT_MONEY.send(sender, StringUtils.format(amount), targetPlayer.getName());
    }

}
