package com.bgsoftware.superiorskyblock.module.bank.commands;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgument;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.InternalIslandsCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArgumentsBuilder;
import com.bgsoftware.superiorskyblock.commands.arguments.types.BigDecimalArgumentType;
import com.bgsoftware.superiorskyblock.commands.arguments.types.MultipleIslandsArgumentType;
import com.bgsoftware.superiorskyblock.commands.context.IslandsCommandContext;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import org.bukkit.command.CommandSender;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

public class CmdAdminDeposit implements InternalIslandsCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("deposit");
    }

    @Override
    public String getPermission() {
        return "superior.admin.deposit";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_ADMIN_DEPOSIT.getMessage(locale);
    }

    @Override
    public List<CommandArgument<?>> getArguments()

    {
        return new CommandArgumentsBuilder()
                .add(CommandArguments.required("islands", MultipleIslandsArgumentType.INCLUDE_PLAYERS, Message.COMMAND_ARGUMENT_PLAYER_NAME, Message.COMMAND_ARGUMENT_ISLAND_NAME, Message.COMMAND_ARGUMENT_ALL_ISLANDS))
                .add(CommandArguments.required("amount", BigDecimalArgumentType.AMOUNT, Message.COMMAND_ARGUMENT_AMOUNT))
                .build();
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return true;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, IslandsCommandContext context) {
        CommandSender dispatcher = context.getDispatcher();

        List<Island> islands = context.getIslands();
        BigDecimal amount = context.getRequiredArgument("amount", BigDecimal.class);

        BukkitExecutor.data(() -> islands.forEach(island -> island.getIslandBank().depositAdminMoney(dispatcher, amount)));

        SuperiorPlayer targetPlayer = context.getTargetPlayer();

        if (targetPlayer == null)
            Message.ADMIN_DEPOSIT_MONEY_NAME.send(dispatcher, Formatters.NUMBER_FORMATTER.format(amount), islands.size() == 1 ? islands.get(0).getName() : "all");
        else
            Message.ADMIN_DEPOSIT_MONEY.send(dispatcher, Formatters.NUMBER_FORMATTER.format(amount), targetPlayer.getName());
    }

}
