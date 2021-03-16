package com.bgsoftware.superiorskyblock.commands;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.key.Key;
import com.bgsoftware.superiorskyblock.utils.legacy.Materials;
import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CmdValue implements ISuperiorCommand {

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
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(sender);

        Key toCheck;
        String keyName = "";

        if(args.length == 1){
            ItemStack inHand = ((Player) sender).getItemInHand();

            if(inHand == null){
                inHand = new ItemStack(Material.AIR);
            }

            toCheck = Key.of(inHand);

            if(inHand.getType() == Materials.SPAWNER.toBukkitType())
                keyName = StringUtils.format(toCheck.getSubKey() + "_Spawner");
        }
        else{
            toCheck = Key.of(args[1].toUpperCase());
        }

        if(keyName.isEmpty())
            keyName = StringUtils.format(toCheck.getGlobalKey());

        java.util.Locale locale = superiorPlayer.getUserLocale();

        StringBuilder stringBuilder = new StringBuilder();

        {
            BigDecimal blockWorth = plugin.getBlockValues().getBlockWorth(toCheck);
            if (blockWorth.doubleValue() == 0) {
                if (!Locale.BLOCK_VALUE_WORTHLESS.isEmpty(locale))
                    stringBuilder.append(Locale.BLOCK_VALUE_WORTHLESS.getMessage(locale, keyName)).append("\n");
            } else {
                if (!Locale.BLOCK_VALUE.isEmpty(locale))
                    stringBuilder.append(Locale.BLOCK_VALUE.getMessage(locale, keyName, StringUtils.format(blockWorth))).append("\n");
            }
        }

        {
            BigDecimal blockLevel = plugin.getBlockValues().getBlockLevel(toCheck);
            if (blockLevel.doubleValue() == 0) {
                if (!Locale.BLOCK_LEVEL_WORTHLESS.isEmpty(locale)) {
                    stringBuilder.append(Locale.BLOCK_LEVEL_WORTHLESS.getMessage(locale, keyName)).append("\n");
                }
            } else {
                if (!Locale.BLOCK_LEVEL.isEmpty(locale))
                    stringBuilder.append(Locale.BLOCK_LEVEL.getMessage(locale, keyName, StringUtils.format(blockLevel))).append("\n");
            }
        }

        Locale.sendMessage(superiorPlayer, stringBuilder.toString(), false);
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        return new ArrayList<>();
    }

}
