package com.bgsoftware.superiorskyblock.module.upgrades.commands;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.KeyCategory;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.CommandTabCompletes;
import com.bgsoftware.superiorskyblock.commands.IAdminIslandCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEventsFactory;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.island.upgrade.IslandUpgradeConstants;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CmdAdminRemoveBlockCategoryLimit implements IAdminIslandCommand {

    @Override
    public List<String> getAliases() {
        return Arrays.asList("removeblockcategorylimit", "remblockcategorylimit");
    }

    @Override
    public String getPermission() {
        return "superior.admin.removeblockcategorylimit";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "admin removeblockcategorylimit <" +
                Message.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "/" +
                Message.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + "/" +
                Message.COMMAND_ARGUMENT_ALL_ISLANDS.getMessage(locale) + "> <" +
                Message.COMMAND_ARGUMENT_BLOCK_CATEGORY.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_ADMIN_REMOVE_BLOCK_CATEGORY_LIMIT.getMessage(locale);
    }

    @Override
    public int getMinArgs() {
        return 4;
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
        return true;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, @Nullable SuperiorPlayer targetPlayer, List<Island> islands, String[] args) {
        KeyCategory blockCategory = CommandArguments.getBlockCategory(plugin, sender, args[3]);
        if (blockCategory == null) {
            return;
        }

        int islandsChangedCount = 0;

        for (Island island : islands) {
            if (island.getBlockCategoryLimitRaw(blockCategory) != IslandUpgradeConstants.SYNCED_VALUE) {
                if (!PluginEventsFactory.callIslandRemoveBlockCategoryLimitEvent(island, sender, blockCategory)) {
                    continue;
                }

                island.removeBlockCategoryLimit(blockCategory);
            }

            ++islandsChangedCount;
        }

        if (islandsChangedCount <= 0) {
            return;
        }

        if (islandsChangedCount > 1) {
            Message.CHANGED_BLOCK_CATEGORY_LIMIT_ALL.send(sender, Formatters.CAPITALIZED_FORMATTER.format(blockCategory.getName()));
        } else if (targetPlayer == null) {
            Message.CHANGED_BLOCK_CATEGORY_LIMIT_NAME.send(sender, Formatters.CAPITALIZED_FORMATTER.format(blockCategory.getName()), islands.get(0).getName());
        } else {
            Message.CHANGED_BLOCK_CATEGORY_LIMIT.send(sender, Formatters.CAPITALIZED_FORMATTER.format(blockCategory.getName()), targetPlayer.getName());
        }
    }

    @Override
    public List<String> adminTabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, Island island, String[] args) {
        return args.length == 4 ? CommandTabCompletes.getBlockCategories(plugin, args[3]) : Collections.emptyList();
    }

}
