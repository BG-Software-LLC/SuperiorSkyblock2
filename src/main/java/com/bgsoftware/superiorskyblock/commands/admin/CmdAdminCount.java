package com.bgsoftware.superiorskyblock.commands.admin;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.CommandTabCompletes;
import com.bgsoftware.superiorskyblock.commands.IAdminIslandCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.key.Keys;
import com.bgsoftware.superiorskyblock.core.menu.view.MenuViewWrapper;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.player.PlayerLocales;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.math.BigInteger;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CmdAdminCount implements IAdminIslandCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("count");
    }

    @Override
    public String getPermission() {
        return "superior.admin.count";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "admin count <" +
                Message.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "/" +
                Message.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + "> [" +
                Message.COMMAND_ARGUMENT_MATERIAL.getMessage(locale) + "/" +
                Message.COMMAND_ARGUMENT_ALL_MATERIALS.getMessage(locale) + "]";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_ADMIN_COUNT.getMessage(locale);
    }

    @Override
    public int getMinArgs() {
        return 3;
    }

    @Override
    public int getMaxArgs() {
        return 4;
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
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, @Nullable SuperiorPlayer targetPlayer, Island island, String[] args) {
        if (args.length == 3) {
            if (!(sender instanceof Player)) {
                Message.CUSTOM.send(sender, "&cYou must be a player in order to open the counts menu.", true);
                return;
            }

            SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(sender);
            plugin.getMenus().openCounts(superiorPlayer, MenuViewWrapper.fromView(superiorPlayer.getOpenedView()), island);
            return;
        }

        String materialName = args[3].toUpperCase(Locale.ENGLISH);

        if (materialName.equals("*")) {
            StringBuilder materialsBuilder = new StringBuilder();

            java.util.Locale locale = PlayerLocales.getLocale(sender);

            if (!Message.BLOCK_COUNTS_CHECK_MATERIAL.isEmpty(locale)) {
                for (Map.Entry<Key, BigInteger> entry : island.getBlockCountsAsBigInteger().entrySet()) {
                    materialsBuilder.append(", ").append(Message.BLOCK_COUNTS_CHECK_MATERIAL
                            .getMessage(locale, Formatters.NUMBER_FORMATTER.format(entry.getValue()),
                                    Formatters.CAPITALIZED_FORMATTER.format(entry.getKey().toString())));
                }
            }

            if (materialsBuilder.length() == 0) {
                Message.BLOCK_COUNTS_CHECK_EMPTY.send(sender);
            } else {
                Message.BLOCK_COUNTS_CHECK.send(sender, materialsBuilder.substring(1));
            }
        } else {
            Material material = CommandArguments.getMaterial(sender, materialName);

            if (material == null)
                return;

            BigInteger blockCount = island.getBlockCountAsBigInteger(Keys.ofMaterialAndData(materialName));

            if (blockCount.compareTo(BigInteger.ONE) > 0)
                materialName = materialName + "s";

            Message.BLOCK_COUNT_CHECK.send(sender, Formatters.NUMBER_FORMATTER.format(blockCount),
                    Formatters.CAPITALIZED_FORMATTER.format(materialName));
        }
    }

    @Override
    public List<String> adminTabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, Island island, String[] args) {
        if (args.length != 4)
            return Collections.emptyList();

        List<String> list = new LinkedList<>(CommandTabCompletes.getMaterials(args[3]));

        if ("*".contains(args[3]))
            list.add("*");

        return Collections.unmodifiableList(list);
    }

}
