package com.bgsoftware.superiorskyblock.commands.command;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPermission;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.ICommand;
import com.bgsoftware.superiorskyblock.hooks.EconomyHook;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class CmdRankup implements ICommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("rankup");
    }

    @Override
    public String getPermission() {
        return "superior.island.rankup";
    }

    @Override
    public String getUsage() {
        return "island rankup <upgrade-name>";
    }

    @Override
    public String getDescription() {
        return Locale.COMMAND_DESCRIPTION_RANKUP.getMessage();
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
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(sender);
        Island island = superiorPlayer.getIsland();

        if(island == null){
            Locale.INVALID_ISLAND.send(superiorPlayer);
            return;
        }

        if(!superiorPlayer.hasPermission(IslandPermission.RANKUP)){
            Locale.NO_RANKUP_PERMISSION.send(superiorPlayer, island.getRequiredRole(IslandPermission.RANKUP));
            return;
        }

        String upgradeName = args[1];

        if(!plugin.getUpgrades().isUpgrade(upgradeName)){
            Locale.INVALID_UPGRADE.send(superiorPlayer, upgradeName, getUpgradesString(plugin));
            return;
        }

        int level = island.getUpgradeLevel(upgradeName);
        double nextUpgradePrice = plugin.getUpgrades().getUpgradePrice(upgradeName, level);
        boolean hasNextLevel;

        if(EconomyHook.getMoneyInBank(superiorPlayer) < nextUpgradePrice){
            Locale.NOT_ENOUGH_MONEY_TO_UPGRADE.send(superiorPlayer);
            hasNextLevel = false;
        }

        else{
            List<String> commands = plugin.getUpgrades().getUpgradeCommands(upgradeName, level);

            EconomyHook.withdrawMoney(superiorPlayer, nextUpgradePrice);
            for (String command : commands) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%player%", superiorPlayer.getName()));
            }

            hasNextLevel = true;
        }

        Sound sound = plugin.getUpgrades().getClickSound(upgradeName, level, hasNextLevel);
        if(sound != null)
            superiorPlayer.asPlayer().playSound(superiorPlayer.getLocation(), sound, 1, 1);
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        return null;
    }

    private String getUpgradesString(SuperiorSkyblockPlugin plugin){
        StringBuilder stringBuilder = new StringBuilder();

        for(String upgrade : plugin.getUpgrades().getAllUpgrades())
            stringBuilder.append(", ").append(upgrade);

        return stringBuilder.toString().substring(2);
    }

}
