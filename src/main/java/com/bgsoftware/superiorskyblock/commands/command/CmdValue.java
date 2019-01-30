package com.bgsoftware.superiorskyblock.commands.command;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.commands.ICommand;
import com.bgsoftware.superiorskyblock.utils.StringUtil;
import com.bgsoftware.superiorskyblock.utils.key.SKey;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;

public final class CmdValue implements ICommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("value");
    }

    @Override
    public String getPermission() {
        return "superior.island.value";
    }

    @Override
    public String getUsage() {
        return "island value [block-type]";
    }

    @Override
    public int getMinArgs() {
        return 1;
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

        SKey toCheck;

        if(args.length == 1){
            ItemStack inHand = superiorPlayer.asPlayer().getItemInHand();

            if(inHand == null){
                inHand = new ItemStack(Material.AIR);
            }

            toCheck = SKey.of(inHand);
        }
        else{
            toCheck = SKey.of(args[1]);
        }

        int value = plugin.getGrid().getBlockValue(toCheck);
        String key = StringUtil.format(toCheck.toString().split(":")[0]);

        if(value == 0)
            Locale.BLOCK_VALUE_WORTHLESS.send(superiorPlayer, key);
        else
            Locale.BLOCK_VALUE.send(superiorPlayer, key, value);
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        return null;
    }
}
