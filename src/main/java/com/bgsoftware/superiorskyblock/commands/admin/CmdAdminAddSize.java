package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.IAdminIslandCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.commands.arguments.NumberArgument;
import com.bgsoftware.superiorskyblock.core.events.EventResult;
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
                Message.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + "> <" +
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

        boolean anyIslandChanged = false;

        for (Island island : islands) {
            EventResult<Integer> eventResult = plugin.getEventsBus().callIslandChangeBorderSizeEvent(sender,
                    island, island.getIslandSize() + size);
            anyIslandChanged |= !eventResult.isCancelled();
            if (!eventResult.isCancelled())
                island.setIslandSize(eventResult.getResult());
        }

        if (!anyIslandChanged)
            return;

        if (islands.size() > 1)
            Message.CHANGED_ISLAND_SIZE_ALL.send(sender);
        else if (targetPlayer == null)
            Message.CHANGED_ISLAND_SIZE_NAME.send(sender, islands.get(0).getName());
        else
            Message.CHANGED_ISLAND_SIZE.send(sender, targetPlayer.getName());

        if (plugin.getSettings().isBuildOutsideIsland())
            Message.CHANGED_ISLAND_SIZE_BUILD_OUTSIDE.send(sender);
    }

}
