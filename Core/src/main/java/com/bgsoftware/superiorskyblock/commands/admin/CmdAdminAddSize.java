package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.IAdminIslandCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.commands.arguments.NumberArgument;
import com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEvent;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEventsFactory;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class CmdAdminAddSize implements IAdminIslandCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("addsize");
    }

    @Override
    public String getPermission() {
        return "superior.admin.addsize";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "admin addsize <" +
                Message.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "/" +
                Message.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + "/" +
                Message.COMMAND_ARGUMENT_ALL_ISLANDS.getMessage(locale) + "> <" +
                Message.COMMAND_ARGUMENT_SIZE.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_ADMIN_ADD_SIZE.getMessage(locale);
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
        NumberArgument<Integer> arguments = CommandArguments.getSize(sender, args[3]);

        if (!arguments.isSucceed())
            return;

        int size = arguments.getNumber();

        if (size > plugin.getSettings().getMaxIslandSize()) {
            Message.SIZE_BIGGER_MAX.send(sender);
            return;
        }

        int islandsChangedCount = 0;

        for (Island island : islands) {
            PluginEvent<PluginEventArgs.IslandChangeBorderSize> event = PluginEventsFactory.callIslandChangeBorderSizeEvent(
                    island, sender, island.getIslandSize() + size);
            if (!event.isCancelled()) {
                island.setIslandSize(event.getArgs().borderSize);
                ++islandsChangedCount;
            }
        }

        if (islandsChangedCount <= 0)
            return;

        if (islandsChangedCount > 1)
            Message.CHANGED_ISLAND_SIZE_ALL.send(sender);
        else if (targetPlayer == null)
            Message.CHANGED_ISLAND_SIZE_NAME.send(sender, islands.get(0).getName());
        else
            Message.CHANGED_ISLAND_SIZE.send(sender, targetPlayer.getName());

        if (plugin.getSettings().isBuildOutsideIsland())
            Message.CHANGED_ISLAND_SIZE_BUILD_OUTSIDE.send(sender);
    }

}
