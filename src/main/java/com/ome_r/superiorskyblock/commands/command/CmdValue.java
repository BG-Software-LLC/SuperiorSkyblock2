package com.ome_r.superiorskyblock.commands.command;

import com.ome_r.superiorskyblock.Locale;
import com.ome_r.superiorskyblock.SuperiorSkyblock;
import com.ome_r.superiorskyblock.commands.ICommand;
import com.ome_r.superiorskyblock.utils.ItemUtil;
import com.ome_r.superiorskyblock.utils.StringUtil;
import com.ome_r.superiorskyblock.utils.key.Key;
import com.ome_r.superiorskyblock.wrappers.WrappedPlayer;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;

public class CmdValue implements ICommand {

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
    public void execute(SuperiorSkyblock plugin, CommandSender sender, String[] args) {
        WrappedPlayer wrappedPlayer = WrappedPlayer.of(sender);

        Key toCheck;

        if(args.length == 1){
            ItemStack inHand = wrappedPlayer.asPlayer().getItemInHand();

            if(inHand == null){
                inHand = new ItemStack(Material.AIR);
            }

            toCheck = Key.of(inHand);
        }
        else{
            toCheck = Key.of(args[1]);
        }

        int value = plugin.getGrid().getBlockValue(toCheck);
        String key = StringUtil.format(toCheck.toString().split(":")[0]);

        if(value == 0)
            Locale.BLOCK_VALUE_WORTHLESS.send(wrappedPlayer, key);
        else
            Locale.BLOCK_VALUE.send(wrappedPlayer, key, value);
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblock plugin, CommandSender sender, String[] args) {
        return null;
    }
}
