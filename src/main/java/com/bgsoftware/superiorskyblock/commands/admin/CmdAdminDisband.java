package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.IAdminIslandCommand;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.events.EventsCaller;
import com.bgsoftware.superiorskyblock.utils.islands.IslandUtils;
import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import org.bukkit.command.CommandSender;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CmdAdminDisband implements IAdminIslandCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("disband");
    }

    @Override
    public String getPermission() {
        return "superior.admin.disband";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "admin disband <" +
                Locale.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "/" +
                Locale.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Locale.COMMAND_DESCRIPTION_ADMIN_DISBAND.getMessage(locale);
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
        return false;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, SuperiorPlayer targetPlayer, Island island, String[] args) {
        if(EventsCaller.callIslandDisbandEvent(targetPlayer, island)){
            IslandUtils.sendMessage(island, Locale.DISBAND_ANNOUNCEMENT, new ArrayList<>(), sender.getName());

            if(targetPlayer == null)
                Locale.DISBANDED_ISLAND_OTHER_NAME.send(sender, island.getName());
            else
                Locale.DISBANDED_ISLAND_OTHER.send(sender, targetPlayer.getName());

            if(plugin.getSettings().disbandRefund > 0 && island.getOwner().isOnline()) {
                Locale.DISBAND_ISLAND_BALANCE_REFUND.send(island.getOwner(), StringUtils.format(island.getIslandBank()
                        .getBalance().multiply(BigDecimal.valueOf(plugin.getSettings().disbandRefund))));
            }

            island.disbandIsland();
        }
    }

}
