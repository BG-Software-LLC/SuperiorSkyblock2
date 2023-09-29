package com.bgsoftware.superiorskyblock.commands.player;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgument;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.InternalPermissibleCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArgumentsBuilder;
import com.bgsoftware.superiorskyblock.commands.arguments.types.StringArgumentType;
import com.bgsoftware.superiorskyblock.commands.context.IslandCommandContext;
import com.bgsoftware.superiorskyblock.core.events.EventResult;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.island.privilege.IslandPrivileges;

import java.util.Collections;
import java.util.List;

public class CmdSetDiscord implements InternalPermissibleCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("setdiscord");
    }

    @Override
    public String getPermission() {
        return "superior.island.setdiscord";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "setdiscord <" + Message.COMMAND_ARGUMENT_DISCORD.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_SET_DISCORD.getMessage(locale);
    }

    @Override
    public List<CommandArgument<?>> getArguments()

    {
        return new CommandArgumentsBuilder()
                .add(CommandArguments.required("discord", StringArgumentType.MULTIPLE, Message.COMMAND_ARGUMENT_DISCORD))
                .build();
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return false;
    }

    @Override
    public IslandPrivilege getPrivilege() {
        return IslandPrivileges.SET_DISCORD;
    }

    @Override
    public Message getPermissionLackMessage() {
        return Message.NO_SET_DISCORD_PERMISSION;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, IslandCommandContext context) {
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(context.getDispatcher());
        Island island = context.getIsland();

        String discord = context.getRequiredArgument("discord", String.class);

        EventResult<String> eventResult = plugin.getEventsBus().callIslandChangeDiscordEvent(superiorPlayer, island, discord);

        if (eventResult.isCancelled())
            return;

        island.setDiscord(eventResult.getResult());
        Message.CHANGED_DISCORD.send(superiorPlayer, eventResult.getResult());
    }

}
