package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.ISuperiorCommand;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import org.bukkit.Bukkit;
import org.bukkit.block.Biome;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class CmdAdminSetBiome implements ISuperiorCommand {

    @Override
    public List<String> getAliases() {
        return Arrays.asList("setbiome", "biome");
    }

    @Override
    public String getPermission() {
        return "superior.admin.setbiome";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "admin setbiome <" +
                Locale.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "/" +
                Locale.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + "/" +
                Locale.COMMAND_ARGUMENT_ALL_ISLANDS.getMessage(locale) + "> <" +
                Locale.COMMAND_ARGUMENT_BIOME.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Locale.COMMAND_DESCRIPTION_ADMIN_SET_BIOME.getMessage(locale);
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
        SuperiorPlayer targetPlayer = plugin.getPlayers().getSuperiorPlayer(args[2]);
        List<Island> islands = new ArrayList<>();

        if(args[2].equalsIgnoreCase("*")){
            islands.addAll(plugin.getGrid().getIslands());
        }

        else {
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

        Biome biome;

        try{
            biome = Biome.valueOf(args[3].toUpperCase());
        }catch(IllegalArgumentException ex){
            Locale.INVALID_BIOME.send(sender, args[3]);
            return;
        }

        Executor.data(() -> islands.forEach(island -> island.setBiome(biome)));

        if(islands.size() > 1)
            Locale.CHANGED_BIOME_ALL.send(sender, StringUtils.format(biome.name()));
        else if(targetPlayer == null)
            Locale.CHANGED_BIOME_NAME.send(sender, StringUtils.format(biome.name()), islands.get(0).getName());
        else
            Locale.CHANGED_BIOME_OTHER.send(sender, StringUtils.format(biome.name()), targetPlayer.getName());
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
            for(Biome biome : Biome.values()){
                String biomeName = biome.name().toLowerCase();
                if(biomeName.contains(args[3].toLowerCase()))
                    list.add(biomeName);
            }
        }

        return list;
    }
}
