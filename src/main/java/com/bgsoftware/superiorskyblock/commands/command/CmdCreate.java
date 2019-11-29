package com.bgsoftware.superiorskyblock.commands.command;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.menu.IslandCreationMenu;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.commands.ICommand;

import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CmdCreate implements ICommand {

    private final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("create");
    }

    @Override
    public String getPermission() {
        return "superior.island.create";
    }

    @Override
    public String getUsage() {
        return plugin.getSettings().islandNamesRequiredForCreation ?
                "island create <" + Locale.COMMAND_ARGUMENT_ISLAND_NAME.getMessage() + ">" :
                "island create";
    }

    @Override
    public String getDescription() {
        return Locale.COMMAND_DESCRIPTION_CREATE.getMessage();
    }

    @Override
    public int getMinArgs() {
        return plugin.getSettings().islandNamesRequiredForCreation ? 2 : 1;
    }

    @Override
    public int getMaxArgs() {
        return plugin.getSettings().islandNamesRequiredForCreation ? 2 : 1;
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return false;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(sender);

        if(superiorPlayer.getIsland() != null){
            Locale.ALREADY_IN_ISLAND.send(superiorPlayer);
            return;
        }

//        if(!plugin.getSettings().islandNamesRequiredForCreation && args.length == 2){
//            Locale.COMMAND_USAGE.send(sender, getUsage());
//            return;
//        }

        String islandName = "";

        if(args.length == 2){
            islandName = args[1];
            if(plugin.getGrid().getIsland(islandName) != null){
                Locale.ISLAND_ALREADY_EXIST.send(superiorPlayer);
                return;
            }
        }

        IslandCreationMenu.openInventory(superiorPlayer, null, islandName);
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}
