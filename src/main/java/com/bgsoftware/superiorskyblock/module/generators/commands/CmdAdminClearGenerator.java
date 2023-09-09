package com.bgsoftware.superiorskyblock.module.generators.commands;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.IAdminIslandCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;

public class CmdAdminClearGenerator implements IAdminIslandCommand {

    @Override
    public List<String> getAliases() {
        return Arrays.asList("cleargenerator", "cg");
    }

    @Override
    public String getPermission() {
        return "superior.admin.cleargenerator";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "admin cleargenerator <" +
                Message.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "/" +
                Message.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + "/" +
                Message.COMMAND_ARGUMENT_ALL_ISLANDS.getMessage(locale) + "> [" +
                Message.COMMAND_ARGUMENT_WORLD.getMessage(locale) + "]";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_ADMIN_CLEAR_GENERATOR.getMessage(locale);
    }

    @Override
    public int getMinArgs() {
        return 3;
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
        World.Environment environment = args.length == 3 ? plugin.getSettings().getWorlds().getDefaultWorld() :
                CommandArguments.getEnvironment(sender, args[3]);

        if (environment == null)
            return;

        boolean anyIslandChanged = false;

        for (Island island : islands) {
            if (!plugin.getEventsBus().callIslandClearGeneratorRatesEvent(sender, island, environment))
                continue;

            anyIslandChanged = true;

            island.clearGeneratorAmounts(environment);
        }

        if (!anyIslandChanged)
            return;

        if (islands.size() != 1)
            Message.GENERATOR_CLEARED_ALL.send(sender);
        else if (targetPlayer == null)
            Message.GENERATOR_CLEARED_NAME.send(sender, islands.get(0).getName());
        else
            Message.GENERATOR_CLEARED.send(sender, targetPlayer.getName());
    }

}
