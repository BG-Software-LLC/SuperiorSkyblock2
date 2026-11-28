package com.bgsoftware.superiorskyblock.module.missions.commands;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.missions.MissionCategory;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.CommandTabCompletes;
import com.bgsoftware.superiorskyblock.commands.ISuperiorCommand;
import com.bgsoftware.superiorskyblock.commands.arguments.CommandArguments;
import com.bgsoftware.superiorskyblock.core.menu.view.MenuViewWrapper;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CmdMissions implements ISuperiorCommand {

    @Override
    public List<String> getAliases() {
        return Arrays.asList("missions", "challenges");
    }

    @Override
    public String getPermission() {
        return "superior.island.missions";
    }

    @Override
    public String getUsage(java.util.Locale locale) {
        return "missions [" + Message.COMMAND_ARGUMENT_MISSION_CATEGORY.getMessage(locale) + "]";
    }

    @Override
    public String getDescription(java.util.Locale locale) {
        return Message.COMMAND_DESCRIPTION_MISSIONS.getMessage(locale);
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

        if (args.length == 1) {
            if (!superiorPlayer.hasIsland() && !plugin.getMissions().hasAnyPlayerMissionCategories()) {
                Message.INVALID_ISLAND.send(superiorPlayer);
                return;
            }

            plugin.getMenus().openMissions(superiorPlayer, MenuViewWrapper.fromView(superiorPlayer.getOpenedView()));
        } else {
            MissionCategory missionCategory = CommandArguments.getMissionCategory(plugin, sender, args[1]);

            if (missionCategory == null)
                return;

            if (!superiorPlayer.hasIsland() && !plugin.getMissions().isPlayerMissionCategory(missionCategory)) {
                Message.INVALID_ISLAND.send(superiorPlayer);
                return;
            }

            plugin.getMenus().openMissionsCategory(superiorPlayer,
                    MenuViewWrapper.fromView(superiorPlayer.getOpenedView()), missionCategory);
        }
    }

    @Override
    public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
        if (args.length == 2) {
            boolean hasIsland = plugin.getPlayers().getSuperiorPlayer(sender).hasIsland();
            return CommandTabCompletes.getMissionCategories(plugin, args[1], category ->
                    hasIsland || plugin.getMissions().isPlayerMissionCategory(category));
        }

        return Collections.emptyList();
    }

}
