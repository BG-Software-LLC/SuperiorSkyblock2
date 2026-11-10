package com.bgsoftware.superiorskyblock.nms.v26_2_folia.dragon;

import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.persistence.PersistentDataType;
import com.bgsoftware.superiorskyblock.api.world.Dimension;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.nms.v26_2.dragon.SpikesCache;
import com.bgsoftware.superiorskyblock.service.dragon.IslandEndCrystals;
import net.minecraft.core.BlockPos;
import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.dimension.end.DragonRespawnStage;
import net.minecraft.world.level.dimension.end.EnderDragonFight;
import net.minecraft.world.level.levelgen.feature.EndSpikeFeature;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.phys.AABB;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftEnderDragon;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.scheduler.BukkitTask;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public final class IslandDragonFight extends EnderDragonFight {
    public static final String PREVIOUSLY_KILLED_TAG = "superiorskyblock.previously_killed";

    private static final int DRAGON_SPAWN_HEIGHT_ABOVE_ISLAND = 64;
    private static final int SUMMON_PILLAR_TICKS = 40;
    private static final ReflectField<UUID> DRAGON_UUID = new ReflectField<>(EnderDragonFight.class, UUID.class,
            Modifier.PRIVATE, 1);
    private static final ReflectField<Boolean> DRAGON_KILLED = new ReflectField<>(EnderDragonFight.class, boolean.class,
            Modifier.PRIVATE, 1);
    private final SuperiorSkyblockPlugin plugin;
    private final Island island;
    private final Location portal;
    private final DragonArena arena;
    private final Consumer<IslandDragonFight> onIdle;
    private final String stateKey;
    private final List<EndSpikeFeature.EndSpike> spikes;
    private final boolean schematicSpikes;
    private volatile org.bukkit.entity.EnderDragon dragon;
    private volatile Node[] navigation;
    private volatile HealthSnapshot health = new HealthSnapshot(1.0F, Component.translatable("entity.minecraft.ender_dragon"));
    private volatile boolean closed;
    private volatile long lastPlayerActivity = System.nanoTime();
    private volatile UUID dragonId;
    private volatile boolean previouslyKilled;
    private volatile boolean respawning;
    private volatile String savedState;
    private boolean killed;
    private int stageTime;
    private int ticks;
    private BukkitTask task;
    private boolean scanningPlayers;
    private int activePlayers;
    private volatile int crystalCount;

    public IslandDragonFight(SuperiorSkyblockPlugin plugin, Island island, Dimension dimension, Location portal,
                             DragonArena arena, boolean fresh, UUID existing, boolean previouslyKilled,
                             Consumer<IslandDragonFight> onIdle) {
        this(plugin, island, dimension, portal, arena, DragonFightState.key(dimension),
                DragonFightState.load(island, DragonFightState.key(dimension), fresh, existing, previouslyKilled), onIdle);
    }

    private IslandDragonFight(SuperiorSkyblockPlugin plugin, Island island, Dimension dimension, Location portal, DragonArena arena,
                              String stateKey, DragonFightState state, Consumer<IslandDragonFight> onIdle) {
        super(false, state.killed(), state.previouslyKilled(),
                state.phase() == null ? Optional.empty() : Optional.of(DragonRespawnStage.valueOf(state.phase())),
                state.time(), Optional.ofNullable(state.dragon()),
                Optional.of(new BlockPos(portal.getBlockX(), state.portalY() == null ? portal.getBlockY() : state.portalY(),
                        portal.getBlockZ())),
                state.gateways(), state.crystals().stream().map(EntityReference::<EndCrystal>of).toList());
        this.plugin = plugin;
        this.island = island;
        this.portal = portal.clone();
        this.portal.setY(this.exitPortalLocation.getY());
        this.arena = arena;
        this.onIdle = onIdle;
        this.stateKey = stateKey;
        this.killed = state.killed();
        this.dragonId = state.dragon();
        this.previouslyKilled = state.previouslyKilled();
        this.stageTime = state.time();
        this.respawning = this.respawnStage != null;
        ServerLevel level = ((CraftWorld) portal.getWorld()).getHandle();
        init(level, level.getSeed(), new BlockPos(portal.getBlockX(), 0, portal.getBlockZ()));
        List<EndSpikeFeature.EndSpike> crystalSpikes = SpikesCache.createCrystalSpikes(
                IslandEndCrystals.getSchematicCrystals(island, dimension), this.origin);
        this.schematicSpikes = crystalSpikes != null;
        this.spikes = schematicSpikes ? crystalSpikes : EndSpikeFeature.getSpikesForLevel(level).stream().map(spike ->
                new EndSpikeFeature.EndSpike(spike.getCenterX() + this.origin.getX(),
                        spike.getCenterZ() + this.origin.getZ(), spike.getRadius(), spike.getHeight(), spike.isGuarded())).toList();
        this.savedState = island.getPersistentDataContainer().get(stateKey, PersistentDataType.STRING);
    }

    public static CompletableFuture<DragonArena> loadArena(SuperiorSkyblockPlugin plugin, Location portal, BooleanSupplier active) {
        return DragonArena.load(plugin, portal, active);
    }

    public void start(boolean fresh, org.bukkit.entity.EnderDragon existing, boolean respawn) {
        if (closed)
            return;
        if (!arena.isOwned()) {
            BukkitExecutor.sync(portal, () -> start(fresh, existing, respawn), 1L);
            return;
        }
        navigation = DragonNavigation.create(level, this.exitPortalLocation, schematicSpikes ? List.of() : spikes);
        task = BukkitExecutor.timer(portal, this::tick, 1L);
        if (existing != null) {
            BukkitExecutor.ensureMain(existing, () -> attach(((CraftEnderDragon) existing).getHandle()));
        } else if (dragonUUID() != null && level.getEntity(dragonUUID()) instanceof EnderDragon loaded) {
            attach(loaded);
        }
        if (fresh || existing == null && !killed && dragon == null && this.respawnStage == null) {
            this.respawnStage = DragonRespawnStage.START;
            setRespawnStage(DragonRespawnStage.END);
        } else if (respawn && this.respawnStage == null) {
            requestRespawn(null);
        }
        if (!respawning)
            resetSpikeCrystals();
        persist();
    }

    @Override
    public UUID dragonUUID() {
        return dragonId;
    }

    @Override
    public boolean hasPreviouslyKilledDragon() {
        return previouslyKilled;
    }

    public org.bukkit.entity.EnderDragon getDragon() {
        return dragon;
    }

    public boolean isRespawning() {
        return respawning;
    }

    public void wake() {
        lastPlayerActivity = System.nanoTime();
    }

    public void attach(EnderDragon entity) {
        if (closed)
            return;
        Node[] nodes = this.navigation;
        if (nodes == null) {
            BukkitExecutor.sync(entity.getBukkitEntity(), () -> attach(entity), 1L);
            return;
        }
        DragonNavigation.install(entity, nodes);
        entity.setDragonFight(this);
        entity.setFightOrigin(this.origin);
        entity.setPodium(this.exitPortalLocation);
        org.bukkit.entity.EnderDragon restored = (org.bukkit.entity.EnderDragon) entity.getBukkitEntity();
        DragonNavigation.restore(restored, plugin);
        if (dragonUUID() == null || dragonUUID().equals(entity.getUUID())) {
            this.dragon = restored;
            if (previouslyKilled)
                restored.addScoreboardTag(PREVIOUSLY_KILLED_TAG);
            updateDragon(entity);
        }
    }

    public void replaceDragon(org.bukkit.entity.EnderDragon replacement) {
        BukkitExecutor.ensureMain(portal, () -> {
            if (closed || replacement.getUniqueId().equals(dragonId))
                return;
            if (!arena.isOwned()) {
                BukkitExecutor.sync(portal, () -> replaceDragon(replacement), 1L);
                return;
            }
            if (!BukkitExecutor.isOwned(replacement) || !replacement.isValid() || replacement.getWorld() != portal.getWorld() ||
                    !island.isInside(replacement.getLocation()))
                return;

            if (this.respawnStage != null) {
                for (EntityReference<EndCrystal> reference : this.respawnCrystals) {
                    EndCrystal crystal = reference.getEntity(level, EndCrystal.class);
                    if (crystal != null)
                        crystal.getBukkitEntity().remove();
                }
                this.respawnCrystals = List.of();
                this.respawnStage = null;
                this.stageTime = 0;
                this.respawning = false;
                resetSpikeCrystals();
            } else if (killed) {
                spawnExitPortal(false);
            }

            org.bukkit.entity.EnderDragon previous = this.dragon;
            DRAGON_UUID.set(this, replacement.getUniqueId());
            DRAGON_KILLED.set(this, false);
            this.dragonId = replacement.getUniqueId();
            this.killed = false;
            this.dragon = replacement;
            attach(((CraftEnderDragon) replacement).getHandle());
            if (previous != null && previous != replacement)
                BukkitExecutor.ensureMain(previous, previous::remove);
            persist();
        });
    }

    @Override
    public void tick() {
        if (closed || !arena.isOwned())
            return;
        this.dragonEvent.setVisible(!killed);
        HealthSnapshot snapshot = this.health;
        this.dragonEvent.setProgress(snapshot.progress());
        this.dragonEvent.setName(snapshot.name());
        if (++ticks % 100 == 0) {
            this.crystalCount = getSpikeCrystals().size();
            persist();
            if (!respawning && (killed || activePlayers == 0 && !scanningPlayers && health.progress() > 0.0F
                    && System.nanoTime() - lastPlayerActivity >= 10_000_000_000L)) {
                onIdle.accept(this);
                return;
            }
        }
        if (ticks % 20 == 0)
            updatePlayers();
        if (this.respawnStage != null) {
            List<EndCrystal> crystals = this.respawnCrystals.stream()
                    .map(reference -> reference.getEntity(level, EndCrystal.class))
                    .filter(Objects::nonNull).toList();
            if (crystals.size() != this.respawnCrystals.size() || crystals.isEmpty()) {
                abortRespawn();
                return;
            }
            IslandDragonRespawn.tick(this, crystals, stageTime++);
        }
    }

    @Override
    public void updateDragon(EnderDragon entity) {
        if (dragonUUID() != null && dragonUUID().equals(entity.getUUID())) {
            this.health = new HealthSnapshot(Math.max(0.0F, Math.min(1.0F, entity.getHealth() / entity.getMaxHealth())),
                    entity.hasCustomName() ? entity.getDisplayName() : Component.translatable("entity.minecraft.ender_dragon"));
        }
    }

    @Override
    public void setDragonKilled(EnderDragon entity) {
        if (!entity.getUUID().equals(dragonUUID()))
            return;
        BukkitExecutor.ensureMain(portal, () -> {
            if (closed || killed || !entity.getUUID().equals(dragonUUID()))
                return;
            if (!arena.isOwned()) {
                BukkitExecutor.sync(portal, () -> setDragonKilled(entity), 1L);
                return;
            }
            super.setDragonKilled(entity);
            this.killed = true;
            this.previouslyKilled = true;
            this.dragon = null;
            persist();
        });
    }

    @Override
    public boolean spawnNewGateway() {
        if (this.gateways.isEmpty())
            return false;
        int index = this.gateways.remove(this.gateways.size() - 1);
        double angle = 2.0 * (-Math.PI + Math.PI / 20 * index);
        super.spawnNewGateway(new BlockPos(this.origin.getX() + Mth.floor(96.0 * Math.cos(angle)),
                75, this.origin.getZ() + Mth.floor(96.0 * Math.sin(angle))));
        setDirty();
        return true;
    }

    @Override
    public List<EndCrystal> getSpikeCrystals() {
        List<EndCrystal> crystals = new ArrayList<>();
        for (EndSpikeFeature.EndSpike spike : spikes) {
            if (!schematicSpikes || isSpikeOwned(spike))
                crystals.addAll(level.getEntitiesOfClass(EndCrystal.class, spike.getTopBoundingBox()));
        }
        return crystals;
    }

    @Override
    public int aliveCrystals() {
        return crystalCount;
    }

    @Override
    public void resetSpikeCrystals() {
        for (EndCrystal crystal : getSpikeCrystals()) {
            crystal.setInvulnerable(false);
            crystal.setBeamTarget(null);
            crystal.getBukkitEntity().getPersistentDataContainer().remove(crystalKey(plugin));
        }
    }

    public BlockPos beamPosition() {
        return new BlockPos(origin.getX(), dragonSpawnY(), origin.getZ());
    }

    public void summonPillars(List<EndCrystal> crystals, int time) {
        int spikeIndex = time / SUMMON_PILLAR_TICKS;
        if (!schematicSpikes || spikeIndex >= spikes.size()) {
            setRespawnStage(DragonRespawnStage.SUMMONING_DRAGON);
            return;
        }

        EndSpikeFeature.EndSpike spike = spikes.get(spikeIndex);
        BlockPos position = new BlockPos(spike.getCenterX(), spike.getHeight() + 1, spike.getCenterZ());
        int pillarTime = time % SUMMON_PILLAR_TICKS;
        if (pillarTime == 0) {
            crystals.forEach(crystal -> crystal.setBeamTarget(position));
        } else if (pillarTime == SUMMON_PILLAR_TICKS - 1) {
            restoreSpikeCrystal(spike, position);
        }
    }

    private void restoreSpikeCrystal(EndSpikeFeature.EndSpike spike, BlockPos position) {
        if (!isSpikeOwned(spike) || !portal.getWorld().isChunkLoaded(position.getX() >> 4, position.getZ() >> 4) ||
                !portal.getWorld().getChunkAt(position.getX() >> 4, position.getZ() >> 4).isEntitiesLoaded() ||
                level.getBlockState(position.below()).isAir())
            return;

        List<EndCrystal> crystals = level.getEntitiesOfClass(EndCrystal.class, new AABB(position));
        if (crystals.isEmpty()) {
            EndCrystal crystal = new EndCrystal(level, position.getX() + 0.5D, position.getY(), position.getZ() + 0.5D);
            if (!level.addFreshEntity(crystal, CreatureSpawnEvent.SpawnReason.DEFAULT))
                return;
            crystals = List.of(crystal);
        }

        for (EndCrystal crystal : crystals) {
            crystal.setBeamTarget(beamPosition());
            crystal.setInvulnerable(true);
            crystal.getBukkitEntity().getPersistentDataContainer().set(crystalKey(plugin),
                    org.bukkit.persistence.PersistentDataType.STRING, island.getUniqueId().toString());
        }
        if (level.getBlockState(position).isAir())
            level.setBlockAndUpdate(position, BaseFireBlock.getState(level, position));
    }

    private boolean isSpikeOwned(EndSpikeFeature.EndSpike spike) {
        return Bukkit.isOwnedByCurrentRegion(portal.getWorld(), spike.getCenterX() >> 4, spike.getCenterZ() >> 4, 1);
    }

    private int dragonSpawnY() {
        return Math.min(portal.getBlockY() + DRAGON_SPAWN_HEIGHT_ABOVE_ISLAND, portal.getWorld().getMaxHeight() - 1);
    }

    public void requestRespawn(BlockPos placedCrystal) {
        BukkitExecutor.ensureMain(portal, () -> {
            if (closed)
                return;
            if (!arena.isOwned()) {
                BukkitExecutor.sync(portal, () -> requestRespawn(placedCrystal), 1L);
                return;
            }
            tryRespawn(placedCrystal);
        });
    }

    @Override
    public boolean tryRespawn(BlockPos placedCrystal) {
        if (closed || !arena.isOwned())
            return false;
        return super.tryRespawn(placedCrystal);
    }

    @Override
    public boolean respawnDragon(List<EndCrystal> crystals) {
        boolean started = super.respawnDragon(crystals);
        if (started) {
            for (EndCrystal crystal : crystals)
                crystal.getBukkitEntity().getPersistentDataContainer().set(crystalKey(plugin),
                        org.bukkit.persistence.PersistentDataType.STRING, island.getUniqueId().toString());
            this.stageTime = 0;
            this.respawning = true;
            persist();
        }
        return started;
    }

    @Override
    public void setRespawnStage(DragonRespawnStage stage) {
        this.stageTime = 0;
        List<ServerPlayer> players = stage == DragonRespawnStage.END ? List.copyOf(this.dragonEvent.getPlayers()) : List.of();
        if (stage == DragonRespawnStage.END)
            this.dragonEvent.removeAllPlayers();
        super.setRespawnStage(stage);
        this.dragonId = super.dragonUUID();
        this.respawning = this.respawnStage != null;
        if (stage == DragonRespawnStage.END) {
            this.killed = false;
            if (this.level.getEntity(dragonUUID()) instanceof EnderDragon spawned) {
                spawned.snapTo(this.origin.getX(), dragonSpawnY(), this.origin.getZ(), spawned.getYRot(), spawned.getXRot());
                attach(spawned);
                for (ServerPlayer player : players)
                    BukkitExecutor.ensureMain(player.getBukkitEntity(), () -> CriteriaTriggers.SUMMONED_ENTITY.trigger(player, spawned));
            }
        }
        persist();
    }

    public void crystalDestroyed(EndCrystal crystal, DamageSource source) {
        crystalDestroyed(crystal, source, crystal.getUUID(), crystal.blockPosition());
    }

    private void crystalDestroyed(EndCrystal crystal, DamageSource source, UUID uuid, BlockPos position) {
        BukkitExecutor.ensureMain(portal, () -> {
            if (closed)
                return;
            if (!arena.isOwned()) {
                BukkitExecutor.sync(portal, () -> crystalDestroyed(crystal, source, uuid, position), 1L);
                return;
            }
            if (this.respawnStage != null && this.respawnCrystals.stream().anyMatch(reference -> reference.getUUID().equals(uuid))) {
                abortRespawn();
            } else {
                this.crystalCount = getSpikeCrystals().size();
                org.bukkit.entity.EnderDragon current = this.dragon;
                if (current != null)
                    BukkitExecutor.ensureMain(current, () -> ((CraftEnderDragon) current).getHandle()
                            .onCrystalDestroyed(level, crystal, position, source));
            }
        });
    }

    private void abortRespawn() {
        for (EntityReference<EndCrystal> reference : this.respawnCrystals) {
            EndCrystal crystal = reference.getEntity(level, EndCrystal.class);
            if (crystal != null) {
                crystal.setBeamTarget(null);
                crystal.getBukkitEntity().getPersistentDataContainer().remove(crystalKey(plugin));
            }
        }
        this.respawnCrystals = List.of();
        this.respawnStage = null;
        this.respawning = false;
        this.stageTime = 0;
        resetSpikeCrystals();
        spawnExitPortal(true);
        persist();
    }

    public static NamespacedKey crystalKey(SuperiorSkyblockPlugin plugin) {
        return new NamespacedKey(plugin, "island_dragon_crystal");
    }

    private void updatePlayers() {
        if (scanningPlayers)
            return;
        scanningPlayers = true;
        List<CompletableFuture<ServerPlayer>> checks = new ArrayList<>();
        for (SuperiorPlayer superiorPlayer : island.getAllPlayersInside()) {
            Player player = superiorPlayer.asPlayer();
            if (player != null) {
                CompletableFuture<ServerPlayer> check = new CompletableFuture<>();
                checks.add(check);
                plugin.getTaskScheduler().entity(player, () -> {
                    try {
                        check.complete(player.getWorld() == portal.getWorld() && island.isInside(player.getLocation())
                                ? ((CraftPlayer) player).getHandle() : null);
                    } catch (RuntimeException error) {
                        check.complete(null);
                    }
                }, () -> check.complete(null), 1L, 0L);
            }
        }
        CompletableFuture.allOf(checks.toArray(CompletableFuture[]::new)).thenRun(() -> BukkitExecutor.sync(portal, () -> {
            scanningPlayers = false;
            if (closed)
                return;
            Set<ServerPlayer> players = new HashSet<>();
            for (CompletableFuture<ServerPlayer> check : checks) {
                ServerPlayer player = check.getNow(null);
                if (player != null)
                    players.add(player);
            }
            activePlayers = players.size();
            if (!players.isEmpty())
                wake();
            for (ServerPlayer player : List.copyOf(this.dragonEvent.getPlayers())) {
                if (!players.contains(player))
                    this.dragonEvent.removePlayer(player);
            }
            players.forEach(this.dragonEvent::addPlayer);
        }));
    }

    private void persist() {
        DragonFightState state = new DragonFightState(killed, hasPreviouslyKilledDragon(), dragonUUID(),
                List.copyOf(gateways), respawnStage == null ? null : respawnStage.name(), stageTime,
                this.respawnCrystals.stream().map(EntityReference::getUUID).toList(), this.exitPortalLocation.getY());
        String encoded = state.serialize();
        if (!encoded.equals(savedState)) {
            savedState = encoded;
            island.getPersistentDataContainer().put(stateKey, PersistentDataType.STRING, encoded);
        }
    }

    public void close(boolean removeDragon) {
        this.closed = true;
        if (this.task != null)
            this.task.cancel();
        if (plugin.isEnabled()) {
            BukkitExecutor.sync(portal, () -> {
                this.dragonEvent.removeAllPlayers();
                if (arena.isOwned()) {
                    if (removeDragon)
                        resetSpikeCrystals();
                }
                this.arena.close();
            });
            org.bukkit.entity.EnderDragon current = this.dragon;
            if (removeDragon && current != null)
                BukkitExecutor.ensureMain(current, current::remove);
        } else {
            this.arena.close();
        }
    }

    private record HealthSnapshot(float progress, Component name) {}
}
