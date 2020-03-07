package com.bgsoftware.superiorskyblock.schematics;

import com.bgsoftware.superiorskyblock.api.events.IslandSchematicPasteEvent;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.schematic.Schematic;
import com.bgsoftware.superiorskyblock.island.SIsland;
import com.bgsoftware.superiorskyblock.utils.key.KeyMap;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.history.change.BlockChange;
import com.sk89q.worldedit.history.change.Change;
import com.sk89q.worldedit.math.transform.Transform;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;

import java.lang.reflect.Method;
import java.util.Iterator;

@SuppressWarnings("JavaReflectionMemberAccess")
public final class WorldEditSchematic extends BaseSchematic implements Schematic {

    private static Method blockVector3AtMethod = null;
    private static Method blockVector3PasteMethod = null;

    private static Method getCurrentBaseblock = null;
    private static Method baseBlockId = null;
    private static Method baseBlockData = null;
    private static Method getBlockTypes = null;
    private static Method adaptBlockType = null;

    static {
        try{
            Class<?> blockVector3Class = Class.forName("com.sk89q.worldedit.math.BlockVector3");
            blockVector3AtMethod = blockVector3Class.getMethod("at", int.class, int.class, int.class);
            //noinspection JavaReflectionMemberAccess
            blockVector3PasteMethod = com.boydti.fawe.object.schematic.Schematic.class
                    .getMethod("paste", World.class, blockVector3Class, boolean.class, boolean.class, Transform.class);
        }catch(Throwable ignored){ }

        try{
            Class<?> blockTypesClass = Class.forName("com.sk89q.worldedit.world.block.BlockTypes");
            getBlockTypes = BlockStateHolder.class.getMethod("getBlockType");
            adaptBlockType = BukkitAdapter.class.getMethod("adapt", blockTypesClass);
        }catch(Throwable ignored){}

        try{
            getCurrentBaseblock = BlockChange.class.getMethod("getCurrent");
            baseBlockId = BaseBlock.class.getMethod("getId");
            baseBlockData = BaseBlock.class.getMethod("getData");
        }catch(Throwable ignored){}

    }

    private com.boydti.fawe.object.schematic.Schematic schematic;


    public WorldEditSchematic(String name, com.boydti.fawe.object.schematic.Schematic schematic){
        super(name);
        this.schematic = schematic;
    }

    @Override
    public void pasteSchematic(Location location, Runnable callback) {
        pasteSchematic(null, location, callback);
    }

    @Override
    public void pasteSchematic(Island island, Location location, Runnable callback) {
        if(schematicProgress) {
            pasteSchematicQueue.push(new PasteSchematicData(this, island, location, callback));
            return;
        }

        schematicProgress = true;

        EditSession editSession;

        try{
            Object point = blockVector3AtMethod.invoke(null, location.getBlockX(), location.getBlockY(), location.getBlockZ());
            editSession = (EditSession) blockVector3PasteMethod.invoke(schematic, new BukkitWorld(location.getWorld()), point, false, true, null);
        }catch(Throwable ex){
            com.sk89q.worldedit.Vector point = new com.sk89q.worldedit.Vector(location.getBlockX(), location.getBlockY(), location.getBlockZ());
            editSession = schematic.paste(new BukkitWorld(location.getWorld()), point, false, true, null);
        }

        KeyMap<Integer> blocks = new KeyMap<>();
        Iterator<Change> changeIterator = editSession.getChangeSet().forwardIterator();

        while (changeIterator.hasNext()){
            Change change = changeIterator.next();
            if(change instanceof BlockChange){
                BlockChange blockChange = (BlockChange) change;
                Key key;
                try {
                    //noinspection rawtypes
                    BlockStateHolder blockStateHolder = blockChange.getCurrent();
                    Material material = (Material) adaptBlockType.invoke(null, getBlockTypes.invoke(blockStateHolder));
                    //noinspection deprecation
                    key = Key.of(material, (byte) blockStateHolder.getInternalId());
                }catch(Throwable ignored){
                    try{
                        Object baseBlock = getCurrentBaseblock.invoke(blockChange);
                        int id = (Integer) baseBlockId.invoke(baseBlock);
                        int data = (Integer) baseBlockData.invoke(baseBlock);
                        //noinspection deprecation
                        key = Key.of(Material.getMaterial(id), (byte) data);
                    }catch(Throwable ex){
                        ex.printStackTrace();
                        break;
                    }
                }
                blocks.put(key, blocks.getRaw(key, 0) + 1);
            }
        }

        ((SIsland) island).handleBlocksPlace(blocks);

        editSession.addNotifyTask(() -> {

            IslandSchematicPasteEvent islandSchematicPasteEvent = new IslandSchematicPasteEvent(island, name, location);
            Bukkit.getPluginManager().callEvent(islandSchematicPasteEvent);

            callback.run();

            schematicProgress = false;

            if(pasteSchematicQueue.size() != 0){
                PasteSchematicData data = pasteSchematicQueue.pop();
                data.schematic.pasteSchematic(data.island, data.location, data.callback);
            }
        });
    }
}
