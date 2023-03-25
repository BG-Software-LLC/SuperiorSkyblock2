package com.bgsoftware.superiorskyblock.nms.v1_12_R1.dragon;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import net.minecraft.server.v1_12_R1.BlockPosition;
import net.minecraft.server.v1_12_R1.EnderDragonBattle;
import net.minecraft.server.v1_12_R1.EntityEnderDragon;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import net.minecraft.server.v1_12_R1.World;
import net.minecraft.server.v1_12_R1.WorldProviderTheEnd;
import net.minecraft.server.v1_12_R1.WorldServer;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEnderDragon;

public class IslandEntityEnderDragon extends EntityEnderDragon {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private BlockPosition islandBlockPosition;

    public IslandEntityEnderDragon(World world) {
        // Used when loading entities to the world.
        super(world);
        this.islandBlockPosition = BlockPosition.ZERO;
    }

    public IslandEntityEnderDragon(WorldServer worldServer, BlockPosition islandBlockPosition) {
        super(worldServer);
        this.islandBlockPosition = islandBlockPosition;
    }

    @Override
    public void a(NBTTagCompound nbtTagCompound) {
        super.a(nbtTagCompound);

        if (!(world.worldProvider instanceof WorldProviderTheEnd) || !plugin.getGrid().isIslandsWorld(world.getWorld()))
            return;

        EnderDragonBattle enderDragonBattle = ((WorldProviderTheEnd) world.worldProvider).t();

        if (!(enderDragonBattle instanceof EndWorldEnderDragonBattleHandler))
            return;

        EndWorldEnderDragonBattleHandler dragonBattleHandler = (EndWorldEnderDragonBattleHandler) enderDragonBattle;

        Location entityLocation = getBukkitEntity().getLocation();
        Island island = plugin.getGrid().getIslandAt(entityLocation);

        if (island == null)
            return;

        Location middleBlock = plugin.getSettings().getWorlds().getEnd().getPortalOffset()
                .applyToLocation(island.getCenter(org.bukkit.World.Environment.THE_END));
        this.islandBlockPosition = new BlockPosition(middleBlock.getX(), middleBlock.getY(), middleBlock.getZ());

        dragonBattleHandler.addDragonBattle(island.getUniqueId(), new IslandEnderDragonBattle(island,
                (WorldServer) world, this.islandBlockPosition, this));
    }

    @Override
    public void n() {
        DragonUtils.runWithPodiumPosition(this.islandBlockPosition, super::n);
    }

    @Override
    public CraftEnderDragon getBukkitEntity() {
        return (CraftEnderDragon) super.getBukkitEntity();
    }

}
