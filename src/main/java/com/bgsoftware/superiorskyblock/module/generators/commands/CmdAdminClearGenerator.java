package com.bgsoftware.superiorskyblock.module.generators.commands;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.world.Dimension;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.CommandTabCompletes;
import com.bgsoftware.superiorskyblock.commands.IAdminIslandCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEventsFactory;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.Collections;
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
                Message.COMMAND_ARGUMENT_DIMENSION.getMessage(locale) + "]";
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
        Dimension dimension = args.length == 3 ? plugin.getSettings().getWorlds().getDefaultWorldDimension() :
                CommandArguments.getDimension(sender, args[3]);

        if (dimension == null)
            return;

        int islandsChangedCount = 0;

        for (Island island : islands) {
            if (!PluginEventsFactory.callIslandClearGeneratorRatesEvent(island, sender, dimension))
                continue;

            ++islandsChangedCount;

            island.clearGeneratorAmounts(dimension);
        }

        if (islandsChangedCount <= 0)
            return;

        if (islands.size() != 1)
            Message.GENERATOR_CLEARED_ALL.send(sender);
        else if (targetPlayer == null)
            Message.GENERATOR_CLEARED_NAME.send(sender, islands.get(0).getName());
        else
            Message.GENERATOR_CLEARED.send(sender, targetPlayer.getName());
    }

    @Override
    public List<String> adminTabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, Island island, String[] args) {
        return args.length == 4 ? CommandTabCompletes.getDimensions(plugin, args[3]) : Collections.emptyList();
    }
}
