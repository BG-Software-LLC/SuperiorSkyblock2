package com.ome_r.superiorskyblock.handlers;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import com.ome_r.superiorskyblock.Locale;
import com.ome_r.superiorskyblock.SuperiorSkyblock;
import com.ome_r.superiorskyblock.gui.SyncGUIInventory;
import com.ome_r.superiorskyblock.island.Island;
import com.ome_r.superiorskyblock.island.IslandRegistry;
import com.ome_r.superiorskyblock.island.SpawnIsland;
import com.ome_r.superiorskyblock.schematics.Schematic;
import com.ome_r.superiorskyblock.utils.FileUtil;
import com.ome_r.superiorskyblock.utils.ItemBuilder;
import com.ome_r.superiorskyblock.utils.key.Key;
import com.ome_r.superiorskyblock.utils.jnbt.CompoundTag;
import com.ome_r.superiorskyblock.utils.jnbt.IntTag;
import com.ome_r.superiorskyblock.utils.jnbt.ListTag;
import com.ome_r.superiorskyblock.utils.jnbt.StringTag;
import com.ome_r.superiorskyblock.utils.key.KeyMap;
import com.ome_r.superiorskyblock.utils.queue.Queue;
import com.ome_r.superiorskyblock.utils.jnbt.Tag;
import com.ome_r.superiorskyblock.wrappers.WrappedLocation;
import com.ome_r.superiorskyblock.wrappers.WrappedPlayer;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@SuppressWarnings({"WeakerAccess", "unused"})
public class GridHandler {

    private SuperiorSkyblock plugin;

    private Queue<CreateIslandData> islandCreationsQueue = new Queue<>();
    private boolean creationProgress = false;

    private IslandRegistry islands = new IslandRegistry();
    private StackedBlocksHandler stackedBlocks = new StackedBlocksHandler();
    private IslandTopHandler topIslands = new IslandTopHandler();
    private BlockValuesHandler blockValues = new BlockValuesHandler();

    private Island spawnIsland;
    private WrappedLocation lastIsland;

    public GridHandler(SuperiorSkyblock plugin){
        this.plugin = plugin;
        lastIsland = WrappedLocation.of(plugin.getSettings().islandWorld, 0, 100, 0);
        spawnIsland = new SpawnIsland(WrappedLocation.of(plugin.getSettings().spawnLocation));
    }

    public void createIsland(CompoundTag tag){
        UUID owner = UUID.fromString(((StringTag) tag.getValue().get("owner")).getValue());
        islands.add(owner, new Island(tag));
    }

    public void createIsland(WrappedPlayer wrappedPlayer, String schemName){
        if(creationProgress) {
            islandCreationsQueue.push(new CreateIslandData(wrappedPlayer.getUniqueId(), schemName));
            return;
        }

        long startTime = System.currentTimeMillis();
        creationProgress = true;

        Location islandLocation = getNextLocation();
        Island island = new Island(wrappedPlayer, islandLocation);

        islands.add(wrappedPlayer.getUniqueId(), island);
        lastIsland = WrappedLocation.of(getNextLocation());

        for(Chunk chunk : island.getAllChunks()) {
            chunk.getWorld().regenerateChunk(chunk.getX(), chunk.getZ());
            plugin.getNMSAdapter().refreshChunk(chunk);
        }

        Schematic schematic = plugin.getSchematics().getSchematic(schemName);
        schematic.pasteSchematic(islandLocation.getBlock().getRelative(BlockFace.DOWN).getLocation());

        if(wrappedPlayer.asOfflinePlayer().isOnline()) {
            Locale.CREATE_ISLAND.send(wrappedPlayer, WrappedLocation.of(islandLocation), System.currentTimeMillis() - startTime);
            wrappedPlayer.asPlayer().teleport(islandLocation);
            Bukkit.getScheduler().runTaskLater(plugin, () -> plugin.getNMSAdapter().setWorldBorder(wrappedPlayer, island), 20L);
        }

        new Thread(() -> island.calcIslandWorth(null)).start();

        creationProgress = false;

        if(islandCreationsQueue.size() != 0){
            CreateIslandData data = islandCreationsQueue.pop();
            createIsland(WrappedPlayer.of(data.player), data.schemName);
        }
    }

    public void deleteIsland(Island island){
        WrappedPlayer targetPlayer;
        for(UUID uuid : island.allPlayersInside()){
            targetPlayer = WrappedPlayer.of(uuid);
            targetPlayer.asPlayer().teleport(spawnIsland.getCenter());
            Locale.ISLAND_GOT_DELETED_WHILE_INSIDE.send(targetPlayer);
        }
        islands.remove(island.getOwner().getUniqueId());
    }

    public Island getIsland(WrappedPlayer wrappedPlayer){
        return getIsland(wrappedPlayer.getTeamLeader());
    }

    private Island getIsland(UUID uuid){
        return islands.get(uuid);
    }

    public Island getIsland(int index){
        return index >= islands.size() ? null : islands.get(index);
    }

    public Island getIslandAt(Location location){
        if(!location.getWorld().getName().equals(plugin.getSettings().islandWorld))
            return null;

        if(spawnIsland.isInside(location))
            return spawnIsland;

        Iterator<Island> islands = this.islands.iterator();
        Island island;

        while(islands.hasNext()){
            island = islands.next();
            if(island.isInside(location))
                return island;
        }

        return null;
    }

    public Island getSpawnIsland(){
        return spawnIsland;
    }

    public Location getNextLocation(){
        Location location = lastIsland.parse().clone();
        BlockFace islandFace = getIslandFace();

        int islandRange = plugin.getSettings().maxIslandSize * 3;

        if(islandFace == BlockFace.NORTH){
            location.add(islandRange, 0, 0);
        }else if(islandFace == BlockFace.EAST){
            if(lastIsland.getX() == -lastIsland.getZ())
                location.add(islandRange, 0, 0);
            else if(lastIsland.getX() == lastIsland.getZ())
                location.subtract(islandRange, 0, 0);
            else
                location.add(0, 0, islandRange);
        }else if(islandFace == BlockFace.SOUTH){
            if(lastIsland.getX() == -lastIsland.getZ())
                location.subtract(0, 0, islandRange);
            else
                location.subtract(islandRange, 0, 0);
        }else if(islandFace == BlockFace.WEST){
            if(lastIsland.getX() == lastIsland.getZ())
                location.add(islandRange, 0, 0);
            else
                location.subtract(0, 0, islandRange);
        }

        return location;
    }

    public List<UUID> getAllIslands(){
        return Lists.newArrayList(islands.uuidIterator());
    }

    public int getBlockValue(Key key){
        return blockValues.getBlockValue(key);
    }

    public int getBlockAmount(Block block){
        return stackedBlocks.getOrDefault(WrappedLocation.of(block.getLocation()), 1);
    }

    public void setBlockAmount(Block block, int amount){
        stackedBlocks.put(WrappedLocation.of(block.getLocation()), amount);
        stackedBlocks.updateName(block);
    }

    public SyncGUIInventory getTopIslands(){
        return topIslands.topIslands;
    }

    public void openTopIslands(WrappedPlayer wrappedPlayer){
        topIslands.openTopIslands(wrappedPlayer);
    }

    public void loadGrid(CompoundTag tag){
        Map<String, Tag> compoundValues = tag.getValue(), _compoundValues;

        lastIsland = WrappedLocation.of(((StringTag) compoundValues.get("lastIsland")).getValue());

        for(Tag _tag : ((ListTag) compoundValues.get("stackedBlocks")).getValue()){
            _compoundValues = ((CompoundTag) _tag).getValue();
            String location = ((StringTag) _compoundValues.get("location")).getValue();
            int stackAmount = ((IntTag) _compoundValues.get("stackAmount")).getValue();
            stackedBlocks.put(WrappedLocation.of(location), stackAmount);
        }
    }

    public void calcAllIslands(){
        for (Island island : islands)
            island.calcIslandWorth(null);
    }

    public CompoundTag getAsTag(){
        Map<String, Tag> compoundValues = Maps.newHashMap(), _compoundValues;
        List<Tag> stackedBlocks = Lists.newArrayList();

        compoundValues.put("lastIsland", new StringTag(lastIsland.toString()));

        for(Map.Entry<WrappedLocation, Integer> entry : this.stackedBlocks.entrySet()){
            _compoundValues = Maps.newHashMap();
            _compoundValues.put("location", new StringTag(entry.getKey().toString()));
            _compoundValues.put("stackAmount", new IntTag(entry.getValue()));
            stackedBlocks.add(new CompoundTag(_compoundValues));
        }

        compoundValues.put("stackedBlocks", new ListTag(CompoundTag.class, stackedBlocks));

        return new CompoundTag(compoundValues);
    }

    private BlockFace getIslandFace(){
        //Possibilities: North / East
        if(lastIsland.getX() >= lastIsland.getZ()) {
            return -lastIsland.getX() > lastIsland.getZ() ? BlockFace.NORTH : BlockFace.EAST;
        }
        //Possibilities: South / West
        else{
            return -lastIsland.getX() > lastIsland.getZ() ? BlockFace.WEST : BlockFace.SOUTH;
        }
    }

    private class CreateIslandData {

        public UUID player;
        public String schemName;

        public CreateIslandData(UUID player, String schemName){
            this.player = player;
            this.schemName = schemName;
        }

    }

    private class StackedBlocksHandler {

        private Map<WrappedLocation, Integer> stackedBlocks = Maps.newHashMap();

        void put(WrappedLocation location, int amount){
            stackedBlocks.put(location, amount);
        }

        @SuppressWarnings("SameParameterValue")
        int getOrDefault(WrappedLocation location, int def){
            return stackedBlocks.getOrDefault(location, def);
        }

        Set<Map.Entry<WrappedLocation, Integer>> entrySet(){
            return stackedBlocks.entrySet();
        }

        private void updateName(Block block){
            int amount = getBlockAmount(block);
            ArmorStand armorStand = getHologram(block);

            if(amount <= 1){
                stackedBlocks.remove(WrappedLocation.of(block.getLocation()));
                armorStand.remove();
            }else{
                armorStand.setCustomName(plugin.getSettings().stackedBlocksName
                        .replace("{0}", String.valueOf(amount))
                        .replace("{1}", getFormattedType(block.getType().name()))
                );
            }

        }

        private ArmorStand getHologram(Block block){
            Location hologramLocation = block.getLocation().add(0.5, 1, 0.5);

            // Looking for an armorstand
            for(Entity entity : block.getChunk().getEntities()){
                if(entity instanceof ArmorStand && entity.getLocation().equals(hologramLocation)){
                    return (ArmorStand) entity;
                }
            }

            // Couldn't find one, creating one...

            ArmorStand armorStand = block.getWorld().spawn(hologramLocation, ArmorStand.class);

            armorStand.setGravity(false);
            armorStand.setSmall(true);
            armorStand.setVisible(false);
            armorStand.setCustomNameVisible(true);
            armorStand.setMarker(true);

            return armorStand;
        }

        private String getFormattedType(String type){
            StringBuilder stringBuilder = new StringBuilder();

            for(String section : type.split("_")){
                stringBuilder.append(" ").append(section.substring(0, 1).toUpperCase()).append(section.substring(1).toLowerCase());
            }

            return stringBuilder.toString().substring(1);
        }

    }

    private class IslandTopHandler {

        private SyncGUIInventory topIslands;

        public IslandTopHandler(){
            SuperiorSkyblock plugin = SuperiorSkyblock.getPlugin();

            File file = new File(plugin.getDataFolder(), "guis/top-islands.yml");

            if(!file.exists())
                FileUtil.saveResource("guis/top-islands.yml");

            YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

            topIslands = FileUtil.getGUI(cfg.getConfigurationSection("top-islands"), 6, "&lTop Islands").toSyncGUI();

            ItemStack islandItem = FileUtil.getItemStack(cfg.getConfigurationSection("top-islands.island-item"));
            ItemStack noIslandItem = FileUtil.getItemStack(cfg.getConfigurationSection("top-islands.no-island-item"));

            List<Integer> slots = new ArrayList<>();
            Arrays.stream(cfg.getString("top-islands.slots").split(","))
                    .forEach(slot -> slots.add(Integer.valueOf(slot)));

            topIslands.getData().put("islandItem", islandItem);
            topIslands.getData().put("noIslandItem", noIslandItem);
            topIslands.getData().put("slots", slots.toArray(new Integer[0]));

            reloadGUI();
        }

        void openTopIslands(WrappedPlayer wrappedPlayer){
            if(!Bukkit.isPrimaryThread()){
                Bukkit.getScheduler().runTask(plugin, () -> openTopIslands(wrappedPlayer));
                return;
            }

            islands.sort();
            plugin.getPanel().openedPanel.put(wrappedPlayer.getUniqueId(), PanelHandler.PanelType.TOP);
            topIslands.openInventory(wrappedPlayer);

            reloadGUI();
        }

        private void reloadGUI(){
            if(Bukkit.isPrimaryThread()){
                new Thread(this::reloadGUI).start();
                return;
            }

            Integer[] slots = (Integer[]) topIslands.getData().get("slots");

            for(int i = 0; i < slots.length; i++){
                Island island = i >= islands.size() ? null : islands.get(i);
                ItemStack itemStack = getTopItem(island, i + 1);
                topIslands.setItem(slots[i], itemStack);
            }
        }

        private ItemStack getTopItem(Island island, int place){
            WrappedPlayer islandOwner = island == null ? null : island.getOwner();

            ItemStack itemStack;

            if(islandOwner == null){
                itemStack = ((ItemStack) topIslands.getData().get("noIslandItem")).clone();
            }

            else{
                itemStack = ((ItemStack) topIslands.getData().get("islandItem")).clone();
            }

            ItemBuilder itemBuilder = new ItemBuilder(itemStack).asSkullOf(islandOwner);

            if(island != null && islandOwner != null) {
                itemBuilder.replaceName("{0}", islandOwner.getName())
                        .replaceName("{1}", String.valueOf(place))
                        .replaceName("{2}", String.valueOf(island.getIslandLevel()))
                        .replaceName("{3}", String.valueOf(island.getWorth()));

                if(itemStack.getItemMeta().hasLore()){
                    List<String> lore = new ArrayList<>();

                    for(String line : itemStack.getItemMeta().getLore()){
                        if(line.contains("{4}")){
                            String memberFormat = line.split("\\{4}:")[1];
                            if(island.getMembers().size() == 1){
                                lore.add(memberFormat.replace("{}", "None"));
                            }
                            else {
                                for (UUID memberUUID : island.getMembers()) {
                                    lore.add(memberFormat.replace("{}", WrappedPlayer.of(memberFormat).getName()));
                                }
                            }
                        }else{
                            lore.add(line
                                    .replace("{0}", island.getOwner().getName())
                                    .replace("{1}", String.valueOf(place))
                                    .replace("{2}", String.valueOf(island.getIslandLevel()))
                                    .replace("{3}", String.valueOf(island.getWorth())));
                        }
                    }

                    itemBuilder.withLore(lore);
                }
            }

            return itemBuilder.build();
        }

    }

    private class BlockValuesHandler {

        private final KeyMap<Integer> blockValues = new KeyMap<>();

        private BlockValuesHandler(){
            SuperiorSkyblock plugin = SuperiorSkyblock.getPlugin();

            File file = new File(plugin.getDataFolder(), "blockvalues.yml");

            if(!file.exists())
                plugin.saveResource("blockvalues.yml", true);

            YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

            for(String key : cfg.getConfigurationSection("block-values").getKeys(false))
                blockValues.put(Key.of(key), cfg.getInt("block-values." + key));
        }

        int getBlockValue(Key key) {
            return blockValues.getOrDefault(key, 0);
        }
    }

}
