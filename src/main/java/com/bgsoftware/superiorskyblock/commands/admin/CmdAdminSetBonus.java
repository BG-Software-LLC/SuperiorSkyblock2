package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.events.IslandChangeLevelBonusEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandChangeWorthBonusEvent;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.CommandTabCompletes;
import com.bgsoftware.superiorskyblock.commands.IAdminIslandCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.core.events.EventResult;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import org.bukkit.command.CommandSender;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CmdAdminSetBonus implements IAdminIslandCommand {

    @Override
    public List<String> getAliases() {
        return Arrays.asList("setbonus", "bonus");
    }

    @Override
    public String getPermission() {
        return "superior.admin.bonus";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "admin setbonus <" +
                Message.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "/" +
                Message.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + "/" +
                Message.COMMAND_ARGUMENT_ALL_ISLANDS.getMessage(locale) + "> <worth/level> <" +
                Message.COMMAND_ARGUMENT_AMOUNT.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_ADMIN_BONUS.getMessage(locale);
    }

    @Override
    public int getMinArgs() {
        return 5;
    }

    @Override
    public int getMaxArgs() {
        return 5;
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
        boolean isWorthBonus = !args[3].equalsIgnoreCase("level");

        BigDecimal bonus = CommandArguments.getBigDecimalAmount(sender, args[4]);

        if (bonus == null)
            return;

        boolean anyIslandChanged = false;

        for (Island island : islands) {
            if (isWorthBonus) {
                EventResult<BigDecimal> eventResult = plugin.getEventsBus().callIslandChangeWorthBonusEvent(sender, island,
                        IslandChangeWorthBonusEvent.Reason.COMMAND, bonus);
                if (!eventResult.isCancelled()) {
                    island.setBonusWorth(eventResult.getResult());
                    anyIslandChanged = true;
                }
            } else {
                EventResult<BigDecimal> eventResult = plugin.getEventsBus().callIslandChangeLevelBonusEvent(sender, island,
                        IslandChangeLevelBonusEvent.Reason.COMMAND, bonus);
                if (!eventResult.isCancelled()) {
                    island.setBonusLevel(eventResult.getResult());
                    anyIslandChanged = true;
                }
            }
        }

        if (!anyIslandChanged)
            return;

        Message.BONUS_SET_SUCCESS.send(sender, bonus.toString());
    }

    @Override
    public List<String> adminTabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, Island island, String[] args) {
        return args.length == 4 ? CommandTabCompletes.getCustomComplete(args[3], "worth", "level") : Collections.emptyList();
    }

}
