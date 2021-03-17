package com.bgsoftware.superiorskyblock.nms;

import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.google.common.collect.Sets;
import net.minecraft.server.v1_15_R1.AxisAlignedBB;
import net.minecraft.server.v1_15_R1.BlockPosition;
import net.minecraft.server.v1_15_R1.Blocks;
import net.minecraft.server.v1_15_R1.Chunk;
import net.minecraft.server.v1_15_R1.ChunkCoordIntPair;
import net.minecraft.server.v1_15_R1.ChunkStatus;
import net.minecraft.server.v1_15_R1.CriterionTriggers;
import net.minecraft.server.v1_15_R1.DamageSource;
import net.minecraft.server.v1_15_R1.DragonControllerDying;
import net.minecraft.server.v1_15_R1.DragonControllerFly;
import net.minecraft.server.v1_15_R1.DragonControllerHold;
import net.minecraft.server.v1_15_R1.DragonControllerLanding;
import net.minecraft.server.v1_15_R1.DragonControllerLandingFly;
import net.minecraft.server.v1_15_R1.DragonControllerManager;
import net.minecraft.server.v1_15_R1.DragonControllerPhase;
import net.minecraft.server.v1_15_R1.EnderDragonBattle;
import net.minecraft.server.v1_15_R1.Entity;
import net.minecraft.server.v1_15_R1.EntityEnderCrystal;
import net.minecraft.server.v1_15_R1.EntityEnderDragon;
import net.minecraft.server.v1_15_R1.EntityHuman;
import net.minecraft.server.v1_15_R1.EntityPlayer;
import net.minecraft.server.v1_15_R1.EnumDragonRespawn;
import net.minecraft.server.v1_15_R1.GameProfileSerializer;
import net.minecraft.server.v1_15_R1.HeightMap;
import net.minecraft.server.v1_15_R1.IChunkAccess;
import net.minecraft.server.v1_15_R1.IDragonController;
import net.minecraft.server.v1_15_R1.MathHelper;
import net.minecraft.server.v1_15_R1.NBTTagCompound;
import net.minecraft.server.v1_15_R1.NBTTagInt;
import net.minecraft.server.v1_15_R1.NBTTagList;
import net.minecraft.server.v1_15_R1.Particles;
import net.minecraft.server.v1_15_R1.PathEntity;
import net.minecraft.server.v1_15_R1.PathPoint;
import net.minecraft.server.v1_15_R1.PathfinderTargetCondition;
import net.minecraft.server.v1_15_R1.PlayerChunk;
import net.minecraft.server.v1_15_R1.TicketType;
import net.minecraft.server.v1_15_R1.Unit;
import net.minecraft.server.v1_15_R1.Vec3D;
import net.minecraft.server.v1_15_R1.World;
import net.minecraft.server.v1_15_R1.WorldGenEndGatewayConfiguration;
import net.minecraft.server.v1_15_R1.WorldGenEndTrophy;
import net.minecraft.server.v1_15_R1.WorldGenFeatureConfiguration;
import net.minecraft.server.v1_15_R1.WorldGenerator;
import net.minecraft.server.v1_15_R1.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftEnderDragon;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
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
public final class NMSDragonFight_v1_15_R1 implements NMSDragonFight {

    private static final ReflectField<EnderDragonBattle> DRAGON_BATTLE = new ReflectField<>(EntityEnderDragon.class, EnderDragonBattle.class, "bN");
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
        public Vec3D u(float f) {
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
            super(worldServer, new NBTTagCompound());
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

            if (this.m != null) {
                nbtTagCompound.a("Dragon", this.m);
            }

            nbtTagCompound.setBoolean("DragonKilled", this.dragonKilled);
            nbtTagCompound.setBoolean("PreviouslyKilled", this.previouslyKilled);

            if (this.o != null) {
                nbtTagCompound.set("ExitPortalLocation", GameProfileSerializer.a(this.o));
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
                this.d.getChunkProvider().removeTicket(TicketType.DRAGON, islandChunkCoord, 9, Unit.INSTANCE);
                return;
            }

            this.d.getChunkProvider().addTicket(TicketType.DRAGON, islandChunkCoord, 9, Unit.INSTANCE);

            boolean tickingChunks = areChunkTicking();

            if (this.p != null && tickingChunks) {
                if (crystalsList == null) {
                    this.p = null;
                    e();
                }

                if (++crystalsCountTick >= 100) {
                    countCrystals();
                    crystalsCountTick = 0;
                }

                this.p.a(this.d, this, crystalsList, respawnTick++, this.o);
            }
        }

        @Override
        public void a(EnumDragonRespawn dragonRespawn) {
            if (this.p == null)
                throw new IllegalStateException("Dragon respawn isn't in progress, can't skip ahead in the animation.");

            respawnTick = 0;

            if(dragonRespawn != EnumDragonRespawn.END){
                this.p = dragonRespawn;
            }

            else{
                this.p = null;
                dragonKilled = false;
                EntityEnderDragon entityEnderDragon = spawnEnderDragon();
                for(EntityPlayer entityPlayer : bossBattle.getPlayers())
                    CriterionTriggers.n.a(entityPlayer, entityEnderDragon);
            }
        }

        @Override
        public void a(EntityEnderDragon entityEnderDragon) {
            if(!entityEnderDragon.getUniqueID().equals(this.m))
                return;

            this.bossBattle.setProgress(0.0F);
            this.bossBattle.setVisible(false);
            generateExitPortal(true);

            if (!gateways.isEmpty()) {
                int i = gateways.remove(gateways.size() - 1);
                int j = MathHelper.floor(96.0D * Math.cos(2.0D * (-3.141592653589793D + 0.15707963267948966D * (double)i)));
                int k = MathHelper.floor(96.0D * Math.sin(2.0D * (-3.141592653589793D + 0.15707963267948966D * (double)i)));
                BlockPosition blockPosition = new BlockPosition(j, 75, k);
                this.d.triggerEffect(3000, blockPosition, 0);
                WorldGenerator.END_GATEWAY.b(WorldGenEndGatewayConfiguration.a())
                        .a(this.d, this.d.getChunkProvider().getChunkGenerator(), new Random(), blockPosition);
            }

            if (!previouslyKilled) {
                this.d.setTypeUpdate(this.d.getHighestBlockYAt(HeightMap.Type.MOTION_BLOCKING, islandBlockPosition), Blocks.DRAGON_EGG.getBlockData());
            }

            previouslyKilled = true;
            dragonKilled = true;
        }

        @Override
        public void b(EntityEnderDragon entityEnderDragon) {
            if(!entityEnderDragon.getUniqueID().equals(this.m))
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
            if (this.p != null && crystalsList.contains(entityEnderCrystal)) {
                this.p = null;
                respawnTick = 0;
                f();
                generateExitPortal(true);
            }
            else {
                countCrystals();
                Entity entity = this.d.getEntity(this.m);
                if (entity instanceof EntityEnderDragon)
                    ((EntityEnderDragon)entity).a(entityEnderCrystal, entityEnderCrystal.getChunkCoordinates(), damageSource);
            }
        }

        @Override
        public boolean d() {
            return previouslyKilled;
        }

        @Override
        public void e() {
            if(!dragonKilled || this.p != null)
                return;

            crystalsList = this.d.a(EntityEnderCrystal.class, borderArea);

            this.p = EnumDragonRespawn.START;
            respawnTick = 0;
            generateExitPortal(false);
        }

        @Override
        public void f() {
            for(EntityEnderCrystal entityEnderCrystal : this.d.a(EntityEnderCrystal.class, borderArea)){
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
                    IChunkAccess chunkAccess = this.d.getChunkAt(islandChunkCoord.x + i, islandChunkCoord.z + j, ChunkStatus.FULL, false);

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
                if(((CraftWorld) player.getWorld()).getHandle() == this.d){
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
            EntityEnderDragon entityEnderDragon = new IslandEntityEnderDragon(this.d, islandBlockPosition);
            entityEnderDragon.getDragonControllerManager().setControllerPhase(DragonControllerPhase.HOLDING_PATTERN);
            entityEnderDragon.setPositionRotation(islandBlockPosition.getX(), 128, islandBlockPosition.getZ(),
                    this.d.random.nextFloat() * 360.0F, 0.0F);
            DRAGON_BATTLE.set(entityEnderDragon, this);

            this.d.addEntity(entityEnderDragon, CreatureSpawnEvent.SpawnReason.NATURAL);

            this.m = entityEnderDragon.getUniqueID();
            f();

            return entityEnderDragon;
        }

        private void countCrystals() {
            this.crystalsCount = this.d.a(EntityEnderCrystal.class, borderArea).size();
        }

        private void generateExitPortal(boolean flag) {
            WorldGenEndTrophy worldGenEndTrophy = new WorldGenEndTrophy(flag);

            if (this.o == null) {
                this.o = this.d.getHighestBlockYAt(HeightMap.Type.MOTION_BLOCKING_NO_LEAVES, islandBlockPosition).down();
                while (this.d.getType(this.o).getBlock() == Blocks.BEDROCK && this.o.getY() > this.d.getSeaLevel())
                    this.o = this.o.down();
            }

            worldGenEndTrophy.b(WorldGenFeatureConfiguration.e).a(this.d, this.d.getChunkProvider().getChunkGenerator(), new Random(), this.o.up(2));
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
                targetBlock = new Vec3D(this.a.world.getHighestBlockYAt(HeightMap.Type.MOTION_BLOCKING, islandBlockPosition));

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
            int closestNode = a.l();
            Vec3D headLookVector = a.u(1.0F);

            int headClosestNode = a.o(-headLookVector.x * 40.0D, 105.0D, -headLookVector.z * 40.0D);

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
            if (currentPath != null && currentPath.b()) {
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

            if (currentPath == null || currentPath.b()) {
                int closestNode = this.a.l();
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
            Vec3D vec3D = this.a.u(1.0F).d();
            vec3D.b(-0.7853982F);

            double originLocX = this.a.bw.locX();
            double originLocY = this.a.bw.e(0.5D);
            double originLocZ = this.a.bw.locZ();

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
                targetBlock = new Vec3D(this.a.world.getHighestBlockYAt(HeightMap.Type.MOTION_BLOCKING_NO_LEAVES, islandBlockPosition));
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
            float xzSquared = MathHelper.sqrt(Entity.b(this.a.getMot())) + 1.0F;
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
            if (currentPath== null || currentPath.b()) {
                int closestNode = this.a.l();
                BlockPosition highestBlock = this.a.world.getHighestBlockYAt(HeightMap.Type.MOTION_BLOCKING_NO_LEAVES, islandBlockPosition);
                EntityHuman closestHuman = this.a.world.a(targetCondition, highestBlock.getX(), highestBlock.getY(), highestBlock.getZ());
                int closestNode1;
                if (closestHuman != null) {
                    Vec3D var4 = (new Vec3D(closestHuman.locX(), 0.0D, closestHuman.locZ())).d();
                    closestNode1 = this.a.o(-var4.x * 40.0D, 105.0D, -var4.z * 40.0D);
                } else {
                    closestNode1 = this.a.o(40.0D, highestBlock.getY(), 0.0D);
                }

                PathPoint var4 = new PathPoint(highestBlock.getX(), highestBlock.getY(), highestBlock.getZ());
                currentPath = this.a.a(closestNode, closestNode1, var4);

                if (currentPath != null)
                    currentPath.a();
            }

            targetBlock = navigateToNextPathNode(currentPath, this.a, targetBlock);

            if (currentPath != null && currentPath.b())
                this.a.getDragonControllerManager().setControllerPhase(DragonControllerPhase.LANDING);
        }

    }

    private static Vec3D navigateToNextPathNode(PathEntity currentPath, EntityEnderDragon entityEnderDragon, Vec3D currentTargetBlock) {
        if (currentPath != null && !currentPath.b()) {
            Vec3D basePosition = currentPath.g();
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
