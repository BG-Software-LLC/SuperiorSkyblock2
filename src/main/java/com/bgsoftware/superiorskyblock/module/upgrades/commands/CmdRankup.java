package com.bgsoftware.superiorskyblock.module.upgrades.commands;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgument;
import com.bgsoftware.superiorskyblock.api.events.IslandUpgradeEvent;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.service.placeholders.PlaceholdersService;
import com.bgsoftware.superiorskyblock.api.upgrades.Upgrade;
import com.bgsoftware.superiorskyblock.api.upgrades.UpgradeLevel;
import com.bgsoftware.superiorskyblock.api.upgrades.cost.UpgradeCost;
import com.bgsoftware.superiorskyblock.api.world.GameSound;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.InternalPermissibleCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArgumentsBuilder;
import com.bgsoftware.superiorskyblock.commands.arguments.types.UpgradeArgumentType;
import com.bgsoftware.superiorskyblock.commands.context.IslandCommandContext;
import com.bgsoftware.superiorskyblock.core.GameSoundImpl;
import com.bgsoftware.superiorskyblock.core.LazyReference;
import com.bgsoftware.superiorskyblock.core.events.EventResult;
import com.bgsoftware.superiorskyblock.core.events.EventsBus;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.island.privilege.IslandPrivileges;
import com.bgsoftware.superiorskyblock.island.upgrade.SUpgradeLevel;
import org.bukkit.Bukkit;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

public class CmdRankup implements InternalPermissibleCommand {

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
        return "superior.island.rankup";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_RANKUP.getMessage(locale);
    }

    @Override
    public List<CommandArgument<?>> getArguments() {
        return new CommandArgumentsBuilder()
                .add(CommandArgument.required("upgrade", UpgradeArgumentType.INSTANCE, Message.COMMAND_ARGUMENT_UPGRADE_NAME))
                .build();
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return false;
    }

    @Override
    public IslandPrivilege getPrivilege() {
        return IslandPrivileges.RANKUP;
    }

    @Override
    public Message getPermissionLackMessage() {
        return Message.NO_RANKUP_PERMISSION;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, IslandCommandContext context) {
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(context.getDispatcher());

        Island island = context.getIsland();
        Upgrade upgrade = context.getRequiredArgument("upgrade", Upgrade.class);

        UpgradeLevel currentLevel = island.getUpgradeLevel(upgrade);
        UpgradeLevel nextLevel = upgrade.getUpgradeLevel(currentLevel.getLevel() + 1);

        String permission = nextLevel == null ? "" : nextLevel.getPermission();

        if (!permission.isEmpty() && !superiorPlayer.hasPermission(permission)) {
            Message.NO_UPGRADE_PERMISSION.send(superiorPlayer);
            return;
        }

        boolean hasNextLevel;

        if (island.hasActiveUpgradeCooldown()) {
            long timeNow = System.currentTimeMillis();
            long lastUpgradeTime = island.getLastTimeUpgrade();
            long duration = lastUpgradeTime + plugin.getSettings().getUpgradeCooldown() - timeNow;
            Message.UPGRADE_COOLDOWN_FORMAT.send(superiorPlayer, Formatters.TIME_FORMATTER.format(
                    Duration.ofMillis(duration), superiorPlayer.getUserLocale()));
            hasNextLevel = false;
        } else {
            String requiredCheckFailure = nextLevel == null ? "" : nextLevel.checkRequirements(superiorPlayer);

            if (!requiredCheckFailure.isEmpty()) {
                Message.CUSTOM.send(superiorPlayer, requiredCheckFailure, false);
                hasNextLevel = false;
            } else {
                EventResult<EventsBus.UpgradeResult> event = plugin.getEventsBus().callIslandUpgradeEvent(
                        superiorPlayer, island, upgrade, currentLevel, nextLevel, IslandUpgradeEvent.Cause.PLAYER_RANKUP);

                UpgradeCost upgradeCost = event.getResult().getUpgradeCost();

                if (event.isCancelled()) {
                    hasNextLevel = false;

                } else if (!upgradeCost.hasEnoughBalance(superiorPlayer)) {
                    Message.NOT_ENOUGH_MONEY_TO_UPGRADE.send(superiorPlayer);
                    hasNextLevel = false;

                } else {
                    upgradeCost.withdrawCost(superiorPlayer);

                    for (String command : event.getResult().getCommands()) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                                placeholdersService.get().parsePlaceholders(superiorPlayer.asOfflinePlayer(), command
                                        .replace("%player%", superiorPlayer.getName())
                                        .replace("%leader%", island.getOwner().getName()))
                        );
                    }

                    hasNextLevel = true;
                }
            }
        }

        SUpgradeLevel.ItemData itemData = ((SUpgradeLevel) currentLevel).getItemData();
        GameSound sound = hasNextLevel ? itemData.hasNextLevelSound : itemData.noNextLevelSound;

        if (sound != null)
            superiorPlayer.runIfOnline(player -> GameSoundImpl.playSound(player, sound));
    }

}
