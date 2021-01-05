package com.bgsoftware.superiorskyblock.commands;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.menu.MenuConfirmDisband;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.events.EventsCaller;
import com.bgsoftware.superiorskyblock.utils.islands.IslandPrivileges;
import com.bgsoftware.superiorskyblock.utils.islands.IslandUtils;
import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;

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
        return Locale.COMMAND_DESCRIPTION_DISBAND.getMessage(locale);
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
    public Locale getPermissionLackMessage() {
        return Locale.NO_DISBAND_PERMISSION;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, SuperiorPlayer superiorPlayer, Island island, String[] args) {
        if (!superiorPlayer.hasDisbands() && plugin.getSettings().disbandCount > 0) {
            Locale.NO_MORE_DISBANDS.send(superiorPlayer);
            return;
        }

        if(plugin.getSettings().disbandConfirm) {
            MenuConfirmDisband.openInventory(superiorPlayer, null);
        }

        else if(EventsCaller.callIslandDisbandEvent(superiorPlayer, island)){
            IslandUtils.sendMessage(island, Locale.DISBAND_ANNOUNCEMENT, new ArrayList<>(), superiorPlayer.getName());

            Locale.DISBANDED_ISLAND.send(superiorPlayer);

            if(plugin.getSettings().disbandRefund > 0 && island.getOwner().isOnline()) {
                Locale.DISBAND_ISLAND_BALANCE_REFUND.send(island.getOwner(), StringUtils.format(island.getIslandBank()
                        .getBalance().multiply(BigDecimal.valueOf(plugin.getSettings().disbandRefund))));
            }

            superiorPlayer.setDisbands(superiorPlayer.getDisbands() - 1);
            island.disbandIsland();
        }

    }

}
