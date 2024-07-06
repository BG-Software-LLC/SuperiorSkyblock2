package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.service.portals.PortalsManagerService;
import com.bgsoftware.superiorskyblock.api.world.Dimension;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.CommandTabCompletes;
import com.bgsoftware.superiorskyblock.commands.IAdminIslandCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.core.LazyReference;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import org.bukkit.PortalType;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CmdAdminTeleport implements IAdminIslandCommand {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
    private static final LazyReference<PortalsManagerService> portalsManager = new LazyReference<PortalsManagerService>() {
        @Override
        protected PortalsManagerService create() {
            return plugin.getServices().getService(PortalsManagerService.class);
        }
    };

    @Override
    public List<String> getAliases() {
        return Arrays.asList("tp", "teleport", "go", "visit");
    }

    @Override
    public String getPermission() {
        return "superior.admin.teleport";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "admin teleport <" +
                Message.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "/" +
                Message.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + "> [normal/nether/the_end]";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_ADMIN_TELEPORT.getMessage(locale);
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
        return false;
    }

    @Override
    public boolean supportMultipleIslands() {
        return false;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, @Nullable SuperiorPlayer targetPlayer, Island island, String[] args) {
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(sender);

        Dimension dimension;

        if (args.length != 4) {
            dimension = plugin.getSettings().getWorlds().getDefaultWorldDimension();
        } else {
            dimension = CommandArguments.getDimension(sender, args[3]);
            if (dimension == null)
                return;
        }

        if (plugin.getGrid().getIslandsWorld(island, dimension) == null) {
            Message.WORLD_NOT_ENABLED.send(sender);
            return;
        }

        if (dimension != plugin.getSettings().getWorlds().getDefaultWorldDimension()) {
            if (!island.wasSchematicGenerated(dimension)) {
                PortalType portalType = dimension.getEnvironment() == World.Environment.NETHER ? PortalType.NETHER : PortalType.ENDER;
                portalsManager.get().handlePlayerPortalFromIsland(superiorPlayer, island, superiorPlayer.getLocation(),
                        portalType, false);
                return;
            }
        }

        superiorPlayer.teleport(island, dimension, result -> {
            if (!result) {
                superiorPlayer.teleport(island.getIslandHome(dimension));
            }
        });
    }

    @Override
    public List<String> adminTabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, Island island, String[] args) {
        return args.length == 4 ? CommandTabCompletes.getDimensions(args[3]) : Collections.emptyList();
    }

}
