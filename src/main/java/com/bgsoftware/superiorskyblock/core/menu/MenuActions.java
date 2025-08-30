package com.bgsoftware.superiorskyblock.core.menu;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.bank.BankTransaction;
import com.bgsoftware.superiorskyblock.api.island.warps.IslandWarp;
import com.bgsoftware.superiorskyblock.api.menu.MenuIslandCreationConfig;
import com.bgsoftware.superiorskyblock.api.menu.view.MenuView;
import com.bgsoftware.superiorskyblock.api.schematic.Schematic;
import com.bgsoftware.superiorskyblock.api.world.Dimension;
import com.bgsoftware.superiorskyblock.api.world.GameSound;
import com.bgsoftware.superiorskyblock.api.wrappers.BlockOffset;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.GameSoundImpl;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.island.privilege.IslandPrivileges;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.Locale;

public class MenuActions {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    public static void handleDeposit(SuperiorPlayer superiorPlayer, Island island, BankTransaction bankTransaction,
                                     @Nullable GameSound successSound, @Nullable GameSound failSound, BigDecimal amount) {
        if (bankTransaction.getFailureReason().isEmpty()) {
            superiorPlayer.runIfOnline(player -> GameSoundImpl.playSound(player, successSound));
        } else {
            superiorPlayer.runIfOnline(player -> GameSoundImpl.playSound(player, failSound));

            String failureReason = bankTransaction.getFailureReason();

            if (!failureReason.isEmpty()) {
                switch (failureReason) {
                    case "No permission":
                        Message.NO_DEPOSIT_PERMISSION.send(superiorPlayer, island.getRequiredPlayerRole(IslandPrivileges.DEPOSIT_MONEY));
                        break;
                    case "Invalid amount":
                        Message.INVALID_AMOUNT.send(superiorPlayer, Formatters.NUMBER_FORMATTER.format(amount));
                        break;
                    case "Not enough money":
                        Message.NOT_ENOUGH_MONEY_TO_DEPOSIT.send(superiorPlayer, Formatters.NUMBER_FORMATTER.format(amount));
                        break;
                    case "Exceed bank limit":
                        Message.BANK_LIMIT_EXCEED.send(superiorPlayer);
                        break;
                    case "Vault is not installed":
                        Message.VAULT_NOT_INSTALLED.send(superiorPlayer);
                        break;
                    default:
                        Message.DEPOSIT_ERROR.send(superiorPlayer, failureReason);
                        break;
                }
            }
        }
    }

    public static void handleWithdraw(SuperiorPlayer superiorPlayer, Island island, BankTransaction bankTransaction,
                                      GameSound successSound, GameSound failSound, BigDecimal amount) {
        if (bankTransaction.getFailureReason().isEmpty()) {
            superiorPlayer.runIfOnline(player -> GameSoundImpl.playSound(player, successSound));
        } else {
            superiorPlayer.runIfOnline(player -> GameSoundImpl.playSound(player, failSound));

            String failureReason = bankTransaction.getFailureReason();

            if (!failureReason.isEmpty()) {
                switch (failureReason) {
                    case "No permission":
                        Message.NO_WITHDRAW_PERMISSION.send(superiorPlayer, island.getRequiredPlayerRole(IslandPrivileges.WITHDRAW_MONEY));
                        break;
                    case "Invalid amount":
                        Message.INVALID_AMOUNT.send(superiorPlayer, Formatters.NUMBER_FORMATTER.format(amount));
                        break;
                    case "Bank is empty":
                        Message.ISLAND_BANK_EMPTY.send(superiorPlayer);
                        break;
                    case "Vault is not installed":
                        Message.VAULT_NOT_INSTALLED.send(superiorPlayer);
                        break;
                    default:
                        Message.WITHDRAW_ERROR.send(superiorPlayer, failureReason);
                        break;
                }
            }
        }
    }

    public static void simulateIslandCreationClick(SuperiorPlayer clickedPlayer, String islandName,
                                                   MenuIslandCreationConfig creationConfig, boolean isPreviewMode,
                                                   @Nullable MenuView<?, ?> menuView) {
        Schematic schematic = creationConfig.getSchematic();

        // Checking for preview of islands.
        if (isPreviewMode) {
            Location previewLocation = plugin.getSettings().getPreviewIslands().get(schematic.getName().toLowerCase(Locale.ENGLISH));
            if (previewLocation != null) {
                plugin.getGrid().startIslandPreview(clickedPlayer, schematic, islandName);
                return;
            }
        }

        Player whoClicked = clickedPlayer.asPlayer();

        GameSoundImpl.playSound(whoClicked, creationConfig.getSound());

        creationConfig.getCommands().forEach(command -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                command.replace("%player%", clickedPlayer.getName())));

        Message.ISLAND_CREATE_PROCCESS_REQUEST.send(clickedPlayer);

        if (menuView != null)
            menuView.closeView();

        Dimension dimension = plugin.getSettings().getWorlds().getDefaultWorldDimension();
        boolean offset = creationConfig.shouldOffsetIslandValue() ||
                plugin.getSettings().getWorlds().getDimensionConfig(dimension).isSchematicOffset();

        BlockOffset spawnOffset = creationConfig.getSpawnOffset();

        plugin.getGrid().createIsland(clickedPlayer, schematic.getName(), creationConfig.getBonusWorth(),
                creationConfig.getBonusLevel(), creationConfig.getBiome(), islandName, offset, spawnOffset);
    }

    public static void simulateWarpsClick(SuperiorPlayer superiorPlayer, Island island, IslandWarp islandWarp) {
        BukkitExecutor.sync(() -> {
            superiorPlayer.runIfOnline(player -> {
                MenuView<?, ?> currentView = superiorPlayer.getOpenedView();
                if (currentView == null) {
                    player.closeInventory();
                } else {
                    currentView.closeView();
                }
                island.warpPlayer(superiorPlayer, islandWarp.getName());
            });
        }, 1L);
    }

    private MenuActions() {

    }

}
