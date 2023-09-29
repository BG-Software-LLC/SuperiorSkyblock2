package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgument;
import com.bgsoftware.superiorskyblock.api.events.IslandChangeLevelBonusEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandChangeWorthBonusEvent;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.schematic.Schematic;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.InternalIslandsCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArgumentsBuilder;
import com.bgsoftware.superiorskyblock.commands.arguments.types.MultipleIslandsArgumentType;
import com.bgsoftware.superiorskyblock.commands.arguments.types.StringArgumentType;
import com.bgsoftware.superiorskyblock.commands.context.IslandsCommandContext;
import com.bgsoftware.superiorskyblock.core.events.EventResult;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CmdAdminSyncBonus implements InternalIslandsCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("syncbonus");
    }

    @Override
    public String getPermission() {
        return "superior.admin.syncbonus";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_ADMIN_SYNC_BONUS.getMessage(locale);
    }

    @Override
    public List<CommandArgument<?>> getArguments()

    {
        return new CommandArgumentsBuilder()
                .add(CommandArguments.required("islands", MultipleIslandsArgumentType.INCLUDE_PLAYERS, Message.COMMAND_ARGUMENT_PLAYER_NAME, Message.COMMAND_ARGUMENT_ISLAND_NAME, Message.COMMAND_ARGUMENT_ALL_ISLANDS))
                .add(CommandArgument.required("bonus-type", "worth/level", StringArgumentType.INSTANCE))
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
        String bonusType = context.getRequiredArgument("bonus-type", String.class);

        boolean isWorthBonus = !bonusType.equalsIgnoreCase("level");

        boolean anyIslandChanged = false;

        for (Island island : islands) {
            BigDecimal currentBonus = isWorthBonus ? island.getBonusWorth() : island.getBonusLevel();
            BigDecimal newBonus = calculateValue(plugin, island, isWorthBonus);
            if (!newBonus.equals(currentBonus)) {
                if (isWorthBonus) {
                    EventResult<BigDecimal> eventResult = plugin.getEventsBus().callIslandChangeWorthBonusEvent(
                            dispatcher, island, IslandChangeWorthBonusEvent.Reason.COMMAND, newBonus);
                    if (!eventResult.isCancelled()) {
                        island.setBonusWorth(eventResult.getResult());
                        anyIslandChanged = true;
                    }
                } else {
                    EventResult<BigDecimal> eventResult = plugin.getEventsBus().callIslandChangeLevelBonusEvent(
                            dispatcher, island, IslandChangeLevelBonusEvent.Reason.COMMAND, newBonus);
                    if (!eventResult.isCancelled()) {
                        island.setBonusLevel(eventResult.getResult());
                        anyIslandChanged = true;
                    }
                }
            }
        }

        if (!anyIslandChanged)
            return;

        SuperiorPlayer targetPlayer = context.getTargetPlayer();

        if (islands.size() > 1)
            Message.BONUS_SYNC_ALL.send(dispatcher);
        else if (targetPlayer == null)
            Message.BONUS_SYNC_NAME.send(dispatcher, islands.get(0).getName());
        else
            Message.BONUS_SYNC.send(dispatcher, targetPlayer.getName());
    }

    private static BigDecimal calculateValue(SuperiorSkyblockPlugin plugin, Island island, boolean calculateWorth) {
        BigDecimal value = BigDecimal.ZERO;

        String generatedSchematic = island.getSchematicName();

        if (island.wasSchematicGenerated(World.Environment.NORMAL)) {
            Schematic schematic = plugin.getSchematics().getSchematic(generatedSchematic);
            if (schematic != null) {
                value = value.add(_calculateValues(plugin, schematic.getBlockCounts(), calculateWorth));
            }
        }

        if (island.wasSchematicGenerated(World.Environment.NETHER)) {
            Schematic schematic = plugin.getSchematics().getSchematic(generatedSchematic + "_nether");
            if (schematic != null) {
                value = value.add(_calculateValues(plugin, schematic.getBlockCounts(), calculateWorth));
            }
        }

        if (island.wasSchematicGenerated(World.Environment.THE_END)) {
            Schematic schematic = plugin.getSchematics().getSchematic(generatedSchematic + "_the_end");
            if (schematic != null) {
                value = value.add(_calculateValues(plugin, schematic.getBlockCounts(), calculateWorth));
            }
        }

        return value.negate();
    }

    private static BigDecimal _calculateValues(SuperiorSkyblockPlugin plugin, Map<Key, Integer> blockCounts, boolean calculateWorth) {
        BigDecimal value = BigDecimal.ZERO;

        for (Map.Entry<Key, Integer> blockEntry : blockCounts.entrySet()) {
            BigDecimal blockValue = calculateWorth ? plugin.getBlockValues().getBlockWorth(blockEntry.getKey()) :
                    plugin.getBlockValues().getBlockLevel(blockEntry.getKey());
            if (blockValue.compareTo(BigDecimal.ZERO) > 0) {
                value = value.add(blockValue.multiply(new BigDecimal(blockEntry.getValue())));
            }
        }

        return value;
    }

}
