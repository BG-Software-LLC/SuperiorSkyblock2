package com.bgsoftware.superiorskyblock.commands;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPermission;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.menu.MenuPlayerLanguage;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import org.bukkit.block.Biome;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class CmdLang implements ISuperiorCommand {

    @Override
    public List<String> getAliases() {
        return Arrays.asList("lang", "language");
    }

    @Override
    public String getPermission() {
        return "superior.island.lang";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "lang";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Locale.COMMAND_DESCRIPTION_LANG.getMessage(locale);
    }

    @Override
    public int getMinArgs() {
        return 1;
    }

    @Override
    public int getMaxArgs() {
        return 1;
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return false;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        MenuPlayerLanguage.openInventory(SSuperiorPlayer.of(sender), null);
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(sender);
        Island island = superiorPlayer.getIsland();

        if(args.length == 2 && island != null && superiorPlayer.hasPermission(IslandPermission.SET_BIOME)){
            List<String> list = new ArrayList<>();

            for(Biome biome : Biome.values()){
                if(biome.name().toLowerCase().startsWith(args[1].toLowerCase()))
                    list.add(biome.name().toLowerCase());
            }

            return list;
        }

        return new ArrayList<>();
    }

}
