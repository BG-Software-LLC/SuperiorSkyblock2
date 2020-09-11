package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.commands.ISuperiorCommand;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class CmdAdminSetBlockAmount implements ISuperiorCommand {

    @Override
    public List<String> getAliases() {
        return Arrays.asList("setblockamount", "setblocksize");
    }

    @Override
    public String getPermission() {
        return "superior.admin.setblockamount";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "admin setblockamount <" +
                Locale.COMMAND_ARGUMENT_WORLD.getMessage(locale) + "> <x> <y> <z> <" +
                Locale.COMMAND_ARGUMENT_AMOUNT.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Locale.COMMAND_DESCRIPTION_ADMIN_SET_BLOCK_AMOUNT.getMessage(locale);
    }

    @Override
    public int getMinArgs() {
        return 7;
    }

    @Override
    public int getMaxArgs() {
        return 7;
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return true;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        World world = Bukkit.getWorld(args[2]);

        if(world == null){
            Locale.INVALID_WORLD.send(sender, args[2]);
            return;
        }

        String formattedLocation = args[2] + ", " + args[3] + ", " + args[4] + ", " + args[5];
        Location location;

        try{
            int x = Integer.parseInt(args[3]), y = Integer.parseInt(args[4]), z = Integer.parseInt(args[5]);
            location = new Location(world, x, y, z);
        }catch (Throwable ex){
            Locale.INVALID_BLOCK.send(sender, formattedLocation);
            return;
        }

        int amount;

        try{
            amount = Integer.parseInt(args[6]);
        }catch (Exception ex){
            Locale.INVALID_AMOUNT.send(sender, args[6]);
            return;
        }

        plugin.getGrid().setBlockAmount(location.getBlock(), amount);

        Locale.CHANGED_BLOCK_AMOUNT.send(sender, formattedLocation, amount);
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        List<String> list = new ArrayList<>();

        if(args.length == 3){
            list.addAll(Bukkit.getWorlds().stream().map(world -> world.getName().toLowerCase())
                    .filter(name -> name.contains(args[2].toLowerCase())).collect(Collectors.toList()));
        }

        else if(sender instanceof Player){
            Location location = ((Player) sender).getLocation();
            if(args.length == 4){
                if((location.getBlockX() + "").contains(args[3]))
                    list.add(location.getBlockX() + "");
            }
            else if(args.length == 5){
                if((location.getBlockY() + "").contains(args[4]))
                    list.add(location.getBlockY() + "");

            }
            else if(args.length == 6){
                if((location.getBlockZ() + "").contains(args[5]))
                    list.add(location.getBlockZ() + "");
            }
        }

        return list;
    }
}
