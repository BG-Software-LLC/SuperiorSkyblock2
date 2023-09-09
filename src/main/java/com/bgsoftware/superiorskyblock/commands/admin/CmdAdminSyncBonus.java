package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.events.IslandChangeLevelBonusEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandChangeWorthBonusEvent;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.schematic.Schematic;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.CommandTabCompletes;
import com.bgsoftware.superiorskyblock.commands.IAdminIslandCommand;
import com.bgsoftware.superiorskyblock.core.events.EventResult;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CmdAdminSyncBonus implements IAdminIslandCommand {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("syncbonus");
    }

    @Override
    public String getPermission() {
        return "superior.admin.syncbonus";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "admin syncbonus <" +
                Message.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "/" +
                Message.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + "/" +
                Message.COMMAND_ARGUMENT_ALL_ISLANDS.getMessage(locale) + "> <worth/level>";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_ADMIN_SYNC_BONUS.getMessage(locale);
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
        boolean isWorthBonus = !args[3].equalsIgnoreCase("level");

        boolean anyIslandChanged = false;

        for (Island island : islands) {
            BigDecimal currentBonus = isWorthBonus ? island.getBonusWorth() : island.getBonusLevel();
            BigDecimal newBonus = calculateValue(island, isWorthBonus);
            if (!newBonus.equals(currentBonus)) {
                if (isWorthBonus) {
                    EventResult<BigDecimal> eventResult = plugin.getEventsBus().callIslandChangeWorthBonusEvent(sender, island,
                            IslandChangeWorthBonusEvent.Reason.COMMAND, newBonus);
                    if (!eventResult.isCancelled()) {
                        island.setBonusWorth(eventResult.getResult());
                        anyIslandChanged = true;
                    }
                } else {
                    EventResult<BigDecimal> eventResult = plugin.getEventsBus().callIslandChangeLevelBonusEvent(sender, island,
                            IslandChangeLevelBonusEvent.Reason.COMMAND, newBonus);
                    if (!eventResult.isCancelled()) {
                        island.setBonusLevel(eventResult.getResult());
                        anyIslandChanged = true;
                    }
                }
            }
        }

        if (!anyIslandChanged)
            return;

        if (islands.size() > 1)
            Message.BONUS_SYNC_ALL.send(sender);
        else if (targetPlayer == null)
            Message.BONUS_SYNC_NAME.send(sender, islands.get(0).getName());
        else
            Message.BONUS_SYNC.send(sender, targetPlayer.getName());
    }

    @Override
    public List<String> adminTabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, Island island, String[] args) {
        return args.length == 4 ? CommandTabCompletes.getCustomComplete(args[3], "worth", "level") : Collections.emptyList();
    }

    private static BigDecimal calculateValue(Island island, boolean calculateWorth) {
        BigDecimal value = BigDecimal.ZERO;

        String generatedSchematic = island.getSchematicName();

        if (island.wasSchematicGenerated(World.Environment.NORMAL)) {
            Schematic schematic = plugin.getSchematics().getSchematic(generatedSchematic);
            if (schematic != null) {
                value = value.add(_calculateValues(schematic.getBlockCounts(), calculateWorth));
            }
        }

        if (island.wasSchematicGenerated(World.Environment.NETHER)) {
            Schematic schematic = plugin.getSchematics().getSchematic(generatedSchematic + "_nether");
            if (schematic != null) {
                value = value.add(_calculateValues(schematic.getBlockCounts(), calculateWorth));
            }
        }

        if (island.wasSchematicGenerated(World.Environment.THE_END)) {
            Schematic schematic = plugin.getSchematics().getSchematic(generatedSchematic + "_the_end");
            if (schematic != null) {
                value = value.add(_calculateValues(schematic.getBlockCounts(), calculateWorth));
            }
        }

        return value.negate();
    }

    private static BigDecimal _calculateValues(Map<Key, Integer> blockCounts, boolean calculateWorth) {
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
