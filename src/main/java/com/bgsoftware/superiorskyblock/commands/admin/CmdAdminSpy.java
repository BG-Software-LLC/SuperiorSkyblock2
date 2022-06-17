package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.commands.ISuperiorCommand;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class CmdAdminSpy implements ISuperiorCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("spy");
    }

    @Override
    public String getPermission() {
        return "superior.admin.spy";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "admin spy";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_ADMIN_SPY.getMessage(locale);
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

        if (!plugin.getEventsBus().callPlayerToggleSpyEvent(superiorPlayer))
            return;

        if (superiorPlayer.hasAdminSpyEnabled()) {
            Message.TOGGLED_SPY_OFF.send(superiorPlayer);
        } else {
            Message.TOGGLED_SPY_ON.send(superiorPlayer);
        }

        superiorPlayer.toggleAdminSpy();
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        return Collections.emptyList();
    }

}
