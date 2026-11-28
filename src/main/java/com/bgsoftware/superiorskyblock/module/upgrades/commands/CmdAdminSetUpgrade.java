package com.bgsoftware.superiorskyblock.module.upgrades.commands;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.events.IslandUpgradeEvent;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.upgrades.Upgrade;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.CommandTabCompletes;
import com.bgsoftware.superiorskyblock.commands.IAdminIslandCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.commands.arguments.NumberArgument;
import com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEvent;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEventsFactory;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class CmdAdminSetUpgrade implements IAdminIslandCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("setupgrade");
    }

    @Override
    public String getPermission() {
        return "superior.admin.setupgrade";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "admin setupgrade <" +
                Message.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "/" +
                Message.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + "/" +
                Message.COMMAND_ARGUMENT_ALL_ISLANDS.getMessage(locale) + "> <" +
                Message.COMMAND_ARGUMENT_UPGRADE_NAME.getMessage(locale) + "> <" +
                Message.COMMAND_ARGUMENT_LEVEL.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_ADMIN_SET_UPGRADE.getMessage(locale);
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
        Upgrade upgrade = CommandArguments.getUpgrade(plugin, sender, args[3]);

        if (upgrade == null)
            return;

        NumberArgument<Integer> arguments = CommandArguments.getLevel(sender, args[4]);

        if (!arguments.isSucceed())
            return;

        int level = arguments.getNumber();
        int maxLevel = upgrade.getMaxUpgradeLevel();

        if (level > maxLevel) {
            Message.MAXIMUM_LEVEL.send(sender, maxLevel);
            return;
        }

        int islandsChangedCount = 0;

        for (Island island : islands) {
            PluginEvent<PluginEventArgs.IslandUpgrade> event = PluginEventsFactory.callIslandUpgradeEvent(
                    island, sender, upgrade, upgrade.getUpgradeLevel(level), IslandUpgradeEvent.Cause.ADMIN_SET_UPGRADE);
            if (!event.isCancelled()) {
                island.setUpgradeLevel(upgrade, level);
                ++islandsChangedCount;
            }
        }

        if (islandsChangedCount <= 0)
            return;

        if (islandsChangedCount > 1)
            Message.SET_UPGRADE_LEVEL_ALL.send(sender, upgrade.getName());
        else if (targetPlayer == null)
            Message.SET_UPGRADE_LEVEL_NAME.send(sender, upgrade.getName(), islands.get(0).getName());
        else
            Message.SET_UPGRADE_LEVEL.send(sender, upgrade.getName(), targetPlayer.getName());
    }

    @Override
    public List<String> adminTabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, Island island, String[] args) {
        return args.length == 4 ? CommandTabCompletes.getUpgrades(plugin, args[3]) : Collections.emptyList();
    }

}
