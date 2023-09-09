package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.CommandTabCompletes;
import com.bgsoftware.superiorskyblock.commands.IAdminIslandCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import org.bukkit.block.Biome;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CmdAdminSetBiome implements IAdminIslandCommand {

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
                Message.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "/" +
                Message.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + "/" +
                Message.COMMAND_ARGUMENT_ALL_ISLANDS.getMessage(locale) + "> <" +
                Message.COMMAND_ARGUMENT_BIOME.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_ADMIN_SET_BIOME.getMessage(locale);
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
    public boolean supportMultipleIslands() {
        return true;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, @Nullable SuperiorPlayer targetPlayer, List<Island> islands, String[] args) {
        Biome biome = CommandArguments.getBiome(sender, args[3]);

        if (biome == null)
            return;

        islands.forEach(island -> island.setBiome(biome));

        if (islands.size() > 1)
            Message.CHANGED_BIOME_ALL.send(sender, Formatters.CAPITALIZED_FORMATTER.format(biome.name()));
        else if (targetPlayer == null)
            Message.CHANGED_BIOME_NAME.send(sender, Formatters.CAPITALIZED_FORMATTER.format(biome.name()), islands.get(0).getName());
        else
            Message.CHANGED_BIOME_OTHER.send(sender, Formatters.CAPITALIZED_FORMATTER.format(biome.name()), targetPlayer.getName());
    }

    @Override
    public List<String> adminTabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, Island island, String[] args) {
        return args.length == 4 ? CommandTabCompletes.getBiomes(args[3]) : Collections.emptyList();
    }

}
