package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgument;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.service.portals.PortalsManagerService;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.InternalIslandCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArgumentsBuilder;
import com.bgsoftware.superiorskyblock.commands.arguments.types.EnumArgumentType;
import com.bgsoftware.superiorskyblock.commands.arguments.types.IslandArgumentType;
import com.bgsoftware.superiorskyblock.commands.context.IslandCommandContext;
import com.bgsoftware.superiorskyblock.core.LazyReference;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import org.bukkit.PortalType;
import org.bukkit.World;

import java.util.Arrays;
import java.util.List;

public class CmdAdminTeleport implements InternalIslandCommand {

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
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_ADMIN_TELEPORT.getMessage(locale);
    }

    @Override
    public List<CommandArgument<?>> getArguments() {
        return new CommandArgumentsBuilder()
                .add(CommandArgument.required("island", IslandArgumentType.INSTANCE, Message.COMMAND_ARGUMENT_PLAYER_NAME, Message.COMMAND_ARGUMENT_ISLAND_NAME))
                .add(CommandArgument.optional("environment", EnumArgumentType.WORLD_ENVIRONMENT, "normal/nether/the_end"))
                .build();
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return false;
    }

    @Override
    public boolean isSelfIsland() {
        return false;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, IslandCommandContext context) {
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(context.getDispatcher());

        Island island = context.getIsland();
        World.Environment environment = context.getOptionalArgument("environment", World.Environment.class)
                .orElse(plugin.getSettings().getWorlds().getDefaultWorld());

        if (plugin.getGrid().getIslandsWorld(island, environment) == null) {
            Message.WORLD_NOT_ENABLED.send(superiorPlayer);
            return;
        }

        if (environment != plugin.getSettings().getWorlds().getDefaultWorld()) {
            if (!island.wasSchematicGenerated(environment)) {
                PortalType portalType = environment == World.Environment.NETHER ? PortalType.NETHER : PortalType.ENDER;
                portalsManager.get().handlePlayerPortalFromIsland(superiorPlayer, island, superiorPlayer.getLocation(),
                        portalType, false);
                return;
            }
        }

        superiorPlayer.teleport(island, environment, result -> {
            if (!result) {
                superiorPlayer.teleport(island.getIslandHome(environment));
            }
        });
    }

}
