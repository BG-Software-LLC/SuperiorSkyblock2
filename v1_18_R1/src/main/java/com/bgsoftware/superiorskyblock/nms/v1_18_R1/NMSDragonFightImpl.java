package com.bgsoftware.superiorskyblock.nms.v1_18_R1;

import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.nms.NMSDragonFight;
import com.bgsoftware.superiorskyblock.nms.v1_18_R1.chunks.IslandsChunkGenerator;
import com.google.common.collect.ImmutableList;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.core.BaseBlockPosition;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.particles.Particles;
import net.minecraft.data.worldgen.features.EndFeatures;
import net.minecraft.nbt.GameProfileSerializer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.PlayerChunk;
import net.minecraft.server.level.TicketType;
import net.minecraft.server.level.WorldServer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Unit;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.targeting.PathfinderTargetCondition;
import net.minecraft.world.entity.boss.enderdragon.EntityEnderCrystal;
import net.minecraft.world.entity.boss.enderdragon.EntityEnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonControllerDying;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonControllerFly;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonControllerHold;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonControllerLanding;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonControllerLandingFly;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonControllerManager;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonControllerPhase;
import net.minecraft.world.entity.boss.enderdragon.phases.IDragonController;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.level.ChunkCoordIntPair;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.block.entity.TileEntityEnderPortal;
import net.minecraft.world.level.block.state.pattern.ShapeDetector;
import net.minecraft.world.level.block.state.pattern.ShapeDetectorBlock;
import net.minecraft.world.level.block.state.pattern.ShapeDetectorBuilder;
import net.minecraft.world.level.block.state.predicate.BlockPredicate;
import net.minecraft.world.level.chunk.Chunk;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.IChunkAccess;
import net.minecraft.world.level.dimension.end.EnderDragonBattle;
import net.minecraft.world.level.dimension.end.EnumDragonRespawn;
import net.minecraft.world.level.levelgen.HeightMap;
import net.minecraft.world.level.levelgen.feature.WorldGenEndTrophy;
import net.minecraft.world.level.levelgen.feature.configurations.WorldGenFeatureConfiguration;
import net.minecraft.world.level.pathfinder.PathEntity;
import net.minecraft.world.level.pathfinder.PathPoint;
import net.minecraft.world.level.storage.Convertable;
import net.minecraft.world.phys.AxisAlignedBB;
import net.minecraft.world.phys.Vec3D;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.advancement.Advancement;
import org.bukkit.craftbukkit.v1_18_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftEnderDragon;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftPlayer;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import static com.bgsoftware.superiorskyblock.nms.v1_18_R1.NMSMappings.*;

@SuppressWarnings({"unused", "NullableProblems"})
public final class NMSDragonFightImpl implements NMSDragonFight {

    private static final ReflectField<EnderDragonBattle> DRAGON_BATTLE = new ReflectField<>(
            EntityEnderDragon.class, EnderDragonBattle.class,
            Modifier.PRIVATE | Modifier.FINAL, 0);
    private static final ReflectField<IDragonController> DRAGON_PHASE = new ReflectField<>(
            DragonControllerManager.class, IDragonController.class,
            Modifier.PRIVATE, 0);
    private static final ReflectMethod<PathEntity> DRAGON_FIND_PATH = new ReflectMethod<>(
            EntityEnderDragon.class, 0, int.class, int.class, PathPoint.class);

    static {
        DRAGON_BATTLE.removeFinal();
    }

    private final Map<UUID, EnderDragonBattle> activeBattles = new HashMap<>();

    private static Vec3D navigateToNextPathNode(PathEntity currentPath, EntityEnderDragon entityEnderDragon, Vec3D currentTargetBlock) {
        if (currentPath != null && !currentPath.c()) {
            BaseBlockPosition basePosition = currentPath.g();
            currentPath.a();

            double y;
            do {
                y = getY(basePosition) + getRandom(entityEnderDragon).nextFloat() * 20.0F;
            } while (y < getY(basePosition));

            return new Vec3D(getX(basePosition), y, getZ(basePosition));
        }

        return currentTargetBlock;
    }

    @Override
    public void startDragonBattle(Island island, Location location) {
        org.bukkit.World bukkitWorld = location.getWorld();
        if (bukkitWorld != null) {
            WorldServer worldServer = ((CraftWorld) bukkitWorld).getHandle();
            IslandEnderDragonBattle islandEnderDragonBattle = new IslandEnderDragonBattle(island, worldServer, location);
            activeBattles.put(island.getUniqueId(), islandEnderDragonBattle);
        }
    }

    @Override
    public void removeDragonBattle(Island island) {
        EnderDragonBattle enderDragonBattle = activeBattles.remove(island.getUniqueId());
        if (enderDragonBattle instanceof IslandEnderDragonBattle)
            ((IslandEnderDragonBattle) enderDragonBattle).removeBattlePlayers();
    }

    @Override
    public void tickBattles() {
        activeBattles.values().forEach(EnderDragonBattle::b);
    }

    @Override
    public void setDragonPhase(EnderDragon enderDragon, Object objectPhase) {
        EnderDragon.Phase phase = (EnderDragon.Phase) objectPhase;

        if (!(((CraftEnderDragon) enderDragon).getHandle() instanceof IslandEntityEnderDragon entityEnderDragon))
            return;

        switch (phase) {
            case DYING -> DRAGON_PHASE.set(getDragonControllerManager(entityEnderDragon), new IslandDragonControllerDying(entityEnderDragon));
            case LEAVE_PORTAL -> DRAGON_PHASE.set(getDragonControllerManager(entityEnderDragon), new IslandDragonControllerFly(entityEnderDragon));
            case CIRCLING -> DRAGON_PHASE.set(getDragonControllerManager(entityEnderDragon), new IslandDragonControllerHold(entityEnderDragon));
            case LAND_ON_PORTAL -> DRAGON_PHASE.set(getDragonControllerManager(entityEnderDragon), new IslandDragonControllerLanding(entityEnderDragon));
            case FLY_TO_PORTAL -> DRAGON_PHASE.set(getDragonControllerManager(entityEnderDragon), new IslandDragonControllerLandingFly(entityEnderDragon));
        }
    }

    @Override
    public void awardTheEndAchievement(Player player) {
        Advancement advancement = Bukkit.getAdvancement(NamespacedKey.minecraft("end/root"));
        if (advancement != null)
            player.getAdvancementProgress(advancement).awardCriteria("");
    }

    private static final class IslandEntityEnderDragon extends EntityEnderDragon {

        private final BlockPosition islandBlockPosition;

        IslandEntityEnderDragon(WorldServer world, BlockPosition islandBlockPosition, IslandEnderDragonBattle islandEnderDragonBattle) {
            super(null, getDragonBattleInjection(islandEnderDragonBattle));
            this.islandBlockPosition = islandBlockPosition;
            a(world);
        }

        @Override
        public Vec3D y(float f) {
            IDragonController dragonController = getDragonControllerManager(this).a();
            DragonControllerPhase<? extends IDragonController> dragonControllerPhase = getControllerPhase(dragonController);
            float f1;
            Vec3D vec3d;

            if (dragonControllerPhase != DragonControllerPhase.d && dragonControllerPhase != DragonControllerPhase.e) {
                if (dragonController.a()) {
                    float f2 = getXRot(this);
                    setXRot(this, -45.0F);
                    vec3d = this.e(f);
                    setXRot(this, f2);
                } else {
                    vec3d = this.e(f);
                }
            } else {
                BlockPosition blockposition = getHighestBlockYAt(this.t, HeightMap.Type.f, islandBlockPosition);
                f1 = Math.max(MathHelper.c(blockposition.a(getPositionVector(this), true)) / 4.0F, 1.0F);
                float f3 = 6.0F / f1;
                float f4 = getXRot(this);
                setXRot(this, -f3 * 1.5F * 5.0F);
                vec3d = this.e(f);
                setXRot(this, f4);
            }

            return vec3d;
        }

        private static WorldServer getDragonBattleInjection(IslandEnderDragonBattle islandEnderDragonBattle) {
            WorldServer worldServer = islandEnderDragonBattle.l;
            return new WorldServer(worldServer.n(), worldServer.n().az, worldServer.convertable, worldServer.N,
                    worldServer.aa(), worldServer.q_(), worldServer.n().L.create(11), new IslandsChunkGenerator(worldServer),
                    worldServer.N.A().g(), j, ImmutableList.of(), true, World.Environment.NORMAL,
                    worldServer.generator, worldServer.getWorld().getBiomeProvider()) {

                @Override
                public EnderDragonBattle F() {
                    return islandEnderDragonBattle;
                }
            };
        }

    }

    private static final class IslandEnderDragonBattle extends EnderDragonBattle {

        private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

        private static final ShapeDetector portalShape = ShapeDetectorBuilder.a()
                .a(new String[]{"       ", "       ", "       ", "   #   ", "       ", "       ", "       "})
                .a(new String[]{"       ", "       ", "       ", "   #   ", "       ", "       ", "       "})
                .a(new String[]{"       ", "       ", "       ", "   #   ", "       ", "       ", "       "})
                .a(new String[]{"  ###  ", " #   # ", "#     #", "#  #  #", "#     #", " #   # ", "  ###  "})
                .a(new String[]{"       ", "  ###  ", " ##### ", " ##### ", " ##### ", "  ###  ", "       "})
                .a('#', ShapeDetectorBlock.a(BlockPredicate.a(Blocks.z)))
                .b();

        private final BlockPosition islandBlockPosition;
        private final ChunkCoordIntPair islandChunkCoord;
        private final AxisAlignedBB borderArea;
        private final List<Integer> gateways = new ArrayList<>();
        private final Island island;

        private List<EntityEnderCrystal> crystalsList;
        private int currentTick = 0;
        private int crystalsCountTick = 0;
        private int respawnTick = 0;
        private int crystalsCount = 0;
        private boolean dragonKilled = false;
        private boolean previouslyKilled = false;

        public IslandEnderDragonBattle(Island island, WorldServer worldServer, Location location) {
            super(worldServer, getSeed(getGeneratorSettings(getWorldData(worldServer))), new NBTTagCompound());
            this.islandBlockPosition = new BlockPosition(location.getX(), location.getY(), location.getZ());
            this.islandChunkCoord = new ChunkCoordIntPair(islandBlockPosition);
            this.island = island;

            int radius = plugin.getSettings().getMaxIslandSize();
            this.borderArea = new AxisAlignedBB(new BlockPosition(islandBlockPosition.c(-radius, -radius, -radius)),
                    new BlockPosition(islandBlockPosition.c(radius, radius, radius)));

            spawnEnderDragon();
        }

        @Override
        public NBTTagCompound a() {
            NBTTagCompound nbtTagCompound = new NBTTagCompound();

            if (this.u != null) {
                nbtTagCompound.a("Dragon", this.u);
            }

            setBoolean(nbtTagCompound, "DragonKilled", this.dragonKilled);
            setBoolean(nbtTagCompound, "PreviouslyKilled", this.previouslyKilled);

            if (this.w != null) {
                set(nbtTagCompound, "ExitPortalLocation", GameProfileSerializer.a(this.w));
            }

            NBTTagList nbtTagList = new NBTTagList();
            for (Integer gateway : this.gateways)
                nbtTagList.add(NBTTagInt.a(gateway));

            set(nbtTagCompound, "Gateways", nbtTagList);

            return nbtTagCompound;
        }

        @Override
        public void b() {
            setVisible(this.k, !dragonKilled);

            // Update battle players
            if (++currentTick >= 20) {
                updateBattlePlayers();
                currentTick = 0;
            }

            if (getPlayers(this.k).isEmpty()) {
                removeTicket(getChunkProvider(this.l), TicketType.b, islandChunkCoord, 9, Unit.a);
                return;
            }

            addTicket(getChunkProvider(this.l), TicketType.b, islandChunkCoord, 9, Unit.a);

            boolean tickingChunks = areChunkTicking();

            if (this.x != null && tickingChunks) {
                if (crystalsList == null) {
                    this.x = null;
                    e();
                }

                if (++crystalsCountTick >= 100) {
                    countCrystals();
                    crystalsCountTick = 0;
                }

                this.x.a(this.l, this, crystalsList, respawnTick++, this.w);
            }
        }

        @Override
        public void a(EnumDragonRespawn dragonRespawn) {
            if (this.x == null)
                throw new IllegalStateException("Dragon respawn isn't in progress, can't skip ahead in the animation.");

            respawnTick = 0;

            if (dragonRespawn != EnumDragonRespawn.e) {
                this.x = dragonRespawn;
            } else {
                this.x = null;
                dragonKilled = false;
                EntityEnderDragon entityEnderDragon = spawnEnderDragon();
                for (EntityPlayer entityPlayer : getPlayers(this.k))
                    CriterionTriggers.n.a(entityPlayer, entityEnderDragon);
            }
        }

        @Override
        public ShapeDetector.ShapeDetectorCollection j() {
            for (int x = -8; x <= 8; x++) {
                for (int z = -8; z <= 8; z++) {
                    Chunk chunk = getChunkAt(this.l, islandChunkCoord.c + x, islandChunkCoord.d + z);

                    for (TileEntity tileEntity : getTileEntities(chunk).values()) {
                        if (tileEntity instanceof TileEntityEnderPortal) {
                            ShapeDetector.ShapeDetectorCollection shapeDetectorCollection =
                                    portalShape.a(this.l, getPosition(tileEntity));
                            if (shapeDetectorCollection != null) {
                                BlockPosition blockposition = getPosition(shapeDetectorCollection.a(3, 3, 3));
                                if (this.w == null && getX(blockposition) == 0 && getZ(blockposition) == 0) {
                                    this.w = blockposition;
                                }

                                return shapeDetectorCollection;
                            }
                        }
                    }
                }
            }

            int highestBlock = getY(getHighestBlockYAt(this.l, HeightMap.Type.e, islandBlockPosition));

            for (int y = highestBlock; y >= 0; y--) {
                ShapeDetector.ShapeDetectorCollection shapeDetectorCollection = portalShape.a(
                        this.l, new BlockPosition(getX(islandBlockPosition), y, getZ(islandBlockPosition)));
                if (shapeDetectorCollection != null) {
                    if (this.w == null) {
                        this.w = getPosition(shapeDetectorCollection.a(3, 3, 3));
                    }

                    return shapeDetectorCollection;
                }
            }

            return null;
        }

        @Override
        public void a(EntityEnderDragon entityEnderDragon) {
            if (!getUniqueID(entityEnderDragon).equals(this.u))
                return;

            setProgress(this.k, 0.0F);
            setVisible(this.k, false);
            this.a(true);

            if (!gateways.isEmpty()) {
                int i = gateways.remove(gateways.size() - 1);
                int j = floor(96.0D * Math.cos(2.0D * (-3.141592653589793D + 0.15707963267948966D * (double) i)));
                int k = floor(96.0D * Math.sin(2.0D * (-3.141592653589793D + 0.15707963267948966D * (double) i)));
                BlockPosition blockPosition = new BlockPosition(j, 75, k);
                triggerEffect(this.l, 3000, blockPosition, 0);
                EndFeatures.c.a(this.l, getChunkProvider(this.l).g(), new Random(), blockPosition);
            }

            if (!previouslyKilled) {
                setTypeUpdate(this.l, getHighestBlockYAt(this.l, HeightMap.Type.e, islandBlockPosition),
                        getBlockData(Blocks.er));
            }

            previouslyKilled = true;
            dragonKilled = true;
        }

        @Override
        public void a(boolean flag) {
            WorldGenEndTrophy worldGenEndTrophy = new WorldGenEndTrophy(flag);

            if (this.w == null) {
                this.w = down(getHighestBlockYAt(this.l, HeightMap.Type.f, islandBlockPosition));
                while (getType(this.l, this.w).a(Blocks.z) && getY(this.w) > getSeaLevel(this.l))
                    this.w = down(this.w);
            }

            worldGenEndTrophy.a(WorldGenFeatureConfiguration.m).a(this.l, getChunkProvider(this.l).g(),
                    new Random(), up(this.w, 2));
        }

        @Override
        public void b(EntityEnderDragon entityEnderDragon) {
            if (!getUniqueID(entityEnderDragon).equals(this.u))
                return;

            setProgress(this.k, getHealth(entityEnderDragon) / getMaxHealth(entityEnderDragon));

            if (hasCustomName(entityEnderDragon))
                this.k.a(getScoreboardDisplayName(entityEnderDragon));
        }

        @Override
        public int c() {
            return this.crystalsCount;
        }

        @Override
        public void a(EntityEnderCrystal entityEnderCrystal, DamageSource damageSource) {
            if (this.x != null && crystalsList.contains(entityEnderCrystal)) {
                this.x = null;
                respawnTick = 0;
                f();
                a(true);
            } else {
                countCrystals();
                Entity entity = getEntity(this.l, this.u);
                if (entity instanceof EntityEnderDragon)
                    ((EntityEnderDragon) entity).a(entityEnderCrystal, getChunkCoordinates(entityEnderCrystal), damageSource);
            }
        }

        @Override
        public boolean d() {
            return previouslyKilled;
        }

        @Override
        public void e() {
            if (!dragonKilled || this.x != null)
                return;

            crystalsList = this.l.a(EntityEnderCrystal.class, borderArea);

            this.x = EnumDragonRespawn.a;
            respawnTick = 0;
            a(false);
        }

        @Override
        public void f() {
            for (EntityEnderCrystal entityEnderCrystal : this.l.a(EntityEnderCrystal.class, borderArea)) {
                setInvulnerable(entityEnderCrystal, false);
                setBeamTarget(entityEnderCrystal, null);
            }
        }

        public void removeBattlePlayers() {
            for (EntityPlayer entityPlayer : getPlayers(this.k))
                removePlayer(this.k, entityPlayer);
        }

        private boolean areChunkTicking() {
            for (int i = -8; i <= 8; ++i) {
                for (int j = 8; j <= 8; ++j) {
                    IChunkAccess chunkAccess = getChunkAt(this.l, islandChunkCoord.c + i, islandChunkCoord.d + j,
                            ChunkStatus.m, false);

                    if (!(chunkAccess instanceof Chunk) ||
                            !isAtLeast(getState((Chunk) chunkAccess), PlayerChunk.State.c)) {
                        return false;
                    }
                }
            }

            return true;
        }

        private void updateBattlePlayers() {
            Set<EntityPlayer> nearbyPlayers = new HashSet<>();

            for (SuperiorPlayer superiorPlayer : island.getAllPlayersInside()) {
                Player player = superiorPlayer.asPlayer();
                assert player != null;
                if (((CraftWorld) player.getWorld()).getHandle() == this.l) {
                    EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
                    addPlayer(this.k, entityPlayer);
                    nearbyPlayers.add(entityPlayer);
                }
            }

            new HashSet<>(getPlayers(this.k)).stream()
                    .filter(entityPlayer -> !nearbyPlayers.contains(entityPlayer))
                    .forEach(entityPlayer -> removePlayer(this.k, entityPlayer));
        }

        private EntityEnderDragon spawnEnderDragon() {
            EntityEnderDragon entityEnderDragon = new IslandEntityEnderDragon(this.l, islandBlockPosition, this);
            setControllerPhase(getDragonControllerManager(entityEnderDragon), DragonControllerPhase.a);
            setPositionRotation(entityEnderDragon, getX(islandBlockPosition), 128, getZ(islandBlockPosition),
                    this.l.w.nextFloat() * 360.0F, 0.0F);
            DRAGON_BATTLE.set(entityEnderDragon, this);

            addEntity(this.l, entityEnderDragon, CreatureSpawnEvent.SpawnReason.NATURAL);

            this.u = getUniqueID(entityEnderDragon);
            f();

            return entityEnderDragon;
        }

        private void countCrystals() {
            this.crystalsCount = this.l.a(EntityEnderCrystal.class, borderArea).size();
        }

    }

    private static final class IslandDragonControllerDying extends DragonControllerDying {

        private final BlockPosition islandBlockPosition;

        private Vec3D targetBlock;
        private int currentTick;

        IslandDragonControllerDying(IslandEntityEnderDragon entityEnderDragon) {
            super(entityEnderDragon);
            this.islandBlockPosition = entityEnderDragon.islandBlockPosition;
        }

        @Override
        public void b() {
            if (currentTick++ % 10 == 0) {
                float offsetX = (getRandom(this.a).nextFloat() - 0.5F) * 8.0F;
                float offsetY = (getRandom(this.a).nextFloat() - 0.5F) * 4.0F;
                float offsetZ = (getRandom(this.a).nextFloat() - 0.5F) * 8.0F;
                addParticle(this.a.t,
                        Particles.x,
                        locX(this.a) + offsetX,
                        locY(this.a) + 2.0D + offsetY,
                        locZ(this.a) + offsetZ,
                        0.0D,
                        0.0D,
                        0.0D
                );
            }
        }

        @Override
        public void c() {
            currentTick++;

            if (targetBlock == null)
                targetBlock = Vec3D.c(getHighestBlockYAt(this.a.t, HeightMap.Type.e, islandBlockPosition));

            double distance = targetBlock.c(locX(this.a), locY(this.a), locZ(this.a));

            if (distance >= 100.0D && distance <= 22500.0D && !a.A && !a.B) {
                setHealth(this.a, 1.0F);
            } else {
                setHealth(this.a, 0.0F);
            }

        }

        @Override
        public void d() {
            targetBlock = null;
            currentTick = 0;
        }

        public Vec3D g() {
            return this.targetBlock;
        }

    }

    private static final class IslandDragonControllerFly extends DragonControllerFly {

        private final BlockPosition islandBlockPosition;

        private PathEntity currentPath;
        private Vec3D targetBlock;
        private boolean firstTick;

        IslandDragonControllerFly(IslandEntityEnderDragon entityEnderDragon) {
            super(entityEnderDragon);
            this.islandBlockPosition = entityEnderDragon.islandBlockPosition;
        }

        @Override
        public void c() {
            if (!firstTick && currentPath != null) {
                BlockPosition highestBlock = getHighestBlockYAt(this.a.t, HeightMap.Type.f, islandBlockPosition);
                if (!highestBlock.a(getPositionVector(this.a), 10.0D)) {
                    setControllerPhase(getDragonControllerManager(this.a), DragonControllerPhase.a);
                }
            } else {
                firstTick = false;
                findNewTarget();
            }
        }

        @Override
        public void d() {
            firstTick = true;
            currentPath = null;
            targetBlock = null;
        }

        public Vec3D g() {
            return this.targetBlock;
        }

        private void findNewTarget() {
            int closestNode = a.q();
            Vec3D headLookVector = a.y(1.0F);

            int headClosestNode = a.q(-headLookVector.b * 40.0D, 105.0D, -headLookVector.c * 40.0D);

            if (getEnderDragonBattle(this.a) != null && getEnderDragonBattle(this.a).c() > 0) {
                headClosestNode %= 12;
                if (headClosestNode < 0) {
                    headClosestNode += 12;
                }
            } else {
                headClosestNode -= 12;
                headClosestNode &= 7;
                headClosestNode += 12;
            }

            if (DRAGON_FIND_PATH.isValid()) {
                currentPath = DRAGON_FIND_PATH.invoke(this.a, closestNode, headClosestNode, null);
            } else {
                currentPath = findPath(this.a, closestNode, headClosestNode, null);
            }

            targetBlock = navigateToNextPathNode(currentPath, this.a, targetBlock);
        }

    }

    private static final class IslandDragonControllerHold extends DragonControllerHold {

        private static final PathfinderTargetCondition targetCondition = PathfinderTargetCondition.a().a(64.0D);

        private final BlockPosition islandBlockPosition;

        private PathEntity currentPath;
        private Vec3D targetBlock;
        private boolean clockwise;

        IslandDragonControllerHold(IslandEntityEnderDragon entityEnderDragon) {
            super(entityEnderDragon);
            this.islandBlockPosition = entityEnderDragon.islandBlockPosition;
        }

        @Override
        public void c() {
            double distance = targetBlock == null ? 0.0D : targetBlock.c(locX(this.a), locY(this.a), locZ(this.a));
            if (distance < 100.0D || distance > 22500.0D || this.a.A || this.a.B)
                findNewTarget();
        }

        @Override
        public void d() {
            currentPath = null;
            targetBlock = null;
        }

        public Vec3D g() {
            return targetBlock;
        }

        @Override
        public void a(EntityEnderCrystal entityEnderCrystal, BlockPosition blockPosition, DamageSource damageSource,
                      EntityHuman entityHuman) {
            if (entityHuman != null && !isInvisible(entityHuman))
                strafePlayer(entityHuman);
        }

        private void findNewTarget() {
            if (currentPath != null && currentPath.c()) {
                BlockPosition highestBlock = getHighestBlockYAt(this.a.t, HeightMap.Type.f, islandBlockPosition);
                int crystalsCount = getEnderDragonBattle(this.a) == null ? 0 : getEnderDragonBattle(this.a).c();
                if (getRandom(this.a).nextInt(crystalsCount + 3) == 0) {
                    setControllerPhase(getDragonControllerManager(this.a), DragonControllerPhase.c);
                    return;
                }

                double distance = 64.0D;
                EntityHuman closestHuman = this.a.t.a(targetCondition, getX(highestBlock), getY(highestBlock), getZ(highestBlock));
                if (closestHuman != null)
                    distance = highestBlock.a(getPositionVector(closestHuman), true) / 512.0D;

                if (closestHuman != null && (getRandom(this.a).nextInt(MathHelper.a((int) distance) + 2) == 0 ||
                        getRandom(this.a).nextInt(crystalsCount + 2) == 0)) {
                    strafePlayer(closestHuman);
                    return;
                }
            }

            if (currentPath == null || currentPath.c()) {
                int closestNode = this.a.q();
                int closestNode1 = closestNode;
                if (getRandom(this.a).nextInt(8) == 0) {
                    clockwise = !clockwise;
                    closestNode1 = closestNode + 6;
                }

                if (clockwise) {
                    ++closestNode;
                } else {
                    --closestNode;
                }

                if (getEnderDragonBattle(this.a) != null && getEnderDragonBattle(this.a).c() >= 0) {
                    closestNode %= 12;
                    if (closestNode < 0) {
                        closestNode += 12;
                    }
                } else {
                    closestNode -= 12;
                    closestNode &= 7;
                    closestNode += 12;
                }

                if (DRAGON_FIND_PATH.isValid()) {
                    currentPath = DRAGON_FIND_PATH.invoke(this.a, closestNode1, closestNode, null);
                } else {
                    currentPath = findPath(this.a, closestNode1, closestNode, null);
                }

                if (currentPath != null) {
                    currentPath.a();
                }
            }

            targetBlock = navigateToNextPathNode(currentPath, this.a, targetBlock);
        }

        private void strafePlayer(EntityHuman entityHuman) {
            setControllerPhase(getDragonControllerManager(this.a), DragonControllerPhase.b);
            getDragonControllerManager(this.a).b(DragonControllerPhase.b).a(entityHuman);
        }

    }

    private static final class IslandDragonControllerLanding extends DragonControllerLanding {

        private final BlockPosition islandBlockPosition;

        private Vec3D targetBlock;

        IslandDragonControllerLanding(IslandEntityEnderDragon entityEnderDragon) {
            super(entityEnderDragon);
            this.islandBlockPosition = entityEnderDragon.islandBlockPosition;
        }

        @Override
        public void b() {
            Vec3D vec3D = this.a.y(1.0F).d();
            vec3D.b(-0.7853982F);

            double originLocX = locX(this.a.e);
            double originLocY = this.a.e.e(0.5D);
            double originLocZ = locZ(this.a.e);

            Random random = getRandom(this.a);

            for (int i = 0; i < 8; i++) {
                double locX = originLocX + random.nextGaussian() / 2.0D;
                double locY = originLocY + random.nextGaussian() / 2.0D;
                double locZ = originLocZ + random.nextGaussian() / 2.0D;
                Vec3D mot = getMot(this.a);
                addParticle(this.a.t, Particles.j, locX, locY, locZ, -vec3D.b * 0.07999999821186066D + mot.b,
                        -vec3D.c * 0.30000001192092896D + mot.c, -vec3D.d * 0.07999999821186066D + mot.d);
                vec3D.b(0.19634955F);
            }
        }

        @Override
        public void c() {
            if (targetBlock == null) {
                targetBlock = Vec3D.c(getHighestBlockYAt(this.a.t, HeightMap.Type.f, islandBlockPosition));
            }

            if (targetBlock.c(locX(this.a), locY(this.a), locZ(this.a)) < 1.0D) {
                getDragonControllerManager(this.a).b(DragonControllerPhase.f).j();
                setControllerPhase(getDragonControllerManager(this.a), DragonControllerPhase.g);
            }

        }

        @Override
        public float f() {
            return 1.5F;
        }

        @Override
        public float h() {
            // Turning speed
            float xzSquared = (float) getMot(this.a).h() + 1.0F;
            return Math.min(xzSquared, 40.0F) / xzSquared;
        }

        @Override
        public void d() {
            targetBlock = null;
        }

        public Vec3D g() {
            return targetBlock;
        }

    }

    private static final class IslandDragonControllerLandingFly extends DragonControllerLandingFly {

        private static final PathfinderTargetCondition targetCondition = PathfinderTargetCondition.a().a(128.0D);

        private final BlockPosition islandBlockPosition;

        private PathEntity currentPath;
        private Vec3D targetBlock;

        IslandDragonControllerLandingFly(IslandEntityEnderDragon entityEnderDragon) {
            super(entityEnderDragon);
            this.islandBlockPosition = entityEnderDragon.islandBlockPosition;
        }

        @Override
        public void d() {
            currentPath = null;
            targetBlock = null;
        }

        @Override
        public void c() {
            double distance = targetBlock == null ? 0.0D : targetBlock.c(locX(this.a), locY(this.a), locZ(this.a));
            if (distance < 100.0D || distance > 22500.0D || this.a.A || this.a.B)
                findNewTarget();
        }

        public Vec3D g() {
            return targetBlock;
        }

        private void findNewTarget() {
            if (currentPath == null || currentPath.c()) {
                int closestNode = this.a.eO();
                BlockPosition highestBlock = getHighestBlockYAt(this.a.t, HeightMap.Type.f, islandBlockPosition);
                EntityHuman closestHuman = this.a.t.a(targetCondition, getX(highestBlock), getY(highestBlock), getZ(highestBlock));
                int closestNode1;
                if (closestHuman != null) {
                    Vec3D var4 = (new Vec3D(locX(closestHuman), 0.0D, locZ(closestHuman))).d();
                    closestNode1 = this.a.q(-var4.b * 40.0D, 105.0D, -var4.c * 40.0D);
                } else {
                    closestNode1 = this.a.q(40.0D, getY(highestBlock), 0.0D);
                }

                PathPoint var4 = new PathPoint(getX(highestBlock), getY(highestBlock), getZ(highestBlock));

                if (DRAGON_FIND_PATH.isValid()) {
                    currentPath = DRAGON_FIND_PATH.invoke(this.a, closestNode, closestNode1, var4);
                } else {
                    currentPath = findPath(this.a, closestNode, closestNode1, var4);
                }

                if (currentPath != null)
                    currentPath.a();
            }

            targetBlock = navigateToNextPathNode(currentPath, this.a, targetBlock);

            if (currentPath != null && currentPath.c())
                setControllerPhase(getDragonControllerManager(this.a), DragonControllerPhase.d);
        }

    }

}
