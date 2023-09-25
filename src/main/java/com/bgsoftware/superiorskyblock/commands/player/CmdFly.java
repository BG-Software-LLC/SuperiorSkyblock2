package com.bgsoftware.superiorskyblock.commands.player;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.CommandContext;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgument;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.InternalSuperiorCommand;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.island.privilege.IslandPrivileges;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class CmdFly implements InternalSuperiorCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("fly");
    }

    @Override
    public String getPermission() {
        return "superior.island.fly";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_FLY.getMessage(locale);
    }

    @Override
    public List<CommandArgument<?>> getArguments() {
        return Collections.emptyList();
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return false;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, CommandContext context) {
        Player player = (Player) context.getDispatcher();
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(player);

        if (!plugin.getEventsBus().callPlayerToggleFlyEvent(superiorPlayer))
            return;

        Island island = plugin.getGrid().getIslandAt(superiorPlayer.getLocation());

        if (superiorPlayer.hasIslandFlyEnabled()) {
            player.setAllowFlight(false);
            player.setFlying(false);

            Message.TOGGLED_FLY_OFF.send(superiorPlayer);
        } else {
            if (island != null && island.hasPermission(superiorPlayer, IslandPrivileges.FLY)) {
                player.setAllowFlight(true);
                player.setFlying(true);
            }

            Message.TOGGLED_FLY_ON.send(superiorPlayer);
        }

        superiorPlayer.toggleIslandFly();
    }

}
