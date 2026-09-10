package com.bgsoftware.superiorskyblock.module.upgrades.commands;

import com.bgsoftware.superiorskyblock.commands.CommandsManagerImpl;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.events.IslandUpgradeEvent;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.modules.ModuleLogger;
import com.bgsoftware.superiorskyblock.api.service.placeholders.PlaceholdersService;
import com.bgsoftware.superiorskyblock.api.upgrades.Upgrade;
import com.bgsoftware.superiorskyblock.api.upgrades.UpgradeLevel;
import com.bgsoftware.superiorskyblock.api.upgrades.cost.UpgradeCost;
import com.bgsoftware.superiorskyblock.api.world.GameSound;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.CommandTabCompletes;
import com.bgsoftware.superiorskyblock.commands.IPermissibleCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.core.GameSoundImpl;
import com.bgsoftware.superiorskyblock.core.LazyReference;
import com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEvent;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEventsFactory;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.island.IslandUtils;
import com.bgsoftware.superiorskyblock.island.privilege.IslandPrivileges;
import com.bgsoftware.superiorskyblock.island.upgrade.SUpgradeLevel;
import com.bgsoftware.superiorskyblock.module.BuiltinModules;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CmdRankup implements IPermissibleCommand {

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
    public String getUsage(java.util.Locale locale) {
        return "rankup <" + Message.COMMAND_ARGUMENT_UPGRADE_NAME.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_RANKUP.getMessage(locale);
    }

    @Override
    public int getMinArgs() {
        return 2;
    }

    @Override
    public int getMaxArgs() {
        return 2;
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
    public void execute(SuperiorSkyblockPlugin plugin, SuperiorPlayer superiorPlayer, Island island, String[] args) {
        if (BukkitExecutor.isFolia()) {
            Player player = superiorPlayer.asPlayer();
            if (player == null)
                return;
            RankupQueue.submit(island, superiorPlayer, () -> BukkitExecutor.submit(player, () -> {
                if (!player.isOnline() || !island.equals(superiorPlayer.getIsland()))
                    return CompletableFuture.<Void>completedFuture(null);
                if (!superiorPlayer.hasPermission(getPrivilege())) {
                    getPermissionLackMessage().send(superiorPlayer, island.getRequiredPlayerRole(getPrivilege()));
                    return CompletableFuture.<Void>completedFuture(null);
                }
                return executeRankup(plugin, superiorPlayer, island, args);
            }).thenCompose(future -> future)).exceptionally(error -> {
                ((ModuleLogger) BuiltinModules.UPGRADES.getLogger()).e("An unexpected error occurred while upgrading an island", error);
                return null;
            });
        } else {
            executeRankup(plugin, superiorPlayer, island, args);
        }
    }

    private CompletableFuture<Void> executeRankup(SuperiorSkyblockPlugin plugin, SuperiorPlayer superiorPlayer,
                                                   Island island, String[] args) {
        Upgrade upgrade = CommandArguments.getUpgrade(plugin, superiorPlayer, args[1]);

        if (upgrade == null)
            return CompletableFuture.completedFuture(null);

        UpgradeLevel currentLevel = island.getUpgradeLevel(upgrade);
        UpgradeLevel nextLevel = upgrade.getUpgradeLevel(currentLevel.getLevel() + 1);

        String permission = nextLevel == null ? "" : nextLevel.getPermission();

        if (!permission.isEmpty() && !superiorPlayer.hasPermission(permission)) {
            Message.NO_UPGRADE_PERMISSION.send(superiorPlayer);
            return CompletableFuture.completedFuture(null);
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
                PluginEvent<PluginEventArgs.IslandUpgrade> event = PluginEventsFactory.callIslandUpgradeEvent(
                        island, superiorPlayer, upgrade, currentLevel, nextLevel, IslandUpgradeEvent.Cause.PLAYER_RANKUP);

                List<UpgradeCost> upgradeCosts = event.getArgs().upgradeCosts;

                if (event.isCancelled()) {
                    hasNextLevel = false;

                } else if (!IslandUtils.hasEnoughBalance(upgradeCosts, superiorPlayer)) {
                    Message.NOT_ENOUGH_MONEY_TO_UPGRADE.send(superiorPlayer);
                    hasNextLevel = false;

                } else {
                    if (BukkitExecutor.isFolia())
                        return executeUpgrade(superiorPlayer, island, currentLevel, upgradeCosts, event.getArgs().commands);

                    upgradeCosts.forEach(upgradeCost -> upgradeCost.withdrawCost(superiorPlayer));

                    for (String command : event.getArgs().commands) {
                        String parsedCommand = placeholdersService.get().parsePlaceholders(superiorPlayer.asOfflinePlayer(), command
                                .replace("%player%", superiorPlayer.getName())
                                .replace("%leader%", island.getOwner().getName()));

                        try {
                            CommandsManagerImpl.dispatchCommand(Bukkit.getConsoleSender(), parsedCommand);
                        } catch (Throwable error) {
                            ModuleLogger logger = (ModuleLogger) BuiltinModules.UPGRADES.getLogger();
                            logger.e("An unexpected error occurred while executing command:\n" + parsedCommand, error);
                        }
                    }

                    hasNextLevel = true;
                }
            }
        }

        playSound(superiorPlayer, currentLevel, hasNextLevel);
        return CompletableFuture.completedFuture(null);
    }

    private CompletableFuture<Void> executeUpgrade(SuperiorPlayer superiorPlayer, Island island,
                                                    UpgradeLevel currentLevel, List<UpgradeCost> upgradeCosts,
                                                    List<String> commands) {
        Player player = superiorPlayer.asPlayer();
        CompletableFuture<Void> future = CompletableFuture.completedFuture(null);
        for (UpgradeCost upgradeCost : new ArrayList<>(upgradeCosts)) {
            future = future.thenCompose(ignored -> BukkitExecutor.submit(player,
                    () -> upgradeCost.withdrawCostAsync(superiorPlayer)).thenCompose(withdrawal -> withdrawal));
        }
        for (String command : new ArrayList<>(commands)) {
            future = future.thenCompose(ignored -> RankupQueue.parseCommand(superiorPlayer,
                            () -> placeholdersService.get().parsePlaceholders(superiorPlayer.asOfflinePlayer(), command
                                    .replace("%player%", superiorPlayer.getName())
                                    .replace("%leader%", island.getOwner().getName()))))
                    .thenCompose(parsedCommand -> CommandsManagerImpl.dispatchCommand(Bukkit.getConsoleSender(), parsedCommand)
                            .handle((result, error) -> {
                                if (error != null)
                                    ((ModuleLogger) BuiltinModules.UPGRADES.getLogger()).e(
                                            "An unexpected error occurred while executing command:\n" + parsedCommand, error);
                                return null;
                            }));
        }
        return future.thenRun(() -> playSound(superiorPlayer, currentLevel, true));
    }

    private void playSound(SuperiorPlayer superiorPlayer, UpgradeLevel currentLevel, boolean hasNextLevel) {
        SUpgradeLevel.ItemData itemData = ((SUpgradeLevel) currentLevel).getItemData();
        if (itemData != null) {
            GameSound sound = hasNextLevel ? itemData.hasNextLevelSound : itemData.noNextLevelSound;
            if (sound != null)
                superiorPlayer.runIfOnline(player -> GameSoundImpl.playSound(player, sound));
        }
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, SuperiorPlayer superiorPlayer, Island island, String[] args) {
        return args.length == 2 ? CommandTabCompletes.getUpgrades(plugin, args[1]) : Collections.emptyList();
    }

}
