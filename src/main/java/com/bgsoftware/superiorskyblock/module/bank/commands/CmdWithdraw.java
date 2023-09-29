package com.bgsoftware.superiorskyblock.module.bank.commands;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgument;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.bank.BankTransaction;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.InternalIslandCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArgumentsBuilder;
import com.bgsoftware.superiorskyblock.commands.arguments.types.StringArgumentType;
import com.bgsoftware.superiorskyblock.commands.context.IslandCommandContext;
import com.bgsoftware.superiorskyblock.core.menu.Menus;
import com.bgsoftware.superiorskyblock.core.messages.Message;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

public class CmdWithdraw implements InternalIslandCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("withdraw");
    }

    @Override
    public String getPermission() {
        return "superior.island.withdraw";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_WITHDRAW.getMessage(locale);
    }

    @Override
    public List<CommandArgument<?>> getArguments() {
        return new CommandArgumentsBuilder()
                .add(CommandArguments.required("amount", StringArgumentType.INSTANCE, "all", Message.COMMAND_ARGUMENT_AMOUNT))
                .build();
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return false;
    }

    @Override
    public boolean isSelfIsland() {
        return true;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, IslandCommandContext context) {
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(context.getDispatcher());

        Island island = context.getIsland();
        String amountArgument = context.getRequiredArgument("amount", String.class);

        BigDecimal amount;

        if (amountArgument.equals("*") || amountArgument.equalsIgnoreCase("all")) {
            amount = island.getIslandBank().getBalance();
        } else try {
            amount = new BigDecimal(amountArgument);
        } catch (IllegalArgumentException ignored) {
            amount = BigDecimal.valueOf(-1);
        }

        BankTransaction transaction = island.getIslandBank().withdrawMoney(superiorPlayer, amount, null);
        Menus.MENU_ISLAND_BANK.handleWithdraw(superiorPlayer, island, transaction, null, null, amount);
    }

}
