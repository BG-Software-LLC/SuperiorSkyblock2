package com.bgsoftware.superiorskyblock.commands;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.menu.MenuWarpCategories;
import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.utils.commands.CommandArguments;
import com.bgsoftware.superiorskyblock.utils.commands.CommandTabCompletes;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;

import org.bukkit.command.CommandSender;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CmdWarp implements ISuperiorCommand {

	@Override
	public List<String> getAliases() {
		return Collections.singletonList("warp");
	}

	@Override
	public String getPermission() {
		return "superior.island.warp";
	}

	@Override
	public String getUsage(java.util.Locale locale) {
		return "warp [" +
				Locale.COMMAND_ARGUMENT_PLAYER_NAME.getMessage(locale) + "/" +
				Locale.COMMAND_ARGUMENT_ISLAND_NAME.getMessage(locale) + "] [" +
                Locale.COMMAND_ARGUMENT_ISLAND_WARP.getMessage(locale) + "]";
	}

	@Override
	public String getDescription(java.util.Locale locale) {
		return Locale.COMMAND_DESCRIPTION_WARP.getMessage(locale);
	}

	@Override
	public int getMinArgs() {
		return 1;
	}

	@Override
	public int getMaxArgs() {
		return 3;
	}

	@Override
	public boolean canBeExecutedByConsole() {
		return false;
	}

	@Override
	public void execute(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
		Pair<Island, SuperiorPlayer> arguments = args.length == 1 ? CommandArguments.getSenderIsland(plugin, sender) :
			CommandArguments.getIsland(plugin, sender, args[1]);

		Island island = arguments.getKey();
		if(island == null)
			return;

		SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(sender);
		if (args.length == 3)
			for (String warp : island.getIslandWarps().keySet()) {
				if (!warp.equalsIgnoreCase(args[3]))
					continue;
				if(!superiorPlayer.hasBypassModeEnabled() && plugin.getSettings().chargeOnWarp > 0) {
					if(plugin.getProviders().getBalance(superiorPlayer).compareTo(BigDecimal.valueOf(plugin.getSettings().chargeOnWarp)) < 0){
						Locale.NOT_ENOUGH_MONEY_TO_WARP.send(superiorPlayer);
						return;
					}

					plugin.getProviders().withdrawMoney(superiorPlayer, plugin.getSettings().chargeOnWarp);
				}
				Executor.sync(() -> {
					island.warpPlayer(superiorPlayer, args[3]);
				}, 1L);

				return;
			}

		MenuWarpCategories.openInventory(superiorPlayer, null, island);
	}

	@Override
	public List<String> tabComplete(SuperiorSkyblockPlugin plugin, CommandSender sender, String[] args) {
		return args.length == 2 ? CommandTabCompletes.getPlayerIslandsExceptSender(plugin, sender, args[1],
				plugin.getSettings().tabCompleteHideVanished) : new ArrayList<>();
	}

}
