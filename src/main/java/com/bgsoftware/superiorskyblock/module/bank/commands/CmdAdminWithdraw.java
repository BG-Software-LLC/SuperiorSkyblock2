package com.bgsoftware.superiorskyblock.module.bank.commands;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgument;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.InternalIslandCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArgumentsBuilder;
import com.bgsoftware.superiorskyblock.commands.arguments.types.IslandArgumentType;
import com.bgsoftware.superiorskyblock.commands.arguments.types.StringArgumentType;
import com.bgsoftware.superiorskyblock.commands.context.IslandCommandContext;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import org.bukkit.command.CommandSender;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

public class CmdAdminWithdraw implements InternalIslandCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("withdraw");
    }

    @Override
    public String getPermission() {
        return "superior.admin.withdraw";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_ADMIN_WITHDRAW.getMessage(locale);
    }

    @Override
    public List<CommandArgument<?>> getArguments() {
        return new CommandArgumentsBuilder()
                .add(CommandArgument.required("island", IslandArgumentType.INCLUDE_PLAYERS, Message.COMMAND_ARGUMENT_PLAYER_NAME, Message.COMMAND_ARGUMENT_ISLAND_NAME))
                .add(CommandArgument.required("amount", StringArgumentType.INSTANCE, "all", Message.COMMAND_ARGUMENT_AMOUNT))
                .build();
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return true;
    }

    @Override
    public boolean isSelfIsland() {
        return false;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, IslandCommandContext context) {
        CommandSender dispatcher = context.getDispatcher();

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

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            Message.INVALID_AMOUNT.send(dispatcher, context.getInputArgument("amount"));
            return;
        }

        if (island.getIslandBank().getBalance().compareTo(BigDecimal.ZERO) == 0) {
            Message.ISLAND_BANK_EMPTY.send(dispatcher);
            return;
        }

        if (island.getIslandBank().getBalance().compareTo(amount) < 0) {
            Message.WITHDRAW_ALL_MONEY.send(dispatcher, island.getIslandBank().getBalance().toString());
            amount = island.getIslandBank().getBalance();
        }

        island.getIslandBank().withdrawAdminMoney(dispatcher, amount);

        SuperiorPlayer targetPlayer = context.getTargetPlayer();

        if (targetPlayer == null)
            Message.WITHDRAWN_MONEY_NAME.send(dispatcher, Formatters.NUMBER_FORMATTER.format(amount), island.getName());
        else
            Message.WITHDRAWN_MONEY.send(dispatcher, Formatters.NUMBER_FORMATTER.format(amount), targetPlayer.getName());
    }

}
