package com.bgsoftware.superiorskyblock.nms.v1_18_R2;

import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.nms.NMSDragonFight;
import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.BlockPosition;
import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.level.BossBattleServer;
import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.level.WorldServer;
import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EntityEnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonControllerManager;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonControllerPhase;
import net.minecraft.world.entity.boss.enderdragon.phases.IDragonController;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.pattern.ShapeDetector;
import net.minecraft.world.level.block.state.pattern.ShapeDetectorBlock;
import net.minecraft.world.level.block.state.pattern.ShapeDetectorBuilder;
import net.minecraft.world.level.block.state.predicate.BlockPredicate;
import net.minecraft.world.level.dimension.end.EnderDragonBattle;
import net.minecraft.world.level.levelgen.HeightMap;
import net.minecraft.world.level.levelgen.feature.WorldGenEndTrophy;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.craftbukkit.v1_18_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftEnderDragon;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;

import javax.annotation.Nullable;
import java.lang.reflect.Modifier;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

@SuppressWarnings({"unused", "NullableProblems"})
public final class NMSDragonFightImpl implements NMSDragonFight {

    private static final ReflectField<net.minecraft.core.BlockPosition> END_PODIUM_LOCATION = new ReflectField<net.minecraft.core.BlockPosition>(
            WorldGenEndTrophy.class, net.minecraft.core.BlockPosition.class,
            Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL, 1)
            .removeFinal();

    private static final ReflectField<EnderDragonBattle> DRAGON_BATTLE = new ReflectField<EnderDragonBattle>(
            EntityEnderDragon.class, EnderDragonBattle.class, Modifier.PRIVATE | Modifier.FINAL, 1)
            .removeFinal();

    private static final ReflectField<IDragonController> DRAGON_PHASE = new ReflectField<>(
            net.minecraft.world.entity.boss.enderdragon.phases.DragonControllerManager.class, IDragonController.class,
            Modifier.PRIVATE, 1);

    private static final Map<EnderDragon.Phase, Function<IslandEntityEnderDragon, IDragonController>>
            PHASE_FACTORY_MAP = new EnumMap<>(EnderDragon.Phase.class);

    static {
//        PHASE_FACTORY_MAP.put(EnderDragon.Phase.CIRCLING, IslandDragonControllerHold::new);
//        PHASE_FACTORY_MAP.put(EnderDragon.Phase.STRAFING, IslandDragonControllerStrafe::new);
//        PHASE_FACTORY_MAP.put(EnderDragon.Phase.FLY_TO_PORTAL, IslandDragonControllerLandingFly::new);
//        PHASE_FACTORY_MAP.put(EnderDragon.Phase.LAND_ON_PORTAL, IslandDragonControllerLanding::new);
//        PHASE_FACTORY_MAP.put(EnderDragon.Phase.LEAVE_PORTAL, IslandDragonControllerFly::new);
//        PHASE_FACTORY_MAP.put(EnderDragon.Phase.BREATH_ATTACK, IslandDragonControllerLandedFlame::new);
//        PHASE_FACTORY_MAP.put(EnderDragon.Phase.SEARCH_FOR_BREATH_ATTACK_TARGET, IslandDragonControllerLandedSearch::new);
//        PHASE_FACTORY_MAP.put(EnderDragon.Phase.ROAR_BEFORE_ATTACK, IslandDragonControllerLandedAttack::new);
//        PHASE_FACTORY_MAP.put(EnderDragon.Phase.CHARGE_PLAYER, IslandDragonControllerCharge::new);
//        PHASE_FACTORY_MAP.put(EnderDragon.Phase.DYING, IslandDragonControllerDying::new);
//        PHASE_FACTORY_MAP.put(EnderDragon.Phase.HOVER, IslandDragonControllerHover::new);
    }

    private final Map<UUID, EnderDragonBattle> activeBattles = new HashMap<>();

    @Override
    public void startDragonBattle(Island island, Location location) {
        org.bukkit.World bukkitWorld = location.getWorld();
        if (bukkitWorld != null) {
            WorldServer worldServer = new WorldServer(((CraftWorld) bukkitWorld).getHandle());
            IslandEnderDragonBattle islandEnderDragonBattle = new IslandEnderDragonBattle(island, worldServer, location);
            activeBattles.put(island.getUniqueId(), islandEnderDragonBattle);
        }
    }

    @Override
    public void removeDragonBattle(Island island) {
        EnderDragonBattle enderDragonBattle = activeBattles.remove(island.getUniqueId());
        if (enderDragonBattle instanceof IslandEnderDragonBattle islandEnderDragonBattle)
            islandEnderDragonBattle.removeBattlePlayers();
    }

    @Override
    public void tickBattles() {
        activeBattles.values().forEach(EnderDragonBattle::b);
    }

    @Override
    public void setDragonPhase(EnderDragon enderDragon, Object objectPhase) {
        EnderDragon.Phase phase = (EnderDragon.Phase) objectPhase;
        Entity entity = new Entity(((CraftEnderDragon) enderDragon).getHandle());

        if (!(entity.getHandle() instanceof IslandEntityEnderDragon entityEnderDragon))
            return;

        DragonControllerManager dragonControllerManager = entity.getDragonControllerManager().getHandle();

        //DRAGON_PHASE.set(dragonControllerManager, PHASE_FACTORY_MAP.get(phase).apply(entityEnderDragon));
    }

    @Override
    public void awardTheEndAchievement(Player player) {
        Advancement advancement = Bukkit.getAdvancement(NamespacedKey.minecraft("end/root"));
        if (advancement != null)
            player.getAdvancementProgress(advancement).awardCriteria("");
    }

    private static void runWithPodiumPosition(BlockPosition podiumPosition, Runnable runnable) {
        try {
            END_PODIUM_LOCATION.set(null, podiumPosition.getHandle());
            runnable.run();
        } finally {
            END_PODIUM_LOCATION.set(null, BlockPosition.ZERO.getHandle());
        }
    }

    private static <R> R runWithPodiumPosition(BlockPosition podiumPosition, Supplier<R> supplier) {
        try {
            END_PODIUM_LOCATION.set(null, podiumPosition.getHandle());
            return supplier.get();
        } finally {
            END_PODIUM_LOCATION.set(null, BlockPosition.ZERO.getHandle());
        }
    }

    private static final class IslandEntityEnderDragon extends EntityEnderDragon {

        private final BlockPosition islandBlockPosition;
        private final Entity entity = new Entity(this);

        IslandEntityEnderDragon(WorldServer worldServer, BlockPosition islandBlockPosition, IslandEnderDragonBattle islandEnderDragonBattle) {
            super(null, worldServer.getHandle());
            this.islandBlockPosition = islandBlockPosition;
        }

//        @Override
//        public float a(int segmentOffset, double[] segment1, double[] segment2) {
//            // getHeadPartYOffset(Integer, Double[], Double[])
//            return runWithPodiumPosition(this.islandBlockPosition, () -> super.a(segmentOffset, segment1, segment2));
//        }
//
//        @Override
//        public Vec3D y(float f) {
//            // getHeadLookVector(Float)
//            return runWithPodiumPosition(this.islandBlockPosition, () -> super.y(f));
//        }

        @Override
        public void w_() {
            runWithPodiumPosition(this.islandBlockPosition, super::w_);
        }

    }

    private static final class IslandEnderDragonBattle extends EnderDragonBattle {

        private static final ShapeDetector n = ShapeDetectorBuilder.a().a(new String[]{"       ", "       ", "       ", "   #   ", "       ", "       ", "       "}).a(new String[]{"       ", "       ", "       ", "   #   ", "       ", "       ", "       "}).a(new String[]{"       ", "       ", "       ", "   #   ", "       ", "       ", "       "}).a(new String[]{"  ###  ", " #   # ", "#     #", "#  #  #", "#     #", " #   # ", "  ###  "}).a(new String[]{"       ", "  ###  ", " ##### ", " ##### ", " ##### ", "  ###  ", "       "}).a('#', ShapeDetectorBlock.a(BlockPredicate.a(Blocks.z))).b();

        private final Island island;
        private final BlockPosition islandBlockPosition;

        private final BossBattleServer bossBattleServer;
        private final WorldServer worldServer;

        private byte currentTick = 0;
        private boolean previouslyGeneratedPortal = false;

        public IslandEnderDragonBattle(Island island, WorldServer worldServer, Location location) {
            super(worldServer.getHandle(), worldServer.getSeed(), new net.minecraft.nbt.NBTTagCompound());
            this.island = island;
            this.islandBlockPosition = new BlockPosition(location.getX(), location.getY(), location.getZ());
            this.bossBattleServer = new BossBattleServer(this.k);
            this.worldServer = worldServer;
            spawnEnderDragon();
        }

        @Override
        public void b() {
            super.b();
            if (++currentTick >= 20) {
                updateBattlePlayers();
                currentTick = 0;
            }
        }

        @Nullable
        @Override
        public ShapeDetector.ShapeDetectorCollection j() {
            int i;
            int l;

            i = this.l.a(HeightMap.Type.e, islandBlockPosition.getHandle()).v();

            for (l = i; l >= this.l.u_(); --l) {
                ShapeDetector.ShapeDetectorCollection blockPatternMatch2 = n.a(this.l, new net.minecraft.core.BlockPosition(WorldGenEndTrophy.e.u(), l, WorldGenEndTrophy.e.w()));
                if (blockPatternMatch2 != null) {
                    if (this.w == null) {
                        this.w = blockPatternMatch2.a(3, 3, 3).d();
                    }

                    return blockPatternMatch2;
                }
            }

            return null;
        }

        //        @Nullable
//        @Override
//        public ShapeDetector.ShapeDetectorCollection j() {
//            // findExitPortal()
//            return runWithPodiumPosition(this.islandBlockPosition, super::j);
//        }
//
//        @Override
//        public void a(EntityEnderDragon dragon) {
//            // setDragonKilled(EntityEnderDragon)
//            runWithPodiumPosition(this.islandBlockPosition, () -> super.a(dragon));
//        }
//
//        @Override
//        public void a(boolean previouslyKilled) {
//            // spawnExitPortal(Boolean)
//            runWithPodiumPosition(this.islandBlockPosition, () -> super.a(previouslyKilled));
//        }

        public void removeBattlePlayers() {
            for (Entity entity : bossBattleServer.getPlayers())
                bossBattleServer.removePlayer(entity);
        }

        private void updateBattlePlayers() {
            Set<UUID> nearbyPlayers = new HashSet<>();

            for (SuperiorPlayer superiorPlayer : island.getAllPlayersInside()) {
                Player bukkitPlayer = superiorPlayer.asPlayer();
                assert bukkitPlayer != null;

                Entity player = new Entity(((CraftPlayer) bukkitPlayer).getHandle());

                if (player.isInWorld(this.worldServer)) {
                    bossBattleServer.addPlayer(player);
                    nearbyPlayers.add(player.getUniqueID());
                }
            }

            bossBattleServer.getPlayers().stream()
                    .filter(entityPlayer -> !nearbyPlayers.contains(entityPlayer.getUniqueID()))
                    .forEach(bossBattleServer::removePlayer);
        }

        private void spawnEnderDragon() {
            IslandEntityEnderDragon entityEnderDragon = new IslandEntityEnderDragon(worldServer, islandBlockPosition, this);
            entityEnderDragon.entity.getDragonControllerManager().setControllerPhase(DragonControllerPhase.a);
            entityEnderDragon.entity.setPositionRotation(islandBlockPosition.getX(), 128,
                    islandBlockPosition.getZ(), worldServer.getRandom().nextFloat() * 360.0F, 0.0F);
            DRAGON_BATTLE.set(entityEnderDragon, this);

            worldServer.addEntity(entityEnderDragon.entity, CreatureSpawnEvent.SpawnReason.NATURAL);

            this.u = entityEnderDragon.entity.getUniqueID();
            f();
        }

    }

//    private static final class IslandDragonControllerHold extends DragonControllerHold {
//
//        private final BlockPosition islandBlockPosition;
//
//        IslandDragonControllerHold(IslandEntityEnderDragon entityEnderDragon) {
//            super(entityEnderDragon);
//            this.islandBlockPosition = entityEnderDragon.islandBlockPosition;
//        }
//
//        @Override
//        public void c() {
//            // doServerTick()
//            runWithPodiumPosition(this.islandBlockPosition, super::c);
//        }
//
//    }
//
//    private static final class IslandDragonControllerStrafe extends DragonControllerStrafe {
//
//        private final BlockPosition islandBlockPosition;
//
//        IslandDragonControllerStrafe(IslandEntityEnderDragon entityEnderDragon) {
//            super(entityEnderDragon);
//            this.islandBlockPosition = entityEnderDragon.islandBlockPosition;
//        }
//
//        @Override
//        public void c() {
//            // doServerTick()
//            runWithPodiumPosition(this.islandBlockPosition, super::c);
//        }
//
//    }
//
//    private static final class IslandDragonControllerLandingFly extends DragonControllerLandingFly {
//
//        private final BlockPosition islandBlockPosition;
//
//        IslandDragonControllerLandingFly(IslandEntityEnderDragon entityEnderDragon) {
//            super(entityEnderDragon);
//            this.islandBlockPosition = entityEnderDragon.islandBlockPosition;
//        }
//
//        @Override
//        public void c() {
//            // doServerTick()
//            runWithPodiumPosition(this.islandBlockPosition, super::c);
//        }
//
//    }
//
//    private static final class IslandDragonControllerLanding extends DragonControllerLanding {
//
//        private final BlockPosition islandBlockPosition;
//
//        IslandDragonControllerLanding(IslandEntityEnderDragon entityEnderDragon) {
//            super(entityEnderDragon);
//            this.islandBlockPosition = entityEnderDragon.islandBlockPosition;
//        }
//
//        @Override
//        public void c() {
//            // doServerTick()
//            runWithPodiumPosition(this.islandBlockPosition, super::c);
//        }
//
//    }
//
//    private static final class IslandDragonControllerFly extends DragonControllerFly {
//
//        private final BlockPosition islandBlockPosition;
//
//        IslandDragonControllerFly(IslandEntityEnderDragon entityEnderDragon) {
//            super(entityEnderDragon);
//            this.islandBlockPosition = entityEnderDragon.islandBlockPosition;
//        }
//
//        @Override
//        public void c() {
//            // doServerTick()
//            runWithPodiumPosition(this.islandBlockPosition, super::c);
//        }
//
//    }
//
//    private static final class IslandDragonControllerLandedFlame extends DragonControllerLandedFlame {
//
//        private final BlockPosition islandBlockPosition;
//
//        IslandDragonControllerLandedFlame(IslandEntityEnderDragon entityEnderDragon) {
//            super(entityEnderDragon);
//            this.islandBlockPosition = entityEnderDragon.islandBlockPosition;
//        }
//
//        @Override
//        public void c() {
//            // doServerTick()
//            runWithPodiumPosition(this.islandBlockPosition, super::c);
//        }
//
//    }
//
//    private static final class IslandDragonControllerLandedSearch extends DragonControllerLandedSearch {
//
//        private final BlockPosition islandBlockPosition;
//
//        IslandDragonControllerLandedSearch(IslandEntityEnderDragon entityEnderDragon) {
//            super(entityEnderDragon);
//            this.islandBlockPosition = entityEnderDragon.islandBlockPosition;
//        }
//
//        @Override
//        public void c() {
//            // doServerTick()
//            runWithPodiumPosition(this.islandBlockPosition, super::c);
//        }
//
//    }
//
//    private static final class IslandDragonControllerLandedAttack extends DragonControllerLandedAttack {
//
//        private final BlockPosition islandBlockPosition;
//
//        IslandDragonControllerLandedAttack(IslandEntityEnderDragon entityEnderDragon) {
//            super(entityEnderDragon);
//            this.islandBlockPosition = entityEnderDragon.islandBlockPosition;
//        }
//
//        @Override
//        public void c() {
//            // doServerTick()
//            runWithPodiumPosition(this.islandBlockPosition, super::c);
//        }
//
//    }
//
//    private static final class IslandDragonControllerCharge extends DragonControllerCharge {
//
//        private final BlockPosition islandBlockPosition;
//
//        IslandDragonControllerCharge(IslandEntityEnderDragon entityEnderDragon) {
//            super(entityEnderDragon);
//            this.islandBlockPosition = entityEnderDragon.islandBlockPosition;
//        }
//
//        @Override
//        public void c() {
//            // doServerTick()
//            runWithPodiumPosition(this.islandBlockPosition, super::c);
//        }
//
//    }
//
//    private static final class IslandDragonControllerDying extends DragonControllerDying {
//
//
//        private final BlockPosition islandBlockPosition;
//
//        IslandDragonControllerDying(IslandEntityEnderDragon entityEnderDragon) {
//            super(entityEnderDragon);
//            this.islandBlockPosition = entityEnderDragon.islandBlockPosition;
//        }
//
//        @Override
//        public void c() {
//            // doServerTick()
//            runWithPodiumPosition(this.islandBlockPosition, super::c);
//        }
//
//    }
//
//    private static final class IslandDragonControllerHover extends DragonControllerHover {
//
//
//        private final BlockPosition islandBlockPosition;
//
//        IslandDragonControllerHover(IslandEntityEnderDragon entityEnderDragon) {
//            super(entityEnderDragon);
//            this.islandBlockPosition = entityEnderDragon.islandBlockPosition;
//        }
//
//        @Override
//        public void c() {
//            // doServerTick()
//            runWithPodiumPosition(this.islandBlockPosition, super::c);
//        }
//
//    }

}
