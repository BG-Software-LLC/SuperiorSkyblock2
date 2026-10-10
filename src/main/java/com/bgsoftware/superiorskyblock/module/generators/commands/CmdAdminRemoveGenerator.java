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
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEventsFactory;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.key.Keys;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class CmdAdminRemoveGenerator implements IAdminIslandCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("removegenerator");
    }

    @Override
    public String getPermission() {
        return "superior.admin.removegenerator";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "admin removegenerator <" +
                Message.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "/" +
                Message.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + "/" +
                Message.COMMAND_ARGUMENT_ALL_ISLANDS.getMessage(locale) + "> <" +
                Message.COMMAND_ARGUMENT_MATERIAL.getMessage(locale) + "> [" +
                Message.COMMAND_ARGUMENT_DIMENSION.getMessage(locale) + "]";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_ADMIN_REMOVE_GENERATOR.getMessage(locale);
    }

    @Override
    public int getMinArgs() {
        return 4;
    }

    @Override
    public int getMaxArgs() {
        return 5;
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

        Dimension dimension = args.length == 4 ? plugin.getSettings().getWorlds().getDefaultWorldDimension() :
                CommandArguments.getDimension(sender, args[4]);

        if (dimension == null) {
            return;
        }

        int islandsChangedCount = 0;

        for (Island island : islands) {
            if (PluginEventsFactory.callIslandRemoveGeneratorRateEvent(island, sender, material, dimension)) {
                island.removeGeneratorAmount(material, dimension);
                ++islandsChangedCount;
            }
        }

        if (islandsChangedCount <= 0) {
            return;
        }

        if (islandsChangedCount > 1) {
            Message.GENERATOR_UPDATED_ALL.send(sender, Formatters.CAPITALIZED_FORMATTER.format(material.getGlobalKey()));
        } else if (targetPlayer == null) {
            Message.GENERATOR_UPDATED_NAME.send(sender, Formatters.CAPITALIZED_FORMATTER.format(material.getGlobalKey()), islands.get(0).getName());
        } else {
            Message.GENERATOR_UPDATED.send(sender, Formatters.CAPITALIZED_FORMATTER.format(material.getGlobalKey()), targetPlayer.getName());
        }
    }

    @Override
    public List<String> adminTabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, Island island, String[] args) {
        return args.length == 4 ? CommandTabCompletes.getMaterialsForGenerators(args[3]) :
                args.length == 5 ? CommandTabCompletes.getDimensions(plugin, args[4]) : Collections.emptyList();
    }

}
