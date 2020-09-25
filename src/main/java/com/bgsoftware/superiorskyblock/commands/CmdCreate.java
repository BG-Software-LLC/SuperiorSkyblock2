package com.bgsoftware.superiorskyblock.commands;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.schematic.Schematic;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.menu.MenuIslandCreation;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.Locale;

import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class CmdCreate implements ISuperiorCommand {

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
    public String getUsage(java.util.Locale locale) {
        StringBuilder usage = new StringBuilder("create");

        if(plugin.getSettings().islandNamesRequiredForCreation)
            usage.append(" <").append(Locale.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale)).append(">");

        if(plugin.getSettings().schematicNameArgument)
            usage.append(" [").append(Locale.COMMAND_ARGUMENT_SCHEMATIC_NAME.getMessage(locale)).append("]");

        return usage.toString();
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Locale.COMMAND_DESCRIPTION_CREATE.getMessage(locale);
    }

    @Override
    public int getMinArgs() {
        return plugin.getSettings().islandNamesRequiredForCreation ? 2 : 1;
    }

    @Override
    public int getMaxArgs() {
        int args = 3;

        if(!plugin.getSettings().islandNamesRequiredForCreation)
            args--;

        if(!plugin.getSettings().schematicNameArgument)
            args--;

        return args;
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return false;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(sender);

        if(superiorPlayer.getIsland() != null){
            Locale.ALREADY_IN_ISLAND.send(superiorPlayer);
            return;
        }

        if(plugin.getGrid().hasActiveCreateRequest(superiorPlayer)){
            Locale.ISLAND_CREATE_PROCESS_FAIL.send(superiorPlayer);
            return;
        }

        String islandName = "", schematicName = null;

        if(plugin.getSettings().islandNamesRequiredForCreation) {
            if (args.length >= 2) {
                islandName = args[1];
                if(!StringUtils.isValidName(sender, null, islandName))
                    return;
            }
        }

        if(plugin.getSettings().schematicNameArgument && args.length == (plugin.getSettings().islandNamesRequiredForCreation ? 3 : 2)){
            schematicName = args[plugin.getSettings().islandNamesRequiredForCreation ? 2 : 1];
            Schematic schematic = plugin.getSchematics().getSchematic(schematicName);
            if(schematic == null || schematicName.endsWith("_nether") || schematicName.endsWith("_the_end")){
                Locale.INVALID_SCHEMATIC.send(sender, schematicName);
                return;
            }
        }

        if(schematicName == null) {
            MenuIslandCreation.openInventory(superiorPlayer, null, islandName);
        }
        else{
            MenuIslandCreation.simulateClick(superiorPlayer, islandName, schematicName, false);
        }
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        int argumentLength = plugin.getSettings().islandNamesRequiredForCreation ? 3 : 2;
        if(plugin.getSettings().schematicNameArgument && args.length == argumentLength){
            String argument = args[argumentLength - 1].toLowerCase();
            return plugin.getSchematics().getSchematics().stream()
                    .filter(schematic -> !schematic.endsWith("_nether") && !schematic.endsWith("_the_end") &&
                            schematic.toLowerCase().contains(argument))
                    .collect(Collectors.toList());
        }

        return new ArrayList<>();
    }
}
