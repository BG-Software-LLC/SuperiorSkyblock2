package com.bgsoftware.superiorskyblock.commands.player;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.CommandTabCompletes;
import com.bgsoftware.superiorskyblock.commands.ISuperiorCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.commands.arguments.IslandArgument;
import com.bgsoftware.superiorskyblock.core.menu.view.MenuViewWrapper;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CmdPanel implements ISuperiorCommand {

    @Override
    public List<String> getAliases() {
        return Arrays.asList("panel", "manager", "cp");
    }

    @Override
    public String getPermission() {
        return "superior.island.panel";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "panel [members/visitors] [toggle]";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_PANEL.getMessage(locale);
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
        IslandArgument arguments = CommandArguments.getSenderIsland(plugin, sender);

        Island island = arguments.getIsland();

        if (island == null)
            return;

        SuperiorPlayer superiorPlayer = arguments.getSuperiorPlayer();

        if (args.length > 1) {
            if (args[1].equalsIgnoreCase("members")) {
                plugin.getCommands().dispatchSubCommand(sender, "members");
                return;
            } else if (args[1].equalsIgnoreCase("visitors")) {
                plugin.getCommands().dispatchSubCommand(sender, "visitors");
                return;
            } else if (args[1].equalsIgnoreCase("toggle")) {
                if (!plugin.getEventsBus().callPlayerTogglePanelEvent(superiorPlayer))
                    return;

                if (superiorPlayer.hasToggledPanel()) {
                    superiorPlayer.setToggledPanel(false);
                    Message.PANEL_TOGGLE_OFF.send(superiorPlayer);
                } else {
                    superiorPlayer.setToggledPanel(true);
                    Message.PANEL_TOGGLE_ON.send(superiorPlayer);
                }
                return;
            }
        }

        plugin.getMenus().openControlPanel(superiorPlayer, MenuViewWrapper.fromView(superiorPlayer.getOpenedView()), island);
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        return args.length == 2 ? CommandTabCompletes.getCustomComplete(args[1], "members", "visitors", "toggle") : Collections.emptyList();
    }

}
