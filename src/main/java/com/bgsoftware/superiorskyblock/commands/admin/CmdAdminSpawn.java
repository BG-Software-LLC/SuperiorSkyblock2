package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.CommandContext;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgument;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.InternalSuperiorCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArgumentsBuilder;
import com.bgsoftware.superiorskyblock.commands.arguments.types.PlayerArgumentType;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class CmdAdminSpawn implements InternalSuperiorCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("spawn");
    }

    @Override
    public String getPermission() {
        return "superior.admin.spawn";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_ADMIN_SPAWN.getMessage(locale);
    }

    @Override
    public List<CommandArgument<?>> getArguments() {
        return new CommandArgumentsBuilder()
                .add(CommandArgument.optional("player", PlayerArgumentType.ONLINE_PLAYERS, Message.COMMAND_ARGUMENT_PLAYER_NAME))
                .build();
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return true;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, CommandContext context) {
        CommandSender dispatcher = context.getDispatcher();

        SuperiorPlayer targetPlayer = context.getOptionalArgument("player", SuperiorPlayer.class).orElse(null);

        if (targetPlayer == null) {
            if (dispatcher instanceof Player) {
                targetPlayer = plugin.getPlayers().getSuperiorPlayer(dispatcher);
            } else {
                dispatcher.sendMessage(ChatColor.RED + "You must specify a player to teleport.");
                return;
            }
        }

        targetPlayer.teleport(plugin.getGrid().getSpawnIsland());

        Message.SPAWN_TELEPORT_SUCCESS.send(dispatcher, targetPlayer.getName());
    }

}
