package com.bgsoftware.superiorskyblock.module.upgrades.commands;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgument;
import com.bgsoftware.superiorskyblock.api.events.IslandUpgradeEvent;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.upgrades.Upgrade;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.InternalIslandCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArgumentsBuilder;
import com.bgsoftware.superiorskyblock.commands.arguments.types.IntArgumentType;
import com.bgsoftware.superiorskyblock.commands.arguments.types.IslandArgumentType;
import com.bgsoftware.superiorskyblock.commands.arguments.types.UpgradeArgumentType;
import com.bgsoftware.superiorskyblock.commands.context.IslandCommandContext;
import com.bgsoftware.superiorskyblock.core.events.EventResult;
import com.bgsoftware.superiorskyblock.core.events.EventsBus;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class CmdAdminSetUpgrade implements InternalIslandCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("setupgrade");
    }

    @Override
    public String getPermission() {
        return "superior.admin.setupgrade";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_ADMIN_SET_UPGRADE.getMessage(locale);
    }

    @Override
    public List<CommandArgument<?>> getArguments() {
        return new CommandArgumentsBuilder().add(CommandArguments.required("island", IslandArgumentType.INCLUDE_PLAYERS, Message.COMMAND_ARGUMENT_PLAYER_NAME, Message.COMMAND_ARGUMENT_ISLAND_NAME)).add(CommandArguments.required("upgrade", UpgradeArgumentType.INSTANCE, Message.COMMAND_ARGUMENT_UPGRADE_NAME)).add(CommandArguments.required("level", IntArgumentType.LEVEL, Message.COMMAND_ARGUMENT_LEVEL)).build();
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

        Upgrade upgrade = context.getRequiredArgument("upgrade", Upgrade.class);
        int level = context.getRequiredArgument("level", Integer.class);

        int maxLevel = upgrade.getMaxUpgradeLevel();
        if (level > maxLevel) {
            Message.MAXIMUM_LEVEL.send(dispatcher, maxLevel);
            return;
        }

        Island island = context.getIsland();

        EventResult<EventsBus.UpgradeResult> eventResult = plugin.getEventsBus().callIslandUpgradeEvent(dispatcher, island, upgrade, upgrade.getUpgradeLevel(level), IslandUpgradeEvent.Cause.ADMIN_SET_UPGRADE);

        if (eventResult.isCancelled()) return;

        island.setUpgradeLevel(upgrade, level);

        SuperiorPlayer targetPlayer = context.getTargetPlayer();

        if (targetPlayer == null) Message.SET_UPGRADE_LEVEL_NAME.send(dispatcher, upgrade.getName(), island.getName());
        else Message.SET_UPGRADE_LEVEL.send(dispatcher, upgrade.getName(), targetPlayer.getName());
    }

}
