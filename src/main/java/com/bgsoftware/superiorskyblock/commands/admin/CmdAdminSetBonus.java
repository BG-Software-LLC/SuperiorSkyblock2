package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.events.IslandChangeLevelBonusEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandChangeWorthBonusEvent;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.CommandTabCompletes;
import com.bgsoftware.superiorskyblock.commands.CommandsHelper;
import com.bgsoftware.superiorskyblock.commands.IAdminIslandCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEvent;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEventsFactory;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.player.PlayerLocales;
import org.bukkit.command.CommandSender;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

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
        boolean isWorthBonus;

        if (args[3].equalsIgnoreCase("worth")) {
            isWorthBonus = true;
        } else if (args[3].equalsIgnoreCase("level")) {
            isWorthBonus = false;
        } else {
            Locale locale = PlayerLocales.getLocale(sender);
            Message.COMMAND_USAGE.send(sender, locale, CommandsHelper.getCommandUsage(this, locale));
            return;
        }

        BigDecimal bonus = CommandArguments.getBigDecimalAmount(sender, args[4]);

        if (bonus == null)
            return;

        int islandsChangedCount = 0;

        for (Island island : islands) {
            if (isWorthBonus) {
                PluginEvent<PluginEventArgs.IslandChangeWorthBonus> event = PluginEventsFactory.callIslandChangeWorthBonusEvent(
                        island, sender, IslandChangeWorthBonusEvent.Reason.COMMAND, bonus);
                if (!event.isCancelled()) {
                    island.setBonusWorth(event.getArgs().worthBonus);
                    ++islandsChangedCount;
                }
            } else {
                PluginEvent<PluginEventArgs.IslandChangeLevelBonus> event = PluginEventsFactory.callIslandChangeLevelBonusEvent(
                        island, sender, IslandChangeLevelBonusEvent.Reason.COMMAND, bonus);
                if (!event.isCancelled()) {
                    island.setBonusLevel(event.getArgs().levelBonus);
                    ++islandsChangedCount;
                }
            }
        }

        if (islandsChangedCount <= 0)
            return;

        if (isWorthBonus) {
            if (islandsChangedCount > 1)
                Message.CHANGED_BONUS_WORTH_ALL.send(sender);
            else if (targetPlayer == null)
                Message.CHANGED_BONUS_WORTH_NAME.send(sender, islands.get(0).getName());
            else
                Message.CHANGED_BONUS_WORTH.send(sender, targetPlayer.getName());
        } else {
            if (islandsChangedCount > 1)
                Message.CHANGED_BONUS_LEVEL_ALL.send(sender);
            else if (targetPlayer == null)
                Message.CHANGED_BONUS_LEVEL_NAME.send(sender, islands.get(0).getName());
            else
                Message.CHANGED_BONUS_LEVEL.send(sender, targetPlayer.getName());
        }
    }

    @Override
    public List<String> adminTabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, Island island, String[] args) {
        return args.length == 4 ? CommandTabCompletes.getCustomComplete(args[3], "worth", "level") : Collections.emptyList();
    }

}
