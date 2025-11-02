package com.bgsoftware.superiorskyblock.commands.player;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
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
import com.google.common.base.Preconditions;
import org.bukkit.Location;

import java.util.Collections;
import java.util.List;

public class CmdSetWarp implements IPermissibleCommand {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

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

        if (plugin.getSettings().isWarpCategories())
            usage.append(" [").append(Message.COMMAND_ARGUMENT_WARP_CATEGORY.getMessage(locale)).append("]");

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
        return plugin.getSettings().isWarpCategories() ? 3 : 2;
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
        if (island.getIslandWarps().size() >= island.getWarpsLimit()) {
            Message.NO_MORE_WARPS.send(superiorPlayer);
            return;
        }

        String warpName = args[1];

        if (!IslandNames.isValidWarpName(superiorPlayer, island, warpName, true))
            return;

        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            if (!island.isInsideRange(superiorPlayer.getLocation(wrapper.getHandle()))) {
                Message.SET_WARP_OUTSIDE.send(superiorPlayer);
                return;
            }
        }

        String categoryName = null;

        if (args.length == 3) {
            categoryName = args[2];

            if (!IslandNames.isValidWarpCategoryName(superiorPlayer, categoryName))
                return;

            if (island.getWarpCategory(categoryName) == null &&
                    !PluginEventsFactory.callIslandCreateWarpCategoryEvent(island, superiorPlayer, categoryName))
                return;
        }

        WarpCategory warpCategory = categoryName == null ? null : island.createWarpCategory(categoryName);

        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            Location warpLocation = superiorPlayer.getLocation(wrapper.getHandle());

            Preconditions.checkState(warpLocation != null, "Null location for a warp.");

            if (!PluginEventsFactory.callIslandCreateWarpEvent(island, superiorPlayer, warpName, warpLocation, warpCategory))
                return;

            island.createWarp(warpName, warpLocation, warpCategory);

            Message.SET_WARP.send(superiorPlayer, Formatters.LOCATION_FORMATTER.format(warpLocation));
        }
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, SuperiorPlayer superiorPlayer, Island island, String[] args) {
        return args.length == 3 && plugin.getSettings().isWarpCategories() ?
                CommandTabCompletes.getWarpCategories(island, args[2]) : Collections.emptyList();
    }

}
