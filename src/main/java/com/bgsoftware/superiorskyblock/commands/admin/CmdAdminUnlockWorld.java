package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.IAdminIslandCommand;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.commands.CommandArguments;
import com.bgsoftware.superiorskyblock.utils.commands.CommandTabCompletes;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class CmdAdminUnlockWorld implements IAdminIslandCommand {

    @Override
    public List<String> getAliases() {
        return Arrays.asList("unlockworld", "world", "uworld");
    }

    @Override
    public String getPermission() {
        return "superior.admin.world";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "admin unlockworld <" +
                Locale.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "/" +
                Locale.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + "/" +
                Locale.COMMAND_ARGUMENT_ALL_ISLANDS.getMessage(locale) + "> <nether/the_end> <true/false>";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Locale.COMMAND_DESCRIPTION_ADMIN_UNLOCK_WORLD.getMessage(locale);
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
    public boolean supportMultipleIslands() {
        return true;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, SuperiorPlayer targetPlayer, List<Island> islands, String[] args) {
        World.Environment environment = CommandArguments.getEnvironment(sender, args[3]);

        if(environment == null)
            return;

        if(environment == World.Environment.NORMAL){
            Locale.INVALID_ENVIRONMENT.send(sender, args[3]);
            return;
        }

        boolean enable = Boolean.parseBoolean(args[4]);
        boolean netherUnlock = environment == World.Environment.NETHER;

        islands.forEach(island -> {
            if(netherUnlock){
                island.setNetherEnabled(enable);
            }
            else{
                island.setEndEnabled(enable);
            }
        });

        Locale.UNLOCK_WORLD_ANNOUNCEMENT.send(sender, StringUtils.format(args[3]));
    }

    @Override
    public List<String> adminTabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, Island island, String[] args) {
        return args.length == 4 ? CommandTabCompletes.getCustomComplete(args[3], "nether", "the_end") :
                args.length == 5 ? CommandTabCompletes.getCustomComplete(args[3], "true", "false") : new ArrayList<>();
    }

}
