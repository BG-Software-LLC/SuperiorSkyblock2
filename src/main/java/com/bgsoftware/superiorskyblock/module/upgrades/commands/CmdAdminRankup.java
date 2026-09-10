package com.bgsoftware.superiorskyblock.module.upgrades.commands;

import com.bgsoftware.superiorskyblock.commands.CommandsManagerImpl;
import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.events.IslandUpgradeEvent;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.modules.ModuleLogger;
import com.bgsoftware.superiorskyblock.api.service.placeholders.PlaceholdersService;
import com.bgsoftware.superiorskyblock.api.upgrades.Upgrade;
import com.bgsoftware.superiorskyblock.api.upgrades.UpgradeLevel;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.CommandTabCompletes;
import com.bgsoftware.superiorskyblock.commands.IAdminIslandCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.core.LazyReference;
import com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEvent;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEventsFactory;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.module.BuiltinModules;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class CmdAdminRankup implements IAdminIslandCommand {

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
    public String getUsage(java.util.Locale locale) {
        return "admin rankup <" +
                Message.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "/" +
                Message.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + "/" +
                Message.COMMAND_ARGUMENT_ALL_ISLANDS.getMessage(locale) + "> <" +
                Message.COMMAND_ARGUMENT_UPGRADE_NAME.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_ADMIN_RANKUP.getMessage(locale);
    }

    @Override
    public int getMinArgs() {
        return 4;
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
    public boolean supportMultipleIslands() {
        return true;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, @Nullable SuperiorPlayer targetPlayer, List<Island> islands, String[] args) {
        Upgrade upgrade = CommandArguments.getUpgrade(plugin, sender, args[3]);

        if (upgrade == null)
            return;

        SuperiorPlayer playerSender = sender instanceof Player ? plugin.getPlayers().getSuperiorPlayer(sender) : null;

        if (BukkitExecutor.isFolia()) {
            CompletableFuture<?>[] futures = new CompletableFuture<?>[islands.size()];
            for (int i = 0; i < islands.size(); ++i) {
                Island island = islands.get(i);
                futures[i] = RankupQueue.submit(island, null, () -> {
                    Supplier<CompletableFuture<Void>> task = () -> executeRankup(island, playerSender, upgrade);
                    Player owner = island.getOwner().asPlayer();
                    return (owner != null ? BukkitExecutor.submit(owner, task) :
                            BukkitExecutor.submit(task)).thenCompose(future -> future);
                });
            }
            CompletableFuture.allOf(futures).thenCompose(ignored -> {
                Supplier<Void> task = () -> {
                    sendSuccess(sender, targetPlayer, islands, upgrade);
                    return null;
                };
                return sender instanceof Player ? BukkitExecutor.submit((Player) sender, task) : BukkitExecutor.submit(task);
            }).exceptionally(error -> {
                        ((ModuleLogger) BuiltinModules.UPGRADES.getLogger()).e("An unexpected error occurred while upgrading an island", error);
                        return null;
                    });
        } else {
            islands.forEach(island -> executeRankup(island, playerSender, upgrade));
            sendSuccess(sender, targetPlayer, islands, upgrade);
        }
    }

    private CompletableFuture<Void> executeRankup(Island island, SuperiorPlayer playerSender, Upgrade upgrade) {
        UpgradeLevel currentLevel = island.getUpgradeLevel(upgrade);
        UpgradeLevel nextLevel = upgrade.getUpgradeLevel(currentLevel.getLevel() + 1);

        PluginEvent<PluginEventArgs.IslandUpgrade> event = PluginEventsFactory.callIslandUpgradeEvent(
                island, playerSender, upgrade, currentLevel, nextLevel, IslandUpgradeEvent.Cause.PLAYER_RANKUP);

        CompletableFuture<Void> future = CompletableFuture.completedFuture(null);
        if (!event.isCancelled()) {
            SuperiorPlayer owner = island.getOwner();

            for (String command : new ArrayList<>(event.getArgs().commands)) {
                Supplier<String> parser = () -> placeholdersService.get().parsePlaceholders(owner.asOfflinePlayer(), command
                        .replace("%player%", owner.getName())
                        .replace("%leader%", owner.getName()));
                if (BukkitExecutor.isFolia()) {
                    future = future.thenCompose(ignored -> RankupQueue.parseCommand(owner, parser))
                            .thenCompose(parsedCommand -> CommandsManagerImpl.dispatchCommand(
                                    Bukkit.getConsoleSender(), parsedCommand).thenApply(result -> null));
                } else {
                    CommandsManagerImpl.dispatchCommand(Bukkit.getConsoleSender(), parser.get());
                }
            }
        }
        return future;
    }

    private void sendSuccess(CommandSender sender, SuperiorPlayer targetPlayer, List<Island> islands, Upgrade upgrade) {
        if (islands.size() > 1)
            Message.RANKUP_SUCCESS_ALL.send(sender, upgrade.getName());
        else if (targetPlayer == null)
            Message.RANKUP_SUCCESS_NAME.send(sender, upgrade.getName(), islands.get(0).getName());
        else
            Message.RANKUP_SUCCESS.send(sender, upgrade.getName(), targetPlayer.getName());
    }

    @Override
    public List<String> adminTabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, Island island, String[] args) {
        return args.length == 4 ? CommandTabCompletes.getUpgrades(plugin, args[3]) : Collections.emptyList();
    }

}
