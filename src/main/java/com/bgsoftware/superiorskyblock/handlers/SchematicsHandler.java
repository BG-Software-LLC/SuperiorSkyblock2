package com.bgsoftware.superiorskyblock.handlers;

import com.bgsoftware.superiorskyblock.schematics.Schematic;
import com.bgsoftware.superiorskyblock.schematics.TagBuilder;
import com.google.common.collect.Lists;
import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblock;
import com.bgsoftware.superiorskyblock.utils.jnbt.ByteTag;
import com.bgsoftware.superiorskyblock.utils.jnbt.CompoundTag;
import com.bgsoftware.superiorskyblock.utils.jnbt.ListTag;
import com.bgsoftware.superiorskyblock.utils.jnbt.NBTInputStream;
import com.bgsoftware.superiorskyblock.utils.jnbt.NBTOutputStream;
import com.bgsoftware.superiorskyblock.utils.jnbt.Tag;
import com.bgsoftware.superiorskyblock.wrappers.BlockPosition;

import com.bgsoftware.superiorskyblock.wrappers.WrappedPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Banner;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"WeakerAccess", "ResultOfMethodCallIgnored"})
public class SchematicsHandler {

    private SuperiorSkyblock plugin;

    private Map<String, Schematic> schematics = new HashMap<>();

    public SchematicsHandler(SuperiorSkyblock plugin){
        this.plugin = plugin;

        File schematicsFolder = new File(plugin.getDataFolder(), "schematics");

        if(!schematicsFolder.exists()) {
            schematicsFolder.mkdirs();
            if(plugin.getServer().getBukkitVersion().contains("1.13")){
                plugin.saveResource("schematics/default1_13.schematic", true);
                File fileDefault = new File(plugin.getDataFolder(), "schematics/default.schematic");
                File fileDefault1_13 = new File(plugin.getDataFolder(), "schematics/default1_13.schematic");
                fileDefault1_13.renameTo(fileDefault);
            }else{
                plugin.saveResource("schematics/default.schematic", true);
            }
        }

        //noinspection ConstantConditions
        for(File schemFile : schematicsFolder.listFiles()){
            schematics.put(schemFile.getName().replace(".schematic", ""), loadFromFile(schemFile));
        }
    }

    public Schematic getSchematic(String name) {
        return schematics.get(name);
    }

    public List<String> getSchematics(){
        return Lists.newArrayList(schematics.keySet());
    }

    public void saveSchematic(String schematicName, WrappedPlayer wrappedPlayer){
        if(Bukkit.isPrimaryThread()){
            new Thread(() -> saveSchematic(schematicName, wrappedPlayer)).start();
            return;
        }

        Location pos1 = wrappedPlayer.getSchematicPos1().parse(), pos2 = wrappedPlayer.getSchematicPos2().parse();
        wrappedPlayer.setSchematicPos1(null);
        wrappedPlayer.setSchematicPos2(null);

        World world = pos1.getWorld();
        Location min = new Location(world, Math.min(pos1.getX(), pos2.getX()), Math.min(pos1.getY(), pos2.getY()), Math.min(pos1.getZ(), pos2.getZ()));
        Location max = new Location(world, Math.max(pos1.getX(), pos2.getX()), Math.max(pos1.getY(), pos2.getY()), Math.max(pos1.getZ(), pos2.getZ()));

        int xSize = max.getBlockX() - min.getBlockX();
        int ySize = max.getBlockY() - min.getBlockY();
        int zSize = max.getBlockZ() - min.getBlockZ();

        List<Tag> blocks = new ArrayList<>(), entities = new ArrayList<>();

        for(int x = 0; x <= xSize; x++){
            for(int z = 0; z <= zSize; z++){
                for(int y = 0; y <= ySize; y++){
                    int _x = x + min.getBlockX(), _y = y + min.getBlockY(),  _z = z + min.getBlockZ();
                    Block block = world.getBlockAt(_x, _y, _z);

                    if(block.getType() != Material.AIR) {
                        TagBuilder tagBuilder = new TagBuilder().withBlockPosition(BlockPosition.of(x, y, z)).withCombinedId(getCombinedId(block));

                        if(block.getState() instanceof Banner){
                            tagBuilder.applyBanner((Banner) block.getState());
                        }
                        else if(block.getState() instanceof InventoryHolder){
                            tagBuilder.applyContents(((InventoryHolder) block.getState()).getInventory().getContents());
                        }
                        else if(block.getType() == Material.FLOWER_POT){
                            tagBuilder.applyFlower(getFlower(block));
                        }
                        else if(block.getState() instanceof Skull){
                            tagBuilder.applySkull((Skull) block.getState());
                        }else if(block.getState() instanceof Sign){
                            tagBuilder.applySign((Sign) block.getState());
                        }

                        blocks.add(tagBuilder.build());
                    }
                }
            }
        }

        //noinspection IntegerDivisionInFloatingPointContext
        Location center = new Location(world, xSize / 2, ySize / 2, zSize / 2).add(min);
        for(LivingEntity livingEntity : getEntities(min, max)){
            entities.add(new TagBuilder().applyEntity(livingEntity, center).build());
        }

        Map<String, Tag> compoundValue = new HashMap<>();
        compoundValue.put("xSize", new ByteTag((byte) xSize));
        compoundValue.put("ySize", new ByteTag((byte) ySize));
        compoundValue.put("zSize", new ByteTag((byte) zSize));
        compoundValue.put("blocks", new ListTag(CompoundTag.class, blocks));
        compoundValue.put("entities", new ListTag(CompoundTag.class, entities));

        Schematic schematic = new Schematic(new CompoundTag(compoundValue));
        schematics.put(schematicName, schematic);
        saveIntoFile(schematicName, schematic);

        Locale.SCHEMATIC_SAVED.send(wrappedPlayer);
    }

    private int getCombinedId(Block block) {
        return plugin.getNMSAdapter().getCombinedId(block.getLocation());
    }

    private ItemStack getFlower(Block block){
        return plugin.getNMSAdapter().getFlowerPot(block.getLocation());
    }

    private Schematic loadFromFile(File file){
        try {
            if(!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }

            NBTInputStream reader = new NBTInputStream(new FileInputStream(file));

            CompoundTag tag = null;

            try{
                tag = (CompoundTag) reader.readTag();
            }catch(ClassCastException ignored){ }

            reader.close();

            return tag == null ? null : new Schematic(tag);
        }catch(IOException ex){
            ex.printStackTrace();
        }

        return null;
    }

    private void saveIntoFile(String name, Schematic schematic){
        try {
            File file = new File(plugin.getDataFolder(), "schematics/" + name + ".schematic");

            if(file.exists())
                file.delete();

            file.getParentFile().mkdirs();
            file.createNewFile();

            NBTOutputStream writer = new NBTOutputStream(new FileOutputStream(file));

            writer.writeTag(schematic.getTag());

            writer.close();
        }catch(IOException ex){
            ex.printStackTrace();
        }
    }

    private List<LivingEntity> getEntities(Location min, Location max){
        List<LivingEntity> livingEntities = new ArrayList<>();

        Chunk minChunk = min.getChunk(), maxChunk = max.getChunk();
        for(int x = minChunk.getX(); x <= maxChunk.getX(); x++){
            for(int z = minChunk.getZ(); z <= maxChunk.getZ(); z++){
                Chunk currentChunk = min.getWorld().getChunkAt(x, z);
                for(Entity entity : currentChunk.getEntities()) {
                    if (entity instanceof LivingEntity && !(entity instanceof Player) && betweenLocations(entity.getLocation(), min, max))
                        livingEntities.add((LivingEntity) entity);
                }
            }
        }

        return livingEntities;
    }

    private boolean betweenLocations(Location location, Location min, Location max){
        return location.getBlockX() >= min.getBlockX() && location.getBlockX() <= max.getBlockX() &&
                location.getBlockY() >= min.getBlockY() && location.getBlockY() <= max.getBlockY() &&
                location.getBlockZ() >= min.getBlockZ() && location.getBlockZ() <= max.getBlockZ();
    }

}
