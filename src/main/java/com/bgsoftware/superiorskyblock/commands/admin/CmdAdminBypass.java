package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.ISuperiorCommand;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class CmdAdminBypass implements ISuperiorCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("bypass");
    }

    @Override
    public String getPermission() {
        return "superior.admin.bypass";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "admin bypass";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_ADMIN_BYPASS.getMessage(locale);
    }

    @Override
    public int getMinArgs() {
        return 2;
    }

    @Override
    public int getMaxArgs() {
        return 2;
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return false;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(sender);

        if (!plugin.getEventsBus().callPlayerToggleBypassEvent(superiorPlayer))
            return;

        if (superiorPlayer.hasBypassModeEnabled()) {
            Message.TOGGLED_BYPASS_OFF.send(superiorPlayer);
        } else {
            Message.TOGGLED_BYPASS_ON.send(superiorPlayer);
        }

        superiorPlayer.toggleBypassMode();
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        return Collections.emptyList();
    }

}
