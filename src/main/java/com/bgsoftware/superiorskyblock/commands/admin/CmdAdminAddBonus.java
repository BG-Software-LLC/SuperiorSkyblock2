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
import com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEvent;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEventsFactory;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import org.bukkit.command.CommandSender;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

public class CmdAdminAddBonus implements IAdminIslandCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("addbonus");
    }

    @Override
    public String getPermission() {
        return "superior.admin.addbonus";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "admin addbonus <" +
                Message.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "/" +
                Message.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + "/" +
                Message.COMMAND_ARGUMENT_ALL_ISLANDS.getMessage(locale) + "> <worth/level> <" +
                Message.COMMAND_ARGUMENT_AMOUNT.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_ADMIN_ADD_BONUS.getMessage(locale);
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

        int islandsChangedCount = 0;

        for (Island island : islands) {
            if (isWorthBonus) {
                PluginEvent<PluginEventArgs.IslandChangeWorthBonus> event = PluginEventsFactory.callIslandChangeWorthBonusEvent(
                        island, sender, IslandChangeWorthBonusEvent.Reason.COMMAND, island.getBonusWorth().add(bonus));
                if (!event.isCancelled()) {
                    island.setBonusWorth(event.getArgs().worthBonus);
                    ++islandsChangedCount;
                }
            } else {
                PluginEvent<PluginEventArgs.IslandChangeLevelBonus> event = PluginEventsFactory.callIslandChangeLevelBonusEvent(
                        island, sender, IslandChangeLevelBonusEvent.Reason.COMMAND, island.getBonusLevel().add(bonus));
                if (!event.isCancelled()) {
                    island.setBonusLevel(event.getArgs().levelBonus);
                    ++islandsChangedCount;
                }
            }
        }

        if (islandsChangedCount <= 0)
            return;

        Message.BONUS_SET_SUCCESS.send(sender, bonus.toString());
    }

    @Override
    public List<String> adminTabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, Island island, String[] args) {
        return args.length == 4 ? CommandTabCompletes.getCustomComplete(args[3], "worth", "level") : Collections.emptyList();
    }

}
