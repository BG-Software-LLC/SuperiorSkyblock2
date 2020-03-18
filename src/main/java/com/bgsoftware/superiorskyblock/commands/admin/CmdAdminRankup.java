package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.events.IslandUpgradeEvent;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.upgrades.Upgrade;
import com.bgsoftware.superiorskyblock.api.upgrades.UpgradeLevel;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.ISuperiorCommand;
import com.bgsoftware.superiorskyblock.hooks.PlaceholderHook;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CmdAdminRankup implements ISuperiorCommand {

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
                Locale.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "/" +
                Locale.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + "/" +
                Locale.COMMAND_ARGUMENT_ALL_ISLANDS.getMessage(locale) + "> <" +
                Locale.COMMAND_ARGUMENT_UPGRADE_NAME.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Locale.COMMAND_DESCRIPTION_ADMIN_RANKUP.getMessage(locale);
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
        return false;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        SuperiorPlayer superiorPlayer = SSuperiorPlayer.of(sender);
        SuperiorPlayer targetPlayer = SSuperiorPlayer.of(args[2]);
        List<Island> islands = new ArrayList<>();

        if(args[2].equalsIgnoreCase("*")) {
            islands.addAll(plugin.getGrid().getIslands());
        }

        else{
            Island island = targetPlayer == null ? plugin.getGrid().getIsland(args[2]) : targetPlayer.getIsland();

            if (island == null) {
                if (args[2].equalsIgnoreCase(sender.getName()))
                    Locale.INVALID_ISLAND.send(sender);
                else if (targetPlayer == null)
                    Locale.INVALID_ISLAND_OTHER_NAME.send(sender, StringUtils.stripColors(args[2]));
                else
                    Locale.INVALID_ISLAND_OTHER.send(sender, targetPlayer.getName());
                return;
            }

            islands.add(island);
        }

        String upgradeName = args[3];

        if(!plugin.getUpgrades().isUpgrade(upgradeName)){
            Locale.INVALID_UPGRADE.send(sender, upgradeName, StringUtils.getUpgradesString(plugin));
            return;
        }

        Upgrade upgrade = plugin.getUpgrades().getUpgrade(upgradeName);

        islands.forEach(island -> {
            UpgradeLevel upgradeLevel = island.getUpgradeLevel(upgrade);

            IslandUpgradeEvent islandUpgradeEvent = new IslandUpgradeEvent(superiorPlayer, island, upgradeName, upgradeLevel.getCommands(), upgradeLevel.getPrice());
            Bukkit.getPluginManager().callEvent(islandUpgradeEvent);

            if(!islandUpgradeEvent.isCancelled()){
                SuperiorPlayer owner = island.getOwner();
                for (String command : islandUpgradeEvent.getCommands()) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), PlaceholderHook.parse(owner, command
                            .replace("%player%", owner.getName())
                            .replace("%leader%", owner.getName()))
                    );
                }
            }
        });

        if(islands.size() > 1)
            Locale.RANKUP_SUCCESS_ALL.send(sender, upgradeName);
        else if(targetPlayer == null)
            Locale.RANKUP_SUCCESS_NAME.send(sender, upgradeName, islands.get(0).getName());
        else
            Locale.RANKUP_SUCCESS.send(sender, upgradeName, targetPlayer.getName());
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        List<String> list = new ArrayList<>();

        if(args.length == 3){
            for(Player player : Bukkit.getOnlinePlayers()){
                SuperiorPlayer onlinePlayer = SSuperiorPlayer.of(player);
                Island playerIsland = onlinePlayer.getIsland();
                if (playerIsland != null) {
                    if (player.getName().toLowerCase().startsWith(args[2].toLowerCase()))
                        list.add(player.getName());
                    if(!playerIsland.getName().isEmpty() && playerIsland.getName().toLowerCase().startsWith(args[2].toLowerCase()))
                        list.add(playerIsland.getName());
                }
            }
        }
        else if(args.length == 4){
            plugin.getUpgrades().getUpgrades().stream()
                    .filter(upgrade -> upgrade.getName().toLowerCase().startsWith(args[3].toLowerCase()))
                    .forEach(upgrade -> list.add(upgrade.getName().toLowerCase()));
        }

        return list;
    }
}
