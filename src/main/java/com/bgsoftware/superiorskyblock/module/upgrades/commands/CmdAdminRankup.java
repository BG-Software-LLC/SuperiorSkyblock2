package com.bgsoftware.superiorskyblock.module.upgrades.commands;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgument;
import com.bgsoftware.superiorskyblock.api.events.IslandUpgradeEvent;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.service.placeholders.PlaceholdersService;
import com.bgsoftware.superiorskyblock.api.upgrades.Upgrade;
import com.bgsoftware.superiorskyblock.api.upgrades.UpgradeLevel;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.InternalIslandsCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArgumentsBuilder;
import com.bgsoftware.superiorskyblock.commands.arguments.types.MultipleIslandsArgumentType;
import com.bgsoftware.superiorskyblock.commands.arguments.types.UpgradeArgumentType;
import com.bgsoftware.superiorskyblock.commands.context.IslandsCommandContext;
import com.bgsoftware.superiorskyblock.core.LazyReference;
import com.bgsoftware.superiorskyblock.core.events.EventResult;
import com.bgsoftware.superiorskyblock.core.events.EventsBus;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class CmdAdminRankup implements InternalIslandsCommand {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();
    private static final LazyReference<PlaceholdersService> placeholdersService = new LazyReference<PlaceholdersService>() {
        @Override
        protected PlaceholdersService create() {
            return plugin.getServices().getService(PlaceholdersService.class);
        }
    };

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("rankup");
    }

    @Override
    public String getPermission() {
        return "superior.admin.rankup";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_ADMIN_RANKUP.getMessage(locale);
    }

    @Override
    public List<CommandArgument<?>> getArguments()

    {
        return new CommandArgumentsBuilder()
                .add(CommandArguments.required("islands", MultipleIslandsArgumentType.INCLUDE_PLAYERS, Message.COMMAND_ARGUMENT_PLAYER_NAME, Message.COMMAND_ARGUMENT_ISLAND_NAME, Message.COMMAND_ARGUMENT_ALL_ISLANDS))
                .add(CommandArguments.required("upgrade", UpgradeArgumentType.INSTANCE, Message.COMMAND_ARGUMENT_UPGRADE_NAME))
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
        Upgrade upgrade = context.getRequiredArgument("upgrade", Upgrade.class);

        SuperiorPlayer superiorPlayer = dispatcher instanceof Player ?
                plugin.getPlayers().getSuperiorPlayer(dispatcher) : null;

        islands.forEach(island -> {
            UpgradeLevel currentLevel = island.getUpgradeLevel(upgrade);
            UpgradeLevel nextLevel = upgrade.getUpgradeLevel(currentLevel.getLevel() + 1);

            EventResult<EventsBus.UpgradeResult> event = plugin.getEventsBus().callIslandUpgradeEvent(
                    superiorPlayer, island, upgrade, currentLevel, nextLevel, IslandUpgradeEvent.Cause.PLAYER_RANKUP);

            if (!event.isCancelled()) {
                SuperiorPlayer owner = island.getOwner();

                for (String command : event.getResult().getCommands()) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                            placeholdersService.get().parsePlaceholders(owner.asOfflinePlayer(), command
                                    .replace("%player%", owner.getName())
                                    .replace("%leader%", owner.getName()))
                    );
                }
            }
        });

        SuperiorPlayer targetPlayer = context.getTargetPlayer();

        if (islands.size() > 1)
            Message.RANKUP_SUCCESS_ALL.send(dispatcher, upgrade.getName());
        else if (targetPlayer == null)
            Message.RANKUP_SUCCESS_NAME.send(dispatcher, upgrade.getName(), islands.get(0).getName());
        else
            Message.RANKUP_SUCCESS.send(dispatcher, upgrade.getName(), targetPlayer.getName());
    }

}
