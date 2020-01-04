package com.bgsoftware.superiorskyblock.nms;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.schematics.data.BlockType;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_8_R1.AxisAlignedBB;
import net.minecraft.server.v1_8_R1.Block;
import net.minecraft.server.v1_8_R1.BlockLeaves;
import net.minecraft.server.v1_8_R1.BlockPosition;
import net.minecraft.server.v1_8_R1.Chunk;
import net.minecraft.server.v1_8_R1.ChunkSection;
import net.minecraft.server.v1_8_R1.Entity;
import net.minecraft.server.v1_8_R1.EntityPlayer;
import net.minecraft.server.v1_8_R1.IBlockData;
import net.minecraft.server.v1_8_R1.IChatBaseComponent;
import net.minecraft.server.v1_8_R1.ItemStack;
import net.minecraft.server.v1_8_R1.MinecraftServer;
import net.minecraft.server.v1_8_R1.MobSpawnerAbstract;
import net.minecraft.server.v1_8_R1.NBTTagCompound;
import net.minecraft.server.v1_8_R1.NBTTagList;
import net.minecraft.server.v1_8_R1.PacketPlayOutMapChunk;
import net.minecraft.server.v1_8_R1.TileEntity;
import net.minecraft.server.v1_8_R1.TileEntityBanner;
import net.minecraft.server.v1_8_R1.TileEntityFlowerPot;
import net.minecraft.server.v1_8_R1.TileEntityMobSpawner;
import net.minecraft.server.v1_8_R1.TileEntitySign;
import net.minecraft.server.v1_8_R1.TileEntitySkull;
import net.minecraft.server.v1_8_R1.World;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.SkullType;
import org.bukkit.block.BlockFace;
import org.bukkit.block.banner.Pattern;
import org.bukkit.craftbukkit.v1_8_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_8_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R1.block.CraftSign;
import org.bukkit.craftbukkit.v1_8_R1.inventory.CraftItemStack;
import org.bukkit.entity.EntityType;

import java.lang.reflect.Field;
import java.util.List;

@SuppressWarnings("unused")
public final class NMSBlocks_v1_8_R1 implements NMSBlocks {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    @Override
    public void setBlock(org.bukkit.Chunk bukkitChunk, Location location, int combinedId, BlockType blockType, Object... args) {
        World world = ((CraftWorld) location.getWorld()).getHandle();
        Chunk chunk = world.getChunkAt(location.getChunk().getX(), location.getChunk().getZ());

        BlockPosition blockPosition = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        IBlockData blockData = Block.getByCombinedId(combinedId);

        if(blockData.getBlock() instanceof BlockLeaves)
            blockData = blockData.set(BlockLeaves.DECAYABLE, false);

        if(blockData.getBlock().getMaterial().isLiquid() && plugin.getSettings().liquidUpdate) {
            world.setTypeAndData(blockPosition, blockData, 3);
            return;
        }

        int indexY = location.getBlockY() >> 4;

        ChunkSection chunkSection = chunk.getSections()[indexY];

        if(chunkSection == null)
            chunkSection = chunk.getSections()[indexY] = new ChunkSection(indexY << 4, !chunk.world.worldProvider.o());

        int blockX = location.getBlockX() & 15, blockY = location.getBlockY() & 15, blockZ = location.getBlockZ() & 15;

        chunkSection.setType(blockX, blockY, blockZ, blockData);

        if(blockType != BlockType.BLOCK) {
            TileEntity tileEntity = world.getTileEntity(blockPosition);

            switch (blockType) {
                case BANNER:
                    //noinspection unchecked
                    setTileEntityBanner(tileEntity, (DyeColor) args[0], (List<Pattern>) args[1]);
                    break;
                case INVENTORY_HOLDER:
                    setTileEntityInventoryHolder(tileEntity, (org.bukkit.inventory.ItemStack[]) args[0]);
                    break;
                case FLOWER_POT:
                    setTileEntityFlowerPot(tileEntity, (org.bukkit.inventory.ItemStack) args[0]);
                    break;
                case SKULL:
                    setTileEntitySkull(tileEntity, (SkullType) args[0], (BlockFace) args[1], (String) args[2]);
                    break;
                case SIGN:
                    setTileEntitySign(tileEntity, (String[]) args[0]);
                    break;
                case SPAWNER:
                    setTileEntityMobSpawner(tileEntity, (EntityType) args[0]);
                    break;
            }
        }

    }

    @Override
    public void refreshChunk(org.bukkit.Chunk bukkitChunk) {
        World world = ((CraftWorld) bukkitChunk.getWorld()).getHandle();
        Chunk chunk = ((CraftChunk) bukkitChunk).getHandle();

        PacketPlayOutMapChunk packetPlayOutMapChunk = new PacketPlayOutMapChunk(chunk, true, 65535);

        AxisAlignedBB bb = new AxisAlignedBB((bukkitChunk.getX() << 4) - 60, 0, (bukkitChunk.getZ() << 4) - 60,
                (bukkitChunk.getX() << 4) + 60, 256, (bukkitChunk.getZ() << 4) + 60);

        //noinspection unchecked
        for(Entity entity : (List<Entity>) ((CraftWorld) bukkitChunk.getWorld()).getHandle().getEntities(null, bb)){
            if(entity instanceof EntityPlayer)
                ((EntityPlayer) entity).playerConnection.sendPacket(packetPlayOutMapChunk);
        }
    }

    @Override
    public void refreshLight(org.bukkit.Chunk chunk) {
        ((CraftChunk) chunk).getHandle().initLighting();
    }

    @Override
    public org.bukkit.inventory.ItemStack getFlowerPot(Location location) {
        World world = ((CraftWorld) location.getWorld()).getHandle();
        BlockPosition blockPosition = new BlockPosition(location.getX(), location.getY(), location.getZ());
        TileEntityFlowerPot tileEntityFlowerPot = (TileEntityFlowerPot) world.getTileEntity(blockPosition);
        ItemStack itemStack = new ItemStack(tileEntityFlowerPot.b(), 1, tileEntityFlowerPot.c());
        return CraftItemStack.asBukkitCopy(itemStack);
    }

    @Override
    public int getCombinedId(Location location) {
        World world = ((CraftWorld) location.getWorld()).getHandle();
        IBlockData blockData =  world.getType(new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ()));
        return Block.getCombinedId(blockData);
    }

    @Override
    public void setTileEntityBanner(Object objectTileEntityBanner, DyeColor dyeColor, List<Pattern> patterns) {
        TileEntityBanner tileEntityBanner = (TileEntityBanner) objectTileEntityBanner;
        //noinspection deprecation
        tileEntityBanner.color = dyeColor.getDyeData();
        tileEntityBanner.patterns = new NBTTagList();

        for(Pattern pattern : patterns){
            NBTTagCompound compound = new NBTTagCompound();
            //noinspection deprecation
            compound.setInt("Color", pattern.getColor().getDyeData());
            compound.setString("Pattern", pattern.getPattern().getIdentifier());
            tileEntityBanner.patterns.add(compound);
        }
    }

    @Override
    public void setTileEntityInventoryHolder(Object tileEntityInventoryHolder, org.bukkit.inventory.ItemStack[] contents) {
        try{
            Field field = tileEntityInventoryHolder.getClass().getDeclaredField("items");
            field.setAccessible(true);
            ItemStack[] items = (ItemStack[]) field.get(tileEntityInventoryHolder);
            for(int i = 0; i < items.length && i < contents.length; i++){
                items[i] = CraftItemStack.asNMSCopy(contents[i]);
            }
        }catch(Exception ignored){ }
    }

    @Override
    public void setTileEntityFlowerPot(Object objectTileEntityFlowerPot, org.bukkit.inventory.ItemStack bukkitFlower) {
        TileEntityFlowerPot tileEntityFlowerPot = (TileEntityFlowerPot) objectTileEntityFlowerPot;
        ItemStack flower = CraftItemStack.asNMSCopy(bukkitFlower);
        tileEntityFlowerPot.a(flower.getItem(), flower.getData());
    }

    @Override
    public void setTileEntitySkull(Object objectTileEntitySkull, SkullType skullType, BlockFace rotation, String owner) {
        TileEntitySkull tileEntitySkull = (TileEntitySkull) objectTileEntitySkull;

        if (skullType == SkullType.PLAYER) {
            if(owner != null) {
                GameProfile gameProfile = MinecraftServer.getServer().getUserCache().getProfile(owner);
                if (gameProfile != null)
                    tileEntitySkull.setGameProfile(gameProfile);
            }
        } else {
            tileEntitySkull.setSkullType(skullType.ordinal() - 1);
        }

        switch(rotation) {
            case NORTH:
                tileEntitySkull.setRotation(0);
                break;
            case EAST:
                tileEntitySkull.setRotation(4);
                break;
            case SOUTH:
                tileEntitySkull.setRotation(8);
                break;
            case WEST:
                tileEntitySkull.setRotation(12);
                break;
            case NORTH_EAST:
                tileEntitySkull.setRotation(2);
                break;
            case NORTH_WEST:
                tileEntitySkull.setRotation(14);
                break;
            case SOUTH_EAST:
                tileEntitySkull.setRotation(6);
                break;
            case SOUTH_WEST:
                tileEntitySkull.setRotation(10);
                break;
            case WEST_NORTH_WEST:
                tileEntitySkull.setRotation(13);
                break;
            case NORTH_NORTH_WEST:
                tileEntitySkull.setRotation(15);
                break;
            case NORTH_NORTH_EAST:
                tileEntitySkull.setRotation(1);
                break;
            case EAST_NORTH_EAST:
                tileEntitySkull.setRotation(3);
                break;
            case EAST_SOUTH_EAST:
                tileEntitySkull.setRotation(5);
                break;
            case SOUTH_SOUTH_EAST:
                tileEntitySkull.setRotation(7);
                break;
            case SOUTH_SOUTH_WEST:
                tileEntitySkull.setRotation(9);
                break;
            case WEST_SOUTH_WEST:
                tileEntitySkull.setRotation(11);
                break;
        }
    }

    @Override
    public void setTileEntitySign(Object objectTileEntitySign, String[] lines) {
        TileEntitySign tileEntitySign = (TileEntitySign) objectTileEntitySign;
        IChatBaseComponent[] newLines = CraftSign.sanitizeLines(lines);
        System.arraycopy(newLines, 0, tileEntitySign.lines, 0, 4);
    }

    @Override
    public void setTileEntityMobSpawner(Object objectTileEntityMobSpawner, EntityType spawnedType) {
        MobSpawnerAbstract mobSpawner = ((TileEntityMobSpawner) objectTileEntityMobSpawner).getSpawner();
        mobSpawner.setMobName(spawnedType.name());
    }

}
