package com.bgsoftware.superiorskyblock.commands.command.admin;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.commands.ICommand;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public final class CmdAdminSetUpgrade implements ICommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("setupgrade");
    }

    @Override
    public String getPermission() {
        return "superior.admin.setupgrade";
    }

    @Override
    public String getUsage() {
        return "island admin setupgrade <player-name> <upgrade-name> <level>";
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
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        SuperiorPlayer targetPlayer = SSuperiorPlayer.of(args[2]);

        if(targetPlayer == null){
            Locale.INVALID_PLAYER.send(sender, args[2]);
            return;
        }

        Island island = targetPlayer.getIsland();

        if(island == null){
            Locale.INVALID_ISLAND_OTHER.send(sender, targetPlayer.getName());
            return;
        }

        String upgradeName = args[3].toLowerCase();

        if(!plugin.getUpgrades().isUpgrade(upgradeName)){
            Locale.INVALID_UPGRADE.send(sender, args[3], getUpgradesString(plugin));
            return;
        }

        int level;

        try{
            level = Integer.valueOf(args[4]);
        }catch (IllegalArgumentException ex){
            Locale.INVALID_LEVEL.send(sender, args[4]);
            return;
        }

        int maxLevel = plugin.getUpgrades().getMaxUpgradeLevel(upgradeName);

        if(level > maxLevel){
            Locale.MAXIMUM_LEVEL.send(sender, maxLevel);
            return;
        }

        island.setUpgradeLevel(upgradeName, level);
        Locale.SET_UPGRADE_LEVEL.send(sender, upgradeName, targetPlayer.getName());
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        List<String> list = new ArrayList<>();

        if(args.length == 3){
            SuperiorPlayer targetPlayer;
            for(UUID uuid : plugin.getGrid().getAllIslands()) {
                targetPlayer = SSuperiorPlayer.of(uuid);
                if(targetPlayer.getName().toLowerCase().startsWith(args[2].toLowerCase()))
                    list.add(targetPlayer.getName());
            }
        }

        else if(args.length == 4){
            list.addAll(plugin.getUpgrades().getAllUpgrades().stream()
                    .filter(upgrade -> upgrade.toLowerCase().startsWith(args[3].toLowerCase()))
                    .collect(Collectors.toList()));
        }

        return list;
    }

    private String getUpgradesString(SuperiorSkyblockPlugin plugin){
        StringBuilder stringBuilder = new StringBuilder();

        for(String upgrade : plugin.getUpgrades().getAllUpgrades())
            stringBuilder.append(", ").append(upgrade);

        return stringBuilder.toString().substring(2);
    }

}
