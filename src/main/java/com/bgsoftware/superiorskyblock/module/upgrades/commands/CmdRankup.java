package com.bgsoftware.superiorskyblock.module.upgrades.commands;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.service.placeholders.PlaceholdersService;
import com.bgsoftware.superiorskyblock.api.upgrades.Upgrade;
import com.bgsoftware.superiorskyblock.api.upgrades.UpgradeLevel;
import com.bgsoftware.superiorskyblock.api.upgrades.cost.UpgradeCost;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.CommandTabCompletes;
import com.bgsoftware.superiorskyblock.commands.IPermissibleCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.formatting.Formatters;
import com.bgsoftware.superiorskyblock.island.permissions.IslandPrivileges;
import com.bgsoftware.superiorskyblock.lang.Message;
import com.bgsoftware.superiorskyblock.upgrade.SUpgradeLevel;
import com.bgsoftware.superiorskyblock.utils.events.EventResult;
import com.bgsoftware.superiorskyblock.utils.events.EventsBus;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import org.bukkit.Bukkit;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CmdRankup implements IPermissibleCommand {

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
        Upgrade upgrade = CommandArguments.getUpgrade(plugin, superiorPlayer, args[1]);

        if (upgrade == null)
            return;

        UpgradeLevel upgradeLevel = island.getUpgradeLevel(upgrade);
        UpgradeLevel nextUpgradeLevel = upgrade.getUpgradeLevel(upgradeLevel.getLevel() + 1);

        String permission = nextUpgradeLevel == null ? "" : nextUpgradeLevel.getPermission();

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
            String requiredCheckFailure = nextUpgradeLevel == null ? "" : nextUpgradeLevel.checkRequirements(superiorPlayer);

            if (!requiredCheckFailure.isEmpty()) {
                Message.CUSTOM.send(superiorPlayer, requiredCheckFailure, false);
                hasNextLevel = false;
            } else {
                EventResult<EventsBus.UpgradeResult> event = plugin.getEventsBus().callIslandUpgradeEvent(
                        superiorPlayer, island, upgrade, upgradeLevel);

                UpgradeCost upgradeCost = event.getResult().getUpgradeCost();

                if (event.isCancelled()) {
                    hasNextLevel = false;

                } else if (!upgradeCost.hasEnoughBalance(superiorPlayer)) {
                    Message.NOT_ENOUGH_MONEY_TO_UPGRADE.send(superiorPlayer);
                    hasNextLevel = false;

                } else {
                    PlaceholdersService placeholdersService = plugin.getServices().getPlaceholdersService();

                    upgradeCost.withdrawCost(superiorPlayer);

                    for (String command : event.getResult().getCommands()) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                                placeholdersService.parsePlaceholders(superiorPlayer.asOfflinePlayer(), command
                                        .replace("%player%", superiorPlayer.getName())
                                        .replace("%leader%", island.getOwner().getName()))
                        );
                    }

                    hasNextLevel = true;
                }
            }
        }

        SUpgradeLevel.ItemData itemData = ((SUpgradeLevel) upgradeLevel).getItemData();
        SoundWrapper sound = hasNextLevel ? itemData.hasNextLevelSound : itemData.noNextLevelSound;

        if (sound != null)
            superiorPlayer.runIfOnline(sound::playSound);
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, SuperiorPlayer superiorPlayer, Island island, String[] args) {
        return args.length == 2 ? CommandTabCompletes.getUpgrades(plugin, args[1]) : new ArrayList<>();
    }

}
