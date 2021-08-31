package com.bgsoftware.superiorskyblock.commands.player;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.ISuperiorCommand;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CmdBorder implements ISuperiorCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("border");
    }

    @Override
    public String getPermission() {
        return "superior.island.border";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "border";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Locale.COMMAND_DESCRIPTION_BORDER.getMessage(locale);
    }

    @Override
    public int getMinArgs() {
        return 1;
    }

    @Override
    public int getMaxArgs() {
        return 1;
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return false;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(sender);
        plugin.getMenus().openBorderColor(superiorPlayer, null);
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        return new ArrayList<>();
    }

}
