package com.bgsoftware.superiorskyblock.commands.player;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.CommandContext;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgument;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.InternalSuperiorCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArgumentsBuilder;
import com.bgsoftware.superiorskyblock.commands.arguments.types.StringArgumentType;
import com.bgsoftware.superiorskyblock.core.Materials;
import com.bgsoftware.superiorskyblock.core.Text;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.key.Keys;
import com.bgsoftware.superiorskyblock.core.key.types.CustomKey;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

public class CmdValue implements InternalSuperiorCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("value");
    }

    @Override
    public String getPermission() {
        return "superior.island.value";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_VALUE.getMessage(locale);
    }

    @Override
    public List<CommandArgument<?>> getArguments() {
        return new CommandArgumentsBuilder()
                .add(CommandArgument.optional("material", StringArgumentType.INSTANCE, Message.COMMAND_ARGUMENT_MATERIAL))
                .build();
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return false;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, CommandContext context) {
        Player player = (Player) context.getDispatcher();
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(player);

        String materialName = context.getOptionalArgument("material", String.class).orElse(null);

        Key toCheck;
        String keyName = "";

        if (materialName == null) {
            ItemStack inHand = player.getItemInHand();

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
            toCheck = Keys.ofMaterialAndData(materialName);
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

}
