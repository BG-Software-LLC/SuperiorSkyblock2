package com.bgsoftware.superiorskyblock.nms.v1_17_R1;

import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.nms.NMSDragonFight;
import com.google.common.collect.Sets;
import net.minecraft.advancements.CriterionTriggers;
import net.minecraft.core.BaseBlockPosition;
import net.minecraft.core.BlockPosition;
import net.minecraft.core.particles.Particles;
import net.minecraft.data.worldgen.BiomeDecoratorGroups;
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
import net.minecraft.world.level.World;
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
import net.minecraft.world.level.storage.WorldDataServer;
import net.minecraft.world.phys.AxisAlignedBB;
import net.minecraft.world.phys.Vec3D;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftEnderDragon;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

@SuppressWarnings({"unused", "NullableProblems"})
public final class NMSDragonFightImpl implements NMSDragonFight {

    private static final ReflectField<EnderDragonBattle> DRAGON_BATTLE = new ReflectField<>(
            EntityEnderDragon.class, EnderDragonBattle.class, "cn");
    private static final ReflectField<IDragonController> DRAGON_PHASE = new ReflectField<>(
            DragonControllerManager.class, IDragonController.class, "d");
    private static final ReflectMethod<PathEntity> DRAGON_FIND_PATH = new ReflectMethod<>(
            EntityEnderDragon.class, "a", int.class, int.class, PathPoint.class);

    static {
        DRAGON_BATTLE.removeFinal();
    }

    private final Map<UUID, EnderDragonBattle> activeBattles = new HashMap<>();

    @Override
    public void startDragonBattle(Island island, Location location) {
        WorldServer worldServer = ((CraftWorld) location.getWorld()).getHandle();
        IslandEnderDragonBattle islandEnderDragonBattle = new IslandEnderDragonBattle(island, worldServer, location);
        activeBattles.put(island.getUniqueId(), islandEnderDragonBattle);
    }

    @Override
    public void removeDragonBattle(Island island) {
        EnderDragonBattle enderDragonBattle = activeBattles.remove(island.getUniqueId());
        if(enderDragonBattle instanceof IslandEnderDragonBattle)
            ((IslandEnderDragonBattle) enderDragonBattle).removeBattlePlayers();
    }

    @Override
    public void tickBattles() {
        activeBattles.values().forEach(EnderDragonBattle::b);
    }

    @Override
    public void setDragonPhase(EnderDragon enderDragon, Object objectPhase) {
        EnderDragon.Phase phase = (EnderDragon.Phase) objectPhase;

        if(!(((CraftEnderDragon) enderDragon).getHandle() instanceof IslandEntityEnderDragon entityEnderDragon))
            return;

        switch (phase) {
            case DYING -> DRAGON_PHASE.set(entityEnderDragon.getDragonControllerManager(), new IslandDragonControllerDying(entityEnderDragon));
            case LEAVE_PORTAL -> DRAGON_PHASE.set(entityEnderDragon.getDragonControllerManager(), new IslandDragonControllerFly(entityEnderDragon));
            case CIRCLING -> DRAGON_PHASE.set(entityEnderDragon.getDragonControllerManager(), new IslandDragonControllerHold(entityEnderDragon));
            case LAND_ON_PORTAL -> DRAGON_PHASE.set(entityEnderDragon.getDragonControllerManager(), new IslandDragonControllerLanding(entityEnderDragon));
            case FLY_TO_PORTAL -> DRAGON_PHASE.set(entityEnderDragon.getDragonControllerManager(), new IslandDragonControllerLandingFly(entityEnderDragon));
        }
    }

    @Override
    public void awardTheEndAchievement(Player player) {
        Advancement advancement = Bukkit.getAdvancement(NamespacedKey.minecraft("end/root"));
        if(advancement != null)
            player.getAdvancementProgress(advancement).awardCriteria("");
    }

    private static final class IslandEntityEnderDragon extends EntityEnderDragon {

        private final BlockPosition islandBlockPosition;

        IslandEntityEnderDragon(World world, BlockPosition islandBlockPosition){
            super(null, world);
            this.islandBlockPosition = islandBlockPosition;
        }

        @Override
        public Vec3D y(float f) {
            IDragonController dragonController = getDragonControllerManager().a();
            DragonControllerPhase<? extends IDragonController> dragonControllerPhase = dragonController.getControllerPhase();
            float f1;
            Vec3D vec3d;

            if (dragonControllerPhase != DragonControllerPhase.d && dragonControllerPhase != DragonControllerPhase.e) {
                if (dragonController.a()) {
                    float f2 = this.getXRot();
                    this.setXRot(-45.0F);
                    vec3d = this.e(f);
                    this.setXRot(f2);
                } else {
                    vec3d = this.e(f);
                }
            } else {
                BlockPosition blockposition = this.t.getHighestBlockYAt(HeightMap.Type.f, islandBlockPosition);
                f1 = Math.max(MathHelper.c(blockposition.a(this.getPositionVector(), true)) / 4.0F, 1.0F);
                float f3 = 6.0F / f1;
                float f4 = this.getXRot();
                this.setXRot(-f3 * 1.5F * 5.0F);
                vec3d = this.e(f);
                this.setXRot(f4);
            }

            return vec3d;
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

        public IslandEnderDragonBattle(Island island, WorldServer worldServer, Location location){
            super(worldServer, ((WorldDataServer) worldServer.getWorldData()).getGeneratorSettings().getSeed(), new NBTTagCompound());
            this.islandBlockPosition = new BlockPosition(location.getX(), location.getY(), location.getZ());
            this.islandChunkCoord = new ChunkCoordIntPair(islandBlockPosition);
            this.island = island;

            int radius = plugin.getSettings().getMaxIslandSize();
            this.borderArea = new AxisAlignedBB(islandBlockPosition.c(-radius, -radius, -radius),
                    islandBlockPosition.c(radius, radius, radius));

            spawnEnderDragon();
        }

        @Override
        public NBTTagCompound a() {
            NBTTagCompound nbtTagCompound = new NBTTagCompound();

            if (this.u != null) {
                nbtTagCompound.a("Dragon", this.u);
            }

            nbtTagCompound.setBoolean("DragonKilled", this.dragonKilled);
            nbtTagCompound.setBoolean("PreviouslyKilled", this.previouslyKilled);

            if (this.w != null) {
                nbtTagCompound.set("ExitPortalLocation", GameProfileSerializer.a(this.w));
            }

            NBTTagList nbtTagList = new NBTTagList();
            for(Integer gateway : this.gateways)
                nbtTagList.add(NBTTagInt.a(gateway));

            nbtTagCompound.set("Gateways", nbtTagList);

            return nbtTagCompound;
        }

        @Override
        public void b() {
            this.k.setVisible(!dragonKilled);

            // Update battle players
            if (++currentTick >= 20) {
                updateBattlePlayers();
                currentTick = 0;
            }

            if(this.k.getPlayers().isEmpty()){
                this.l.getChunkProvider().removeTicket(TicketType.b, islandChunkCoord, 9, Unit.a);
                return;
            }

            this.l.getChunkProvider().addTicket(TicketType.b, islandChunkCoord, 9, Unit.a);

            boolean tickingChunks = areChunkTicking();

            if (this.x != null && tickingChunks) {
                if (crystalsList == null) {
                    this.x = null;
                    initiateRespawn();
                }

                if (++crystalsCountTick >= 100) {
                    countCrystals();
                    crystalsCountTick = 0;
                }

                this.x.a(this.l, this, crystalsList, respawnTick++, this.w);
            }
        }

        @Override
        public void setRespawnPhase(EnumDragonRespawn dragonRespawn) {
            if (this.x == null)
                throw new IllegalStateException("Dragon respawn isn't in progress, can't skip ahead in the animation.");

            respawnTick = 0;

            if(dragonRespawn != EnumDragonRespawn.e){
                this.x = dragonRespawn;
            }

            else{
                this.x = null;
                dragonKilled = false;
                EntityEnderDragon entityEnderDragon = spawnEnderDragon();
                for(EntityPlayer entityPlayer : this.k.getPlayers())
                    CriterionTriggers.n.a(entityPlayer, entityEnderDragon);
            }
        }

        @Nullable
        @Override
        public ShapeDetector.ShapeDetectorCollection getExitPortalShape() {
            for(int x = -8; x <= 8; x++) {
                for(int z = -8; z <= 8; z++) {
                    Chunk chunk = this.l.getChunkAt(islandChunkCoord.b + x, islandChunkCoord.c + z);

                    for (TileEntity tileEntity : chunk.getTileEntities().values()) {
                        if (tileEntity instanceof TileEntityEnderPortal) {
                            ShapeDetector.ShapeDetectorCollection shapeDetectorCollection =
                                    portalShape.a(this.l, tileEntity.getPosition());
                            if (shapeDetectorCollection != null) {
                                BlockPosition blockposition = shapeDetectorCollection.a(3, 3, 3).getPosition();
                                if (this.w == null && blockposition.getX() == 0 && blockposition.getZ() == 0) {
                                    this.w = blockposition;
                                }

                                return shapeDetectorCollection;
                            }
                        }
                    }
                }
            }

            int highestBlock = this.l.getHighestBlockYAt(HeightMap.Type.e, islandBlockPosition).getY();

            for(int y = highestBlock; y >= 0; y--) {
                ShapeDetector.ShapeDetectorCollection shapeDetectorCollection = portalShape.a(
                        this.l, new BlockPosition(islandBlockPosition.getX(), y, islandBlockPosition.getZ()));
                if (shapeDetectorCollection != null) {
                    if (this.w == null) {
                        this.w = shapeDetectorCollection.a(3, 3, 3).getPosition();
                    }

                    return shapeDetectorCollection;
                }
            }

            return null;
        }

        @Override
        public void a(EntityEnderDragon entityEnderDragon) {
            if(!entityEnderDragon.getUniqueID().equals(this.u))
                return;

            this.k.setProgress(0.0F);
            this.k.setVisible(false);
            this.generateExitPortal(true);

            if (!gateways.isEmpty()) {
                int i = gateways.remove(gateways.size() - 1);
                int j = MathHelper.floor(96.0D * Math.cos(2.0D * (-3.141592653589793D + 0.15707963267948966D * (double)i)));
                int k = MathHelper.floor(96.0D * Math.sin(2.0D * (-3.141592653589793D + 0.15707963267948966D * (double)i)));
                BlockPosition blockPosition = new BlockPosition(j, 75, k);
                this.l.triggerEffect(3000, blockPosition, 0);
                BiomeDecoratorGroups.c.a(this.l, this.l.getChunkProvider().getChunkGenerator(),
                        new Random(), blockPosition);
            }

            if (!previouslyKilled) {
                this.l.setTypeUpdate(this.l.getHighestBlockYAt(HeightMap.Type.e, islandBlockPosition),
                        Blocks.er.getBlockData());
            }

            previouslyKilled = true;
            dragonKilled = true;
        }

        @Override
        public void generateExitPortal(boolean flag) {
            WorldGenEndTrophy worldGenEndTrophy = new WorldGenEndTrophy(flag);

            if (this.w == null) {
                this.w = this.l.getHighestBlockYAt(HeightMap.Type.f, islandBlockPosition).down();
                while (this.l.getType(this.w).a(Blocks.z) && this.w.getY() > this.l.getSeaLevel())
                    this.w = this.w.down();
            }

            worldGenEndTrophy.b(WorldGenFeatureConfiguration.m).a(this.l, this.l.getChunkProvider().getChunkGenerator(),
                    new Random(), this.w.up(2));
        }

        @Override
        public void b(EntityEnderDragon entityEnderDragon) {
            if(!entityEnderDragon.getUniqueID().equals(this.u))
                return;

            this.k.setProgress(entityEnderDragon.getHealth() / entityEnderDragon.getMaxHealth());

            if (entityEnderDragon.hasCustomName())
                this.k.a(entityEnderDragon.getScoreboardDisplayName());
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
                resetCrystals();
                generateExitPortal(true);
            }
            else {
                countCrystals();
                Entity entity = this.l.getEntity(this.u);
                if (entity instanceof EntityEnderDragon)
                    ((EntityEnderDragon)entity).a(entityEnderCrystal, entityEnderCrystal.getChunkCoordinates(), damageSource);
            }
        }

        @Override
        public boolean isPreviouslyKilled() {
            return previouslyKilled;
        }

        @Override
        public void initiateRespawn() {
            if(!dragonKilled || this.x != null)
                return;

            crystalsList = this.l.a(EntityEnderCrystal.class, borderArea);

            this.x = EnumDragonRespawn.a;
            respawnTick = 0;
            generateExitPortal(false);
        }

        @Override
        public void resetCrystals() {
            for(EntityEnderCrystal entityEnderCrystal : this.l.a(EntityEnderCrystal.class, borderArea)){
                entityEnderCrystal.setInvulnerable(false);
                entityEnderCrystal.setBeamTarget(null);
            }
        }

        public void removeBattlePlayers(){
            for(EntityPlayer entityPlayer : this.k.getPlayers())
                this.k.removePlayer(entityPlayer);
        }

        private boolean areChunkTicking() {
            for(int i = -8; i <= 8; ++i) {
                for(int j = 8; j <= 8; ++j) {
                    IChunkAccess chunkAccess = this.l.getChunkAt(islandChunkCoord.b + i, islandChunkCoord.c + j,
                            ChunkStatus.m, false);

                    if (!(chunkAccess instanceof Chunk) || !((Chunk) chunkAccess).getState()
                            .isAtLeast(PlayerChunk.State.c)) {
                        return false;
                    }
                }
            }

            return true;
        }

        private void updateBattlePlayers(){
            Set<EntityPlayer> nearbyPlayers = Sets.newHashSet();

            for(SuperiorPlayer superiorPlayer : island.getAllPlayersInside()){
                Player player = superiorPlayer.asPlayer();
                assert player != null;
                if(((CraftWorld) player.getWorld()).getHandle() == this.l){
                    EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
                    this.k.addPlayer(entityPlayer);
                    nearbyPlayers.add(entityPlayer);
                }
            }

            for(EntityPlayer entityPlayer : this.k.getPlayers()){
                if(!nearbyPlayers.contains(entityPlayer))
                    this.k.removePlayer(entityPlayer);
            }
        }

        private EntityEnderDragon spawnEnderDragon(){
            EntityEnderDragon entityEnderDragon = new IslandEntityEnderDragon(this.l, islandBlockPosition);
            entityEnderDragon.getDragonControllerManager().setControllerPhase(DragonControllerPhase.a);
            entityEnderDragon.setPositionRotation(islandBlockPosition.getX(), 128, islandBlockPosition.getZ(),
                    this.l.w.nextFloat() * 360.0F, 0.0F);
            DRAGON_BATTLE.set(entityEnderDragon, this);

            this.l.addEntity(entityEnderDragon, CreatureSpawnEvent.SpawnReason.NATURAL);

            this.u = entityEnderDragon.getUniqueID();
            resetCrystals();

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

        IslandDragonControllerDying(IslandEntityEnderDragon entityEnderDragon){
            super(entityEnderDragon);
            this.islandBlockPosition = entityEnderDragon.islandBlockPosition;
        }

        @Override
        public void b() {
            if (currentTick++ % 10 == 0) {
                float offsetX = (this.a.getRandom().nextFloat() - 0.5F) * 8.0F;
                float offsetY = (this.a.getRandom().nextFloat() - 0.5F) * 4.0F;
                float offsetZ = (this.a.getRandom().nextFloat() - 0.5F) * 8.0F;
                this.a.t.addParticle(
                        Particles.x,
                        this.a.locX() + offsetX,
                        this.a.locY() + 2.0D + offsetY,
                        this.a.locZ() + offsetZ,
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
                targetBlock = Vec3D.c(this.a.t.getHighestBlockYAt(HeightMap.Type.e, islandBlockPosition));

            double distance = targetBlock.c(a.locX(), a.locY(), a.locZ());

            if (distance >= 100.0D && distance <= 22500.0D && !a.A && !a.B) {
                this.a.setHealth(1.0F);
            } else {
                this.a.setHealth(0.0F);
            }

        }

        @Override
        public void d() {
            targetBlock = null;
            currentTick = 0;
        }

        @Nullable
        public Vec3D g() {
            return this.targetBlock;
        }

    }

    private static final class IslandDragonControllerFly extends DragonControllerFly {

        private final BlockPosition islandBlockPosition;

        private PathEntity currentPath;
        private Vec3D targetBlock;
        private boolean firstTick;

        IslandDragonControllerFly(IslandEntityEnderDragon entityEnderDragon){
            super(entityEnderDragon);
            this.islandBlockPosition = entityEnderDragon.islandBlockPosition;
        }

        @Override
        public void c() {
            if (!firstTick && currentPath != null) {
                BlockPosition highestBlock = this.a.t.getHighestBlockYAt(HeightMap.Type.f, islandBlockPosition);
                if (!highestBlock.a(this.a.getPositionVector(), 10.0D)) {
                    this.a.getDragonControllerManager().setControllerPhase(DragonControllerPhase.a);
                }
            }
            else {
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

        @Nullable
        public Vec3D g() {
            return this.targetBlock;
        }

        private void findNewTarget() {
            int closestNode = a.p();
            Vec3D headLookVector = a.y(1.0F);

            int headClosestNode = a.q(-headLookVector.b * 40.0D, 105.0D, -headLookVector.c * 40.0D);

            if (a.getEnderDragonBattle() != null && a.getEnderDragonBattle().c() > 0) {
                headClosestNode %= 12;
                if (headClosestNode < 0) {
                    headClosestNode += 12;
                }
            } else {
                headClosestNode -= 12;
                headClosestNode &= 7;
                headClosestNode += 12;
            }

            if(DRAGON_FIND_PATH.isValid()){
                currentPath = DRAGON_FIND_PATH.invoke(this.a, closestNode, headClosestNode, null);
            }
            else{
                currentPath = this.a.findPath(closestNode, headClosestNode, null);
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

        IslandDragonControllerHold(IslandEntityEnderDragon entityEnderDragon){
            super(entityEnderDragon);
            this.islandBlockPosition = entityEnderDragon.islandBlockPosition;
        }

        @Override
        public void c() {
            double distance = targetBlock == null ? 0.0D : targetBlock.c(this.a.locX(), this.a.locY(), this.a.locZ());
            if (distance < 100.0D || distance > 22500.0D || this.a.A || this.a.B)
                findNewTarget();
        }

        @Override
        public void d() {
            currentPath = null;
            targetBlock = null;
        }

        @Nullable
        public Vec3D g() {
            return targetBlock;
        }

        @Override
        public void a(EntityEnderCrystal entityEnderCrystal, BlockPosition blockPosition, DamageSource damageSource, @Nullable EntityHuman entityHuman) {
            if (entityHuman != null && !entityHuman.isInvisible())
                strafePlayer(entityHuman);
        }

        private void findNewTarget() {
            if (currentPath != null && currentPath.c()) {
                BlockPosition highestBlock = this.a.t.getHighestBlockYAt(HeightMap.Type.f, islandBlockPosition);
                int crystalsCount = this.a.getEnderDragonBattle() == null ? 0 : this.a.getEnderDragonBattle().c();
                if (this.a.getRandom().nextInt(crystalsCount + 3) == 0) {
                    this.a.getDragonControllerManager().setControllerPhase(DragonControllerPhase.c);
                    return;
                }

                double distance = 64.0D;
                EntityHuman closestHuman = this.a.t.a(targetCondition, highestBlock.getX(), highestBlock.getY(), highestBlock.getZ());
                if (closestHuman != null)
                    distance = highestBlock.a(closestHuman.getPositionVector(), true) / 512.0D;

                if (closestHuman != null && (this.a.getRandom().nextInt(MathHelper.a((int)distance) + 2) == 0 ||
                        this.a.getRandom().nextInt(crystalsCount + 2) == 0)) {
                    strafePlayer(closestHuman);
                    return;
                }
            }

            if (currentPath == null || currentPath.c()) {
                int closestNode = this.a.p();
                int closestNode1 = closestNode;
                if (this.a.getRandom().nextInt(8) == 0) {
                    clockwise = !clockwise;
                    closestNode1 = closestNode + 6;
                }

                if (clockwise) {
                    ++closestNode;
                } else {
                    --closestNode;
                }

                if (this.a.getEnderDragonBattle() != null && this.a.getEnderDragonBattle().c() >= 0) {
                    closestNode %= 12;
                    if (closestNode < 0) {
                        closestNode += 12;
                    }
                } else {
                    closestNode -= 12;
                    closestNode &= 7;
                    closestNode += 12;
                }

                if(DRAGON_FIND_PATH.isValid()){
                    currentPath = DRAGON_FIND_PATH.invoke(this.a, closestNode1, closestNode, null);
                }
                else{
                    currentPath = this.a.findPath(closestNode1, closestNode, null);
                }

                if (currentPath != null) {
                    currentPath.a();
                }
            }

            targetBlock = navigateToNextPathNode(currentPath, this.a, targetBlock);
        }

        private void strafePlayer(EntityHuman entityHuman) {
            this.a.getDragonControllerManager().setControllerPhase(DragonControllerPhase.b);
            this.a.getDragonControllerManager().b(DragonControllerPhase.b).a(entityHuman);
        }

    }

    private static final class IslandDragonControllerLanding extends DragonControllerLanding {

        private final BlockPosition islandBlockPosition;

        private Vec3D targetBlock;

        IslandDragonControllerLanding(IslandEntityEnderDragon entityEnderDragon){
            super(entityEnderDragon);
            this.islandBlockPosition = entityEnderDragon.islandBlockPosition;
        }

        @Override
        public void b() {
            Vec3D vec3D = this.a.y(1.0F).d();
            vec3D.b(-0.7853982F);

            double originLocX = this.a.e.locX();
            double originLocY = this.a.e.e(0.5D);
            double originLocZ = this.a.e.locZ();

            Random random = this.a.getRandom();

            for(int i = 0; i < 8; i++) {
                double locX = originLocX + random.nextGaussian() / 2.0D;
                double locY = originLocY + random.nextGaussian() / 2.0D;
                double locZ = originLocZ + random.nextGaussian() / 2.0D;
                Vec3D mot = this.a.getMot();
                this.a.t.addParticle(Particles.j, locX, locY, locZ, -vec3D.b * 0.07999999821186066D + mot.b,
                        -vec3D.c * 0.30000001192092896D + mot.c, -vec3D.d * 0.07999999821186066D + mot.d);
                vec3D.b(0.19634955F);
            }
        }

        @Override
        public void c() {
            if (targetBlock == null) {
                targetBlock = Vec3D.c(this.a.t.getHighestBlockYAt(HeightMap.Type.f, islandBlockPosition));
            }

            if (targetBlock.c(this.a.locX(), this.a.locY(), this.a.locZ()) < 1.0D) {
                this.a.getDragonControllerManager().b(DragonControllerPhase.f).j();
                this.a.getDragonControllerManager().setControllerPhase(DragonControllerPhase.g);
            }

        }

        @Override
        public float f() {
            return 1.5F;
        }

        @Override
        public float h() {
            // Turning speed
            float xzSquared = (float)this.a.getMot().h() + 1.0F;
            return Math.min(xzSquared, 40.0F) / xzSquared;
        }

        @Override
        public void d() {
            targetBlock = null;
        }

        @Nullable
        public Vec3D g() {
            return targetBlock;
        }

    }

    private static final class IslandDragonControllerLandingFly extends DragonControllerLandingFly {

        private static final PathfinderTargetCondition targetCondition = PathfinderTargetCondition.a().a(128.0D);

        private final BlockPosition islandBlockPosition;

        private PathEntity currentPath;
        private Vec3D targetBlock;

        IslandDragonControllerLandingFly(IslandEntityEnderDragon entityEnderDragon){
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
            double distance = targetBlock == null ? 0.0D : targetBlock.c(this.a.locX(), this.a.locY(), this.a.locZ());
            if (distance < 100.0D || distance > 22500.0D || this.a.A || this.a.B)
                findNewTarget();
        }

        @Nullable
        public Vec3D g() {
            return targetBlock;
        }

        private void findNewTarget() {
            if (currentPath== null || currentPath.c()) {
                int closestNode = this.a.eI();
                BlockPosition highestBlock = this.a.t.getHighestBlockYAt(HeightMap.Type.f, islandBlockPosition);
                EntityHuman closestHuman = this.a.t.a(targetCondition, highestBlock.getX(), highestBlock.getY(), highestBlock.getZ());
                int closestNode1;
                if (closestHuman != null) {
                    Vec3D var4 = (new Vec3D(closestHuman.locX(), 0.0D, closestHuman.locZ())).d();
                    closestNode1 = this.a.q(-var4.b * 40.0D, 105.0D, -var4.c * 40.0D);
                } else {
                    closestNode1 = this.a.q(40.0D, highestBlock.getY(), 0.0D);
                }

                PathPoint var4 = new PathPoint(highestBlock.getX(), highestBlock.getY(), highestBlock.getZ());

                if(DRAGON_FIND_PATH.isValid()){
                    currentPath = DRAGON_FIND_PATH.invoke(this.a, closestNode, closestNode1, var4);
                }
                else{
                    currentPath = this.a.findPath(closestNode, closestNode1, var4);
                }

                if (currentPath != null)
                    currentPath.a();
            }

            targetBlock = navigateToNextPathNode(currentPath, this.a, targetBlock);

            if (currentPath != null && currentPath.c())
                this.a.getDragonControllerManager().setControllerPhase(DragonControllerPhase.d);
        }

    }

    private static Vec3D navigateToNextPathNode(PathEntity currentPath, EntityEnderDragon entityEnderDragon, Vec3D currentTargetBlock) {
        if (currentPath != null && !currentPath.c()) {
            BaseBlockPosition basePosition = currentPath.g();
            currentPath.a();

            double y;
            do {
                y = basePosition.getY() + entityEnderDragon.getRandom().nextFloat() * 20.0F;
            } while(y < basePosition.getY());

            return new Vec3D(basePosition.getX(), y, basePosition.getZ());
        }

        return currentTargetBlock;
    }

}
