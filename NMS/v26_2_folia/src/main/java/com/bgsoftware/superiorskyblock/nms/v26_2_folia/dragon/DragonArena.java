package com.bgsoftware.superiorskyblock.nms.v26_2_folia.dragon;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.UUID;
import java.util.HashSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;

public final class DragonArena implements AutoCloseable {
    private static final Map<TicketKey, Ticket> SHARED_TICKETS = new ConcurrentHashMap<>();
    private final SuperiorSkyblockPlugin plugin;
    private final World world;
    private final int centerX;
    private final int centerZ;
    private final Set<Long> tickets = new HashSet<>();
    private final AtomicBoolean closed = new AtomicBoolean();

    private DragonArena(SuperiorSkyblockPlugin plugin, Location portal) {
        this.plugin = plugin;
        this.world = portal.getWorld();
        this.centerX = portal.getBlockX() >> 4;
        this.centerZ = portal.getBlockZ() >> 4;
    }

    public static CompletableFuture<DragonArena> load(SuperiorSkyblockPlugin plugin, Location portal, BooleanSupplier active) {
        DragonArena arena = new DragonArena(plugin, portal);
        List<CompletableFuture<Void>> chunks = new ArrayList<>();
        for (int x = arena.centerX - 8; x <= arena.centerX + 8; ++x) {
            for (int z = arena.centerZ - 8; z <= arena.centerZ + 8; ++z) {
                int chunkX = x;
                int chunkZ = z;
                chunks.add(arena.world.getChunkAtAsync(x, z, true).thenCompose(chunk ->
                        BukkitExecutor.submit(arena.location(chunkX, chunkZ), () -> {
                            synchronized (arena.tickets) {
                                if (!active.getAsBoolean() || arena.closed.get())
                                    throw new IllegalStateException("Dragon arena request was cancelled");
                                arena.tickets.add(pack(chunkX, chunkZ));
                            }
                            TicketKey key = new TicketKey(arena.world.getUID(), chunkX, chunkZ);
                            Ticket ticket = SHARED_TICKETS.get(key);
                            if (ticket == null) {
                                ticket = new Ticket(arena.world.addPluginChunkTicket(chunkX, chunkZ, plugin));
                                SHARED_TICKETS.put(key, ticket);
                            }
                            ++ticket.references;
                            return null;
                        })));
            }
        }
        return CompletableFuture.allOf(chunks.toArray(CompletableFuture[]::new)).handle((ignored, error) -> {
            if (error != null) {
                arena.close();
                throw new CompletionException(error);
            }
            return arena;
        });
    }

    public boolean isOwned() {
        return Bukkit.isOwnedByCurrentRegion(world, centerX, centerZ, 8);
    }

    @Override
    public void close() {
        if (!closed.compareAndSet(false, true))
            return;
        List<Long> snapshot;
        synchronized (tickets) {
            snapshot = List.copyOf(tickets);
            tickets.clear();
        }
        for (long ticket : snapshot) {
            int x = (int) (ticket >> 32);
            int z = (int) ticket;
            if (plugin.isEnabled())
                BukkitExecutor.sync(location(x, z), () -> release(x, z));
        }
    }

    private void release(int x, int z) {
        TicketKey key = new TicketKey(world.getUID(), x, z);
        Ticket ticket = SHARED_TICKETS.get(key);
        if (ticket != null && --ticket.references == 0) {
            SHARED_TICKETS.remove(key, ticket);
            if (ticket.acquired)
                world.removePluginChunkTicket(x, z, plugin);
        }
    }

    private record TicketKey(UUID world, int x, int z) {}

    private static final class Ticket {
        private final boolean acquired;
        private int references;

        private Ticket(boolean acquired) {
            this.acquired = acquired;
        }
    }

    private Location location(int x, int z) {
        return new Location(world, (x << 4) + 8, world.getMinHeight(), (z << 4) + 8);
    }

    private static long pack(int x, int z) {
        return (long) x << 32 | z & 0xffffffffL;
    }
}
