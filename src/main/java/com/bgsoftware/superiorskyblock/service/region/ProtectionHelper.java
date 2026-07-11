package com.bgsoftware.superiorskyblock.service.region;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.SuperiorCommand;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.service.region.InteractionResult;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.CommandsHelper;
import com.bgsoftware.superiorskyblock.core.ObjectsPools;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.player.PlayerLocales;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Locale;

public class ProtectionHelper {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private ProtectionHelper() {

    }

    public static boolean shouldPreventInteraction(InteractionResult interactionResult,
                                                   @Nullable SuperiorPlayer superiorPlayer, boolean sendMessages) {
        switch (interactionResult) {
            case ISLAND_RECALCULATE:
                if (sendMessages && superiorPlayer != null)
                    Message.ISLAND_BEING_CALCULATED.send(superiorPlayer);
                return true;
            case MISSING_PRIVILEGE:
                if (sendMessages && superiorPlayer != null)
                    sendProtectionMessage(superiorPlayer.asPlayer());
                return true;
            case OUTSIDE_ISLAND:
                if (sendMessages && superiorPlayer != null)
                    Message.BUILD_OUTSIDE_ISLAND.send(superiorPlayer);
                return true;
            case SUCCESS:
                return false;
        }

        throw new IllegalStateException("No handling for result " + interactionResult);
    }

    public static void sendProtectionMessage(CommandSender sender) {
        boolean isSpawnIsland;
        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            Island island = plugin.getGrid().getIslandAt(((Player) sender).getLocation(wrapper.getHandle()));
            isSpawnIsland = island != null && island.isSpawn();
        }

        Locale locale = PlayerLocales.getLocale(sender);

        if (!isSpawnIsland)
            Message.ISLAND_PROTECTED.send(sender, locale);
        else
            Message.SPAWN_PROTECTED.send(sender, locale);

        SuperiorCommand bypassCommand = plugin.getCommands().getAdminCommand("bypass");
        if (CommandsHelper.hasCommandAccess(bypassCommand, sender))
            if (!isSpawnIsland)
                Message.ISLAND_PROTECTED_OPPED.send(sender, locale);
            else
                Message.SPAWN_PROTECTED_OPPED.send(sender, locale);
    }

}
