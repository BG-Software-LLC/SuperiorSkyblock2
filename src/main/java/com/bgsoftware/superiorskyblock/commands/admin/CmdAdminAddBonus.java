package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgument;
import com.bgsoftware.superiorskyblock.api.events.IslandChangeLevelBonusEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandChangeWorthBonusEvent;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.commands.InternalIslandsCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArgumentsBuilder;
import com.bgsoftware.superiorskyblock.commands.arguments.types.BigDecimalArgumentType;
import com.bgsoftware.superiorskyblock.commands.arguments.types.MultipleIslandsArgumentType;
import com.bgsoftware.superiorskyblock.commands.arguments.types.StringArgumentType;
import com.bgsoftware.superiorskyblock.commands.context.IslandsCommandContext;
import com.bgsoftware.superiorskyblock.core.events.EventResult;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import org.bukkit.command.CommandSender;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

public class CmdAdminAddBonus implements InternalIslandsCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("addbonus");
    }

    @Override
    public String getPermission() {
        return "superior.admin.addbonus";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_ADMIN_ADD_BONUS.getMessage(locale);
    }

    @Override
    public List<CommandArgument<?>> getArguments() {
        return new CommandArgumentsBuilder()
                .add(CommandArguments.required("islands", MultipleIslandsArgumentType.INCLUDE_PLAYERS, Message.COMMAND_ARGUMENT_PLAYER_NAME, Message.COMMAND_ARGUMENT_ISLAND_NAME, Message.COMMAND_ARGUMENT_ALL_ISLANDS))
                .add(CommandArgument.required("bonus-type", "worth/level", StringArgumentType.INSTANCE))
                .add(CommandArguments.required("bonus", BigDecimalArgumentType.AMOUNT, Message.COMMAND_ARGUMENT_AMOUNT))
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
        boolean isWorthBonus = context.getRequiredArgument("bonus-type", String.class).equalsIgnoreCase("worth");
        BigDecimal bonus = context.getRequiredArgument("bonus", BigDecimal.class);

        boolean anyIslandChanged = false;

        for (Island island : islands) {
            if (isWorthBonus) {
                EventResult<BigDecimal> eventResult = plugin.getEventsBus().callIslandChangeWorthBonusEvent(dispatcher, island,
                        IslandChangeWorthBonusEvent.Reason.COMMAND, island.getBonusWorth().add(bonus));
                if (!eventResult.isCancelled()) {
                    island.setBonusWorth(eventResult.getResult());
                    anyIslandChanged = true;
                }
            } else {
                EventResult<BigDecimal> eventResult = plugin.getEventsBus().callIslandChangeLevelBonusEvent(dispatcher, island,
                        IslandChangeLevelBonusEvent.Reason.COMMAND, island.getBonusLevel().add(bonus));
                if (!eventResult.isCancelled()) {
                    island.setBonusLevel(eventResult.getResult());
                    anyIslandChanged = true;
                }
            }
        }

        if (!anyIslandChanged)
            return;

        Message.BONUS_SET_SUCCESS.send(dispatcher, bonus.toString());
    }

}
