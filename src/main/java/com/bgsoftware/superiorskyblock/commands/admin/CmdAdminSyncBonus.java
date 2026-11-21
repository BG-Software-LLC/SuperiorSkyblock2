package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.events.IslandChangeLevelBonusEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandChangeWorthBonusEvent;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.schematic.Schematic;
import com.bgsoftware.superiorskyblock.api.world.Dimension;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.BulkChange;
import com.bgsoftware.superiorskyblock.commands.ChangeResult;
import com.bgsoftware.superiorskyblock.commands.CommandTabCompletes;
import com.bgsoftware.superiorskyblock.commands.IAdminIslandCommand;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEventsFactory;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import org.bukkit.command.CommandSender;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
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

        BulkChange<Island> change = BulkChange.ofIslands(islands, island -> {
            BigDecimal currentBonus = isWorthBonus ? island.getBonusWorth() : island.getBonusLevel();
            BigDecimal newBonus = calculateValue(island, isWorthBonus);

            if (newBonus.equals(currentBonus))
                return ChangeResult.APPLIED;

            if (isWorthBonus) {
                return ChangeResult.ifAllowed(PluginEventsFactory.callIslandChangeWorthBonusEvent(island, sender,
                                IslandChangeWorthBonusEvent.Reason.COMMAND, newBonus),
                        eventArgs -> eventArgs.island.setBonusWorth(eventArgs.worthBonus));
            }

            return ChangeResult.ifAllowed(PluginEventsFactory.callIslandChangeLevelBonusEvent(island, sender,
                            IslandChangeLevelBonusEvent.Reason.COMMAND, newBonus),
                    eventArgs -> eventArgs.island.setBonusLevel(eventArgs.levelBonus));
        });

        change.sendSummary(sender, args[1]);
    }

    @Override
    public List<String> adminTabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, Island island, String[] args) {
        return args.length == 4 ? CommandTabCompletes.getCustomComplete(args[3], "worth", "level") : Collections.emptyList();
    }

    private static BigDecimal calculateValue(Island island, boolean calculateWorth) {
        BigDecimal value = BigDecimal.ZERO;

        String generatedSchematic = island.getSchematicName();

        for (Dimension dimension : Dimension.values()) {
            if (island.wasSchematicGenerated(dimension)) {
                String suffix = dimension == plugin.getSettings().getWorlds().getDefaultWorldDimension() ? ""
                        : "_" + dimension.getName().toLowerCase(Locale.ENGLISH);
                Schematic schematic = plugin.getSchematics().getSchematic(generatedSchematic + suffix);
                if (schematic != null) {
                    value = value.add(_calculateValues(schematic.getBlockCounts(), calculateWorth));
                }
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
