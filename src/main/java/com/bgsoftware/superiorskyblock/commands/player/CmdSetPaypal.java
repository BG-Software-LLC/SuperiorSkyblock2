package com.bgsoftware.superiorskyblock.commands.player;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.IPermissibleCommand;
import com.bgsoftware.superiorskyblock.utils.islands.IslandPrivileges;

import java.util.Collections;
import java.util.List;

public final class CmdSetPaypal implements IPermissibleCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("setpaypal");
    }

    @Override
    public String getPermission() {
        return "superior.island.setpaypal";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "setpaypal <" + Locale.COMMAND_ARGUMENT_EMAIL.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Locale.COMMAND_DESCRIPTION_SET_PAYPAL.getMessage(locale);
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
    public Locale getPermissionLackMessage() {
        return Locale.NO_SET_PAYPAL_PERMISSION;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, SuperiorPlayer superiorPlayer, Island island, String[] args) {
        island.setPaypal(args[1]);
        Locale.CHANGED_PAYPAL.send(superiorPlayer, args[1]);
    }

}
