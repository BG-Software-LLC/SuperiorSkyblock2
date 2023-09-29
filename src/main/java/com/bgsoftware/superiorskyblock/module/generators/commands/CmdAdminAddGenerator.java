package com.bgsoftware.superiorskyblock.module.generators.commands;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.CommandSyntaxException;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgument;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.InternalIslandsCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArgumentsBuilder;
import com.bgsoftware.superiorskyblock.commands.arguments.types.EnumArgumentType;
import com.bgsoftware.superiorskyblock.commands.arguments.types.MultipleIslandsArgumentType;
import com.bgsoftware.superiorskyblock.commands.arguments.types.PercentageArgumentType;
import com.bgsoftware.superiorskyblock.commands.arguments.types.StringArgumentType;
import com.bgsoftware.superiorskyblock.commands.context.IslandsCommandContext;
import com.bgsoftware.superiorskyblock.core.events.EventResult;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.key.Keys;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class CmdAdminAddGenerator implements InternalIslandsCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("addgenerator");
    }

    @Override
    public String getPermission() {
        return "superior.admin.addgenerator";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_ADMIN_ADD_GENERATOR.getMessage(locale);
    }

    @Override
    public List<CommandArgument<?>> getArguments() {
        return new CommandArgumentsBuilder()
                .add(CommandArgument.required("islands", MultipleIslandsArgumentType.INCLUDE_PLAYERS, Message.COMMAND_ARGUMENT_PLAYER_NAME, Message.COMMAND_ARGUMENT_ISLAND_NAME, Message.COMMAND_ARGUMENT_ALL_ISLANDS))
                .add(CommandArgument.required("material", StringArgumentType.INSTANCE, Message.COMMAND_ARGUMENT_MATERIAL))
                .add(CommandArgument.required("value", PercentageArgumentType.AMOUNT, Message.COMMAND_ARGUMENT_VALUE))
                .add(CommandArgument.optional("environment", EnumArgumentType.WORLD_ENVIRONMENT, "normal/nether/the_end"))
                .build();
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return true;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, IslandsCommandContext context) throws CommandSyntaxException {
        CommandSender dispatcher = context.getDispatcher();

        List<Island> islands = context.getIslands();
        Key material = Keys.ofMaterialAndData(context.getRequiredArgument("material", String.class));
        PercentageArgumentType.Result valueResult = context.getRequiredArgument("value", PercentageArgumentType.Result.class);
        World.Environment environment = context.getOptionalArgument("environment", World.Environment.class)
                .orElse(plugin.getSettings().getWorlds().getDefaultWorld());

        boolean isPercentage = valueResult.isPercentage();
        int amount = valueResult.getValue();

        if (amount > 0) {
            boolean anyIslandChanged = false;

            SuperiorPlayer superiorPlayer = dispatcher instanceof Player ? plugin.getPlayers().getSuperiorPlayer(dispatcher) : null;

            for (Island island : islands) {
                if (isPercentage) {
                    int ratePercentage = Math.max(0, Math.min(100, island.getGeneratorPercentage(material, environment) + amount));
                    if (!island.setGeneratorPercentage(material, ratePercentage, environment, superiorPlayer, true)) {
                        continue;
                    }
                } else {
                    int generatorRate = island.getGeneratorAmount(material, environment) + amount;

                    if (generatorRate <= 0) {
                        if (!plugin.getEventsBus().callIslandRemoveGeneratorRateEvent(dispatcher, island, material, environment))
                            continue;

                        island.removeGeneratorAmount(material, environment);
                    } else {
                        EventResult<Integer> eventResult = plugin.getEventsBus().callIslandChangeGeneratorRateEvent(dispatcher,
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
        }

        SuperiorPlayer targetPlayer = context.getTargetPlayer();

        if (islands.size() != 1)
            Message.GENERATOR_UPDATED_ALL.send(dispatcher, Formatters.CAPITALIZED_FORMATTER.format(material.getGlobalKey()));
        else if (targetPlayer == null)
            Message.GENERATOR_UPDATED_NAME.send(dispatcher, Formatters.CAPITALIZED_FORMATTER.format(material.getGlobalKey()), islands.get(0).getName());
        else
            Message.GENERATOR_UPDATED.send(dispatcher, Formatters.CAPITALIZED_FORMATTER.format(material.getGlobalKey()), targetPlayer.getName());
    }

}
