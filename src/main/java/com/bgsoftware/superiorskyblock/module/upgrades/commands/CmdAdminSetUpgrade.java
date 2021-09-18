package com.bgsoftware.superiorskyblock.module.upgrades.commands;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.upgrades.Upgrade;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.IAdminIslandCommand;
import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.commands.CommandArguments;
import com.bgsoftware.superiorskyblock.commands.CommandTabCompletes;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CmdAdminSetUpgrade implements IAdminIslandCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("setupgrade");
    }

    @Override
    public String getPermission() {
        return "superior.admin.setupgrade";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "admin setupgrade <" +
                Locale.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "/" +
                Locale.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + "> <" +
                Locale.COMMAND_ARGUMENT_UPGRADE_NAME.getMessage(locale) + "> <" +
                Locale.COMMAND_ARGUMENT_LEVEL.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Locale.COMMAND_DESCRIPTION_ADMIN_SET_UPGRADE.getMessage(locale);
    }

    @Override
    public int getMinArgs() {
        return 5;
    }

    @Override
    public int getMaxArgs() {
        return 5;
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return true;
    }

    @Override
    public boolean supportMultipleIslands() {
        return false;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, SuperiorPlayer targetPlayer, Island island, String[] args) {
        Upgrade upgrade = CommandArguments.getUpgrade(plugin, sender, args[3]);

        if(upgrade == null)
            return;

        Pair<Integer, Boolean> arguments = CommandArguments.getLevel(sender, args[4]);

        if(!arguments.getValue())
            return;

        int level = arguments.getKey();
        int maxLevel = upgrade.getMaxUpgradeLevel();

        if(level > maxLevel){
            Locale.MAXIMUM_LEVEL.send(sender, maxLevel);
            return;
        }

        island.setUpgradeLevel(upgrade, level);

        if(targetPlayer == null)
            Locale.SET_UPGRADE_LEVEL_NAME.send(sender, upgrade.getName(), island.getName());
        else
            Locale.SET_UPGRADE_LEVEL.send(sender, upgrade.getName(), targetPlayer.getName());
    }

    @Override
    public List<String> adminTabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, Island island, String[] args) {
        return args.length == 4 ? CommandTabCompletes.getUpgrades(plugin, args[3]) : new ArrayList<>();
    }

}
