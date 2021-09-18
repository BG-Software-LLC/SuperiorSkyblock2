package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.commands.ISuperiorCommand;
import com.bgsoftware.superiorskyblock.commands.CommandArguments;
import com.bgsoftware.superiorskyblock.commands.CommandTabCompletes;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
        World world = CommandArguments.getWorld(sender, args[2]);

        if(world == null)
            return;

        Location location = CommandArguments.getLocation(sender, world, args[3], args[4], args[5]);

        if(location == null)
            return;

        Pair<Integer, Boolean> arguments = CommandArguments.getAmount(sender, args[6]);

        if(!arguments.getValue())
            return;

        int amount = arguments.getKey();

        plugin.getStackedBlocks().setStackedBlock(location.getBlock(), amount);

        String formattedLocation = args[2] + ", " + args[3] + ", " + args[4] + ", " + args[5];
        Locale.CHANGED_BLOCK_AMOUNT.send(sender, formattedLocation, amount);
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        List<String> list = new ArrayList<>();

        if(args.length == 3){
            list = CommandTabCompletes.getWorlds(args[2]);
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
