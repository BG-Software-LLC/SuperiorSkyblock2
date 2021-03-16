package com.bgsoftware.superiorskyblock.commands;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.upgrades.Upgrade;
import com.bgsoftware.superiorskyblock.api.upgrades.cost.UpgradeCost;
import com.bgsoftware.superiorskyblock.api.upgrades.UpgradeLevel;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.hooks.PlaceholderHook;
import com.bgsoftware.superiorskyblock.upgrades.SUpgradeLevel;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.commands.CommandArguments;
import com.bgsoftware.superiorskyblock.utils.commands.CommandTabCompletes;
import com.bgsoftware.superiorskyblock.utils.events.EventResult;
import com.bgsoftware.superiorskyblock.utils.events.EventsCaller;
import com.bgsoftware.superiorskyblock.utils.islands.IslandPrivileges;
import com.bgsoftware.superiorskyblock.wrappers.SoundWrapper;
import org.bukkit.Bukkit;

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
        return "rankup <" + Locale.COMMAND_ARGUMENT_UPGRADE_NAME.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Locale.COMMAND_DESCRIPTION_RANKUP.getMessage(locale);
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
    public Locale getPermissionLackMessage() {
        return Locale.NO_RANKUP_PERMISSION;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, SuperiorPlayer superiorPlayer, Island island, String[] args) {
        Upgrade upgrade = CommandArguments.getUpgrade(plugin, superiorPlayer, args[1]);

        if(upgrade == null)
            return;

        UpgradeLevel upgradeLevel = island.getUpgradeLevel(upgrade), nextUpgradeLevel = upgrade.getUpgradeLevel(upgradeLevel.getLevel() + 1);

        String permission = nextUpgradeLevel == null ? "" : nextUpgradeLevel.getPermission();

        if(!permission.isEmpty() && !superiorPlayer.hasPermission(permission)){
            Locale.NO_UPGRADE_PERMISSION.send(superiorPlayer);
            return;
        }

        boolean hasNextLevel;

        if(island.hasActiveUpgradeCooldown()){
            long timeNow = System.currentTimeMillis(), lastUpgradeTime = island.getLastTimeUpgrade();
            Locale.UPGRADE_COOLDOWN_FORMAT.send(superiorPlayer, StringUtils.formatTime(superiorPlayer.getUserLocale(),
                    lastUpgradeTime + plugin.getSettings().upgradeCooldown - timeNow));
            hasNextLevel = false;
        }
        else {
            String requiredCheckFailure = nextUpgradeLevel == null ? "" : nextUpgradeLevel.checkRequirements(superiorPlayer);

            if (!requiredCheckFailure.isEmpty()) {
                Locale.sendMessage(superiorPlayer, requiredCheckFailure, false);
                hasNextLevel = false;
            } else {
                EventResult<Pair<List<String>, UpgradeCost>> event = EventsCaller.callIslandUpgradeEvent(superiorPlayer, island, upgrade.getName(), upgradeLevel.getCommands(), upgradeLevel.getCost());
                UpgradeCost upgradeCost = event.getResult().getValue();

                if (event.isCancelled()) {
                    hasNextLevel = false;

                } else if (!upgradeCost.hasEnoughBalance(superiorPlayer)) {
                    Locale.NOT_ENOUGH_MONEY_TO_UPGRADE.send(superiorPlayer);
                    hasNextLevel = false;

                } else {
                    upgradeCost.withdrawCost(superiorPlayer);

                    for (String command : event.getResult().getKey()) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), PlaceholderHook.parse(superiorPlayer, command
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

        if(sound != null)
            superiorPlayer.runIfOnline(sound::playSound);
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, SuperiorPlayer superiorPlayer, Island island, String[] args) {
        return args.length == 2 ? CommandTabCompletes.getUpgrades(plugin, args[1]) : new ArrayList<>();
    }

}
