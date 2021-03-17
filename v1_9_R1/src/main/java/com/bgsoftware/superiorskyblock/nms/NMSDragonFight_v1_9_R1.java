package com.bgsoftware.superiorskyblock.nms;

import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.google.common.collect.Sets;
import net.minecraft.server.v1_9_R1.AxisAlignedBB;
import net.minecraft.server.v1_9_R1.BlockPosition;
import net.minecraft.server.v1_9_R1.Blocks;
import net.minecraft.server.v1_9_R1.BossBattleServer;
import net.minecraft.server.v1_9_R1.DamageSource;
import net.minecraft.server.v1_9_R1.DragonControllerDying;
import net.minecraft.server.v1_9_R1.DragonControllerFly;
import net.minecraft.server.v1_9_R1.DragonControllerHold;
import net.minecraft.server.v1_9_R1.DragonControllerLanding;
import net.minecraft.server.v1_9_R1.DragonControllerLandingFly;
import net.minecraft.server.v1_9_R1.DragonControllerManager;
import net.minecraft.server.v1_9_R1.DragonControllerPhase;
import net.minecraft.server.v1_9_R1.EnderDragonBattle;
import net.minecraft.server.v1_9_R1.Entity;
import net.minecraft.server.v1_9_R1.EntityEnderCrystal;
import net.minecraft.server.v1_9_R1.EntityEnderDragon;
import net.minecraft.server.v1_9_R1.EntityHuman;
import net.minecraft.server.v1_9_R1.EntityPlayer;
import net.minecraft.server.v1_9_R1.EntityTypes;
import net.minecraft.server.v1_9_R1.EnumDragonRespawn;
import net.minecraft.server.v1_9_R1.EnumParticle;
import net.minecraft.server.v1_9_R1.GameProfileSerializer;
import net.minecraft.server.v1_9_R1.IDragonController;
import net.minecraft.server.v1_9_R1.MathHelper;
import net.minecraft.server.v1_9_R1.NBTTagCompound;
import net.minecraft.server.v1_9_R1.NBTTagInt;
import net.minecraft.server.v1_9_R1.NBTTagList;
import net.minecraft.server.v1_9_R1.PathEntity;
import net.minecraft.server.v1_9_R1.PathPoint;
import net.minecraft.server.v1_9_R1.Vec3D;
import net.minecraft.server.v1_9_R1.World;
import net.minecraft.server.v1_9_R1.WorldGenEndGateway;
import net.minecraft.server.v1_9_R1.WorldGenEndTrophy;
import net.minecraft.server.v1_9_R1.WorldProviderTheEnd;
import net.minecraft.server.v1_9_R1.WorldServer;
import org.bukkit.Achievement;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_9_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftEnderDragon;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftPlayer;
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
public final class NMSDragonFight_v1_9_R1 implements NMSDragonFight {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private static final ReflectField<EnderDragonBattle> DRAGON_BATTLE = new ReflectField<>(EntityEnderDragon.class, EnderDragonBattle.class, "bJ");
    private static final ReflectField<IDragonController> DRAGON_PHASE = new ReflectField<>(DragonControllerManager.class, IDragonController.class, "currentDragonController");
    private static final ReflectField<BossBattleServer> BATTLE_BOSS_SERVER = new ReflectField<>(EnderDragonBattle.class, BossBattleServer.class, "c");

    private static final Map<UUID, EnderDragonBattle> activeBattles = new HashMap<>();

    static {
        ReflectField<Map<String, Class<? extends Entity>>> ENTITY_TYPES_C = new ReflectField<>(EntityTypes.class, Map.class, "c");
        ENTITY_TYPES_C.get(null).put("EnderDragon", IslandEntityEnderDragon.class);

        ReflectField<Map<Class<? extends Entity>, String>> ENTITY_TYPES_D = new ReflectField<>(EntityTypes.class, Map.class, "d");
        ENTITY_TYPES_D.get(null).put(IslandEntityEnderDragon.class, "EnderDragon");

        ReflectField<Map<Integer, Class<? extends Entity>>> ENTITY_TYPES_E = new ReflectField<>(EntityTypes.class, Map.class, "e");
        ENTITY_TYPES_E.get(null).put(63, IslandEntityEnderDragon.class);

        ReflectField<Map<Class<? extends Entity>, Integer>> ENTITY_TYPES_F = new ReflectField<>(EntityTypes.class, Map.class, "f");
        ENTITY_TYPES_F.get(null).put(IslandEntityEnderDragon.class, 63);

        ReflectField<Map<String, Integer>> ENTITY_TYPES_G = new ReflectField<>(EntityTypes.class, Map.class, "g");
        ENTITY_TYPES_G.get(null).put("EnderDragon", 63);
    }

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
                DRAGON_PHASE.set(entityEnderDragon.cT(), new IslandDragonControllerDying(entityEnderDragon));
                break;
            case LEAVE_PORTAL:
                DRAGON_PHASE.set(entityEnderDragon.cT(), new IslandDragonControllerFly(entityEnderDragon));
                break;
            case CIRCLING:
                DRAGON_PHASE.set(entityEnderDragon.cT(), new IslandDragonControllerHold(entityEnderDragon));
                break;
            case LAND_ON_PORTAL:
                DRAGON_PHASE.set(entityEnderDragon.cT(), new IslandDragonControllerLanding(entityEnderDragon));
                break;
            case FLY_TO_PORTAL:
                DRAGON_PHASE.set(entityEnderDragon.cT(), new IslandDragonControllerLandingFly(entityEnderDragon));
                break;
        }
    }

    @Override
    public void awardTheEndAchievement(Player player) {
        player.awardAchievement(Achievement.THE_END);
    }

    public static final class IslandEntityEnderDragon extends EntityEnderDragon {

        private BlockPosition islandBlockPosition;

        public IslandEntityEnderDragon(World world){
            super(world);
        }

        IslandEntityEnderDragon(World world, BlockPosition islandBlockPosition){
            super(world);
            this.islandBlockPosition = islandBlockPosition;
        }

        @Override
        public void a(NBTTagCompound nbtTagCompound) {
            super.a(nbtTagCompound);
            if(world.worldProvider instanceof WorldProviderTheEnd && plugin.getGrid().isIslandsWorld(world.getWorld())){
                Island island = plugin.getGrid().getIslandAt(new Location(world.getWorld(), locX, locY, locZ));
                if(island != null) {
                    Location middleBlock = island.getCenter(org.bukkit.World.Environment.THE_END);
                    this.islandBlockPosition = new BlockPosition(middleBlock.getX(), middleBlock.getY(), middleBlock.getZ());
                    IslandEnderDragonBattle islandEnderDragonBattle = new IslandEnderDragonBattle(island, this);
                    activeBattles.put(island.getUniqueId(), islandEnderDragonBattle);
                }
            }
        }

        @Override
        public Vec3D a(float f) {
            IDragonController dragonController = cT().a();
            DragonControllerPhase<? extends IDragonController> dragonControllerPhase = dragonController.i();
            float f1;
            Vec3D vec3d;

            if (dragonControllerPhase != DragonControllerPhase.d && dragonControllerPhase != DragonControllerPhase.e) {
                if (dragonController.a()) {
                    float f2 = this.pitch;
                    this.pitch = -45.0F;
                    vec3d = this.f(f);
                    this.pitch = f2;
                } else {
                    vec3d = this.f(f);
                }
            } else {
                BlockPosition blockposition = this.world.q(islandBlockPosition);
                f1 = Math.max(MathHelper.sqrt(this.d(blockposition)) / 4.0F, 1.0F);
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
        private final AxisAlignedBB borderArea;
        private final List<Integer> gateways = new ArrayList<>();
        private final Island island;
        private final WorldServer world;
        private final BossBattleServer bossBattle;

        private UUID dragonUUID;
        private BlockPosition exitPortalLocation;
        private EnumDragonRespawn respawnPhase;
        private List<EntityEnderCrystal> crystalsList;
        private int currentTick = 0;
        private int crystalsCountTick = 0;
        private int respawnTick = 0;
        private int crystalsCount = 0;
        private boolean dragonKilled = false;
        private boolean previouslyKilled = false;

        public IslandEnderDragonBattle(Island island, WorldServer worldServer, Location location){
            this(island, worldServer, new BlockPosition(location.getX(), location.getY(), location.getZ()));
            spawnEnderDragon();
        }

        public IslandEnderDragonBattle(Island island, IslandEntityEnderDragon islandEntityEnderDragon){
            this(island, (WorldServer) islandEntityEnderDragon.world, new BlockPosition(islandEntityEnderDragon.islandBlockPosition));
            DRAGON_BATTLE.set(islandEntityEnderDragon, this);
            dragonUUID = islandEntityEnderDragon.getUniqueID();
        }

        private IslandEnderDragonBattle(Island island, WorldServer worldServer, BlockPosition islandBlockPosition){
            super(worldServer, new NBTTagCompound());
            this.islandBlockPosition = islandBlockPosition;
            this.island = island;
            this.world = worldServer;
            this.bossBattle = BATTLE_BOSS_SERVER.get(this);

            int radius = plugin.getSettings().maxIslandSize;
            this.borderArea = new AxisAlignedBB(islandBlockPosition.a(-radius, -radius, -radius), islandBlockPosition.a(radius, radius, radius));
        }

        @Override
        public NBTTagCompound a() {
            NBTTagCompound nbtTagCompound = new NBTTagCompound();

            if (dragonUUID != null) {
                nbtTagCompound.a("Dragon", dragonUUID);
            }

            nbtTagCompound.setBoolean("DragonKilled", this.dragonKilled);
            nbtTagCompound.setBoolean("PreviouslyKilled", this.previouslyKilled);

            if (exitPortalLocation != null) {
                nbtTagCompound.set("ExitPortalLocation", GameProfileSerializer.a(exitPortalLocation));
            }

            NBTTagList nbtTagList = new NBTTagList();
            for(Integer gateway : this.gateways)
                nbtTagList.add(new NBTTagInt(gateway));

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
                return;
            }

            if (respawnPhase != null) {
                if (crystalsList == null) {
                    respawnPhase = null;
                    e();
                }

                if (++crystalsCountTick >= 100) {
                    countCrystals();
                    crystalsCountTick = 0;
                }

                respawnPhase.a(world, this, crystalsList, respawnTick++, exitPortalLocation);
            }
        }

        @Override
        public void a(EnumDragonRespawn dragonRespawn) {
            if (respawnPhase == null)
                throw new IllegalStateException("Dragon respawn isn't in progress, can't skip ahead in the animation.");

            respawnTick = 0;

            if(dragonRespawn != EnumDragonRespawn.END){
                respawnPhase = dragonRespawn;
            }

            else{
                respawnPhase = null;
                dragonKilled = false;
                spawnEnderDragon();
            }
        }

        @Override
        public void a(EntityEnderDragon entityEnderDragon) {
            if(!entityEnderDragon.getUniqueID().equals(dragonUUID))
                return;

            this.bossBattle.setProgress(0.0F);
            this.bossBattle.setVisible(false);
            generateExitPortal(true);

            if (!gateways.isEmpty()) {
                int i = gateways.remove(gateways.size() - 1);
                int j = MathHelper.floor(96.0D * Math.cos(2.0D * (-3.141592653589793D + 0.15707963267948966D * (double)i)));
                int k = MathHelper.floor(96.0D * Math.sin(2.0D * (-3.141592653589793D + 0.15707963267948966D * (double)i)));
                BlockPosition blockPosition = new BlockPosition(j, 75, k);
                world.triggerEffect(3000, blockPosition, 0);
                new WorldGenEndGateway().generate(world, new Random(), blockPosition);
            }

            if (!previouslyKilled) {
                world.setTypeUpdate(world.getHighestBlockYAt(islandBlockPosition), Blocks.DRAGON_EGG.getBlockData());
            }

            previouslyKilled = true;
            dragonKilled = true;
        }

        @Override
        public void b(EntityEnderDragon entityEnderDragon) {
            if(entityEnderDragon.getUniqueID().equals(dragonUUID))
                bossBattle.setProgress(entityEnderDragon.getHealth() / entityEnderDragon.getMaxHealth());
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
                f();
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
        public boolean d() {
            return previouslyKilled;
        }

        @Override
        public void e() {
            if(!dragonKilled || respawnPhase != null)
                return;

            crystalsList = world.a(EntityEnderCrystal.class, borderArea);

            respawnPhase = EnumDragonRespawn.START;
            respawnTick = 0;
            generateExitPortal(false);
        }

        @Override
        public void f() {
            for(EntityEnderCrystal entityEnderCrystal : world.a(EntityEnderCrystal.class, borderArea)){
                entityEnderCrystal.h(false);
                entityEnderCrystal.a((BlockPosition) null);
            }
        }

        public void removeBattlePlayers(){
            for(EntityPlayer entityPlayer : bossBattle.getPlayers())
                bossBattle.removePlayer(entityPlayer);
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

        private void spawnEnderDragon(){
            EntityEnderDragon entityEnderDragon = new IslandEntityEnderDragon(world, islandBlockPosition);
            entityEnderDragon.cT().a(DragonControllerPhase.a);
            entityEnderDragon.setPositionRotation(islandBlockPosition.getX(), 128, islandBlockPosition.getZ(),
                    world.random.nextFloat() * 360.0F, 0.0F);
            DRAGON_BATTLE.set(entityEnderDragon, this);

            world.addEntity(entityEnderDragon, CreatureSpawnEvent.SpawnReason.NATURAL);

            dragonUUID = entityEnderDragon.getUniqueID();
        }

        private void countCrystals() {
            this.crystalsCount = world.a(EntityEnderCrystal.class, borderArea).size();
        }

        private void generateExitPortal(boolean flag) {
            WorldGenEndTrophy worldGenEndTrophy = new WorldGenEndTrophy(flag);

            if (exitPortalLocation == null) {
                exitPortalLocation = world.q(islandBlockPosition).down();
                while (world.getType(exitPortalLocation).getBlock() == Blocks.BEDROCK && exitPortalLocation.getY() > world.K())
                    exitPortalLocation = exitPortalLocation.down();
            }

            worldGenEndTrophy.generate(world, new Random(), exitPortalLocation.up(2));
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
                        EnumParticle.EXPLOSION_HUGE,
                        this.a.locX + offsetX,
                        this.a.locY + 2.0D + offsetY,
                        this.a.locZ + offsetZ,
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
                targetBlock = new Vec3D(this.a.world.getHighestBlockYAt(islandBlockPosition));

            double distance = targetBlock.c(a.locX, a.locY, a.locZ);

            if (distance >= 100.0D && distance <= 22500.0D && !a.positionChanged && !a.C) {
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
                BlockPosition highestBlock = this.a.world.q(islandBlockPosition);
                if(this.a.d(highestBlock) > 100){
                    this.a.cT().a(DragonControllerPhase.a);
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
            int closestNode = a.o();
            Vec3D headLookVector = a.a(1.0F);

            int headClosestNode = a.l(-headLookVector.x * 40.0D, 105.0D, -headLookVector.z * 40.0D);

            EnderDragonBattle enderDragonBattle = a.cU();

            if (enderDragonBattle != null && enderDragonBattle.c() > 0) {
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
            double distance = targetBlock == null ? 0.0D : targetBlock.c(this.a.locX, this.a.locY, this.a.locZ);
            if (distance < 100.0D || distance > 22500.0D || this.a.positionChanged || this.a.C)
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
            EnderDragonBattle enderDragonBattle = this.a.cU();

            if (currentPath != null && currentPath.b()) {
                BlockPosition highestBlock = this.a.world.q(islandBlockPosition);
                int crystalsCount = enderDragonBattle == null ? 0 : enderDragonBattle.c();
                if (this.a.getRandom().nextInt(crystalsCount + 3) == 0) {
                    this.a.cT().a(DragonControllerPhase.c);
                    return;
                }

                double distance = 64.0D;
                EntityHuman closestHuman = this.a.world.a(highestBlock, distance, distance);
                if (closestHuman != null)
                    distance = closestHuman.d(highestBlock) / 512.0D;

                if (closestHuman != null && !closestHuman.abilities.isInvulnerable && (this.a.getRandom().nextInt(MathHelper.a((int)distance) + 2) == 0 || this.a.getRandom().nextInt(crystalsCount + 2) == 0)) {
                    strafePlayer(closestHuman);
                    return;
                }
            }

            if (currentPath == null || currentPath.b()) {
                int closestNode = this.a.o();
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

                if (enderDragonBattle != null && enderDragonBattle.c() >= 0) {
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
            this.a.cT().a(DragonControllerPhase.b);
            this.a.cT().b(DragonControllerPhase.b).a(entityHuman);
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
            Vec3D vec3D = this.a.a(1.0F).a();
            vec3D.b(-0.7853982F);

            double originLocX = this.a.bw.locX;
            double originLocY = this.a.bw.locY + this.a.bw.length / 2.0F;
            double originLocZ = this.a.bw.locZ;

            Random random = this.a.getRandom();

            for(int i = 0; i < 8; i++) {
                double locX = originLocX + random.nextGaussian() / 2.0D;
                double locY = originLocY + random.nextGaussian() / 2.0D;
                double locZ = originLocZ + random.nextGaussian() / 2.0D;
                this.a.world.addParticle(EnumParticle.EXPLOSION_HUGE, locX, locY, locZ, -vec3D.x * 0.07999999821186066D + this.a.motX, -vec3D.y * 0.30000001192092896D + this.a.motY, -vec3D.z * 0.07999999821186066D + this.a.motZ);
                vec3D.b(0.19634955F);
            }
        }

        @Override
        public void c() {
            if (targetBlock == null) {
                targetBlock = new Vec3D(this.a.world.q(islandBlockPosition));
            }

            if (targetBlock.c(this.a.locX, this.a.locY, this.a.locZ) < 1.0D) {
                this.a.cT().b(DragonControllerPhase.f).j();
                this.a.cT().a(DragonControllerPhase.g);
            }

        }

        @Override
        public float f() {
            return 1.5F;
        }

        @Override
        public float h() {
            // Turning speed
            float xzSquared = MathHelper.sqrt(this.a.motX * this.a.motX + this.a.motZ * this.a.motZ) + 1.0F;
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
            double distance = targetBlock == null ? 0.0D : targetBlock.c(this.a.locX, this.a.locY, this.a.locZ);
            if (distance < 100.0D || distance > 22500.0D || this.a.positionChanged || this.a.C)
                findNewTarget();
        }

        @Nullable
        public Vec3D g() {
            return targetBlock;
        }

        private void findNewTarget() {
            if (currentPath== null || currentPath.b()) {
                int closestNode = this.a.o();
                BlockPosition highestBlock = this.a.world.q(islandBlockPosition);
                EntityHuman closestHuman = this.a.world.a(highestBlock, 128.0D, 128.0D);
                int closestNode1;
                if (closestHuman != null) {
                    Vec3D var4 = (new Vec3D(closestHuman.locX, 0.0D, closestHuman.locZ)).a();
                    closestNode1 = this.a.l(-var4.x * 40.0D, 105.0D, -var4.z * 40.0D);
                } else {
                    closestNode1 = this.a.l(40.0D, highestBlock.getY(), 0.0D);
                }

                PathPoint var4 = new PathPoint(highestBlock.getX(), highestBlock.getY(), highestBlock.getZ());
                currentPath = this.a.a(closestNode, closestNode1, var4);

                if (currentPath != null)
                    currentPath.a();
            }

            targetBlock = navigateToNextPathNode(currentPath, this.a, targetBlock);

            if (currentPath != null && currentPath.b())
                this.a.cT().a(DragonControllerPhase.d);
        }

    }

    private static Vec3D navigateToNextPathNode(PathEntity currentPath, EntityEnderDragon entityEnderDragon, Vec3D currentTargetBlock) {
        if (currentPath != null && !currentPath.b()) {
            Vec3D basePosition = currentPath.f();
            currentPath.a();

            double y;
            do {
                y = basePosition.y + entityEnderDragon.getRandom().nextFloat() * 20.0F;
            } while(y < basePosition.y);

            return new Vec3D(basePosition.x, y, basePosition.z);
        }

        return currentTargetBlock;
    }

}
