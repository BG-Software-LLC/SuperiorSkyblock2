package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.ISuperiorCommand;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.wrappers.player.SSuperiorPlayer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CmdAdminUnignore implements ISuperiorCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("unignore");
    }

    @Override
    public String getPermission() {
        return "superior.admin.unignore";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "admin unignore <" +
                Locale.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "/" +
                Locale.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Locale.COMMAND_DESCRIPTION_ADMIN_UNIGNORE.getMessage(locale);
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
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        SuperiorPlayer targetPlayer = SSuperiorPlayer.of(args[2]);
        Island island = targetPlayer == null ? plugin.getGrid().getIsland(args[2]) : targetPlayer.getIsland();

        if(island == null){
            if(args[2].equalsIgnoreCase(sender.getName()))
                Locale.INVALID_ISLAND.send(sender);
            else if(targetPlayer == null)
                Locale.INVALID_ISLAND_OTHER_NAME.send(sender, StringUtils.stripColors(args[2]));
            else
                Locale.INVALID_ISLAND_OTHER.send(sender, targetPlayer.getName());
            return;
        }

        island.setIgnored(false);

        if(targetPlayer == null)
            Locale.UNIGNORED_ISLAND_NAME.send(sender, island.getName());
        else
            Locale.UNIGNORED_ISLAND.send(sender, targetPlayer.getName());
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

        return list;
    }
}
