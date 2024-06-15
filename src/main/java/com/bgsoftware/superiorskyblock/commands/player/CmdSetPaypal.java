package com.bgsoftware.superiorskyblock.commands.player;

import com.bgsoftware.common.collections.Lists;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.BaseCommand;
import com.bgsoftware.superiorskyblock.commands.IPermissibleCommand;
import com.bgsoftware.superiorskyblock.core.events.EventResult;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.island.privilege.IslandPrivileges;

import java.util.List;

public class CmdSetPaypal extends BaseCommand implements IPermissibleCommand {

    @Override
    protected List<String> aliases() {
        return Lists.singleton("setpaypal");
    }

    @Override
    protected String permission() {
        return "superior.island.setpaypal";
    }

    @Override
    protected String usage(java.util.Locale locale) {
        return "setpaypal <" + Message.COMMAND_ARGUMENT_EMAIL.getMessage(locale) + ">";
    }

    @Override
    protected String description(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_SET_PAYPAL.getMessage(locale);
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
        return IslandPrivileges.SET_PAYPAL;
    }

    @Override
    public Message getPermissionLackMessage() {
        return Message.NO_SET_PAYPAL_PERMISSION;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, SuperiorPlayer superiorPlayer, Island island, String[] args) {
        EventResult<String> eventResult = plugin.getEventsBus().callIslandChangePaypalEvent(superiorPlayer, island, args[1]);

        if (eventResult.isCancelled())
            return;

        island.setPaypal(eventResult.getResult());
        Message.CHANGED_PAYPAL.send(superiorPlayer, eventResult.getResult());
    }

}
