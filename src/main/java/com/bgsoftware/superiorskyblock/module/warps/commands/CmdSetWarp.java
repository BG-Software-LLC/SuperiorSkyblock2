package com.bgsoftware.superiorskyblock.module.warps.commands;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.island.warps.IslandWarp;
import com.bgsoftware.superiorskyblock.api.island.warps.WarpCategory;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.CommandTabCompletes;
import com.bgsoftware.superiorskyblock.commands.IPermissibleCommand;
import com.bgsoftware.superiorskyblock.core.ObjectsPools;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEventsFactory;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.island.IslandNames;
import com.bgsoftware.superiorskyblock.island.privilege.IslandPrivileges;
import com.bgsoftware.superiorskyblock.module.BuiltinModules;
import com.google.common.base.Preconditions;
import org.bukkit.Location;

import java.util.Collections;
import java.util.List;

public class CmdSetWarp implements IPermissibleCommand {

    @Override
    public List<String> getAliases() {
        return Collections.singletonList("setwarp");
    }

    @Override
    public String getPermission() {
        return "superior.island.setwarp";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        StringBuilder usage = new StringBuilder("setwarp <")
                .append(Message.COMMAND_ARGUMENT_WARP_NAME.getMessage(locale)).append(">");

        if (BuiltinModules.WARPS.getConfiguration().isCategoriesEnabled()) {
            usage.append(" [").append(Message.COMMAND_ARGUMENT_WARP_CATEGORY.getMessage(locale)).append("]");
        }

        usage.append(" [").append(Message.COMMAND_ARGUMENT_WARP_PRIVATE.getMessage(locale))
                .append("[true/false]]");

        return usage.toString();
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_SET_WARP.getMessage(locale);
    }

    @Override
    public int getMinArgs() {
        return 2;
    }

    @Override
    public int getMaxArgs() {
        return BuiltinModules.WARPS.getConfiguration().isCategoriesEnabled() ? 4 : 3;
    }

    @Override
    public boolean canBeExecutedByConsole() {
        return false;
    }

    @Override
    public IslandPrivilege getPrivilege() {
        return IslandPrivileges.SET_WARP;
    }

    @Override
    public Message getPermissionLackMessage() {
        return Message.NO_SET_WARP_PERMISSION;
    }

    @Override
    public void execute(SuperiorSkyblockPlugin plugin, SuperiorPlayer superiorPlayer, Island island, String[] args) {
        int warpsLimit = island.getWarpsLimit();

        if (warpsLimit >= 0 && island.getIslandWarps().size() >= warpsLimit) {
            Message.NO_MORE_WARPS.send(superiorPlayer);
            return;
        }

        String warpName = Formatters.STRIP_COLOR_FORMATTER.format(args[1]);

        if (!IslandNames.isValidWarpName(superiorPlayer, island, warpName)) {
            return;
        }

        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            if (!island.isInsideRange(superiorPlayer.getLocation(wrapper.getHandle()))) {
                Message.WARP_SET_OUTSIDE_ISLAND.send(superiorPlayer);
                return;
            }
        }

        String categoryName = null;

        if (args.length >= 3 && BuiltinModules.WARPS.getConfiguration().isCategoriesEnabled()) {
            categoryName = Formatters.STRIP_COLOR_FORMATTER.format(args[2]);

            if (!IslandNames.isValidWarpCategoryName(superiorPlayer, categoryName)) {
                return;
            }

            if (island.getWarpCategory(categoryName) == null &&
                    !PluginEventsFactory.callIslandCreateWarpCategoryEvent(island, superiorPlayer, categoryName)) {
                return;
            }
        }

        WarpCategory warpCategory = categoryName == null ? null : island.createWarpCategory(categoryName);

        boolean privateFlag = BuiltinModules.WARPS.getConfiguration().isPrivateByDefault();

        if ((args.length == 4 && BuiltinModules.WARPS.getConfiguration().isCategoriesEnabled()) || args.length == 3) {
            privateFlag = Boolean.parseBoolean(args[args.length - 1]);
        }

        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            Location warpLocation = superiorPlayer.getLocation(wrapper.getHandle());

            Preconditions.checkState(warpLocation != null, "Null location for a warp.");

            if (!PluginEventsFactory.callIslandCreateWarpEvent(island, superiorPlayer, warpName, warpLocation, warpCategory)) {
                return;
            }

            IslandWarp islandWarp = island.createWarp(warpName, warpLocation, warpCategory);
            islandWarp.setPrivateFlag(privateFlag);

            Message.WARP_SET.send(superiorPlayer, Formatters.LOCATION_FORMATTER.format(warpLocation), islandWarp.getName());
        }
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, SuperiorPlayer superiorPlayer, Island island, String[] args) {
        if (args.length == 3 && BuiltinModules.WARPS.getConfiguration().isCategoriesEnabled()) {
            return CommandTabCompletes.getWarpCategories(island, args[2]);
        } else if ((args.length == 4 && BuiltinModules.WARPS.getConfiguration().isCategoriesEnabled()) || args.length == 3) {
            return CommandTabCompletes.getCustomComplete(args[args.length - 1], "true", "false");
        } else {
            return Collections.emptyList();
        }
    }

}
