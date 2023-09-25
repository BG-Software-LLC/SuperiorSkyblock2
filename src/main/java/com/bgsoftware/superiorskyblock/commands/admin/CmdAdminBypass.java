package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.CommandContext;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgument;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.InternalSuperiorCommand;
import com.bgsoftware.superiorskyblock.core.messages.Message;

import java.util.Collections;
import java.util.List;

public class CmdAdminBypass implements InternalSuperiorCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("bypass");
    }

    @Override
    public String getPermission() {
        return "superior.admin.bypass";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_ADMIN_BYPASS.getMessage(locale);
    }

    @Override
    public List<CommandArgument<?>> getArguments() {
        return Collections.emptyList();
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return false;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, CommandContext context) {
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(context.getDispatcher());

        if (!plugin.getEventsBus().callPlayerToggleBypassEvent(superiorPlayer))
            return;

        if (superiorPlayer.hasBypassModeEnabled()) {
            Message.TOGGLED_BYPASS_OFF.send(superiorPlayer);
        } else {
            Message.TOGGLED_BYPASS_ON.send(superiorPlayer);
        }

        superiorPlayer.toggleBypassMode();
    }

}
