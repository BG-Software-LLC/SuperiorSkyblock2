package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.service.placeholders.PlaceholdersService;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.CommandTabCompletes;
import com.bgsoftware.superiorskyblock.commands.IAdminPlayerCommand;
import com.bgsoftware.superiorskyblock.core.LazyReference;
import com.bgsoftware.superiorskyblock.core.ObjectsPools;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEventsFactory;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.island.privilege.IslandPrivileges;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class CmdAdminFly implements IAdminPlayerCommand {

    private static final LazyReference<PlaceholdersService> placeholdersService = new LazyReference<PlaceholdersService>() {
        @Override
        protected PlaceholdersService create() {
            return SuperiorSkyblockPlugin.getPlugin().getServices().getService(PlaceholdersService.class);
        }
    };

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("fly");
    }

    @Override
    public String getPermission() {
        return "superior.admin.fly";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "admin fly <" +
                Message.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "> [flying[true/false]]";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_ADMIN_FLY.getMessage(locale);
    }

    @Override
    public int getMinArgs() {
        return 3;
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
    public boolean supportMultiplePlayers() {
        return false;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, SuperiorPlayer targetPlayer, String[] args) {
        if (args.length == 4) {
            boolean enableFlying = Boolean.parseBoolean(args[3]);

            if (enableFlying == targetPlayer.hasIslandFlyEnabled()) {
                // Nothing changes, let's just send message to sender and return
                if (enableFlying) {
                    Message.TOGGLED_FLY_ON_OTHER.send(sender, targetPlayer.getName());
                } else {
                    Message.TOGGLED_FLY_OFF_OTHER.send(sender, targetPlayer.getName());
                }
                return;
            }
        }

        if (!PluginEventsFactory.callPlayerToggleFlyEvent(targetPlayer))
            return;

        if (targetPlayer.hasIslandFlyEnabled()) {
            targetPlayer.runIfOnline(player -> {
                player.setAllowFlight(false);
                player.setFlying(false);
            });

            Message.TOGGLED_FLY_OFF.send(targetPlayer);
            Message.TOGGLED_FLY_OFF_OTHER.send(sender, targetPlayer.getName());
        } else {
            Island island;
            try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
                island = plugin.getGrid().getIslandAt(targetPlayer.getLocation(wrapper.getHandle()));
            }

            if (island != null && island.hasPermission(targetPlayer, IslandPrivileges.FLY)) {
                targetPlayer.runIfOnline(player -> {
                    player.setAllowFlight(true);
                    player.setFlying(true);
                });
            }

            Message.TOGGLED_FLY_ON.send(targetPlayer);
            Message.TOGGLED_FLY_ON_OTHER.send(sender, targetPlayer.getName());
        }

        targetPlayer.toggleIslandFly();
    }

    @Override
    public List<String> adminTabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, SuperiorPlayer targetPlayer, String[] args) {
        return args.length == 4 ? CommandTabCompletes.getCustomComplete(args[3], "true", "false") : null;
    }

}
