package com.bgsoftware.superiorskyblock.commands.command;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.commands.ICommand;
import com.bgsoftware.superiorskyblock.utils.StringUtil;
import com.bgsoftware.superiorskyblock.api.key.Key;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.util.ArrayList;
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
    public String getDescription() {
        return Locale.COMMAND_DESCRIPTION_VALUE.getMessage();
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

        Key toCheck;

        if(args.length == 1){
            ItemStack inHand = superiorPlayer.asPlayer().getItemInHand();

            if(inHand == null){
                inHand = new ItemStack(Material.AIR);
            }

            toCheck = Key.of(inHand);
        }
        else{
            toCheck = Key.of(args[1].toUpperCase());
        }

        BigDecimal blockWorth = plugin.getBlockValues().getBlockWorth(toCheck),
                blockLevel = plugin.getBlockValues().getBlockLevel(toCheck);

        String key = StringUtil.format(toCheck.toString().split(":")[0]);

        StringBuilder stringBuilder = new StringBuilder();

        if(blockWorth.doubleValue() == 0) {
            if(!Locale.BLOCK_VALUE_WORTHLESS.isEmpty())
                stringBuilder.append(Locale.BLOCK_VALUE_WORTHLESS.getMessage(key));
        }
        else{
            if(!Locale.BLOCK_VALUE.isEmpty())
                stringBuilder.append(Locale.BLOCK_VALUE.getMessage(key, StringUtil.format(blockWorth)));
        }

        if(!Locale.BLOCK_LEVEL.isEmpty()){
            if(stringBuilder.length() != 0)
                stringBuilder.append("\n");
            stringBuilder.append(Locale.BLOCK_LEVEL.getMessage(key, StringUtil.format(blockLevel)));
        }

        Locale.sendMessage(superiorPlayer, stringBuilder.toString());
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}
