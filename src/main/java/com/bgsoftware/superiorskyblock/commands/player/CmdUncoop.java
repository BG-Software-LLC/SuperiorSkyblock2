package com.bgsoftware.superiorskyblock.commands.player;

import com.bgsoftware.common.collections.Lists;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.events.IslandUncoopPlayerEvent;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.BaseCommand;
import com.bgsoftware.superiorskyblock.commands.CommandTabCompletes;
import com.bgsoftware.superiorskyblock.commands.IPermissibleCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.island.IslandUtils;
import com.bgsoftware.superiorskyblock.island.privilege.IslandPrivileges;

import java.util.Arrays;
import java.util.List;

public class CmdUncoop extends BaseCommand implements IPermissibleCommand {

    @Override
    protected List<String> aliases() {
        return Lists.newLinkedList("uncoop", "untrust");
    }

    @Override
    protected String permission() {
        return "superior.island.uncoop";
    }

    @Override
    protected String usage(java.util.Locale locale) {
        return "uncoop <" + Message.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + ">";
    }

    @Override
    protected String description(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_UNCOOP.getMessage(locale);
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
    public IslandPrivilege getPrivilege() {
        return IslandPrivileges.UNCOOP_MEMBER;
    }

    @Override
    public Message getPermissionLackMessage() {
        return Message.NO_UNCOOP_PERMISSION;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, SuperiorPlayer superiorPlayer, Island island, String[] args) {
        SuperiorPlayer targetPlayer = CommandArguments.getPlayer(plugin, superiorPlayer, args[1]);

        if (targetPlayer == null)
            return;

        if (!island.isCoop(targetPlayer)) {
            Message.PLAYER_NOT_COOP.send(superiorPlayer);
            return;
        }

        if (!plugin.getEventsBus().callIslandUncoopPlayerEvent(island, superiorPlayer, targetPlayer, IslandUncoopPlayerEvent.UncoopReason.PLAYER))
            return;

        island.removeCoop(targetPlayer);

        IslandUtils.sendMessage(island, Message.UNCOOP_ANNOUNCEMENT, Lists.emptyList(), superiorPlayer.getName(), targetPlayer.getName());

        if (island.getName().isEmpty())
            Message.LEFT_ISLAND_COOP.send(targetPlayer, superiorPlayer.getName());
        else
            Message.LEFT_ISLAND_COOP_NAME.send(targetPlayer, island.getName());
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, SuperiorPlayer superiorPlayer, Island island, String[] args) {
        return args.length == 2 ? CommandTabCompletes.getOnlinePlayers(plugin, args[1],
                plugin.getSettings().isTabCompleteHideVanished(), island::isCoop) : Lists.emptyList();
    }

}
