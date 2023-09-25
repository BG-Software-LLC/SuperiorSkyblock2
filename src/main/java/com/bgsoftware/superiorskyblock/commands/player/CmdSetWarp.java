package com.bgsoftware.superiorskyblock.commands.player;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.arguments.CommandArgument;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.island.warps.WarpCategory;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.InternalPermissibleCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArgumentsBuilder;
import com.bgsoftware.superiorskyblock.commands.arguments.types.StringArgumentType;
import com.bgsoftware.superiorskyblock.commands.context.IslandCommandContext;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.island.IslandUtils;
import com.bgsoftware.superiorskyblock.island.privilege.IslandPrivileges;
import com.google.common.base.Preconditions;
import org.bukkit.Location;

import java.util.Collections;
import java.util.List;

public class CmdSetWarp implements InternalPermissibleCommand {

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
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_SET_WARP.getMessage(locale);
    }

    @Override
    public List<CommandArgument<?>> getArguments() {
        CommandArgumentsBuilder builder = new CommandArgumentsBuilder()
                .add(CommandArguments.required("island-warp-name", StringArgumentType.INSTANCE, Message.COMMAND_ARGUMENT_WARP_NAME));

        if (plugin.getSettings().isWarpCategories())
            builder.add(CommandArguments.optional("warp-category-name", StringArgumentType.INSTANCE, Message.COMMAND_ARGUMENT_WARP_CATEGORY));

        return builder.build();
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
    public void execute(SuperiorSkyblockPlugin plugin, IslandCommandContext context) {
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(context.getDispatcher());
        Island island = context.getIsland();

        if (island.getIslandWarps().size() >= island.getWarpsLimit()) {
            Message.NO_MORE_WARPS.send(superiorPlayer);
            return;
        }

        String warpName = context.getRequiredArgument("island-warp-name", String.class);

        if (warpName.isEmpty()) {
            Message.WARP_ILLEGAL_NAME.send(superiorPlayer);
            return;
        }

        if (!IslandUtils.isWarpNameLengthValid(warpName)) {
            Message.WARP_NAME_TOO_LONG.send(superiorPlayer);
            return;
        }

        if (island.getWarp(warpName) != null) {
            Message.WARP_ALREADY_EXIST.send(superiorPlayer);
            return;
        }

        if (!island.isInsideRange(superiorPlayer.getLocation())) {
            Message.SET_WARP_OUTSIDE.send(superiorPlayer);
            return;
        }

        String categoryName = context.getOptionalArgument("warp-category-name", String.class).orElse(null);

        if (categoryName != null) {
            if (categoryName.isEmpty()) {
                Message.WARP_CATEGORY_ILLEGAL_NAME.send(superiorPlayer);
                return;
            }

            if (!IslandUtils.isWarpNameLengthValid(categoryName)) {
                Message.WARP_CATEGORY_NAME_TOO_LONG.send(superiorPlayer);
                return;
            }

            if (island.getWarpCategory(categoryName) == null &&
                    !plugin.getEventsBus().callIslandCreateWarpCategoryEvent(superiorPlayer, island, categoryName))
                return;
        }

        WarpCategory warpCategory = categoryName == null ? null : island.createWarpCategory(categoryName);

        Location warpLocation = superiorPlayer.getLocation();

        Preconditions.checkState(warpLocation != null, "Null location for a warp.");

        if (!plugin.getEventsBus().callIslandCreateWarpEvent(superiorPlayer, island, warpName, warpLocation, warpCategory))
            return;

        island.createWarp(warpName, warpLocation, warpCategory);

        Message.SET_WARP.send(superiorPlayer, Formatters.LOCATION_FORMATTER.format(warpLocation));
    }

}
