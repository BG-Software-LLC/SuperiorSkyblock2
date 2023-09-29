package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgument;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.InternalIslandsCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArgumentsBuilder;
import com.bgsoftware.superiorskyblock.commands.arguments.types.EnumArgumentType;
import com.bgsoftware.superiorskyblock.commands.arguments.types.MultipleIslandsArgumentType;
import com.bgsoftware.superiorskyblock.commands.context.IslandsCommandContext;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import org.bukkit.block.Biome;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;

public class CmdAdminSetBiome implements InternalIslandsCommand {

    @Override
    public List<String> getAliases() {
        return Arrays.asList("setbiome", "biome");
    }

    @Override
    public String getPermission() {
        return "superior.admin.setbiome";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_ADMIN_SET_BIOME.getMessage(locale);
    }

    @Override
    public List<CommandArgument<?>> getArguments()

    {
        return new CommandArgumentsBuilder()
                .add(CommandArguments.required("islands", MultipleIslandsArgumentType.INCLUDE_PLAYERS, Message.COMMAND_ARGUMENT_PLAYER_NAME, Message.COMMAND_ARGUMENT_ISLAND_NAME, Message.COMMAND_ARGUMENT_ALL_ISLANDS))
                .add(CommandArguments.required("biome", EnumArgumentType.BIOME, Message.COMMAND_ARGUMENT_BIOME))
                .build();
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return true;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, IslandsCommandContext context) {
        CommandSender dispatcher = context.getDispatcher();

        List<Island> islands = context.getIslands();
        Biome biome = context.getRequiredArgument("biome", Biome.class);

        islands.forEach(island -> island.setBiome(biome));

        SuperiorPlayer targetPlayer = context.getTargetPlayer();

        if (islands.size() > 1)
            Message.CHANGED_BIOME_ALL.send(dispatcher, Formatters.CAPITALIZED_FORMATTER.format(biome.name()));
        else if (targetPlayer == null)
            Message.CHANGED_BIOME_NAME.send(dispatcher, Formatters.CAPITALIZED_FORMATTER.format(biome.name()), islands.get(0).getName());
        else
            Message.CHANGED_BIOME_OTHER.send(dispatcher, Formatters.CAPITALIZED_FORMATTER.format(biome.name()), targetPlayer.getName());
    }

}
