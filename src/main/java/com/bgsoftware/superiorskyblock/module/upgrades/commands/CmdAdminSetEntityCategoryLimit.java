package com.bgsoftware.superiorskyblock.module.upgrades.commands;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.entity.EntityCategory;
import com.bgsoftware.superiorskyblock.api.island.Island;
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
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class CmdAdminSetEntityCategoryLimit implements IAdminIslandCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("setentitycategorylimit");
    }

    @Override
    public String getPermission() {
        return "superior.admin.setentitycategorylimit";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "admin setentitycategorylimit <" +
                Message.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "/" +
                Message.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + "/" +
                Message.COMMAND_ARGUMENT_ALL_ISLANDS.getMessage(locale) + "> <" +
                Message.COMMAND_ARGUMENT_ENTITY_CATEGORY.getMessage(locale) + "> <" +
                Message.COMMAND_ARGUMENT_LIMIT.getMessage(locale) + ">";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_ADMIN_SET_ENTITY_CATEGORY_LIMIT.getMessage(locale);
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
        EntityCategory entityCategory = CommandArguments.getEntityCategory(plugin, sender, args[3]);
        if (entityCategory == null) {
            return;
        }

        NumberArgument<Integer> arguments = CommandArguments.getLimit(sender, args[4]);
        if (!arguments.isSucceed()) {
            return;
        }

        int limit = arguments.getNumber();

        int islandsChangedCount = 0;

        for (Island island : islands) {
            PluginEvent<PluginEventArgs.IslandChangeEntityCategoryLimit> event = PluginEventsFactory.callIslandChangeEntityCategoryLimitEvent(
                    island, sender, entityCategory, limit);
            if (!event.isCancelled()) {
                island.setEntityCategoryLimit(entityCategory, event.getArgs().entityCategoryLimit);
                ++islandsChangedCount;
            }
        }

        if (islandsChangedCount <= 0) {
            return;
        }

        if (islandsChangedCount > 1) {
            Message.CHANGED_ENTITY_CATEGORY_LIMIT_ALL.send(sender, Formatters.CAPITALIZED_FORMATTER.format(entityCategory.getName()));
        } else if (targetPlayer == null) {
            Message.CHANGED_ENTITY_CATEGORY_LIMIT_NAME.send(sender, Formatters.CAPITALIZED_FORMATTER.format(entityCategory.getName()), islands.get(0).getName());
        } else {
            Message.CHANGED_ENTITY_CATEGORY_LIMIT.send(sender, Formatters.CAPITALIZED_FORMATTER.format(entityCategory.getName()), targetPlayer.getName());
        }
    }

    @Override
    public List<String> adminTabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, Island island, String[] args) {
        return args.length == 4 ? CommandTabCompletes.getEntityCategories(plugin, args[3]) : Collections.emptyList();
    }

}
