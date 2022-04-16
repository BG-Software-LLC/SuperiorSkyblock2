package com.bgsoftware.superiorskyblock.module.generators.commands;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.CommandTabCompletes;
import com.bgsoftware.superiorskyblock.commands.IAdminIslandCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.commands.arguments.NumberArgument;
import com.bgsoftware.superiorskyblock.formatting.Formatters;
import com.bgsoftware.superiorskyblock.key.KeyImpl;
import com.bgsoftware.superiorskyblock.lang.Message;
import com.bgsoftware.superiorskyblock.threads.Executor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CmdAdminSetGenerator implements IAdminIslandCommand {

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
                Message.COMMAND_ARGUMENT_WORLD.getMessage(locale) + "]";
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
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, SuperiorPlayer targetPlayer, List<Island> islands, String[] args) {
        Material rawMaterial = CommandArguments.getMaterial(sender, args[3]);

        if (rawMaterial == null)
            return;

        if (!rawMaterial.isSolid()) {
            Message.MATERIAL_NOT_SOLID.send(sender);
            return;
        }

        Key material = KeyImpl.of(args[3]);
        boolean percentage = args[4].endsWith("%");

        if (percentage)
            args[4] = args[4].substring(0, args[4].length() - 1);

        NumberArgument<Integer> arguments = CommandArguments.getAmount(sender, args[4]);

        if (!arguments.isSucceed())
            return;

        int amount = arguments.getNumber();

        if (percentage && (amount < 0 || amount > 100)) {
            Message.INVALID_PERCENTAGE.send(sender);
            return;
        }

        World.Environment environment = args.length == 5 ? plugin.getSettings().getWorlds().getDefaultWorld() :
                CommandArguments.getEnvironment(sender, args[5]);

        if (environment == null)
            return;

        Executor.data(() -> islands.forEach(island -> {
            if (percentage) {
                island.setGeneratorPercentage(material, amount, environment);
            } else {
                island.setGeneratorAmount(material, amount, environment);
            }
        }));

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
                args.length == 6 ? CommandTabCompletes.getEnvironments(args[5]) : new ArrayList<>();
    }

}
