package com.bgsoftware.superiorskyblock.nms;

import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.google.common.collect.Sets;
import net.minecraft.server.v1_16_R3.AxisAlignedBB;
import net.minecraft.server.v1_16_R3.BaseBlockPosition;
import net.minecraft.server.v1_16_R3.BiomeDecoratorGroups;
import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.BlockPredicate;
import net.minecraft.server.v1_16_R3.Blocks;
import net.minecraft.server.v1_16_R3.Chunk;
import net.minecraft.server.v1_16_R3.ChunkCoordIntPair;
import net.minecraft.server.v1_16_R3.ChunkStatus;
import net.minecraft.server.v1_16_R3.CriterionTriggers;
import net.minecraft.server.v1_16_R3.DamageSource;
import net.minecraft.server.v1_16_R3.DragonControllerDying;
import net.minecraft.server.v1_16_R3.DragonControllerFly;
import net.minecraft.server.v1_16_R3.DragonControllerHold;
import net.minecraft.server.v1_16_R3.DragonControllerLanding;
import net.minecraft.server.v1_16_R3.DragonControllerLandingFly;
import net.minecraft.server.v1_16_R3.DragonControllerManager;
import net.minecraft.server.v1_16_R3.DragonControllerPhase;
import net.minecraft.server.v1_16_R3.EnderDragonBattle;
import net.minecraft.server.v1_16_R3.Entity;
import net.minecraft.server.v1_16_R3.EntityEnderCrystal;
import net.minecraft.server.v1_16_R3.EntityEnderDragon;
import net.minecraft.server.v1_16_R3.EntityHuman;
import net.minecraft.server.v1_16_R3.EntityPlayer;
import net.minecraft.server.v1_16_R3.EnumDragonRespawn;
import net.minecraft.server.v1_16_R3.GameProfileSerializer;
import net.minecraft.server.v1_16_R3.HeightMap;
import net.minecraft.server.v1_16_R3.IChunkAccess;
import net.minecraft.server.v1_16_R3.IDragonController;
import net.minecraft.server.v1_16_R3.MathHelper;
import net.minecraft.server.v1_16_R3.NBTTagCompound;
import net.minecraft.server.v1_16_R3.NBTTagInt;
import net.minecraft.server.v1_16_R3.NBTTagList;
import net.minecraft.server.v1_16_R3.Particles;
import net.minecraft.server.v1_16_R3.PathEntity;
import net.minecraft.server.v1_16_R3.PathPoint;
import net.minecraft.server.v1_16_R3.PathfinderTargetCondition;
import net.minecraft.server.v1_16_R3.PlayerChunk;
import net.minecraft.server.v1_16_R3.ShapeDetector;
import net.minecraft.server.v1_16_R3.ShapeDetectorBlock;
import net.minecraft.server.v1_16_R3.ShapeDetectorBuilder;
import net.minecraft.server.v1_16_R3.TicketType;
import net.minecraft.server.v1_16_R3.TileEntity;
import net.minecraft.server.v1_16_R3.TileEntityEnderPortal;
import net.minecraft.server.v1_16_R3.Unit;
import net.minecraft.server.v1_16_R3.Vec3D;
import net.minecraft.server.v1_16_R3.World;
import net.minecraft.server.v1_16_R3.WorldGenEndTrophy;
import net.minecraft.server.v1_16_R3.WorldGenFeatureConfiguration;
import net.minecraft.server.v1_16_R3.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftEnderDragon;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
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

@SuppressWarnings("unused")
public final class NMSDragonFight_v1_16_R3 implements NMSDragonFight {

    private static final ReflectField<EnderDragonBattle> DRAGON_BATTLE = new ReflectField<>(EntityEnderDragon.class, EnderDragonBattle.class, "bF");
    private static final ReflectField<IDragonController> DRAGON_PHASE = new ReflectField<>(DragonControllerManager.class, IDragonController.class, "currentDragonController");

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

        if(!(((CraftEnderDragon) enderDragon).getHandle() instanceof IslandEntityEnderDragon))
            return;

        IslandEntityEnderDragon entityEnderDragon = (IslandEntityEnderDragon) ((CraftEnderDragon) enderDragon).getHandle();

        switch (phase){
            case DYING:
                DRAGON_PHASE.set(entityEnderDragon.getDragonControllerManager(), new IslandDragonControllerDying(entityEnderDragon));
                break;
            case LEAVE_PORTAL:
                DRAGON_PHASE.set(entityEnderDragon.getDragonControllerManager(), new IslandDragonControllerFly(entityEnderDragon));
                break;
            case CIRCLING:
                DRAGON_PHASE.set(entityEnderDragon.getDragonControllerManager(), new IslandDragonControllerHold(entityEnderDragon));
                break;
            case LAND_ON_PORTAL:
                DRAGON_PHASE.set(entityEnderDragon.getDragonControllerManager(), new IslandDragonControllerLanding(entityEnderDragon));
                break;
            case FLY_TO_PORTAL:
                DRAGON_PHASE.set(entityEnderDragon.getDragonControllerManager(), new IslandDragonControllerLandingFly(entityEnderDragon));
                break;
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
        public Vec3D x(float f) {
            IDragonController dragonController = getDragonControllerManager().a();
            DragonControllerPhase<? extends IDragonController> dragonControllerPhase = dragonController.getControllerPhase();
            float f1;
            Vec3D vec3d;

            if (dragonControllerPhase != DragonControllerPhase.LANDING && dragonControllerPhase != DragonControllerPhase.TAKEOFF) {
                if (dragonController.a()) {
                    float f2 = this.pitch;
                    this.pitch = -45.0F;
                    vec3d = this.f(f);
                    this.pitch = f2;
                } else {
                    vec3d = this.f(f);
                }
            } else {
                BlockPosition blockposition = this.world.getHighestBlockYAt(HeightMap.Type.MOTION_BLOCKING_NO_LEAVES, islandBlockPosition);
                f1 = Math.max(MathHelper.sqrt(blockposition.a(this.getPositionVector(), true)) / 4.0F, 1.0F);
                float f3 = 6.0F / f1;
                float f4 = this.pitch;
                this.pitch = -f3 * 1.5F * 5.0F;
                vec3d = this.f(f);
                this.pitch = f4;
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
                .a('#', ShapeDetectorBlock.a(BlockPredicate.a(Blocks.BEDROCK)))
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
            super(worldServer, worldServer.worldDataServer.getGeneratorSettings().getSeed(), new NBTTagCompound());
            this.islandBlockPosition = new BlockPosition(location.getX(), location.getY(), location.getZ());
            this.islandChunkCoord = new ChunkCoordIntPair(islandBlockPosition);
            this.island = island;

            int radius = plugin.getSettings().maxIslandSize;
            this.borderArea = new AxisAlignedBB(islandBlockPosition.add(-radius, -radius, -radius), islandBlockPosition.add(radius, radius, radius));

            spawnEnderDragon();
        }

        @Override
        public NBTTagCompound a() {
            NBTTagCompound nbtTagCompound = new NBTTagCompound();

            if (this.dragonUUID != null) {
                nbtTagCompound.a("Dragon", this.dragonUUID);
            }

            nbtTagCompound.setBoolean("DragonKilled", this.dragonKilled);
            nbtTagCompound.setBoolean("PreviouslyKilled", this.previouslyKilled);

            if (this.exitPortalLocation != null) {
                nbtTagCompound.set("ExitPortalLocation", GameProfileSerializer.a(this.exitPortalLocation));
            }

            NBTTagList nbtTagList = new NBTTagList();
            for(Integer gateway : this.gateways)
                nbtTagList.add(NBTTagInt.a(gateway));

            nbtTagCompound.set("Gateways", nbtTagList);

            return nbtTagCompound;
        }

        @Override
        public void b() {
            bossBattle.setVisible(!dragonKilled);

            // Update battle players
            if (++currentTick >= 20) {
                updateBattlePlayers();
                currentTick = 0;
            }

            if(bossBattle.getPlayers().isEmpty()){
                world.getChunkProvider().removeTicket(TicketType.DRAGON, islandChunkCoord, 9, Unit.INSTANCE);
                return;
            }

            world.getChunkProvider().addTicket(TicketType.DRAGON, islandChunkCoord, 9, Unit.INSTANCE);

            boolean tickingChunks = areChunkTicking();

            if (respawnPhase != null && tickingChunks) {
                if (crystalsList == null) {
                    respawnPhase = null;
                    initiateRespawn();
                }

                if (++crystalsCountTick >= 100) {
                    countCrystals();
                    crystalsCountTick = 0;
                }

                respawnPhase.a(world, this, crystalsList, respawnTick++, exitPortalLocation);
            }
        }

        @Override
        public void setRespawnPhase(EnumDragonRespawn dragonRespawn) {
            if (respawnPhase == null)
                throw new IllegalStateException("Dragon respawn isn't in progress, can't skip ahead in the animation.");

            respawnTick = 0;

            if(dragonRespawn != EnumDragonRespawn.END){
                respawnPhase = dragonRespawn;
            }

            else{
                respawnPhase = null;
                dragonKilled = false;
                EntityEnderDragon entityEnderDragon = spawnEnderDragon();
                for(EntityPlayer entityPlayer : bossBattle.getPlayers())
                    CriterionTriggers.n.a(entityPlayer, entityEnderDragon);
            }
        }

        @Nullable
        @Override
        public ShapeDetector.ShapeDetectorCollection getExitPortalShape() {
            for(int x = -8; x <= 8; x++) {
                for(int z = -8; z <= 8; z++) {
                    Chunk chunk = this.world.getChunkAt(islandChunkCoord.x + x, islandChunkCoord.z + z);

                    for (TileEntity tileEntity : chunk.getTileEntities().values()) {
                        if (tileEntity instanceof TileEntityEnderPortal) {
                            ShapeDetector.ShapeDetectorCollection shapeDetectorCollection = portalShape.a(this.world, tileEntity.getPosition());
                            if (shapeDetectorCollection != null) {
                                BlockPosition blockposition = shapeDetectorCollection.a(3, 3, 3).getPosition();
                                if (this.exitPortalLocation == null && blockposition.getX() == 0 && blockposition.getZ() == 0) {
                                    this.exitPortalLocation = blockposition;
                                }

                                return shapeDetectorCollection;
                            }
                        }
                    }
                }
            }

            int highestBlock = this.world.getHighestBlockYAt(HeightMap.Type.MOTION_BLOCKING, islandBlockPosition).getY();

            for(int y = highestBlock; y >= 0; y--) {
                ShapeDetector.ShapeDetectorCollection shapeDetectorCollection = portalShape.a(
                        this.world, new BlockPosition(islandBlockPosition.getX(), y, islandBlockPosition.getZ()));
                if (shapeDetectorCollection != null) {
                    if (this.exitPortalLocation == null) {
                        this.exitPortalLocation = shapeDetectorCollection.a(3, 3, 3).getPosition();
                    }

                    return shapeDetectorCollection;
                }
            }

            return null;
        }

        @Override
        public void a(EntityEnderDragon entityEnderDragon) {
            if(!entityEnderDragon.getUniqueID().equals(this.dragonUUID))
                return;

            this.bossBattle.setProgress(0.0F);
            this.bossBattle.setVisible(false);
            this.generateExitPortal(true);

            if (!gateways.isEmpty()) {
                int i = gateways.remove(gateways.size() - 1);
                int j = MathHelper.floor(96.0D * Math.cos(2.0D * (-3.141592653589793D + 0.15707963267948966D * (double)i)));
                int k = MathHelper.floor(96.0D * Math.sin(2.0D * (-3.141592653589793D + 0.15707963267948966D * (double)i)));
                BlockPosition blockPosition = new BlockPosition(j, 75, k);
                world.triggerEffect(3000, blockPosition, 0);
                BiomeDecoratorGroups.END_GATEWAY_DELAYED.a(world, world.getChunkProvider().getChunkGenerator(), new Random(), blockPosition);
            }

            if (!previouslyKilled) {
                world.setTypeUpdate(this.world.getHighestBlockYAt(HeightMap.Type.MOTION_BLOCKING, islandBlockPosition), Blocks.DRAGON_EGG.getBlockData());
            }

            previouslyKilled = true;
            dragonKilled = true;
        }

        @Override
        public void generateExitPortal(boolean flag) {
            WorldGenEndTrophy worldGenEndTrophy = new WorldGenEndTrophy(flag);

            if (exitPortalLocation == null) {
                exitPortalLocation = world.getHighestBlockYAt(HeightMap.Type.MOTION_BLOCKING_NO_LEAVES, islandBlockPosition).down();
                while (world.getType(exitPortalLocation).a(Blocks.BEDROCK) && exitPortalLocation.getY() > world.getSeaLevel())
                    exitPortalLocation = exitPortalLocation.down();
            }

            worldGenEndTrophy.b(WorldGenFeatureConfiguration.k).a(world, world.getChunkProvider().getChunkGenerator(), new Random(), exitPortalLocation.up(2));
        }

        @Override
        public void b(EntityEnderDragon entityEnderDragon) {
            if(!entityEnderDragon.getUniqueID().equals(this.dragonUUID))
                return;

            bossBattle.setProgress(entityEnderDragon.getHealth() / entityEnderDragon.getMaxHealth());

            if (entityEnderDragon.hasCustomName())
                this.bossBattle.a(entityEnderDragon.getScoreboardDisplayName());
        }

        @Override
        public int c() {
            return this.crystalsCount;
        }

        @Override
        public void a(EntityEnderCrystal entityEnderCrystal, DamageSource damageSource) {
            if (respawnPhase != null && crystalsList.contains(entityEnderCrystal)) {
                respawnPhase = null;
                respawnTick = 0;
                resetCrystals();
                generateExitPortal(true);
            }
            else {
                countCrystals();
                Entity entity = world.getEntity(dragonUUID);
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
            if(!dragonKilled || respawnPhase != null)
                return;

            crystalsList = world.a(EntityEnderCrystal.class, borderArea);

            respawnPhase = EnumDragonRespawn.START;
            respawnTick = 0;
            generateExitPortal(false);
        }

        @Override
        public void resetCrystals() {
            for(EntityEnderCrystal entityEnderCrystal : this.world.a(EntityEnderCrystal.class, borderArea)){
                entityEnderCrystal.setInvulnerable(false);
                entityEnderCrystal.setBeamTarget(null);
            }
        }

        public void removeBattlePlayers(){
            for(EntityPlayer entityPlayer : bossBattle.getPlayers())
                bossBattle.removePlayer(entityPlayer);
        }

        private boolean areChunkTicking() {
            for(int i = -8; i <= 8; ++i) {
                for(int j = 8; j <= 8; ++j) {
                    IChunkAccess chunkAccess = world.getChunkAt(islandChunkCoord.x + i, islandChunkCoord.z + j, ChunkStatus.FULL, false);

                    if (!(chunkAccess instanceof Chunk) || !((Chunk) chunkAccess).getState().isAtLeast(PlayerChunk.State.TICKING)) {
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
                if(((CraftWorld) player.getWorld()).getHandle() == world){
                    EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
                    bossBattle.addPlayer(entityPlayer);
                    nearbyPlayers.add(entityPlayer);
                }
            }

            for(EntityPlayer entityPlayer : bossBattle.getPlayers()){
                if(!nearbyPlayers.contains(entityPlayer))
                    bossBattle.removePlayer(entityPlayer);
            }
        }

        private EntityEnderDragon spawnEnderDragon(){
            EntityEnderDragon entityEnderDragon = new IslandEntityEnderDragon(world, islandBlockPosition);
            entityEnderDragon.getDragonControllerManager().setControllerPhase(DragonControllerPhase.HOLDING_PATTERN);
            entityEnderDragon.setPositionRotation(islandBlockPosition.getX(), 128, islandBlockPosition.getZ(),
                    world.random.nextFloat() * 360.0F, 0.0F);
            DRAGON_BATTLE.set(entityEnderDragon, this);

            world.addEntity(entityEnderDragon, CreatureSpawnEvent.SpawnReason.NATURAL);

            dragonUUID = entityEnderDragon.getUniqueID();
            resetCrystals();

            return entityEnderDragon;
        }

        private void countCrystals() {
            this.crystalsCount = world.a(EntityEnderCrystal.class, borderArea).size();
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
                this.a.world.addParticle(
                        Particles.EXPLOSION_EMITTER,
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
                targetBlock = Vec3D.c(this.a.world.getHighestBlockYAt(HeightMap.Type.MOTION_BLOCKING, islandBlockPosition));

            double distance = targetBlock.c(a.locX(), a.locY(), a.locZ());

            if (distance >= 100.0D && distance <= 22500.0D && !a.positionChanged && !a.v) {
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
                BlockPosition highestBlock = this.a.world.getHighestBlockYAt(HeightMap.Type.MOTION_BLOCKING_NO_LEAVES, islandBlockPosition);
                if (!highestBlock.a(this.a.getPositionVector(), 10.0D)) {
                    this.a.getDragonControllerManager().setControllerPhase(DragonControllerPhase.HOLDING_PATTERN);
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
            int closestNode = a.eI();
            Vec3D headLookVector = a.x(1.0F);

            int headClosestNode = a.p(-headLookVector.x * 40.0D, 105.0D, -headLookVector.z * 40.0D);

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

            currentPath = a.a(closestNode, headClosestNode, null);

            targetBlock = navigateToNextPathNode(currentPath, this.a, targetBlock);
        }

    }

    private static final class IslandDragonControllerHold extends DragonControllerHold {

        private static final PathfinderTargetCondition targetCondition = new PathfinderTargetCondition().a(64.0D);

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
            if (distance < 100.0D || distance > 22500.0D || this.a.positionChanged || this.a.v)
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
            if (entityHuman != null && !entityHuman.abilities.isInvulnerable)
                strafePlayer(entityHuman);
        }

        private void findNewTarget() {
            if (currentPath != null && currentPath.c()) {
                BlockPosition highestBlock = this.a.world.getHighestBlockYAt(HeightMap.Type.MOTION_BLOCKING_NO_LEAVES, islandBlockPosition);
                int crystalsCount = this.a.getEnderDragonBattle() == null ? 0 : this.a.getEnderDragonBattle().c();
                if (this.a.getRandom().nextInt(crystalsCount + 3) == 0) {
                    this.a.getDragonControllerManager().setControllerPhase(DragonControllerPhase.LANDING_APPROACH);
                    return;
                }

                double distance = 64.0D;
                EntityHuman closestHuman = this.a.world.a(targetCondition, highestBlock.getX(), highestBlock.getY(), highestBlock.getZ());
                if (closestHuman != null)
                    distance = highestBlock.a(closestHuman.getPositionVector(), true) / 512.0D;

                if (closestHuman != null && !closestHuman.abilities.isInvulnerable && (this.a.getRandom().nextInt(MathHelper.a((int)distance) + 2) == 0 || this.a.getRandom().nextInt(crystalsCount + 2) == 0)) {
                    strafePlayer(closestHuman);
                    return;
                }
            }

            if (currentPath == null || currentPath.c()) {
                int closestNode = this.a.eI();
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

                currentPath = this.a.a(closestNode1, closestNode, null);
                if (currentPath != null) {
                    currentPath.a();
                }
            }

            targetBlock = navigateToNextPathNode(currentPath, this.a, targetBlock);
        }

        private void strafePlayer(EntityHuman entityHuman) {
            this.a.getDragonControllerManager().setControllerPhase(DragonControllerPhase.STRAFE_PLAYER);
            this.a.getDragonControllerManager().b(DragonControllerPhase.STRAFE_PLAYER).a(entityHuman);
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
            Vec3D vec3D = this.a.x(1.0F).d();
            vec3D.b(-0.7853982F);

            double originLocX = this.a.bo.locX();
            double originLocY = this.a.bo.e(0.5D);
            double originLocZ = this.a.bo.locZ();

            Random random = this.a.getRandom();

            for(int i = 0; i < 8; i++) {
                double locX = originLocX + random.nextGaussian() / 2.0D;
                double locY = originLocY + random.nextGaussian() / 2.0D;
                double locZ = originLocZ + random.nextGaussian() / 2.0D;
                Vec3D mot = this.a.getMot();
                this.a.world.addParticle(Particles.DRAGON_BREATH, locX, locY, locZ, -vec3D.x * 0.07999999821186066D + mot.x, -vec3D.y * 0.30000001192092896D + mot.y, -vec3D.z * 0.07999999821186066D + mot.z);
                vec3D.b(0.19634955F);
            }
        }

        @Override
        public void c() {
            if (targetBlock == null) {
                targetBlock = Vec3D.c(this.a.world.getHighestBlockYAt(HeightMap.Type.MOTION_BLOCKING_NO_LEAVES, islandBlockPosition));
            }

            if (targetBlock.c(this.a.locX(), this.a.locY(), this.a.locZ()) < 1.0D) {
                this.a.getDragonControllerManager().b(DragonControllerPhase.SITTING_FLAMING).j();
                this.a.getDragonControllerManager().setControllerPhase(DragonControllerPhase.SITTING_SCANNING);
            }

        }

        @Override
        public float f() {
            return 1.5F;
        }

        @Override
        public float h() {
            // Turning speed
            float xzSquared = MathHelper.sqrt(Entity.c(this.a.getMot())) + 1.0F;
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

        private static final PathfinderTargetCondition targetCondition = new PathfinderTargetCondition().a(128.0D);

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
            if (distance < 100.0D || distance > 22500.0D || this.a.positionChanged || this.a.v)
                findNewTarget();
        }

        @Nullable
        public Vec3D g() {
            return targetBlock;
        }

        private void findNewTarget() {
            if (currentPath== null || currentPath.c()) {
                int closestNode = this.a.eI();
                BlockPosition highestBlock = this.a.world.getHighestBlockYAt(HeightMap.Type.MOTION_BLOCKING_NO_LEAVES, islandBlockPosition);
                EntityHuman closestHuman = this.a.world.a(targetCondition, highestBlock.getX(), highestBlock.getY(), highestBlock.getZ());
                int closestNode1;
                if (closestHuman != null) {
                    Vec3D var4 = (new Vec3D(closestHuman.locX(), 0.0D, closestHuman.locZ())).d();
                    closestNode1 = this.a.p(-var4.x * 40.0D, 105.0D, -var4.z * 40.0D);
                } else {
                    closestNode1 = this.a.p(40.0D, highestBlock.getY(), 0.0D);
                }

                PathPoint var4 = new PathPoint(highestBlock.getX(), highestBlock.getY(), highestBlock.getZ());
                currentPath = this.a.a(closestNode, closestNode1, var4);

                if (currentPath != null)
                    currentPath.a();
            }

            targetBlock = navigateToNextPathNode(currentPath, this.a, targetBlock);

            if (currentPath != null && currentPath.c())
                this.a.getDragonControllerManager().setControllerPhase(DragonControllerPhase.LANDING);
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
