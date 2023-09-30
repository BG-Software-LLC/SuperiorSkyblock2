package com.bgsoftware.superiorskyblock.commands.player;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.api.commands.CommandContext;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgument;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.InternalPermissibleCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArgumentsBuilder;
import com.bgsoftware.superiorskyblock.commands.arguments.SuggestionsSelector;
import com.bgsoftware.superiorskyblock.commands.arguments.types.PlayerArgumentType;
import com.bgsoftware.superiorskyblock.commands.context.IslandCommandContext;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.island.IslandUtils;
import com.bgsoftware.superiorskyblock.island.privilege.IslandPrivileges;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class CmdBan implements InternalPermissibleCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("ban");
    }

    @Override
    public String getPermission() {
        return "superior.island.ban";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_BAN.getMessage(locale);
    }

    @Override
    public List<CommandArgument<?>> getArguments() {
        return new CommandArgumentsBuilder()
                .add(CommandArgument.required("player", PlayerArgumentType.allOf(Selector.INSTANCE), Message.COMMAND_ARGUMENT_PLAYER_NAME))
                .build();
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return false;
    }

    @Override
    public IslandPrivilege getPrivilege() {
        return IslandPrivileges.BAN_MEMBER;
    }

    @Override
    public Message getPermissionLackMessage() {
        return Message.NO_BAN_PERMISSION;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, IslandCommandContext context) {
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(context.getDispatcher());

        SuperiorPlayer targetPlayer = context.getRequiredArgument("player", SuperiorPlayer.class);
        Island island = context.getIsland();

        if (!IslandUtils.checkBanRestrictions(superiorPlayer, island, targetPlayer, true))
            return;

        if (plugin.getSettings().isBanConfirm()) {
            plugin.getMenus().openConfirmBan(superiorPlayer, null, island, targetPlayer);
        } else {
            IslandUtils.handleBanPlayer(superiorPlayer, island, targetPlayer);
        }
    }

    private static class Selector implements SuggestionsSelector<SuperiorPlayer> {

        private static final Selector INSTANCE = new Selector();

        @Override
        public List<SuperiorPlayer> getAllPossibilities(SuperiorSkyblock plugin, CommandContext context) {
            SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer((Player) context.getDispatcher());
            Island island = superiorPlayer.getIsland();
            return island == null ? Collections.emptyList() : plugin.getPlayers().getAllPlayers();
        }

        @Override
        public boolean check(SuperiorSkyblock plugin, CommandContext context, SuperiorPlayer targetPlayer) {
            SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer((Player) context.getDispatcher());
            Island island = superiorPlayer.getIsland();
            return island != null && IslandUtils.checkBanRestrictions(superiorPlayer, island, targetPlayer, true);
        }
    }

}
