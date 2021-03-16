package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.upgrades.Upgrade;
import com.bgsoftware.superiorskyblock.api.upgrades.cost.UpgradeCost;
import com.bgsoftware.superiorskyblock.api.upgrades.UpgradeLevel;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.IAdminIslandCommand;
import com.bgsoftware.superiorskyblock.hooks.PlaceholderHook;
import com.bgsoftware.superiorskyblock.utils.commands.CommandArguments;
import com.bgsoftware.superiorskyblock.utils.commands.CommandTabCompletes;
import com.bgsoftware.superiorskyblock.utils.events.EventResult;
import com.bgsoftware.superiorskyblock.utils.events.EventsCaller;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CmdAdminRankup implements IAdminIslandCommand {

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
        return true;
    }

    @Override
    public boolean supportMultipleIslands() {
        return true;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, SuperiorPlayer targetPlayer, List<Island> islands, String[] args) {
        Upgrade upgrade = CommandArguments.getUpgrade(plugin, sender, args[3]);

        if(upgrade == null)
            return;

        islands.forEach(island -> {
            UpgradeLevel upgradeLevel = island.getUpgradeLevel(upgrade);

            EventResult<Pair<List<String>, UpgradeCost>> event = EventsCaller.callIslandUpgradeEvent(
                    null, island, upgrade.getName(), upgradeLevel.getCommands(), upgradeLevel.getCost());

            if(!event.isCancelled()){
                SuperiorPlayer owner = island.getOwner();
                for (String command : event.getResult().getKey()) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), PlaceholderHook.parse(owner, command
                            .replace("%player%", owner.getName())
                            .replace("%leader%", owner.getName()))
                    );
                }
            }
        });

        if(islands.size() > 1)
            Locale.RANKUP_SUCCESS_ALL.send(sender, upgrade.getName());
        else if(targetPlayer == null)
            Locale.RANKUP_SUCCESS_NAME.send(sender, upgrade.getName(), islands.get(0).getName());
        else
            Locale.RANKUP_SUCCESS.send(sender, upgrade.getName(), targetPlayer.getName());
    }

    @Override
    public List<String> adminTabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, Island island, String[] args) {
        return args.length == 4 ? CommandTabCompletes.getUpgrades(plugin, args[3]) : new ArrayList<>();
    }

}
