package com.bgsoftware.superiorskyblock.module.generators.commands;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.world.Dimension;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.BulkChange;
import com.bgsoftware.superiorskyblock.commands.ChangeResult;
import com.bgsoftware.superiorskyblock.commands.CommandTabCompletes;
import com.bgsoftware.superiorskyblock.commands.IAdminIslandCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.commands.arguments.NumberArgument;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEventsFactory;
import com.bgsoftware.superiorskyblock.core.key.Keys;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.core.mutable.MutableInt;
import com.bgsoftware.superiorskyblock.world.GeneratorType;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class CmdAdminAddGenerator implements IAdminIslandCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("addgenerator");
    }

    @Override
    public String getPermission() {
        return "superior.admin.addgenerator";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "admin addgenerator <" +
                Message.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "/" +
                Message.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + "/" +
                Message.COMMAND_ARGUMENT_ALL_ISLANDS.getMessage(locale) + "> <" +
                Message.COMMAND_ARGUMENT_MATERIAL.getMessage(locale) + "> <" +
                Message.COMMAND_ARGUMENT_VALUE.getMessage(locale) + "> [" +
                Message.COMMAND_ARGUMENT_DIMENSION.getMessage(locale) + "]";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_ADMIN_ADD_GENERATOR.getMessage(locale);
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

        if (amount == 0) {
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
        MutableInt islandsFilledWithDefaultBlockCount = new MutableInt(0);
        MutableInt defaultBlockPercentage = new MutableInt(0);

        BulkChange<Island> change = BulkChange.ofIslands(islands, island -> {
            if (percentage) {
                int ratePercentage = Math.max(0, Math.min(100, island.getExactGeneratorPercentage(material, dimension) + amount));
                int oldDefaultBlockAmount = defaultBlock == null ? 0 : island.getGeneratorAmount(defaultBlock, dimension);

                if (!island.setGeneratorPercentage(material, ratePercentage, dimension,
                        sender instanceof Player ? plugin.getPlayers().getSuperiorPlayer(sender) : null, true)) {
                    return ChangeResult.CANCELLED;
                }

                if (CmdAdminSetGenerator.isFilledWithDefaultBlock(island, material, dimension, defaultBlock, oldDefaultBlockAmount)) {
                    islandsFilledWithDefaultBlockCount.set(islandsFilledWithDefaultBlockCount.get() + 1);
                    defaultBlockPercentage.set(island.getGeneratorPercentage(defaultBlock, dimension));
                }

                return ChangeResult.APPLIED;
            }

            int newAmount = island.getExactGeneratorAmount(material, dimension) + amount;

            if (newAmount < 0) {
                return ChangeResult.BELOW_MINIMUM;
            }

            return ChangeResult.ifAllowed(PluginEventsFactory.callIslandChangeGeneratorRateEvent(
                            island, sender, material, dimension, newAmount),
                    eventArgs -> eventArgs.island.setGeneratorAmount(eventArgs.block, eventArgs.generatorRate, eventArgs.dimension));
        });

        change.sendSummary(sender, args);

        if (islandsFilledWithDefaultBlockCount.get() > 0) {
            CmdAdminSetGenerator.sendFilledWithDefaultBlockMessage(sender, defaultBlock, defaultBlockPercentage.get(),
                    islandsFilledWithDefaultBlockCount.get(), change.getAppliedCount());
        }
    }

    @Override
    public List<String> adminTabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, Island island, String[] args) {
        return args.length == 4 ? CommandTabCompletes.getMaterialsForGenerators(args[3]) :
                args.length == 6 ? CommandTabCompletes.getDimensions(plugin, args[5]) : Collections.emptyList();
    }

}
