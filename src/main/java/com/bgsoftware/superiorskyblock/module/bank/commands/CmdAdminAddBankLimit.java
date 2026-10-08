package com.bgsoftware.superiorskyblock.module.bank.commands;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.IAdminIslandCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEvent;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEventsFactory;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.island.upgrade.IslandUpgradeConstants;
import org.bukkit.command.CommandSender;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

public class CmdAdminAddBankLimit implements IAdminIslandCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("addbanklimit");
    }

    @Override
    public String getPermission() {
        return "superior.admin.addbanklimit";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "admin addbanklimit <" +
                Message.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "/" +
                Message.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + "/" +
                Message.COMMAND_ARGUMENT_ALL_ISLANDS.getMessage(locale) + "> <" +
                Message.COMMAND_ARGUMENT_LIMIT.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_ADMIN_ADD_BANK_LIMIT.getMessage(locale);
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
        BigDecimal limit = CommandArguments.getAdditionalBankLimit(sender, args[3]);

        if (limit == null) {
            return;
        }

        boolean isInvalid = false;
        Island changedIsland = null;
        int islandsChangedCount = 0;

        for (Island island : islands) {
            BigDecimal currentLimit = island.getBankLimit();
            if (currentLimit.compareTo(IslandUpgradeConstants.NO_BANK_LIMIT_VALUE) <= 0
                    || currentLimit.add(limit).compareTo(BigDecimal.ZERO) < 0) {
                isInvalid = true;
                continue;
            }

            PluginEvent<PluginEventArgs.IslandChangeBankLimit> event = PluginEventsFactory.callIslandChangeBankLimitEvent(
                    island, sender, currentLimit.add(limit));
            if (!event.isCancelled()) {
                island.setBankLimit(event.getArgs().bankLimit);
                changedIsland = island;
                ++islandsChangedCount;
            }
        }

        if (islandsChangedCount <= 0) {
            if (isInvalid) {
                Message.INVALID_LIMIT.send(sender, limit);
            }
            return;
        }

        if (islandsChangedCount > 1) {
            Message.CHANGED_BANK_LIMIT_ALL.send(sender);
        } else if (targetPlayer == null) {
            Message.CHANGED_BANK_LIMIT_NAME.send(sender, changedIsland.getName());
        } else {
            Message.CHANGED_BANK_LIMIT.send(sender, targetPlayer.getName());
        }
    }

}
