package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.ISuperiorCommand;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.entities.EntityUtils;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import com.bgsoftware.superiorskyblock.wrappers.player.SSuperiorPlayer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CmdAdminSetEntityLimit implements ISuperiorCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("setentitylimit");
    }

    @Override
    public String getPermission() {
        return "superior.admin.setentitylimit";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "admin setentitylimit <" +
                Locale.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "/" +
                Locale.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + "/" +
                Locale.COMMAND_ARGUMENT_ALL_ISLANDS.getMessage(locale) + "> <" +
                Locale.COMMAND_ARGUMENT_ENTITY.getMessage(locale) + "> <" +
                Locale.COMMAND_ARGUMENT_LIMIT.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Locale.COMMAND_DESCRIPTION_ADMIN_SET_ENTITY_LIMIT.getMessage(locale);
    }

    @Override
    public int getMinArgs() {
        return 5;
    }

    @Override
    public int getMaxArgs() {
        return 5;
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return true;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        SuperiorPlayer targetPlayer = SSuperiorPlayer.of(args[2]);
        List<Island> islands = new ArrayList<>();

        if(args[2].equalsIgnoreCase("*")) {
            islands.addAll(plugin.getGrid().getIslands());
        }

        else{
            Island island = targetPlayer == null ? plugin.getGrid().getIsland(args[2]) : targetPlayer.getIsland();

            if (island == null) {
                if (args[2].equalsIgnoreCase(sender.getName()))
                    Locale.INVALID_ISLAND.send(sender);
                else if (targetPlayer == null)
                    Locale.INVALID_ISLAND_OTHER_NAME.send(sender, StringUtils.stripColors(args[2]));
                else
                    Locale.INVALID_ISLAND_OTHER.send(sender, targetPlayer.getName());
                return;
            }

            islands.add(island);
        }

        EntityType entityType;

        try {
            entityType = EntityType.valueOf(args[3].toUpperCase());
            if(!EntityUtils.canHaveLimit(entityType))
                throw new IllegalArgumentException();
        }catch(IllegalArgumentException ex){
            Locale.INVALID_ENTITY.send(sender, args[4]);
            return;
        }

        int limit;

        try{
            limit = Integer.parseInt(args[4]);
        }catch(IllegalArgumentException ex){
            Locale.INVALID_LIMIT.send(sender, args[4]);
            return;
        }

        Executor.data(() -> islands.forEach(island -> island.setEntityLimit(entityType, limit)));

        if(islands.size() > 1)
            Locale.CHANGED_ENTITY_LIMIT_ALL.send(sender, StringUtils.format(entityType.name()));
        else if(targetPlayer == null)
            Locale.CHANGED_ENTITY_LIMIT_NAME.send(sender, StringUtils.format(entityType.name()), islands.get(0).getName());
        else
            Locale.CHANGED_ENTITY_LIMIT.send(sender, StringUtils.format(entityType.name()), targetPlayer.getName());
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        List<String> list = new ArrayList<>();

        if(args.length == 3){
            for(Player player : Bukkit.getOnlinePlayers()){
                SuperiorPlayer onlinePlayer = SSuperiorPlayer.of(player);
                Island playerIsland = onlinePlayer.getIsland();
                if (playerIsland != null) {
                    if (player.getName().toLowerCase().contains(args[2].toLowerCase()))
                        list.add(player.getName());
                    if(!playerIsland.getName().isEmpty() && playerIsland.getName().toLowerCase().contains(args[2].toLowerCase()))
                        list.add(playerIsland.getName());
                }
            }
        }
        else if(args.length == 4){
            SuperiorPlayer targetPlayer = SSuperiorPlayer.of(args[2]);
            Island island = targetPlayer == null ? plugin.getGrid().getIsland(args[2]) : targetPlayer.getIsland();

            if(island != null){
                for(EntityType entityType : EntityType.values()){
                    if(EntityUtils.canHaveLimit(entityType) && entityType.name().toLowerCase().contains(args[3].toLowerCase()))
                        list.add(entityType.name().toLowerCase());
                }
            }
        }

        return list;
    }
}
