package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.common.collections.Lists;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.BaseCommand;
import com.bgsoftware.superiorskyblock.commands.ISuperiorCommand;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import org.bukkit.command.CommandSender;

import java.util.List;

public class CmdAdminBypass extends BaseCommand implements ISuperiorCommand {

    @Override
    protected List<String> aliases() {
        return Lists.singleton("bypass");
    }

    @Override
    protected String permission() {
        return "superior.admin.bypass";
    }

    @Override
    protected String usage(java.util.Locale locale) {
        return "admin bypass";
    }

    @Override
    protected String description(java.util.Locale locale) {
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
        return Lists.emptyList();
    }

}
