package com.bgsoftware.superiorskyblock.commands.command.admin;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.ICommand;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class CmdAdminSetGenerator implements ICommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("setgenerator");
    }

    @Override
    public String getPermission() {
        return "superior.admin.setgenerator";
    }

    @Override
    public String getUsage() {
        return "island admin setgenerator <player-name/island-name/*> <material> <value>";
    }

    @Override
    public String getDescription() {
        return Locale.COMMAND_DESCRIPTION_ADMIN_SET_GENERATOR.getMessage();
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

        if(args[2].equalsIgnoreCase("*")){
            islands = plugin.getGrid().getIslands();
        }

        else {
            Island island = targetPlayer == null ? plugin.getGrid().getIsland(args[2]) : targetPlayer.getIsland();

            if (island == null) {
                if (args[2].equalsIgnoreCase(sender.getName()))
                    Locale.INVALID_ISLAND.send(sender);
                else if (targetPlayer == null)
                    Locale.INVALID_ISLAND_OTHER_NAME.send(sender, args[2]);
                else
                    Locale.INVALID_ISLAND_OTHER.send(sender, targetPlayer.getName());
                return;
            }

            islands.add(island);
        }

        try{
            Material material = Material.valueOf(args[3].split(":")[0].toUpperCase());
            if(!material.isSolid()){
                Locale.MATERIAL_NOT_SOLID.send(sender);
                return;
            }
        }catch(IllegalArgumentException ex){
            Locale.INVALID_MATERIAL.send(sender, args[3]);
            return;
        }

        Key material = Key.of(args[3].toUpperCase());
        int percentage;

        try{
            percentage = Integer.parseInt(args[4]);
        }catch(IllegalArgumentException ex){
            Locale.INVALID_AMOUNT.send(sender, args[4]);
            return;
        }

        if(percentage < 0 || percentage > 100){
            Locale.INVALID_PERCENTAGE.send(sender);
            return;
        }

        islands.forEach(island -> island.setGeneratorPercentage(material, percentage));

        if(islands.size() != 1)
            Locale.GENERATOR_UPDATED_ALL.send(sender, StringUtils.format(material.toString().split(":")[0]));
        else if(targetPlayer == null)
            Locale.GENERATOR_UPDATED_NAME.send(sender, StringUtils.format(material.toString().split(":")[0]), islands.get(0).getName());
        else
            Locale.GENERATOR_UPDATED.send(sender, StringUtils.format(material.toString().split(":")[0]), targetPlayer.getName());
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
            list.addAll(Arrays.stream(Material.values())
                    .filter(Material::isSolid)
                    .map(material -> material.toString().toLowerCase())
                    .filter(materialName -> materialName.startsWith(args[3].toLowerCase()))
                    .collect(Collectors.toList())
            );
        }

        return list;
    }
}
