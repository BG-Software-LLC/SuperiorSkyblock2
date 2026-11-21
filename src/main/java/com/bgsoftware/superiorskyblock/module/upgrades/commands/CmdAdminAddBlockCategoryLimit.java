package com.bgsoftware.superiorskyblock.module.upgrades.commands;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.KeyCategory;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.CommandTabCompletes;
import com.bgsoftware.superiorskyblock.commands.IAdminIslandCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.commands.arguments.NumberArgument;
import com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEvent;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEventsFactory;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.island.upgrade.IslandUpgradeConstants;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class CmdAdminAddBlockCategoryLimit implements IAdminIslandCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("addblockcategorylimit");
    }

    @Override
    public String getPermission() {
        return "superior.admin.addblockcategorylimit";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "admin addblockcategorylimit <" +
                Message.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "/" +
                Message.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + "/" +
                Message.COMMAND_ARGUMENT_ALL_ISLANDS.getMessage(locale) + "> <" +
                Message.COMMAND_ARGUMENT_BLOCK_CATEGORY.getMessage(locale) + "> <" +
                Message.COMMAND_ARGUMENT_LIMIT.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_ADMIN_ADD_BLOCK_CATEGORY_LIMIT.getMessage(locale);
    }

    @Override
    public int getMinArgs() {
        return 5;
    }

    @Override
    public int getMaxArgs() {
        return 5;
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

        NumberArgument<Integer> arguments = CommandArguments.getAdditionalLimit(sender, args[4]);
        if (!arguments.isSucceed()) {
            return;
        }

        int limit = arguments.getNumber();

        boolean isUnlimited = false;
        boolean isInvalid = false;
        Island changedIsland = null;
        int islandsChangedCount = 0;

        for (Island island : islands) {
            int currentLimit = island.getBlockCategoryLimit(blockCategory);
            if (currentLimit <= IslandUpgradeConstants.NO_LIMIT_VALUE) {
                isUnlimited = true;
                continue;
            }

            int newLimit = currentLimit + limit;

            if (newLimit < 0) {
                isInvalid = true;
                continue;
            }

            PluginEvent<PluginEventArgs.IslandChangeBlockCategoryLimit> event = PluginEventsFactory.callIslandChangeBlockCategoryLimitEvent(
                    island, sender, blockCategory, newLimit);

            if (!event.isCancelled()) {
                island.setBlockCategoryLimit(blockCategory, event.getArgs().blockCategoryLimit);
                changedIsland = island;
                ++islandsChangedCount;
            }
        }

        if (islandsChangedCount <= 0) {
            if (isUnlimited) {
                Message.LIMIT_IS_UNLIMITED.send(sender);
            } else if (isInvalid) {
                Message.INVALID_LIMIT.send(sender, limit);
            }
            return;
        }

        if (islandsChangedCount > 1) {
            Message.CHANGED_BLOCK_CATEGORY_LIMIT_ALL.send(sender, Formatters.CAPITALIZED_FORMATTER.format(blockCategory.getName()));
        } else if (targetPlayer == null) {
            Message.CHANGED_BLOCK_CATEGORY_LIMIT_NAME.send(sender, Formatters.CAPITALIZED_FORMATTER.format(blockCategory.getName()), changedIsland.getName());
        } else {
            Message.CHANGED_BLOCK_CATEGORY_LIMIT.send(sender, Formatters.CAPITALIZED_FORMATTER.format(blockCategory.getName()), targetPlayer.getName());
        }
    }

    @Override
    public List<String> adminTabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, Island island, String[] args) {
        return args.length == 4 ? CommandTabCompletes.getEntityCategories(plugin, args[3]) : Collections.emptyList();
    }

}
