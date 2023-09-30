package com.bgsoftware.superiorskyblock.commands.player;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.api.commands.CommandContext;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgument;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.island.PlayerRole;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.InternalPermissibleCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArgumentsBuilder;
import com.bgsoftware.superiorskyblock.commands.arguments.SuggestionsSelector;
import com.bgsoftware.superiorskyblock.commands.arguments.types.BoolArgumentType;
import com.bgsoftware.superiorskyblock.commands.arguments.types.PlayerArgumentType;
import com.bgsoftware.superiorskyblock.commands.context.IslandCommandContext;
import com.bgsoftware.superiorskyblock.core.menu.view.MenuViewWrapper;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.island.privilege.IslandPrivileges;
import com.bgsoftware.superiorskyblock.island.role.SPlayerRole;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CmdPermissions implements InternalPermissibleCommand {

    @Override
    public List<String> getAliases() {
        return Arrays.asList("permissions", "perms", "setpermission", "setperm");
    }

    @Override
    public String getPermission() {
        return "superior.island.permissions";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_PERMISSIONS.getMessage(locale);
    }

    @Override
    public List<CommandArgument<?>> getArguments() {
        return new CommandArgumentsBuilder()
                .add(CommandArgument.optional("player", PlayerArgumentType.allOf(Selector.INSTANCE), Message.COMMAND_ARGUMENT_PLAYER_NAME))
                .add(CommandArgument.optional("reset", BoolArgumentType.INSTANCE, "reset[true/false]"))
                .build();
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
    public void execute(SuperiorSkyblockPlugin plugin, IslandCommandContext context) {
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(context.getDispatcher());
        Island island = context.getIsland();
        Object permissionHolder = SPlayerRole.guestRole();

        SuperiorPlayer targetPlayer = context.getOptionalArgument("player", SuperiorPlayer.class).orElse(null);
        boolean setToDefault = context.getOptionalArgument("reset", boolean.class).orElse(false);

        if (!setToDefault && targetPlayer != null) {
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
        } else if (permissionHolder instanceof PlayerRole) {
            if (plugin.getEventsBus().callIslandClearRolesPrivilegesEvent(island, superiorPlayer)) {
                island.resetPermissions();
                Message.PERMISSIONS_RESET_ROLES.send(superiorPlayer);
            }
        } else {
            if (plugin.getEventsBus().callIslandClearPlayerPrivilegesEvent(island, superiorPlayer, (SuperiorPlayer) permissionHolder)) {
                island.resetPermissions((SuperiorPlayer) permissionHolder);
                Message.PERMISSIONS_RESET_PLAYER.send(superiorPlayer, ((SuperiorPlayer) permissionHolder).getName());
            }
        }

    }

    private static class Selector implements SuggestionsSelector<SuperiorPlayer> {

        private static final Selector INSTANCE = new Selector();

        @Override
        public List<SuperiorPlayer> getAllPossibilities(SuperiorSkyblock plugin, CommandContext context) {
            SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer((Player) context.getDispatcher());
            Island island = superiorPlayer.getIsland();
            return island == null ? Collections.emptyList() : island.getIslandMembers(true);
        }

        @Override
        public boolean check(SuperiorSkyblock plugin, CommandContext context, SuperiorPlayer targetPlayer) {
            SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer((Player) context.getDispatcher());
            return !superiorPlayer.getPlayerRole().isHigherThan(targetPlayer.getPlayerRole());
        }
    }

}
