package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.IAdminIslandCommand;
import com.bgsoftware.superiorskyblock.island.SPlayerRole;
import com.bgsoftware.superiorskyblock.utils.commands.CommandArguments;
import com.bgsoftware.superiorskyblock.utils.commands.CommandTabCompletes;
import com.bgsoftware.superiorskyblock.utils.events.EventsCaller;
import com.bgsoftware.superiorskyblock.utils.islands.IslandUtils;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CmdAdminAdd implements IAdminIslandCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("add");
    }

    @Override
    public String getPermission() {
        return "superior.admin.add";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "admin add <" +
                Locale.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "/" +
                Locale.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + "> <" +
                Locale.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Locale.COMMAND_DESCRIPTION_ADMIN_ADD.getMessage(locale);
    }

    @Override
    public int getMinArgs() {
        return 4;
    }

    @Override
    public int getMaxArgs() {
        return 4;
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
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, SuperiorPlayer superiorPlayer, Island island, String[] args) {
        SuperiorPlayer targetPlayer = CommandArguments.getPlayer(plugin, sender, args[3]);

        if(targetPlayer == null)
            return;

        if(targetPlayer.getIsland() != null){
            Locale.PLAYER_ALREADY_IN_ISLAND.send(sender);
            return;
        }

        if(!EventsCaller.callIslandJoinEvent(targetPlayer, island))
            return;

        IslandUtils.sendMessage(island, Locale.JOIN_ANNOUNCEMENT, new ArrayList<>(), targetPlayer.getName());

        island.revokeInvite(targetPlayer);
        island.addMember(targetPlayer, SPlayerRole.defaultRole());

        if(superiorPlayer == null) {
            Locale.JOINED_ISLAND_NAME.send(targetPlayer, island.getName());
            Locale.ADMIN_ADD_PLAYER_NAME.send(sender, targetPlayer.getName(), island.getName());
        }
        else {
            Locale.JOINED_ISLAND.send(targetPlayer, superiorPlayer.getName());
            Locale.ADMIN_ADD_PLAYER.send(sender, targetPlayer.getName(), superiorPlayer.getName());
        }

        if(plugin.getSettings().teleportOnJoin && targetPlayer.isOnline())
            targetPlayer.teleport(island);
        if(plugin.getSettings().clearOnJoin)
            plugin.getNMSAdapter().clearInventory(targetPlayer.asOfflinePlayer());
    }

    @Override
    public List<String> adminTabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, Island island, String[] args) {
        return args.length == 4 ? CommandTabCompletes.getOnlinePlayers(plugin, args[2], false,
                superiorPlayer -> superiorPlayer.getIsland() == null) : new ArrayList<>();
    }

}
