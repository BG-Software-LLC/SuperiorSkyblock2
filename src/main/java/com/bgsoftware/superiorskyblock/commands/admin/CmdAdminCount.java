package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgument;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.InternalIslandCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArgumentsBuilder;
import com.bgsoftware.superiorskyblock.commands.arguments.types.IslandArgumentType;
import com.bgsoftware.superiorskyblock.commands.arguments.types.StringArgumentType;
import com.bgsoftware.superiorskyblock.commands.context.IslandCommandContext;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.key.Keys;
import com.bgsoftware.superiorskyblock.core.menu.view.MenuViewWrapper;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.player.PlayerLocales;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CmdAdminCount implements InternalIslandCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("count");
    }

    @Override
    public String getPermission() {
        return "superior.admin.count";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_ADMIN_COUNT.getMessage(locale);
    }

    @Override
    public List<CommandArgument<?>> getArguments() {
        return new CommandArgumentsBuilder()
                .add(CommandArgument.required("island", IslandArgumentType.INSTANCE, Message.COMMAND_ARGUMENT_PLAYER_NAME, Message.COMMAND_ARGUMENT_ISLAND_NAME))
                .add(CommandArgument.optional("material", StringArgumentType.INSTANCE, Message.COMMAND_ARGUMENT_MATERIAL))
                .build();
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return true;
    }

    @Override
    public boolean isSelfIsland() {
        return false;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, IslandCommandContext context) {
        CommandSender dispatcher = context.getDispatcher();

        Island island = context.getIsland();

        String materialName = context.getOptionalArgument("material", String.class).orElse(null);

        if (materialName == null) {
            if (!(dispatcher instanceof Player)) {
                Message.CUSTOM.send(dispatcher, "&cYou must be a player in order to open the counts menu.", true);
                return;
            }

            SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(dispatcher);
            plugin.getMenus().openCounts(superiorPlayer, MenuViewWrapper.fromView(superiorPlayer.getOpenedView()), island);
        } else if (materialName.equals("*")) {
            StringBuilder materialsBuilder = new StringBuilder();

            java.util.Locale locale = PlayerLocales.getLocale(dispatcher);

            if (!Message.BLOCK_COUNTS_CHECK_MATERIAL.isEmpty(locale)) {
                for (Map.Entry<Key, BigInteger> entry : island.getBlockCountsAsBigInteger().entrySet()) {
                    materialsBuilder.append(", ").append(Message.BLOCK_COUNTS_CHECK_MATERIAL
                            .getMessage(locale, Formatters.NUMBER_FORMATTER.format(entry.getValue()),
                                    Formatters.CAPITALIZED_FORMATTER.format(entry.getKey().toString())));
                }
            }

            if (materialsBuilder.length() == 0) {
                Message.BLOCK_COUNTS_CHECK_EMPTY.send(dispatcher);
            } else {
                Message.BLOCK_COUNTS_CHECK.send(dispatcher, materialsBuilder.substring(1));
            }
        } else {
            BigInteger blockCount = island.getBlockCountAsBigInteger(Keys.ofMaterialAndData(materialName));

            if (blockCount.compareTo(BigInteger.ONE) > 0)
                materialName = materialName + "s";

            Message.BLOCK_COUNT_CHECK.send(dispatcher, Formatters.NUMBER_FORMATTER.format(blockCount),
                    Formatters.CAPITALIZED_FORMATTER.format(materialName));
        }
    }

}
