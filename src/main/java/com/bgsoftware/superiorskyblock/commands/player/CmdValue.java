package com.bgsoftware.superiorskyblock.commands.player;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.ISuperiorCommand;
import com.bgsoftware.superiorskyblock.core.Materials;
import com.bgsoftware.superiorskyblock.core.Text;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.key.Keys;
import com.bgsoftware.superiorskyblock.core.key.types.CustomKey;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

public class CmdValue implements ISuperiorCommand {

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
        return "value [" + Message.COMMAND_ARGUMENT_MATERIAL.getMessage(locale) + "]";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_VALUE.getMessage(locale);
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

        if (args.length == 1) {
            ItemStack inHand = ((Player) sender).getItemInHand();

            if (inHand == null) {
                inHand = new ItemStack(Material.AIR);
            }

            toCheck = Keys.of(inHand);

            if (inHand.getType() == Materials.SPAWNER.toBukkitType()) {
                String subKey = toCheck.getSubKey();
                if (!Text.isBlank(subKey))
                    keyName = Formatters.CAPITALIZED_FORMATTER.format(subKey + "_Spawner");
            }

            if (keyName.isEmpty() && toCheck instanceof CustomKey) {
                String subKey = toCheck.getSubKey();
                if (Text.isBlank(subKey)) {
                    keyName = Formatters.CAPITALIZED_FORMATTER.format(toCheck.toString());
                } else {
                    keyName = Formatters.CAPITALIZED_FORMATTER.format(subKey);
                }
            }

        } else {
            toCheck = Keys.ofMaterialAndData(args[1]);
        }

        if (keyName.isEmpty())
            keyName = Formatters.CAPITALIZED_FORMATTER.format(toCheck.getGlobalKey());

        java.util.Locale locale = superiorPlayer.getUserLocale();

        StringBuilder stringBuilder = new StringBuilder();

        {
            BigDecimal blockWorth = plugin.getBlockValues().getBlockWorth(toCheck);
            if (blockWorth.doubleValue() == 0) {
                if (!Message.BLOCK_VALUE_WORTHLESS.isEmpty(locale))
                    stringBuilder.append(Message.BLOCK_VALUE_WORTHLESS.getMessage(locale, keyName)).append("\n");
            } else {
                if (!Message.BLOCK_VALUE.isEmpty(locale))
                    stringBuilder.append(Message.BLOCK_VALUE.getMessage(locale, keyName,
                            Formatters.NUMBER_FORMATTER.format(blockWorth))).append("\n");
            }
        }

        {
            BigDecimal blockLevel = plugin.getBlockValues().getBlockLevel(toCheck);
            if (blockLevel.doubleValue() == 0) {
                if (!Message.BLOCK_LEVEL_WORTHLESS.isEmpty(locale)) {
                    stringBuilder.append(Message.BLOCK_LEVEL_WORTHLESS.getMessage(locale, keyName)).append("\n");
                }
            } else {
                if (!Message.BLOCK_LEVEL.isEmpty(locale))
                    stringBuilder.append(Message.BLOCK_LEVEL.getMessage(locale, keyName,
                            Formatters.NUMBER_FORMATTER.format(blockLevel))).append("\n");
            }
        }

        Message.CUSTOM.send(superiorPlayer, stringBuilder.toString(), false);
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        return Collections.emptyList();
    }

}
