package com.bgsoftware.superiorskyblock.commands.command.admin;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.enums.Rating;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.ICommand;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public final class CmdAdminSetRate implements ICommand {
    @Override
    public List<String> getAliases() {
        return Collections.singletonList("setrate");
    }

    @Override
    public String getPermission() {
        return "superior.admin.setrate";
    }

    @Override
    public String getUsage() {
        return "admin setrate <" +
                Locale.COMMAND_ARGUMENT_PLAYER_NAME.getMessage() + "/" +
                Locale.COMMAND_ARGUMENT_ISLAND_NAME.getMessage() + "> <" +
                Locale.COMMAND_ARGUMENT_PLAYER_NAME.getMessage() + "> <" +
                Locale.COMMAND_ARGUMENT_RATING.getMessage() + ">";
    }

    @Override
    public String getDescription() {
        return Locale.COMMAND_DESCRIPTION_ADMIN_SET_RATE.getMessage();
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
        SuperiorPlayer islandOwner = SSuperiorPlayer.of(args[2]);
        Island targetIsland = islandOwner == null ? plugin.getGrid().getIsland(args[2]) : islandOwner.getIsland();

        if(targetIsland == null){
            if(args[2].equalsIgnoreCase(sender.getName()))
                Locale.INVALID_ISLAND.send(sender);
            else if(islandOwner == null)
                Locale.INVALID_ISLAND_OTHER_NAME.send(sender, args[2]);
            else
                Locale.INVALID_ISLAND_OTHER.send(sender, islandOwner.getName());
            return;
        }

        SuperiorPlayer targetPlayer = SSuperiorPlayer.of(args[3]);

        if(targetPlayer == null){
            Locale.INVALID_PLAYER.send(sender, args[3]);
            return;
        }

        Rating rating;

        try{
            rating = Rating.valueOf(args[4].toUpperCase());
        }catch(Exception ex){
            Locale.INVALID_RATE.send(sender, args[4], Rating.getValuesString());
            return;
        }

        targetIsland.setRating(targetPlayer, rating);

        Locale.RATE_CHANGE_OTHER.send(sender, targetPlayer.getName(), StringUtils.format(rating.name()));
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
        else if(args.length > 3){
            SuperiorPlayer targetPlayer = SSuperiorPlayer.of(args[2]);
            Island island = targetPlayer == null ? plugin.getGrid().getIsland(args[2]) : targetPlayer.getIsland();

            if(island != null){
                if(args.length == 4){
                    for(UUID uuid : island.getRatings().keySet()) {
                        SuperiorPlayer _targetPlayer = SSuperiorPlayer.of(uuid);
                        if (_targetPlayer.getName().toLowerCase().startsWith(args[3].toLowerCase()))
                            list.add(_targetPlayer.getName());
                    }
                }
                else if(args.length == 5){
                    for(Rating rating : Rating.values()){
                        if (rating.name().toLowerCase().startsWith(args[4].toLowerCase()))
                            list.add(rating.name().toLowerCase());
                    }
                }
            }
        }

        return list;
    }
}
