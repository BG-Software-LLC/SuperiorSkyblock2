package com.bgsoftware.superiorskyblock.nms.v1_12_R1.dragon;

import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.google.common.collect.Sets;
import net.minecraft.server.v1_12_R1.AxisAlignedBB;
import net.minecraft.server.v1_12_R1.BlockPosition;
import net.minecraft.server.v1_12_R1.Blocks;
import net.minecraft.server.v1_12_R1.BossBattleServer;
import net.minecraft.server.v1_12_R1.CriterionTriggers;
import net.minecraft.server.v1_12_R1.DamageSource;
import net.minecraft.server.v1_12_R1.DragonControllerPhase;
import net.minecraft.server.v1_12_R1.EnderDragonBattle;
import net.minecraft.server.v1_12_R1.Entity;
import net.minecraft.server.v1_12_R1.EntityEnderCrystal;
import net.minecraft.server.v1_12_R1.EntityEnderDragon;
import net.minecraft.server.v1_12_R1.EntityPlayer;
import net.minecraft.server.v1_12_R1.EnumDragonRespawn;
import net.minecraft.server.v1_12_R1.GameProfileSerializer;
import net.minecraft.server.v1_12_R1.IDragonController;
import net.minecraft.server.v1_12_R1.MathHelper;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import net.minecraft.server.v1_12_R1.NBTTagInt;
import net.minecraft.server.v1_12_R1.NBTTagList;
import net.minecraft.server.v1_12_R1.Vec3D;
import net.minecraft.server.v1_12_R1.WorldGenEndGateway;
import net.minecraft.server.v1_12_R1.WorldGenEndTrophy;
import net.minecraft.server.v1_12_R1.WorldServer;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;

import javax.annotation.Nullable;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class IslandEnderDragonBattle extends EnderDragonBattle {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private static final ReflectField<EnderDragonBattle> DRAGON_BATTLE = new ReflectField<EnderDragonBattle>(
            EntityEnderDragon.class, EnderDragonBattle.class, Modifier.PRIVATE | Modifier.FINAL, 1)
            .removeFinal();

    private static final ReflectField<BossBattleServer> BATTLE_BOSS_SERVER = new ReflectField<>(
            EnderDragonBattle.class, BossBattleServer.class, Modifier.PRIVATE | Modifier.FINAL, 1);

    private static final ReflectField<Boolean> SCAN_FOR_LEGACY_PORTALS = new ReflectField<>(
            EnderDragonBattle.class, boolean.class, Modifier.PRIVATE, 3);

    private static final ReflectField<Boolean> WAS_DRAGON_KILLED = new ReflectField<>(
            EnderDragonBattle.class, boolean.class, Modifier.PRIVATE, 1);

    private static final Vec3D ORIGINAL_END_PODIUM = new Vec3D(0.5, 0, 0.5);

    private final Island island;
    private final BlockPosition islandBlockPosition;
    private final WorldServer worldServer;
    private final IslandEntityEnderDragon entityEnderDragon;
    private final BossBattleServer bossBattleServer;
    private final AxisAlignedBB borderArea;

    private final LinkedList<Integer> gateways = new LinkedList<>();

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

    public IslandEnderDragonBattle(Island island, WorldServer worldServer, Location location) {
        this(island, worldServer, new BlockPosition(location.getX(), location.getY(), location.getZ()),
                null);
    }

    public IslandEnderDragonBattle(Island island, WorldServer worldServer, BlockPosition islandBlockPosition,
                                   @Nullable IslandEntityEnderDragon islandEntityEnderDragon) {
        super(worldServer, new NBTTagCompound());
        SCAN_FOR_LEGACY_PORTALS.set(this, false);
        WAS_DRAGON_KILLED.set(this, false);
        this.island = island;
        this.islandBlockPosition = islandBlockPosition;
        this.worldServer = worldServer;
        this.entityEnderDragon = islandEntityEnderDragon == null ? spawnEnderDragon() : islandEntityEnderDragon;
        this.bossBattleServer = BATTLE_BOSS_SERVER.get(this);

        int radius = plugin.getSettings().getMaxIslandSize();
        this.borderArea = new AxisAlignedBB(islandBlockPosition.a(-radius, -radius, -radius), islandBlockPosition.a(radius, radius, radius));

        DRAGON_BATTLE.set(this.entityEnderDragon, this);
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
        for (int gateway : this.gateways)
            nbtTagList.add(new NBTTagInt(gateway));

        nbtTagCompound.set("Gateways", nbtTagList);

        return nbtTagCompound;
    }

    @Override
    public void b() {
        DragonUtils.runWithPodiumPosition(this.islandBlockPosition, this::doTick);

        IDragonController currentController = this.entityEnderDragon.getDragonControllerManager().a();
        if (currentController != null && ORIGINAL_END_PODIUM.equals(currentController.g())) {
            currentController.d();
        }
    }

    @Override
    public void a(EnumDragonRespawn dragonRespawn) {
        if (respawnPhase == null)
            throw new IllegalStateException("Dragon respawn isn't in progress, can't skip ahead in the animation.");

        respawnTick = 0;

        if (dragonRespawn != EnumDragonRespawn.END) {
            respawnPhase = dragonRespawn;
        } else {
            respawnPhase = null;
            dragonKilled = false;
            EntityEnderDragon entityEnderDragon = spawnEnderDragon();
            for (EntityPlayer entityPlayer : this.bossBattleServer.getPlayers())
                CriterionTriggers.m.a(entityPlayer, entityEnderDragon);
        }
    }

    @Override
    public void a(EntityEnderDragon entityEnderDragon) {
        if (!entityEnderDragon.getUniqueID().equals(dragonUUID))
            return;

        this.bossBattleServer.setProgress(0.0F);
        this.bossBattleServer.setVisible(false);
        this.generateExitPortal(true);

        if (!gateways.isEmpty()) {
            int i = gateways.removeLast();
            int j = MathHelper.floor(96.0D * Math.cos(2.0D * (-3.141592653589793D + 0.15707963267948966D * (double) i)));
            int k = MathHelper.floor(96.0D * Math.sin(2.0D * (-3.141592653589793D + 0.15707963267948966D * (double) i)));
            BlockPosition blockPosition = new BlockPosition(j, 75, k);
            this.worldServer.triggerEffect(3000, blockPosition, 0);
            new WorldGenEndGateway().generate(this.worldServer, new Random(), blockPosition);
        }

        if (!previouslyKilled) {
            this.worldServer.setTypeUpdate(this.worldServer.getHighestBlockYAt(islandBlockPosition), Blocks.DRAGON_EGG.getBlockData());
        }

        previouslyKilled = true;
        dragonKilled = true;
    }

    @Override
    public void b(EntityEnderDragon entityEnderDragon) {
        if (!entityEnderDragon.getUniqueID().equals(dragonUUID))
            return;

        this.bossBattleServer.setProgress(entityEnderDragon.getHealth() / entityEnderDragon.getMaxHealth());

        if (entityEnderDragon.hasCustomName())
            this.bossBattleServer.a(entityEnderDragon.getScoreboardDisplayName());
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
        } else {
            this.countCrystals();
            Entity entity = this.worldServer.getEntity(dragonUUID);
            if (entity instanceof EntityEnderDragon)
                ((EntityEnderDragon) entity).a(entityEnderCrystal, entityEnderCrystal.getChunkCoordinates(), damageSource);
        }
    }

    @Override
    public boolean d() {
        return previouslyKilled;
    }

    @Override
    public void e() {
        if (!dragonKilled || respawnPhase != null)
            return;

        crystalsList = this.worldServer.a(EntityEnderCrystal.class, borderArea);

        respawnPhase = EnumDragonRespawn.START;
        respawnTick = 0;
        this.generateExitPortal(false);
    }

    @Override
    public void f() {
        for (EntityEnderCrystal entityEnderCrystal : this.worldServer.a(EntityEnderCrystal.class, borderArea)) {
            entityEnderCrystal.setInvulnerable(false);
            entityEnderCrystal.setBeamTarget(null);
        }
    }

    public void removeBattlePlayers() {
        this.bossBattleServer.getPlayers().forEach(this.bossBattleServer::removePlayer);
    }

    public IslandEntityEnderDragon getEnderDragon() {
        return this.entityEnderDragon;
    }

    private IslandEntityEnderDragon spawnEnderDragon() {
        IslandEntityEnderDragon entityEnderDragon = new IslandEntityEnderDragon(this.worldServer, islandBlockPosition);
        entityEnderDragon.getDragonControllerManager().setControllerPhase(DragonControllerPhase.a);
        entityEnderDragon.setPositionRotation(islandBlockPosition.getX(), 128,
                islandBlockPosition.getZ(), this.worldServer.random.nextFloat() * 360.0F, 0.0F);

        this.worldServer.addEntity(entityEnderDragon, CreatureSpawnEvent.SpawnReason.NATURAL);

        this.dragonUUID = entityEnderDragon.getUniqueID();
        //this.f(); // scan for crystals

        return entityEnderDragon;
    }

    private void doTick() {
        this.bossBattleServer.setVisible(!dragonKilled);

        // Update battle players
        if (++currentTick >= 20) {
            updateBattlePlayers();
            currentTick = 0;
        }

        if (this.bossBattleServer.getPlayers().isEmpty()) {
            return;
        }

        if (respawnPhase != null) {
            if (crystalsList == null) {
                respawnPhase = null;
                e();
            }

            if (++crystalsCountTick >= 100) {
                this.countCrystals();
                crystalsCountTick = 0;
            }

            respawnPhase.a(this.worldServer, this, crystalsList, respawnTick++, exitPortalLocation);
        }
    }

    private void countCrystals() {
        this.crystalsCount = this.worldServer.a(EntityEnderCrystal.class, borderArea).size();
    }

    private void generateExitPortal(boolean flag) {
        WorldGenEndTrophy worldGenEndTrophy = new WorldGenEndTrophy(flag);

        if (exitPortalLocation == null) {
            exitPortalLocation = this.worldServer.q(islandBlockPosition).down();
            while (this.worldServer.getType(exitPortalLocation).getBlock() == Blocks.BEDROCK && exitPortalLocation.getY() > this.worldServer.getSeaLevel())
                exitPortalLocation = exitPortalLocation.down();
        }

        worldGenEndTrophy.generate(this.worldServer, new Random(), exitPortalLocation.up(2));
    }

    private void updateBattlePlayers() {
        Set<EntityPlayer> nearbyPlayers = Sets.newHashSet();

        for (SuperiorPlayer superiorPlayer : island.getAllPlayersInside()) {
            Player player = superiorPlayer.asPlayer();
            assert player != null;
            if (((CraftWorld) player.getWorld()).getHandle() == this.worldServer) {
                EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
                this.bossBattleServer.addPlayer(entityPlayer);
                nearbyPlayers.add(entityPlayer);
            }
        }

        new HashSet<>(this.bossBattleServer.getPlayers()).stream()
                .filter(entityPlayer -> !nearbyPlayers.contains(entityPlayer))
                .forEach(this.bossBattleServer::removePlayer);
    }

}
