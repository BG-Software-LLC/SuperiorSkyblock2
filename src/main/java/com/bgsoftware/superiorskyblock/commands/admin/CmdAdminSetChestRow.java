package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.ISuperiorCommand;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class CmdAdminSetChestRow implements ISuperiorCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("setchestrow");
    }

    @Override
    public String getPermission() {
        return "superior.admin.setchestrow";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "admin setchestrow <" +
                Locale.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "/" +
                Locale.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + "/" +
                Locale.COMMAND_ARGUMENT_ALL_ISLANDS.getMessage(locale) + "> <" +
                Locale.COMMAND_ARGUMENT_PAGE.getMessage(locale) + "> <" +
                Locale.COMMAND_ARGUMENT_ROWS.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Locale.COMMAND_DESCRIPTION_ADMIN_SET_CHEST_ROW.getMessage(locale);
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
        SuperiorPlayer targetPlayer = plugin.getPlayers().getSuperiorPlayer(args[2]);
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

        int page;

        try{
            page = Integer.parseInt(args[3]);
        }catch (IllegalArgumentException ex){
            Locale.INVALID_PAGE.send(sender, args[3]);
            return;
        }

        int rows = parseInt(args[4]);

        if(rows < 1 || rows > 6){
            Locale.INVALID_ROWS.send(sender, args[4]);
            return;
        }

        Executor.data(() -> islands.forEach(island -> island.setChestRows(page - 1, rows)));

        if(islands.size() > 1)
            Locale.CHANGED_CHEST_SIZE_ALL.send(sender, page, rows);
        else if(targetPlayer == null)
            Locale.CHANGED_CHEST_SIZE_NAME.send(sender, page, rows, islands.get(0).getName());
        else
            Locale.CHANGED_CHEST_SIZE.send(sender, page, rows, targetPlayer.getName());
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        List<String> list = new ArrayList<>();

        if(args.length == 3){
            for(Player player : Bukkit.getOnlinePlayers()){
                SuperiorPlayer onlinePlayer = plugin.getPlayers().getSuperiorPlayer(player);
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
            SuperiorPlayer targetPlayer = plugin.getPlayers().getSuperiorPlayer(args[2]);
            Island island = targetPlayer == null ? plugin.getGrid().getIsland(args[2]) : targetPlayer.getIsland();

            if(island != null)
                return IntStream.range(1, island.getChestSize() + 1).boxed().map(i -> i + "")
                        .filter(i -> i.contains(args[3])).collect(Collectors.toList());
        }
        else if(args.length == 5){
            SuperiorPlayer targetPlayer = plugin.getPlayers().getSuperiorPlayer(args[2]);
            Island island = targetPlayer == null ? plugin.getGrid().getIsland(args[2]) : targetPlayer.getIsland();

            if(island != null)
                return IntStream.range(1, 7).boxed().map(i -> i + "")
                        .filter(i -> i.contains(args[4])).collect(Collectors.toList());
        }

        return list;
    }

    private static int parseInt(String str){
        try{
            return Integer.parseInt(str);
        }catch(IllegalArgumentException ignored){
            return 0;
        }
    }

}
