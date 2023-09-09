package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.IAdminIslandCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.core.events.EventResult;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import org.bukkit.command.CommandSender;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

public class CmdAdminSetBankLimit implements IAdminIslandCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("setbanklimit");
    }

    @Override
    public String getPermission() {
        return "superior.admin.setbanklimit";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "admin setbanklimit <" +
                Message.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "/" +
                Message.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + "/" +
                Message.COMMAND_ARGUMENT_ALL_ISLANDS.getMessage(locale) + "> <" +
                Message.COMMAND_ARGUMENT_LIMIT.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_ADMIN_SET_BANK_LIMIT.getMessage(locale);
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
        BigDecimal limit = CommandArguments.getBigDecimalAmount(sender, args[3]);

        if (limit == null)
            return;

        boolean anyIslandChanged = false;

        for (Island island : islands) {
            EventResult<BigDecimal> eventResult = plugin.getEventsBus().callIslandChangeBankLimitEvent(sender, island, limit);

            if (eventResult.isCancelled())
                continue;

            anyIslandChanged = true;
            island.setBankLimit(eventResult.getResult());
        }

        if (!anyIslandChanged)
            return;

        if (islands.size() > 1)
            Message.CHANGED_BANK_LIMIT_ALL.send(sender);
        else if (targetPlayer == null)
            Message.CHANGED_BANK_LIMIT_NAME.send(sender, islands.get(0).getName());
        else
            Message.CHANGED_BANK_LIMIT.send(sender, targetPlayer.getName());
    }

}
