package com.bgsoftware.superiorskyblock.module.generators.commands;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.world.Dimension;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.CommandTabCompletes;
import com.bgsoftware.superiorskyblock.commands.IAdminIslandCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.commands.arguments.NumberArgument;
import com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEvent;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEventsFactory;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.key.Keys;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.world.GeneratorType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class CmdAdminSetGenerator implements IAdminIslandCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("setgenerator");
    }

    @Override
    public String getPermission() {
        return "superior.admin.setgenerator";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "admin setgenerator <" +
                Message.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "/" +
                Message.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + "/" +
                Message.COMMAND_ARGUMENT_ALL_ISLANDS.getMessage(locale) + "> <" +
                Message.COMMAND_ARGUMENT_MATERIAL.getMessage(locale) + "> <" +
                Message.COMMAND_ARGUMENT_VALUE.getMessage(locale) + "> [" +
                Message.COMMAND_ARGUMENT_DIMENSION.getMessage(locale) + "]";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_ADMIN_SET_GENERATOR.getMessage(locale);
    }

    @Override
    public int getMinArgs() {
        return 5;
    }

    @Override
    public int getMaxArgs() {
        return 6;
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
        Key material = Keys.ofMaterialAndData(args[3]);
        boolean percentage = args[4].endsWith("%");

        if (percentage) {
            args[4] = args[4].substring(0, args[4].length() - 1);
        }

        NumberArgument<Integer> arguments = CommandArguments.getAmount(sender, args[4]);

        if (!arguments.isSucceed()) {
            return;
        }

        int amount = arguments.getNumber();

        if (!percentage && amount < 0) {
            Message.INVALID_AMOUNT.send(sender, amount);
            return;
        }

        if (percentage && (amount < 0 || amount > 100)) {
            Message.INVALID_PERCENTAGE.send(sender);
            return;
        }

        Dimension dimension = args.length == 5 ? plugin.getSettings().getWorlds().getDefaultWorldDimension() :
                CommandArguments.getDimension(sender, args[5]);

        if (dimension == null) {
            return;
        }

        Key defaultBlock = GeneratorType.fromDimension(dimension).getDefaultBlock();
        Island changedIsland = null;
        int islandsChangedCount = 0;
        int islandsFilledWithDefaultBlockCount = 0;
        int defaultBlockPercentage = 0;

        for (Island island : islands) {
            if (percentage) {
                int oldDefaultBlockAmount = defaultBlock == null ? 0 : island.getGeneratorAmount(defaultBlock, dimension);

                if (!island.setGeneratorPercentage(material, amount, dimension,
                        sender instanceof Player ? plugin.getPlayers().getSuperiorPlayer(sender) : null, true)) {
                    continue;
                }

                if (isFilledWithDefaultBlock(island, material, dimension, defaultBlock, oldDefaultBlockAmount)) {
                    ++islandsFilledWithDefaultBlockCount;
                    defaultBlockPercentage = island.getGeneratorPercentage(defaultBlock, dimension);
                }
            } else {
                PluginEvent<PluginEventArgs.IslandChangeGeneratorRate> event = PluginEventsFactory.callIslandChangeGeneratorRateEvent(
                        island, sender, material, dimension, amount);
                if (event.isCancelled()) {
                    continue;
                }

                island.setGeneratorAmount(material, event.getArgs().generatorRate, dimension);
            }

            changedIsland = island;
            ++islandsChangedCount;
        }

        if (islandsChangedCount <= 0) {
            return;
        }

        if (islandsChangedCount > 1) {
            Message.GENERATOR_UPDATED_ALL.send(sender, Formatters.CAPITALIZED_FORMATTER.format(material.getGlobalKey()));
        } else if (targetPlayer == null) {
            Message.GENERATOR_UPDATED_NAME.send(sender, Formatters.CAPITALIZED_FORMATTER.format(material.getGlobalKey()), changedIsland.getName());
        } else {
            Message.GENERATOR_UPDATED.send(sender, Formatters.CAPITALIZED_FORMATTER.format(material.getGlobalKey()), targetPlayer.getName());
        }

        if (islandsFilledWithDefaultBlockCount > 0) {
            sendFilledWithDefaultBlockMessage(sender, defaultBlock, defaultBlockPercentage,
                    islandsFilledWithDefaultBlockCount, islandsChangedCount);
        }
    }

    // Island#setGeneratorPercentage gives the rest of the percentage to the default block when there are no other
    // blocks in the generator. This is detected from the amounts of the default block before and after the call,
    // so the rules of that method are not repeated here. Also used by CmdAdminAddGenerator.
    static boolean isFilledWithDefaultBlock(Island island, Key material, Dimension dimension,
                                            @Nullable Key defaultBlock, int oldDefaultBlockAmount) {
        return defaultBlock != null && !defaultBlock.equals(material) && oldDefaultBlockAmount <= 0 &&
                island.getGeneratorAmount(defaultBlock, dimension) > 0;
    }

    static void sendFilledWithDefaultBlockMessage(CommandSender sender, Key defaultBlock, int defaultBlockPercentage,
                                                  int islandsFilledCount, int islandsChangedCount) {
        String defaultBlockName = Formatters.CAPITALIZED_FORMATTER.format(defaultBlock.getGlobalKey());
        if (islandsChangedCount > 1) {
            Message.GENERATOR_FILLED_WITH_DEFAULT_BLOCK_ALL.send(sender, defaultBlockPercentage, defaultBlockName, islandsFilledCount);
        } else {
            Message.GENERATOR_FILLED_WITH_DEFAULT_BLOCK.send(sender, defaultBlockPercentage, defaultBlockName);
        }
    }

    @Override
    public List<String> adminTabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, Island island, String[] args) {
        return args.length == 4 ? CommandTabCompletes.getMaterialsForGenerators(args[3]) :
                args.length == 6 ? CommandTabCompletes.getDimensions(plugin, args[5]) : Collections.emptyList();
    }

}
