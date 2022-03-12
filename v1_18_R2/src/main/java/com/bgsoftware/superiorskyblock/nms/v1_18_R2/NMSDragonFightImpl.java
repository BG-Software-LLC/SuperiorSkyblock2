package com.bgsoftware.superiorskyblock.nms.v1_18_R2;

import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.nms.NMSDragonFight;
import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.BlockPosition;
import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.ChunkCoordIntPair;
import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.MathHelper;
import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.level.BossBattleServer;
import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.level.ChunkProviderServer;
import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.level.WorldServer;
import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.level.block.Block;
import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.level.block.entity.TileEntity;
import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.level.block.state.BlockData;
import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.level.chunk.ChunkAccess;
import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.level.pathfinder.PathEntity;
import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.nbt.NBTTagCompound;
import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.nbt.NBTTagList;
import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.world.entity.Entity;
import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.world.entity.boss.enderdragon.phases.DragonController;
import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.world.entity.boss.enderdragon.phases.DragonControllerManager;
import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.world.level.block.state.pattern.ShapeDetectorBlock;
import com.bgsoftware.superiorskyblock.nms.v1_18_R2.mapping.world.phys.AxisAlignedBB;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.core.particles.Particles;
import net.minecraft.data.worldgen.features.EndFeatures;
import net.minecraft.nbt.GameProfileSerializer;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.PlayerChunk;
import net.minecraft.server.level.TicketType;
import net.minecraft.util.Unit;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ai.targeting.PathfinderTargetCondition;
import net.minecraft.world.entity.boss.enderdragon.EntityEnderCrystal;
import net.minecraft.world.entity.boss.enderdragon.EntityEnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonControllerDying;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonControllerFly;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonControllerHold;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonControllerLanding;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonControllerLandingFly;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonControllerPhase;
import net.minecraft.world.entity.boss.enderdragon.phases.IDragonController;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.TileEntityEnderPortal;
import net.minecraft.world.level.block.state.pattern.ShapeDetector;
import net.minecraft.world.level.block.state.pattern.ShapeDetectorBuilder;
import net.minecraft.world.level.block.state.predicate.BlockPredicate;
import net.minecraft.world.level.chunk.Chunk;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.dimension.end.EnderDragonBattle;
import net.minecraft.world.level.dimension.end.EnumDragonRespawn;
import net.minecraft.world.level.levelgen.HeightMap;
import net.minecraft.world.level.levelgen.feature.WorldGenEndTrophy;
import net.minecraft.world.level.levelgen.feature.configurations.WorldGenFeatureConfiguration;
import net.minecraft.world.level.pathfinder.PathPoint;
import net.minecraft.world.phys.Vec3D;
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

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

@SuppressWarnings({"unused", "NullableProblems"})
public final class NMSDragonFightImpl implements NMSDragonFight {

    private static final ReflectField<EnderDragonBattle> DRAGON_BATTLE = new ReflectField<EnderDragonBattle>(
            EntityEnderDragon.class, EnderDragonBattle.class, Modifier.PRIVATE | Modifier.FINAL, 1)
            .removeFinal();
    private static final ReflectField<IDragonController> DRAGON_PHASE = new ReflectField<>(
            net.minecraft.world.entity.boss.enderdragon.phases.DragonControllerManager.class, IDragonController.class,
            Modifier.PRIVATE, 1);
    private static final ReflectMethod<net.minecraft.world.level.pathfinder.PathEntity> DRAGON_FIND_PATH = new ReflectMethod<>(
            EntityEnderDragon.class, 1, int.class, int.class, net.minecraft.world.level.pathfinder.PathPoint.class);

    private final Map<UUID, EnderDragonBattle> activeBattles = new HashMap<>();

    private static Vec3D navigateToNextPathNode(PathEntity currentPath, Entity entityEnderDragon, Vec3D currentTargetBlock) {
        if (currentPath != null && !currentPath.isDone()) {
            BlockPosition basePosition = currentPath.getNextNodePos();
            currentPath.advance();

            double y;
            do {
                y = basePosition.getY() + entityEnderDragon.getRandom().nextFloat() * 20.0F;
            } while (y < basePosition.getY());

            return new Vec3D(basePosition.getX(), y, basePosition.getZ());
        }

        return currentTargetBlock;
    }

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
        Entity entity = new Entity(((CraftEnderDragon) enderDragon).getHandle());

        if (!(entity.getHandle() instanceof IslandEntityEnderDragon entityEnderDragon))
            return;

        DragonControllerManager dragonControllerManager = entity.getDragonControllerManager();

        switch (phase) {
            case DYING -> DRAGON_PHASE.set(dragonControllerManager, new IslandDragonControllerDying(entityEnderDragon));
            case LEAVE_PORTAL -> DRAGON_PHASE.set(dragonControllerManager, new IslandDragonControllerFly(entityEnderDragon));
            case CIRCLING -> DRAGON_PHASE.set(dragonControllerManager, new IslandDragonControllerHold(entityEnderDragon));
            case LAND_ON_PORTAL -> DRAGON_PHASE.set(dragonControllerManager, new IslandDragonControllerLanding(entityEnderDragon));
            case FLY_TO_PORTAL -> DRAGON_PHASE.set(dragonControllerManager, new IslandDragonControllerLandingFly(entityEnderDragon));
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
        private final Entity entity = new Entity(this);

        IslandEntityEnderDragon(WorldServer worldServer, BlockPosition islandBlockPosition, IslandEnderDragonBattle islandEnderDragonBattle) {
            super(null, worldServer.getHandle());
            this.islandBlockPosition = islandBlockPosition;
        }

        @Override
        public Vec3D y(float f) {
            DragonController dragonController = entity.getDragonControllerManager().getDragonController();
            DragonControllerPhase<?> dragonControllerPhase = dragonController.getControllerPhase();
            float f1;
            Vec3D vec3d;

            if (dragonControllerPhase != DragonControllerPhase.d && dragonControllerPhase != DragonControllerPhase.e) {
                if (dragonController.isSitting()) {
                    float f2 = entity.getXRot();
                    entity.setXRot(-45.0F);
                    vec3d = this.e(f);
                    entity.setXRot(f2);
                } else {
                    vec3d = this.e(f);
                }
            } else {
                BlockPosition blockPosition = entity.getWorld().getHighestBlockYAt(HeightMap.Type.f, islandBlockPosition);
                f1 = Math.max((float) Math.sqrt(blockPosition.distSqr(entity.getPositionVector())) / 4.0F, 1.0F);
                float f3 = 6.0F / f1;
                float f4 = entity.getXRot();
                entity.setXRot(-f3 * 1.5F * 5.0F);
                vec3d = this.e(f);
                entity.setXRot(f4);
            }

            return vec3d;
        }

    }

    private static final class IslandEnderDragonBattle extends EnderDragonBattle {

        private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

        private static final BlockData DRAGON_EGG_DATA = new Block(Blocks.er).getBlockData();

        private static final ShapeDetector portalShape = ShapeDetectorBuilder.a()
                .a(new String[]{"       ", "       ", "       ", "   #   ", "       ", "       ", "       "})
                .a(new String[]{"       ", "       ", "       ", "   #   ", "       ", "       ", "       "})
                .a(new String[]{"       ", "       ", "       ", "   #   ", "       ", "       ", "       "})
                .a(new String[]{"  ###  ", " #   # ", "#     #", "#  #  #", "#     #", " #   # ", "  ###  "})
                .a(new String[]{"       ", "  ###  ", " ##### ", " ##### ", " ##### ", "  ###  ", "       "})
                .a('#', ShapeDetectorBlock.hasState(BlockPredicate.a(Blocks.z)))
                .b();

        private final BossBattleServer bossBattleServer;
        private final WorldServer worldServer;

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
            super(worldServer.getHandle(), worldServer.getSeed(), new net.minecraft.nbt.NBTTagCompound());
            this.islandBlockPosition = new BlockPosition(location.getX(), location.getY(), location.getZ());
            this.islandChunkCoord = new ChunkCoordIntPair(islandBlockPosition);
            this.island = island;

            int radius = plugin.getSettings().getMaxIslandSize();
            this.borderArea = new AxisAlignedBB(islandBlockPosition.offset(-radius, -radius, -radius),
                    islandBlockPosition.offset(radius, radius, radius));

            this.bossBattleServer = new BossBattleServer(this.k);
            this.worldServer = worldServer;

            spawnEnderDragon();
        }

        @Override
        public net.minecraft.nbt.NBTTagCompound a() {
            NBTTagCompound nbtTagCompound = new NBTTagCompound();

            if (this.u != null) {
                nbtTagCompound.setUUID("Dragon", this.u);
            }

            nbtTagCompound.setBoolean("DragonKilled", this.dragonKilled);
            nbtTagCompound.setBoolean("PreviouslyKilled", this.previouslyKilled);

            if (this.w != null) {
                nbtTagCompound.set("ExitPortalLocation", GameProfileSerializer.a(this.w));
            }

            NBTTagList nbtTagList = new NBTTagList();
            for (Integer gateway : this.gateways)
                nbtTagList.add(NBTTagInt.a(gateway));

            nbtTagCompound.set("Gateways", nbtTagList.getHandle());

            return nbtTagCompound.getHandle();
        }

        @Override
        public void b() {
            bossBattleServer.setVisible(!dragonKilled);

            // Update battle players
            if (++currentTick >= 20) {
                updateBattlePlayers();
                currentTick = 0;
            }

            ChunkProviderServer chunkProviderServer = worldServer.getChunkProvider();

            if (bossBattleServer.getPlayers().isEmpty()) {
                chunkProviderServer.removeTicket(TicketType.b, islandChunkCoord, 9, Unit.a);
                return;
            }

            chunkProviderServer.addTicket(TicketType.b, islandChunkCoord, 9, Unit.a);

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
                for (Entity entityPlayer : bossBattleServer.getPlayers())
                    CriterionTriggers.n.a((EntityPlayer) entityPlayer.getHandle(), entityEnderDragon);
            }
        }

        @Override
        public ShapeDetector.ShapeDetectorCollection j() {
            for (int x = -8; x <= 8; x++) {
                for (int z = -8; z <= 8; z++) {
                    ChunkAccess chunk = worldServer.getChunkAt(islandChunkCoord.getX() + x, islandChunkCoord.getZ() + z);

                    for (TileEntity tileEntity : chunk.getTileEntities().values()) {
                        if (tileEntity.getHandle() instanceof TileEntityEnderPortal) {
                            ShapeDetector.ShapeDetectorCollection shapeDetectorCollection = portalShape.a(
                                    worldServer.getHandle(), tileEntity.getPosition().getHandle());

                            if (shapeDetectorCollection != null) {
                                ShapeDetectorBlock shapeDetectorBlock = new ShapeDetectorBlock(shapeDetectorCollection
                                        .a(3, 3, 3));
                                BlockPosition blockposition = shapeDetectorBlock.getPosition();
                                if (this.w == null && blockposition.getX() == 0 && blockposition.getZ() == 0) {
                                    this.w = blockposition.getHandle();
                                }

                                return shapeDetectorCollection;
                            }
                        }
                    }
                }
            }

            int highestBlock = worldServer.getHighestBlockYAt(HeightMap.Type.e, islandBlockPosition).getY();

            for (int y = highestBlock; y >= 0; y--) {
                ShapeDetector.ShapeDetectorCollection shapeDetectorCollection = portalShape.a(this.l,
                        new net.minecraft.core.BlockPosition(islandBlockPosition.getX(), y, islandBlockPosition.getZ()));

                if (shapeDetectorCollection != null) {
                    if (this.w == null) {
                        ShapeDetectorBlock shapeDetectorBlock = new ShapeDetectorBlock(
                                shapeDetectorCollection.a(3, 3, 3));
                        this.w = shapeDetectorBlock.getPosition().getHandle();
                    }

                    return shapeDetectorCollection;
                }
            }

            return null;
        }

        @Override
        public void a(EntityEnderDragon entityEnderDragon) {
            if (!new Entity(entityEnderDragon).getUniqueID().equals(this.u))
                return;

            bossBattleServer.setProgress(0.0F);
            bossBattleServer.setVisible(false);
            this.a(true);

            if (!gateways.isEmpty()) {
                int i = gateways.remove(gateways.size() - 1);
                int j = MathHelper.floor(96.0D * Math.cos(2.0D * (-3.141592653589793D + 0.15707963267948966D * (double) i)));
                int k = MathHelper.floor(96.0D * Math.sin(2.0D * (-3.141592653589793D + 0.15707963267948966D * (double) i)));
                BlockPosition blockPosition = new BlockPosition(j, 75, k);
                worldServer.triggerEffect(3000, blockPosition, 0);
                EndFeatures.c.a().a(this.l, worldServer.getChunkProvider().getGenerator(), new Random(),
                        blockPosition.getHandle());
            }

            if (!previouslyKilled) {
                worldServer.setTypeUpdate(worldServer.getHighestBlockYAt(HeightMap.Type.e, islandBlockPosition), DRAGON_EGG_DATA);
            }

            previouslyKilled = true;
            dragonKilled = true;
        }

        @Override
        public void a(boolean flag) {
            WorldGenEndTrophy worldGenEndTrophy = new WorldGenEndTrophy(flag);
            BlockPosition exitPortalPos = BlockPosition.ofNullable(this.w);

            if (exitPortalPos == null) {
                int seaLevel = this.worldServer.getWorld().getSeaLevel();
                exitPortalPos = worldServer.getHighestBlockYAt(HeightMap.Type.f, islandBlockPosition).down();
                while (worldServer.getType(exitPortalPos).isSimilar(Blocks.z) && exitPortalPos.getY() > seaLevel)
                    exitPortalPos = exitPortalPos.down();
                this.w = exitPortalPos.getHandle();
            }

            worldGenEndTrophy.a(WorldGenFeatureConfiguration.m, this.l, worldServer.getChunkProvider().getGenerator(),
                    new Random(), exitPortalPos.up(2).getHandle());
        }

        @Override
        public void b(EntityEnderDragon entityEnderDragon) {
            Entity entity = new Entity(entityEnderDragon);

            if (!entity.getUniqueID().equals(this.u))
                return;

            bossBattleServer.setProgress(entity.getHealth() / entity.getMaxHealth());

            if (entity.hasCustomName())
                bossBattleServer.setName(entity.getScoreboardDisplayName());
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
                Entity entity = worldServer.getEntity(this.u);
                if (entity != null && entity.getHandle() instanceof EntityEnderDragon)
                    entity.onCrystalDestroyed(entityEnderCrystal, new Entity(entityEnderCrystal).getChunkCoordinates(), damageSource);
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

            crystalsList = worldServer.getEntities(EntityEnderCrystal.class, borderArea);

            this.x = EnumDragonRespawn.a;
            respawnTick = 0;
            a(false);
        }

        @Override
        public void f() {
            for (EntityEnderCrystal entityEnderCrystal : worldServer.getEntities(EntityEnderCrystal.class, borderArea)) {
                Entity entity = new Entity(entityEnderCrystal);
                entity.setInvulnerable(false);
                entity.setBeamTarget(null);
            }
        }

        public void removeBattlePlayers() {
            for (Entity entity : bossBattleServer.getPlayers())
                bossBattleServer.removePlayer(entity);
        }

        private boolean areChunkTicking() {
            for (int i = -8; i <= 8; ++i) {
                for (int j = 8; j <= 8; ++j) {
                    ChunkAccess chunkAccess = worldServer.getChunkAt(islandChunkCoord.getX() + i,
                            islandChunkCoord.getZ() + j, ChunkStatus.m, false);

                    if (chunkAccess == null || !(chunkAccess.getHandle() instanceof Chunk) ||
                            chunkAccess.getState().ordinal() < PlayerChunk.State.c.ordinal()) {
                        return false;
                    }
                }
            }

            return true;
        }

        private void updateBattlePlayers() {
            Set<UUID> nearbyPlayers = new HashSet<>();

            for (SuperiorPlayer superiorPlayer : island.getAllPlayersInside()) {
                Player bukkitPlayer = superiorPlayer.asPlayer();
                assert bukkitPlayer != null;

                Entity player = new Entity(((CraftPlayer) bukkitPlayer).getHandle());

                if (player.isInWorld(worldServer)) {
                    bossBattleServer.addPlayer(player);
                    nearbyPlayers.add(player.getUniqueID());
                }
            }

            bossBattleServer.getPlayers().stream()
                    .filter(entityPlayer -> !nearbyPlayers.contains(entityPlayer.getUniqueID()))
                    .forEach(bossBattleServer::removePlayer);
        }

        private EntityEnderDragon spawnEnderDragon() {
            IslandEntityEnderDragon entityEnderDragon = new IslandEntityEnderDragon(worldServer, islandBlockPosition, this);
            entityEnderDragon.entity.getDragonControllerManager().setControllerPhase(DragonControllerPhase.a);
            entityEnderDragon.entity.setPositionRotation(islandBlockPosition.getX(), 128,
                    islandBlockPosition.getZ(), worldServer.getRandom().nextFloat() * 360.0F, 0.0F);
            DRAGON_BATTLE.set(entityEnderDragon, this);

            worldServer.addEntity(entityEnderDragon.entity, CreatureSpawnEvent.SpawnReason.NATURAL);

            this.u = entityEnderDragon.entity.getUniqueID();
            f();

            return entityEnderDragon;
        }

        private void countCrystals() {
            this.crystalsCount = this.worldServer.getEntities(EntityEnderCrystal.class, borderArea).size();
        }

    }

    private static final class IslandDragonControllerDying extends DragonControllerDying {

        private final BlockPosition islandBlockPosition;
        private final Entity entityEnderDragon;

        private Vec3D targetBlock;
        private int currentTick;

        IslandDragonControllerDying(IslandEntityEnderDragon entityEnderDragon) {
            super(entityEnderDragon);
            this.islandBlockPosition = entityEnderDragon.islandBlockPosition;
            this.entityEnderDragon = entityEnderDragon.entity;
        }

        @Override
        public void b() {
            if (currentTick++ % 10 == 0) {
                float offsetX = (entityEnderDragon.getRandom().nextFloat() - 0.5F) * 8.0F;
                float offsetY = (entityEnderDragon.getRandom().nextFloat() - 0.5F) * 4.0F;
                float offsetZ = (entityEnderDragon.getRandom().nextFloat() - 0.5F) * 8.0F;
                entityEnderDragon.getWorld().addParticle(Particles.x, entityEnderDragon.locX() + offsetX,
                        entityEnderDragon.locY() + 2.0D + offsetY, entityEnderDragon.locZ() + offsetZ,
                        0.0D, 0.0D, 0.0D);
            }
        }

        @Override
        public void c() {
            currentTick++;

            if (targetBlock == null)
                targetBlock = Vec3D.c(entityEnderDragon.getWorld()
                        .getHighestBlockYAt(HeightMap.Type.e, islandBlockPosition).getHandle());

            double distance = targetBlock.c(entityEnderDragon.locX(), entityEnderDragon.locY(), entityEnderDragon.locZ());

            if (distance >= 100.0D && distance <= 22500.0D && !a.A && !a.B) {
                entityEnderDragon.setHealth(1.0F);
            } else {
                entityEnderDragon.setHealth(0.0F);
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
        private final Entity entityEnderDragon;

        private PathEntity currentPath;
        private Vec3D targetBlock;
        private boolean firstTick;

        IslandDragonControllerFly(IslandEntityEnderDragon entityEnderDragon) {
            super(entityEnderDragon);
            this.islandBlockPosition = entityEnderDragon.islandBlockPosition;
            this.entityEnderDragon = entityEnderDragon.entity;
        }

        @Override
        public void c() {
            if (!firstTick && currentPath != null) {
                BlockPosition highestBlock = entityEnderDragon.getWorld().getHighestBlockYAt(
                        HeightMap.Type.f, islandBlockPosition);
                if (!highestBlock.closerThan(entityEnderDragon.getPositionVector(), 10.0D)) {
                    entityEnderDragon.getDragonControllerManager().setControllerPhase(DragonControllerPhase.a);
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

            if (entityEnderDragon.getEnderDragonBattle() != null &&
                    entityEnderDragon.getEnderDragonBattle().getCrystalsAlive() > 0) {
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
                currentPath = PathEntity.ofNullable(DRAGON_FIND_PATH.invoke(this.a, closestNode, headClosestNode, null));
            } else {
                currentPath = entityEnderDragon.findPath(closestNode, headClosestNode, null);
            }

            targetBlock = navigateToNextPathNode(currentPath, entityEnderDragon, targetBlock);
        }

    }

    private static final class IslandDragonControllerHold extends DragonControllerHold {

        private static final PathfinderTargetCondition targetCondition = PathfinderTargetCondition.a().a(64.0D);

        private final BlockPosition islandBlockPosition;
        private final Entity entityEnderDragon;

        private PathEntity currentPath;
        private Vec3D targetBlock;
        private boolean clockwise;

        IslandDragonControllerHold(IslandEntityEnderDragon entityEnderDragon) {
            super(entityEnderDragon);
            this.islandBlockPosition = entityEnderDragon.islandBlockPosition;
            this.entityEnderDragon = entityEnderDragon.entity;
        }

        @Override
        public void c() {
            double distance = targetBlock == null ? 0.0D : targetBlock.c(entityEnderDragon.locX(),
                    entityEnderDragon.locY(), entityEnderDragon.locZ());
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
        public void a(EntityEnderCrystal entityEnderCrystal, net.minecraft.core.BlockPosition blockPosition,
                      DamageSource damageSource, EntityHuman entityHuman) {
            Entity entity = Entity.ofNullable(entityHuman);
            if (entity != null && !entity.isInvisible())
                strafePlayer(entityHuman);
        }

        private void findNewTarget() {
            if (currentPath != null && currentPath.isDone()) {
                BlockPosition highestBlock = entityEnderDragon.getWorld().getHighestBlockYAt(HeightMap.Type.f, islandBlockPosition);
                int crystalsCount = entityEnderDragon.getEnderDragonBattle() == null ? 0 :
                        entityEnderDragon.getEnderDragonBattle().getCrystalsAlive();
                if (entityEnderDragon.getRandom().nextInt(crystalsCount + 3) == 0) {
                    entityEnderDragon.getDragonControllerManager().setControllerPhase(DragonControllerPhase.c);
                    return;
                }

                double distance = 64.0D;
                Entity closestHuman = entityEnderDragon.getWorld().getNearestPlayer(targetCondition,
                        highestBlock.getX(), highestBlock.getY(), highestBlock.getZ());

                if (closestHuman != null)
                    distance = highestBlock.distSqr(closestHuman.getPositionVector()) / 512.0D;

                if (closestHuman != null && (entityEnderDragon.getRandom()
                        .nextInt(MathHelper.abs((int) distance) + 2) == 0 ||
                        entityEnderDragon.getRandom().nextInt(crystalsCount + 2) == 0)) {
                    strafePlayer((EntityHuman) closestHuman.getHandle());
                    return;
                }
            }

            if (currentPath == null || currentPath.isDone()) {
                int closestNode = this.a.q();
                int closestNode1 = closestNode;
                if (entityEnderDragon.getRandom().nextInt(8) == 0) {
                    clockwise = !clockwise;
                    closestNode1 = closestNode + 6;
                }

                if (clockwise) {
                    ++closestNode;
                } else {
                    --closestNode;
                }

                if (entityEnderDragon.getEnderDragonBattle() != null &&
                        entityEnderDragon.getEnderDragonBattle().getCrystalsAlive() >= 0) {
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
                    currentPath = PathEntity.ofNullable(DRAGON_FIND_PATH.invoke(this.a, closestNode1, closestNode, null));
                } else {
                    currentPath = entityEnderDragon.findPath(closestNode1, closestNode, null);
                }

                if (currentPath != null) {
                    currentPath.advance();
                }
            }

            targetBlock = navigateToNextPathNode(currentPath, entityEnderDragon, targetBlock);
        }

        private void strafePlayer(EntityHuman entityHuman) {
            entityEnderDragon.getDragonControllerManager().setControllerPhase(DragonControllerPhase.b);
            entityEnderDragon.getDragonControllerManager().getHandle().b(DragonControllerPhase.b).a(entityHuman);
        }

    }

    private static final class IslandDragonControllerLanding extends DragonControllerLanding {

        private final BlockPosition islandBlockPosition;
        private final Entity entityEnderDragon;

        private Vec3D targetBlock;

        IslandDragonControllerLanding(IslandEntityEnderDragon entityEnderDragon) {
            super(entityEnderDragon);
            this.islandBlockPosition = entityEnderDragon.islandBlockPosition;
            this.entityEnderDragon = entityEnderDragon.entity;
        }

        @Override
        public void b() {
            Vec3D vec3D = this.a.y(1.0F).d();
            vec3D.b(-0.7853982F);

            Entity dragonHead = entityEnderDragon.getEntityHeadComplexPart();

            double originLocX = dragonHead.locX();
            double originLocY = this.a.e.e(0.5D);
            double originLocZ = dragonHead.locZ();

            Random random = entityEnderDragon.getRandom();

            for (int i = 0; i < 8; i++) {
                double locX = originLocX + random.nextGaussian() / 2.0D;
                double locY = originLocY + random.nextGaussian() / 2.0D;
                double locZ = originLocZ + random.nextGaussian() / 2.0D;
                Vec3D mot = entityEnderDragon.getMot();
                entityEnderDragon.getWorld().addParticle(Particles.j, locX, locY, locZ,
                        -vec3D.b * 0.07999999821186066D + mot.b,
                        -vec3D.c * 0.30000001192092896D + mot.c,
                        -vec3D.d * 0.07999999821186066D + mot.d);
                vec3D.b(0.19634955F);
            }
        }

        @Override
        public void c() {
            if (targetBlock == null) {
                targetBlock = Vec3D.c(entityEnderDragon.getWorld().getHighestBlockYAt(HeightMap.Type.f,
                        islandBlockPosition).getHandle());
            }

            if (targetBlock.c(entityEnderDragon.locX(), entityEnderDragon.locY(), entityEnderDragon.locZ()) < 1.0D) {
                entityEnderDragon.getDragonControllerManager().getHandle().b(DragonControllerPhase.f).j();
                entityEnderDragon.getDragonControllerManager().setControllerPhase(DragonControllerPhase.g);
            }

        }

        @Override
        public float f() {
            return 1.5F;
        }

        @Override
        public float h() {
            // Turning speed
            float xzSquared = (float) entityEnderDragon.getMot().h() + 1.0F;
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
        private final Entity entityEnderDragon;

        private PathEntity currentPath;
        private Vec3D targetBlock;

        IslandDragonControllerLandingFly(IslandEntityEnderDragon entityEnderDragon) {
            super(entityEnderDragon);
            this.islandBlockPosition = entityEnderDragon.islandBlockPosition;
            this.entityEnderDragon = entityEnderDragon.entity;
        }

        @Override
        public void d() {
            currentPath = null;
            targetBlock = null;
        }

        @Override
        public void c() {
            double distance = targetBlock == null ? 0.0D :
                    targetBlock.c(entityEnderDragon.locX(), entityEnderDragon.locY(), entityEnderDragon.locZ());
            if (distance < 100.0D || distance > 22500.0D || this.a.A || this.a.B)
                findNewTarget();
        }

        public Vec3D g() {
            return targetBlock;
        }

        private void findNewTarget() {
            if (currentPath == null || currentPath.isDone()) {
                int closestNode = this.a.q();
                BlockPosition highestBlock = entityEnderDragon.getWorld().getHighestBlockYAt(HeightMap.Type.f, islandBlockPosition);
                Entity closestHuman = entityEnderDragon.getWorld().getNearestPlayer(targetCondition,
                        highestBlock.getX(), highestBlock.getY(), highestBlock.getZ());
                int closestNode1;

                if (closestHuman != null) {
                    Vec3D var4 = new Vec3D(closestHuman.locX(), 0.0D, closestHuman.locZ()).d();
                    closestNode1 = this.a.q(-var4.b * 40.0D, 105.0D, -var4.c * 40.0D);
                } else {
                    closestNode1 = this.a.q(40.0D, highestBlock.getY(), 0.0D);
                }

                PathPoint pathPoint = new PathPoint(highestBlock.getX(), highestBlock.getY(), highestBlock.getZ());

                if (DRAGON_FIND_PATH.isValid()) {
                    currentPath = PathEntity.ofNullable(DRAGON_FIND_PATH.invoke(this.a, closestNode, closestNode1, pathPoint));
                } else {
                    currentPath = entityEnderDragon.findPath(closestNode, closestNode1, pathPoint);
                }

                if (currentPath != null)
                    currentPath.advance();
            }

            targetBlock = navigateToNextPathNode(currentPath, entityEnderDragon, targetBlock);

            if (currentPath != null && currentPath.isDone())
                entityEnderDragon.getDragonControllerManager().setControllerPhase(DragonControllerPhase.d);
        }

    }

}
