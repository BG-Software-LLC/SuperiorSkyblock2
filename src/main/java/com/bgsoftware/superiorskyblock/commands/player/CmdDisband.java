package com.bgsoftware.superiorskyblock.commands.player;

import com.bgsoftware.superiorskyblock.lang.Message;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.IPermissibleCommand;
import com.bgsoftware.superiorskyblock.module.BuiltinModules;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.events.EventsCaller;
import com.bgsoftware.superiorskyblock.island.permissions.IslandPrivileges;
import com.bgsoftware.superiorskyblock.utils.islands.IslandUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class CmdDisband implements IPermissibleCommand {

    @Override
    public List<String> getAliases() {
        return Arrays.asList("disband", "reset", "delete");
    }

    @Override
    public String getPermission() {
        return "superior.island.disband";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "disband";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_DISBAND.getMessage(locale);
    }

    @Override
    public int getMinArgs() {
        return 1;
    }

    @Override
    public int getMaxArgs() {
        return 1;
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return false;
    }

    @Override
    public IslandPrivilege getPrivilege() {
        return IslandPrivileges.DISBAND_ISLAND;
    }

    @Override
    public Message getPermissionLackMessage() {
        return Message.NO_DISBAND_PERMISSION;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, SuperiorPlayer superiorPlayer, Island island, String[] args) {
        if (!superiorPlayer.hasDisbands() && plugin.getSettings().getDisbandCount() > 0) {
            Message.NO_MORE_DISBANDS.send(superiorPlayer);
            return;
        }

        if (plugin.getSettings().isDisbandConfirm()) {
            plugin.getMenus().openConfirmDisband(superiorPlayer, null, island);
        } else if (EventsCaller.callIslandDisbandEvent(superiorPlayer, island)) {
            IslandUtils.sendMessage(island, Message.DISBAND_ANNOUNCEMENT, new ArrayList<>(), superiorPlayer.getName());

            Message.DISBANDED_ISLAND.send(superiorPlayer);

            if (BuiltinModules.BANK.disbandRefund > 0 && island.getOwner().isOnline()) {
                Message.DISBAND_ISLAND_BALANCE_REFUND.send(island.getOwner(), StringUtils.format(island.getIslandBank()
                        .getBalance().multiply(BigDecimal.valueOf(BuiltinModules.BANK.disbandRefund))));
            }

            superiorPlayer.setDisbands(superiorPlayer.getDisbands() - 1);
            island.disbandIsland();
        }

    }

}
