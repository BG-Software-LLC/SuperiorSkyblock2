package com.bgsoftware.superiorskyblock.commands.player;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.enums.BorderColor;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.core.menu.view.MenuViewWrapper;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.commands.CommandTabCompletes;
import com.bgsoftware.superiorskyblock.commands.ISuperiorCommand;
import com.bgsoftware.superiorskyblock.island.IslandUtils;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class CmdBorder implements ISuperiorCommand {

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
        return "border [" + Message.COMMAND_ARGUMENT_BORDER_COLOR.getMessage(locale) + "]";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_BORDER.getMessage(locale);
    }

    @Override
    public int getMinArgs() {
        return 1;
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

        if (args.length != 2) {
            plugin.getMenus().openBorderColor(superiorPlayer, MenuViewWrapper.fromView(superiorPlayer.getOpenedView()));
            return;
        }

        BorderColor borderColor = CommandArguments.getBorderColor(sender, args[1]);

        if (borderColor == null)
            return;

        IslandUtils.handleBorderColorUpdate(superiorPlayer, borderColor);
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        return args.length != 2 ? Collections.emptyList() : CommandTabCompletes.getBorderColors(args[1]);
    }

}
