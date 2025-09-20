package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.IAdminIslandCommand;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEventsFactory;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class CmdAdminResetPermissions implements IAdminIslandCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("resetpermissions");
    }

    @Override
    public String getPermission() {
        return "superior.admin.resetpermissions";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "admin resetpermissions <" +
                Message.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "/" +
                Message.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + "/" +
                Message.COMMAND_ARGUMENT_ALL_ISLANDS.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_ADMIN_RESET_PERMISSIONS.getMessage(locale);
    }

    @Override
    public int getMinArgs() {
        return 3;
    }

    @Override
    public int getMaxArgs() {
        return 3;
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
        int islandsChangedCount = 0;

        for (Island island : islands) {
            if (PluginEventsFactory.callIslandClearRolesPrivilegesEvent(island, sender)) {
                ++islandsChangedCount;
                island.resetPermissions();
            }
        }

        if (islandsChangedCount <= 0)
            return;

        if (islands.size() != 1)
            Message.PERMISSIONS_RESET_ALL.send(sender);
        else if (targetPlayer == null)
            Message.PERMISSIONS_RESET_NAME.send(sender, islands.get(0).getName());
        else
            Message.PERMISSIONS_RESET.send(sender, targetPlayer.getName());
    }

}
