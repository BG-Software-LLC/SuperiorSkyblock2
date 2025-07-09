package com.bgsoftware.superiorskyblock.commands.player;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.CommandTabCompletes;
import com.bgsoftware.superiorskyblock.commands.IPermissibleCommand;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEventsFactory;
import com.bgsoftware.superiorskyblock.core.menu.view.MenuViewWrapper;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.island.privilege.IslandPrivileges;
import com.bgsoftware.superiorskyblock.island.role.SPlayerRole;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class CmdPermissions implements IPermissibleCommand {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    @Override
    public List<String> getAliases() {
        return Arrays.asList("permissions", "perms", "setpermission", "setperm");
    }

    @Override
    public String getPermission() {
        return "superior.island.permissions.roles";
    }

    @Override
    public String getUsage(SuperiorSkyblockPlugin plugin, CommandSender sender, java.util.Locale locale) {
        if (sender.hasPermission("superior.island.permissions.players")) {
            return "permissions [" + Message.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "] [reset]";
        } else {
            return "permissions [reset]";
        }
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_PERMISSIONS.getMessage(locale);
    }

    @Override
    public int getMinArgs() {
        return 1;
    }

    @Override
    public int getMaxArgs(SuperiorSkyblockPlugin plugin, CommandSender sender) {
        return sender.hasPermission("superior.island.permissions.players") ? 3 : 2;
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return false;
    }

    @Override
    public IslandPrivilege getPrivilege() {
        return IslandPrivileges.SET_PERMISSION;
    }

    @Override
    public Message getPermissionLackMessage() {
        return Message.NO_PERMISSION_CHECK_PERMISSION;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, SuperiorPlayer superiorPlayer, Island island, String[] args) {
        Object permissionHolder = SPlayerRole.guestRole();

        boolean setToDefault = (args.length == 2 ? args[1] : args.length == 3 ? args[2] : "").equalsIgnoreCase("reset");

        if (superiorPlayer.hasPermission("superior.island.permissions.players") && ((!setToDefault && args.length == 2) || args.length == 3)) {
            SuperiorPlayer targetPlayer = plugin.getPlayers().getSuperiorPlayer(args[1]);

            if (targetPlayer == null) {
                Message.INVALID_PLAYER.send(superiorPlayer, args[1]);
                return;
            }

            if (island.isMember(targetPlayer) && !superiorPlayer.getPlayerRole().isHigherThan(targetPlayer.getPlayerRole())) {
                Message.CHANGE_PERMISSION_FOR_HIGHER_ROLE.send(superiorPlayer);
                return;
            }

            permissionHolder = targetPlayer;
        }

        if (!setToDefault) {
            if (permissionHolder instanceof PlayerRole) {
                plugin.getMenus().openPermissions(superiorPlayer, MenuViewWrapper.fromView(superiorPlayer.getOpenedView()),
                        island, (PlayerRole) permissionHolder);
            } else {
                plugin.getMenus().openPermissions(superiorPlayer, MenuViewWrapper.fromView(superiorPlayer.getOpenedView()),
                        island, (SuperiorPlayer) permissionHolder);
            }
        } else {
            if (permissionHolder instanceof PlayerRole) {
                if (PluginEventsFactory.callIslandClearRolesPrivilegesEvent(island, superiorPlayer)) {
                    island.resetPermissions();
                    Message.PERMISSIONS_RESET_ROLES.send(superiorPlayer);
                }
            } else {
                if (PluginEventsFactory.callIslandClearPlayerPrivilegesEvent(island, superiorPlayer, (SuperiorPlayer) permissionHolder)) {
                    island.resetPermissions((SuperiorPlayer) permissionHolder);
                    Message.PERMISSIONS_RESET_PLAYER.send(superiorPlayer, ((SuperiorPlayer) permissionHolder).getName());
                }
            }
        }
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, SuperiorPlayer superiorPlayer, Island island, String[] args) {
        List<String> tabVariables = new LinkedList<>();

        if (args.length == 2) {
            if ("reset".contains(args[1].toLowerCase(Locale.ENGLISH)))
                tabVariables.add("reset");
            if (superiorPlayer.hasPermission("superior.island.permissions.players")) {
                tabVariables.addAll(CommandTabCompletes.getOnlinePlayers(plugin, args[1],
                        plugin.getSettings().isTabCompleteHideVanished()));
            }
        } else if (superiorPlayer.hasPermission("superior.island.permissions.players") && args.length == 3) {
            if ("reset".contains(args[2].toLowerCase(Locale.ENGLISH)))
                tabVariables.add("reset");
        }

        return Collections.unmodifiableList(tabVariables);
    }

}
