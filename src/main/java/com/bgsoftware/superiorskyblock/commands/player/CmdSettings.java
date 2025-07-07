package com.bgsoftware.superiorskyblock.commands.player;

import com.bgsoftware.superiorskyblock.commands.CommandTabCompletes;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEventsFactory;
import com.bgsoftware.superiorskyblock.core.menu.view.MenuViewWrapper;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.IPermissibleCommand;
import com.bgsoftware.superiorskyblock.island.privilege.IslandPrivileges;

import java.util.Collections;
import java.util.List;

public class CmdSettings implements IPermissibleCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("settings");
    }

    @Override
    public String getPermission() {
        return "superior.island.settings";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "settings [reset]";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_SETTINGS.getMessage(locale);
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
    public IslandPrivilege getPrivilege() {
        return IslandPrivileges.SET_SETTINGS;
    }

    @Override
    public Message getPermissionLackMessage() {
        return Message.NO_SET_SETTINGS_PERMISSION;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, SuperiorPlayer superiorPlayer, Island island, String[] args) {
        if (args.length == 2)
            if (args[1].equalsIgnoreCase("reset")) {
                if (PluginEventsFactory.callIslandClearFlagsEvent(island, superiorPlayer)) {
                    island.resetSettings();
                    Message.SETTINGS_RESET.send(superiorPlayer);
                }
                return;
            }

        plugin.getMenus().openSettings(superiorPlayer, MenuViewWrapper.fromView(superiorPlayer.getOpenedView()), island);
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, SuperiorPlayer superiorPlayer, Island island, String[] args) {
        return args.length == 2 ? CommandTabCompletes.getCustomComplete(args[1], "reset") : Collections.emptyList();
    }

}
