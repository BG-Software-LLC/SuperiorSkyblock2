package com.bgsoftware.superiorskyblock.listener;

import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.events.IslandCreateEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandEnterEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandLeaveEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandTransferEvent;
import com.bgsoftware.superiorskyblock.api.events.IslandWorthCalculatedEvent;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.core.Singleton;
import com.bgsoftware.superiorskyblock.core.key.ConstantKeys;
import com.bgsoftware.superiorskyblock.world.BukkitItems;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class FeaturesListener implements Listener {

    private final Map<Class<? extends Event>, EventMethods> CACHED_EVENT_METHODS = new HashMap<>();

    private final SuperiorSkyblockPlugin plugin;
    private final Singleton<ProtectionListener> protectionListener;

    public FeaturesListener(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
        this.protectionListener = plugin.getListener(ProtectionListener.class);
    }

    /* EVENT COMMANDS */

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onIslandEvent(IslandEvent event) {
        List<String> commands = plugin.getSettings().getEventCommands().get(event.getClass().getSimpleName().toLowerCase(Locale.ENGLISH));

        if (commands == null)
            return;

        EventMethods eventMethods = CACHED_EVENT_METHODS.computeIfAbsent(event.getClass(), EventMethods::new);

        Map<String, String> placeholdersReplaces = new HashMap<>();

        placeholdersReplaces.put("%island%", event.getIsland().getName());
        eventMethods.getPlayer(event).ifPresent(playerName -> placeholdersReplaces.put("%player%", playerName));
        eventMethods.getTarget(event).ifPresent(targetName -> placeholdersReplaces.put("%target%", targetName));

        if (event instanceof IslandCreateEvent) {
            placeholdersReplaces.put("%schematic%", ((IslandCreateEvent) event).getSchematic());
        } else if (event instanceof IslandEnterEvent) {
            placeholdersReplaces.put("%enter-cause%", ((IslandEnterEvent) event).getCause().name());
        } else if (event instanceof IslandLeaveEvent) {
            placeholdersReplaces.put("%leave-cause%", ((IslandLeaveEvent) event).getCause().name());
        } else if (event instanceof IslandTransferEvent) {
            placeholdersReplaces.put("%old-owner%", ((IslandTransferEvent) event).getOldOwner().getName());
            placeholdersReplaces.put("%new-owner%", ((IslandTransferEvent) event).getNewOwner().getName());
        } else if (event instanceof IslandWorthCalculatedEvent) {
            placeholdersReplaces.put("%worth%", ((IslandWorthCalculatedEvent) event).getWorth().toString());
            placeholdersReplaces.put("%level%", ((IslandWorthCalculatedEvent) event).getLevel().toString());
        }

        for (String command : commands) {
            for (Map.Entry<String, String> replaceEntry : placeholdersReplaces.entrySet())
                command = command.replace(replaceEntry.getKey(), replaceEntry.getValue());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        }
    }

    /* OBSIDIAN TO LAVA */

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void onObsidianClick(PlayerInteractEvent e) {
        if (!plugin.getSettings().isObsidianToLava() || e.getItem() == null || e.getClickedBlock() == null ||
                e.getItem().getType() != Material.BUCKET || e.getClickedBlock().getType() != Material.OBSIDIAN)
            return;


        if (plugin.getStackedBlocks().getStackedBlockAmount(e.getClickedBlock()) != 1)
            return;

        if (this.protectionListener.get().preventBlockBreak(e.getClickedBlock(), e.getPlayer(),
                ProtectionListener.Flag.PREVENT_OUTSIDE_ISLANDS))
            return;

        Island island = plugin.getGrid().getIslandAt(e.getClickedBlock().getLocation());

        // Prevent outside island is set above.
        assert island != null;

        e.setCancelled(true);

        ItemStack inHandItem = e.getItem().clone();
        inHandItem.setAmount(inHandItem.getAmount() - 1);
        BukkitItems.setItem(inHandItem.getAmount() == 0 ? new ItemStack(Material.AIR) : inHandItem, e, e.getPlayer());

        BukkitItems.addItem(new ItemStack(Material.LAVA_BUCKET), e.getPlayer().getInventory(),
                e.getPlayer().getLocation());

        island.handleBlockBreak(ConstantKeys.OBSIDIAN, 1);

        e.getClickedBlock().setType(Material.AIR);
    }

    /* VISITORS BLOCKED COMMANDS */

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerCommand(PlayerCommandPreprocessEvent e) {
        if (plugin.getSettings().getBlockedVisitorsCommands().isEmpty())
            return;

        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());

        if (superiorPlayer.hasBypassModeEnabled())
            return;

        Island island = plugin.getGrid().getIslandAt(e.getPlayer().getLocation());

        if (island == null || island.isSpawn() || !island.isVisitor(superiorPlayer, true))
            return;

        String[] message = e.getMessage().toLowerCase(Locale.ENGLISH).split(" ");

        String commandLabel = message[0].toCharArray()[0] == '/' ? message[0].substring(1) : message[0];

        if (plugin.getSettings().getBlockedVisitorsCommands().stream().anyMatch(commandLabel::contains)) {
            e.setCancelled(true);
            Message.VISITOR_BLOCK_COMMAND.send(superiorPlayer);
        }
    }

    /* INTERNAL */

    private static class EventMethods {

        private final ReflectMethod<SuperiorPlayer> getPlayerMethod;
        private final ReflectMethod<SuperiorPlayer> getTargetMethod;

        EventMethods(Class<? extends Event> eventClass) {
            getPlayerMethod = new ReflectMethod<>(eventClass, "getPlayer");
            getTargetMethod = new ReflectMethod<>(eventClass, "getTarget");
        }

        Optional<String> getPlayer(Event event) {
            return getPlayerMethod.isValid() ? Optional.ofNullable(getPlayerMethod.invoke(event)).map(SuperiorPlayer::getName) : Optional.empty();
        }

        Optional<String> getTarget(Event event) {
            return getTargetMethod.isValid() ? Optional.ofNullable(getTargetMethod.invoke(event)).map(SuperiorPlayer::getName) : Optional.empty();
        }

    }

}
