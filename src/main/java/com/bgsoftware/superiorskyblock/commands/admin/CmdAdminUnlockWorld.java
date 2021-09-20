package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.IAdminIslandCommand;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.commands.CommandArguments;
import com.bgsoftware.superiorskyblock.commands.CommandTabCompletes;
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
                Locale.COMMAND_ARGUMENT_ALL_ISLANDS.getMessage(locale) + "> <nether/the_end/normal> <true/false>";
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

        if(environment == plugin.getSettings().getWorlds().getDefaultWorld()){
            Locale.INVALID_ENVIRONMENT.send(sender, args[3]);
            return;
        }

        boolean enable = Boolean.parseBoolean(args[4]);

        islands.forEach(island -> {
            switch (environment){
                case NORMAL:
                    island.setNormalEnabled(enable);
                    break;
                case NETHER:
                    island.setNetherEnabled(enable);
                    break;
                case THE_END:
                    island.setEndEnabled(enable);
                    break;
            }
        });

        Locale.UNLOCK_WORLD_ANNOUNCEMENT.send(sender, StringUtils.format(args[3]));
    }

    @Override
    public List<String> adminTabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, Island island, String[] args) {
        if(args.length == 5)
            return CommandTabCompletes.getCustomComplete(args[3], "true", "false");

        if(args.length != 4)
            return new ArrayList<>();

        List<String> environments = new ArrayList<>();
        for(World.Environment environment : World.Environment.values()){
            environments.add(environment.name().toLowerCase());
        }

        return CommandTabCompletes.getCustomComplete(args[3], environments.toArray(new String[0]));
    }

}
