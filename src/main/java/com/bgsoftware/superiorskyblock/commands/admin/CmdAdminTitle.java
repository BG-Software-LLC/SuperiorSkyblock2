package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgument;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.InternalPlayersCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArgumentsBuilder;
import com.bgsoftware.superiorskyblock.commands.arguments.types.IntArgumentType;
import com.bgsoftware.superiorskyblock.commands.arguments.types.MultiplePlayersArgumentType;
import com.bgsoftware.superiorskyblock.commands.arguments.types.StringArgumentType;
import com.bgsoftware.superiorskyblock.commands.context.PlayersCommandContext;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CmdAdminTitle implements InternalPlayersCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("title");
    }

    @Override
    public String getPermission() {
        return "superior.admin.title";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_ADMIN_TITLE.getMessage(locale);
    }

    @Override
    public List<CommandArgument<?>> getArguments() {
        return new CommandArgumentsBuilder()
                .add(CommandArguments.required("players", MultiplePlayersArgumentType.ONLINE_PLAYERS, Message.COMMAND_ARGUMENT_PLAYER_NAME, Message.COMMAND_ARGUMENT_ALL_PLAYERS))
                .add(CommandArguments.required("fade-in", IntArgumentType.INTERVAL, Message.COMMAND_ARGUMENT_TITLE_FADE_IN))
                .add(CommandArguments.required("duration", IntArgumentType.INTERVAL, Message.COMMAND_ARGUMENT_TITLE_DURATION))
                .add(CommandArguments.required("fade-out", IntArgumentType.INTERVAL, Message.COMMAND_ARGUMENT_TITLE_FADE_OUT))
                .add(CommandArguments.required("message", StringArgumentType.MULTIPLE_COLORIZE, "-title"))
                .add(CommandArguments.required("unused", StringArgumentType.MULTIPLE_COLORIZE, "-subtitle"))
                .build();
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return true;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, PlayersCommandContext context) {
        CommandSender dispatcher = context.getDispatcher();

        int fadeIn = context.getRequiredArgument("fade-in", Integer.class);
        int duration = context.getRequiredArgument("duration", Integer.class);
        int fadeOut = context.getRequiredArgument("fade-out", Integer.class);

        String message = context.getRequiredArgument("message", String.class);
        Map<String, String> parsedArguments = CommandArguments.parseArguments(message.split(" "));

        String title = parsedArguments.get("title");
        String subtitle = parsedArguments.get("subtitle");

        if (title == null && subtitle == null) {
            Message.INVALID_TITLE.send(dispatcher);
            return;
        }

        String formattedTitle = title == null ? null : Formatters.COLOR_FORMATTER.format(title);
        String formattedSubtitle = subtitle == null ? null : Formatters.COLOR_FORMATTER.format(subtitle);

        List<SuperiorPlayer> players = context.getPlayers();
        players.forEach(targetPlayer -> plugin.getNMSPlayers().sendTitle(targetPlayer.asPlayer(),
                formattedTitle, formattedSubtitle, fadeIn, duration, fadeOut));

        Message.TITLE_SENT.send(dispatcher, players.get(0).getName());
    }

}
