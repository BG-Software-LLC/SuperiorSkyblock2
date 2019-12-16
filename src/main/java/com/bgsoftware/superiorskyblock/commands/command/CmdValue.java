package com.bgsoftware.superiorskyblock.commands.command;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.legacy.Materials;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.commands.ICommand;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
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
    public String getUsage(java.util.Locale locale) {
        return "value [" + Locale.COMMAND_ARGUMENT_MATERIAL.getMessage(locale) + "]";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Locale.COMMAND_DESCRIPTION_VALUE.getMessage(locale);
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
        String keyName = "";

        if(args.length == 1){
            ItemStack inHand = superiorPlayer.asPlayer().getItemInHand();

            if(inHand == null){
                inHand = new ItemStack(Material.AIR);
            }

            toCheck = Key.of(inHand);

            if(inHand.getType() == Materials.SPAWNER.toBukkitType())
                keyName = StringUtils.format(toCheck.toString().split(":")[1] + "_Spawner");
        }
        else{
            toCheck = Key.of(args[1].toUpperCase());
        }

        if(keyName.isEmpty())
            keyName = StringUtils.format(toCheck.toString().split(":")[0]);

        BigDecimal blockWorth = plugin.getBlockValues().getBlockWorth(toCheck),
                blockLevel = plugin.getBlockValues().getBlockLevel(toCheck);

        java.util.Locale locale = superiorPlayer.getUserLocale();

        StringBuilder stringBuilder = new StringBuilder();

        if(blockWorth.doubleValue() <= 0) {
            if(!Locale.BLOCK_VALUE_WORTHLESS.isEmpty(locale))
                stringBuilder.append(Locale.BLOCK_VALUE_WORTHLESS.getMessage(locale, keyName));
        }
        else{
            if(!Locale.BLOCK_VALUE.isEmpty(locale))
                stringBuilder.append(Locale.BLOCK_VALUE.getMessage(locale, keyName, StringUtils.format(blockWorth)));
        }

        if(!Locale.BLOCK_LEVEL.isEmpty(locale)){
            if(stringBuilder.length() != 0)
                stringBuilder.append("\n");
            stringBuilder.append(Locale.BLOCK_LEVEL.getMessage(locale, keyName, StringUtils.format(blockLevel)));
        }

        Locale.sendMessage(superiorPlayer, stringBuilder.toString());
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}
