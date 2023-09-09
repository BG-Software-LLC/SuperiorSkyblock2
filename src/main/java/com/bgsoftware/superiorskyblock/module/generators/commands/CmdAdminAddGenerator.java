package com.bgsoftware.superiorskyblock.module.generators.commands;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.CommandTabCompletes;
import com.bgsoftware.superiorskyblock.commands.IAdminIslandCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.commands.arguments.NumberArgument;
import com.bgsoftware.superiorskyblock.core.events.EventResult;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.key.Keys;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import org.bukkit.World;
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
                Message.COMMAND_ARGUMENT_WORLD.getMessage(locale) + "]";
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

        if (percentage)
            args[4] = args[4].substring(0, args[4].length() - 1);

        NumberArgument<Integer> arguments = CommandArguments.getAmount(sender, args[4]);

        if (!arguments.isSucceed())
            return;

        int amount = arguments.getNumber();

        if (amount == 0 || (percentage && (amount < 0 || amount > 100))) {
            Message.INVALID_PERCENTAGE.send(sender);
            return;
        }

        World.Environment environment = args.length == 5 ? plugin.getSettings().getWorlds().getDefaultWorld() :
                CommandArguments.getEnvironment(sender, args[5]);

        if (environment == null)
            return;

        boolean anyIslandChanged = false;

        for (Island island : islands) {
            if (percentage) {
                int ratePercentage = Math.max(0, Math.min(100, island.getGeneratorPercentage(material, environment) + amount));
                if (!island.setGeneratorPercentage(material, ratePercentage, environment,
                        sender instanceof Player ? plugin.getPlayers().getSuperiorPlayer(sender) : null, true)) {
                    continue;
                }
            } else {
                int generatorRate = island.getGeneratorAmount(material, environment) + amount;

                if (generatorRate <= 0) {
                    if (!plugin.getEventsBus().callIslandRemoveGeneratorRateEvent(sender, island, material, environment))
                        continue;

                    island.removeGeneratorAmount(material, environment);
                } else {
                    EventResult<Integer> eventResult = plugin.getEventsBus().callIslandChangeGeneratorRateEvent(sender,
                            island, material, environment, island.getGeneratorAmount(material, environment) + amount);

                    if (eventResult.isCancelled())
                        continue;

                    island.setGeneratorAmount(material, eventResult.getResult(), environment);
                }
            }
            anyIslandChanged = true;
        }

        if (!anyIslandChanged)
            return;

        if (islands.size() != 1)
            Message.GENERATOR_UPDATED_ALL.send(sender, Formatters.CAPITALIZED_FORMATTER.format(material.getGlobalKey()));
        else if (targetPlayer == null)
            Message.GENERATOR_UPDATED_NAME.send(sender, Formatters.CAPITALIZED_FORMATTER.format(material.getGlobalKey()), islands.get(0).getName());
        else
            Message.GENERATOR_UPDATED.send(sender, Formatters.CAPITALIZED_FORMATTER.format(material.getGlobalKey()), targetPlayer.getName());
    }

    @Override
    public List<String> adminTabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, Island island, String[] args) {
        return args.length == 4 ? CommandTabCompletes.getMaterialsForGenerators(args[3]) :
                args.length == 6 ? CommandTabCompletes.getEnvironments(args[5]) : Collections.emptyList();
    }

}
