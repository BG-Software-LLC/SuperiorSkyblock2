package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.ISuperiorCommand;
import com.bgsoftware.superiorskyblock.island.SIsland;
import com.bgsoftware.superiorskyblock.island.SPlayerRole;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.events.EventsCaller;
import com.bgsoftware.superiorskyblock.wrappers.player.SSuperiorPlayer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class CmdAdminAdd implements ISuperiorCommand {

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
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        SuperiorPlayer playerIsland = SSuperiorPlayer.of(args[2]);
        Island island = playerIsland == null ? plugin.getGrid().getIsland(args[2]) : playerIsland.getIsland();

        if (island == null) {
            if (args[2].equalsIgnoreCase(sender.getName()))
                Locale.INVALID_ISLAND.send(sender);
            else if (playerIsland == null)
                Locale.INVALID_ISLAND_OTHER_NAME.send(sender, StringUtils.stripColors(args[2]));
            else
                Locale.INVALID_ISLAND_OTHER.send(sender, playerIsland.getName());
            return;
        }

        SuperiorPlayer targetPlayer = SSuperiorPlayer.of(args[3]);

        if(targetPlayer == null){
            Locale.INVALID_PLAYER.send(sender, args[3]);
            return;
        }

        if(targetPlayer.getIsland() != null){
            Locale.PLAYER_ALREADY_IN_ISLAND.send(sender);
            return;
        }

        if(!EventsCaller.callIslandJoinEvent(targetPlayer, island))
            return;

        ((SIsland) island).sendMessage(Locale.JOIN_ANNOUNCEMENT, new ArrayList<>(), targetPlayer.getName());

        island.revokeInvite(targetPlayer);
        island.addMember(targetPlayer, SPlayerRole.defaultRole());

        if(playerIsland == null) {
            Locale.JOINED_ISLAND_NAME.send(targetPlayer, island.getName());
            Locale.ADMIN_ADD_PLAYER_NAME.send(sender, targetPlayer.getName(), island.getName());
        }
        else {
            Locale.JOINED_ISLAND.send(targetPlayer, playerIsland.getName());
            Locale.ADMIN_ADD_PLAYER.send(sender, targetPlayer.getName(), playerIsland.getName());
        }

        if(plugin.getSettings().teleportOnJoin && targetPlayer.isOnline())
            targetPlayer.teleport(island);
        if(plugin.getSettings().clearOnJoin)
            plugin.getNMSAdapter().clearInventory(targetPlayer.asPlayer());
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        List<String> list = new ArrayList<>();

        if(args.length == 3){
            for(Player player : Bukkit.getOnlinePlayers()){
                SuperiorPlayer onlinePlayer = SSuperiorPlayer.of(player);
                Island playerIsland = onlinePlayer.getIsland();
                if (playerIsland != null) {
                    if (player.getName().toLowerCase().startsWith(args[2].toLowerCase()))
                        list.add(player.getName());
                    if(!playerIsland.getName().isEmpty() && playerIsland.getName().toLowerCase().startsWith(args[2].toLowerCase()))
                        list.add(playerIsland.getName());
                }
            }
        }
        else if(args.length == 4){
            list.addAll(Stream.of("worth", "level").filter(arg -> arg.startsWith(args[3].toLowerCase())).collect(Collectors.toSet()));
        }

        return list;
    }
}
