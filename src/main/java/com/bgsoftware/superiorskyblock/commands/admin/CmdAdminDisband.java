package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgument;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.InternalIslandCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArgumentsBuilder;
import com.bgsoftware.superiorskyblock.commands.arguments.types.IslandArgumentType;
import com.bgsoftware.superiorskyblock.commands.context.IslandCommandContext;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.island.IslandUtils;
import com.bgsoftware.superiorskyblock.module.BuiltinModules;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

public class CmdAdminDisband implements InternalIslandCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("disband");
    }

    @Override
    public String getPermission() {
        return "superior.admin.disband";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_ADMIN_DISBAND.getMessage(locale);
    }

    @Override
    public List<CommandArgument<?>> getArguments()

    {
        return new CommandArgumentsBuilder()
                .add(CommandArguments.required("island", IslandArgumentType.INCLUDE_PLAYERS, Message.COMMAND_ARGUMENT_PLAYER_NAME, Message.COMMAND_ARGUMENT_ISLAND_NAME))
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

        SuperiorPlayer dispatcherPlayer = dispatcher instanceof Player ?
                plugin.getPlayers().getSuperiorPlayer(dispatcher) : null;
        Island island = context.getIsland();

        if (!plugin.getEventsBus().callIslandDisbandEvent(dispatcherPlayer, island))
            return;

        IslandUtils.sendMessage(island, Message.DISBAND_ANNOUNCEMENT, Collections.emptyList(), dispatcher.getName());

        SuperiorPlayer targetPlayer = context.getTargetPlayer();

        if (targetPlayer == null)
            Message.DISBANDED_ISLAND_OTHER_NAME.send(dispatcher, island.getName());
        else
            Message.DISBANDED_ISLAND_OTHER.send(dispatcher, targetPlayer.getName());

        if (BuiltinModules.BANK.disbandRefund > 0) {
            Message.DISBAND_ISLAND_BALANCE_REFUND.send(island.getOwner(),
                    Formatters.NUMBER_FORMATTER.format(island.getIslandBank()
                            .getBalance().multiply(BigDecimal.valueOf(BuiltinModules.BANK.disbandRefund))));
        }

        island.disbandIsland();
    }

}
