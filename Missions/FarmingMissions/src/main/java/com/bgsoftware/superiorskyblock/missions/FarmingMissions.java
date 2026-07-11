package com.bgsoftware.superiorskyblock.missions;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeySet;
import com.bgsoftware.superiorskyblock.api.missions.MissionLoadException;
import com.bgsoftware.superiorskyblock.api.wrappers.BlockPosition;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.SBlockPosition;
import com.bgsoftware.superiorskyblock.missions.common.BuiltinMission;
import com.bgsoftware.superiorskyblock.missions.common.Placeholders;
import com.bgsoftware.superiorskyblock.missions.common.requirements.KeyRequirements;
import com.bgsoftware.superiorskyblock.missions.common.tracker.KeyDataTracker;
import com.bgsoftware.superiorskyblock.missions.farming.PlantType;
import com.bgsoftware.superiorskyblock.missions.farming.PlantsTracker;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class FarmingMissions extends BuiltinMission<KeyDataTracker> implements Listener {

    private static final BlockFace[] STEM_NEARBY_BLOCKS = new BlockFace[]{
            BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH
    };
    private static final BlockFace[] CHORUS_NEARBY_BLOCKS = new BlockFace[]{
            BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.DOWN
    };

    private final Map<KeyRequirements, Integer> requiredPlants = new LinkedHashMap<>();

    @Override
    protected void loadConfiguration(ConfigurationSection section) throws MissionLoadException {
        ConfigurationSection requiredPlantsSection = section.getConfigurationSection("required-plants");

        if (requiredPlantsSection == null)
            throw new MissionLoadException("You must have the \"required-plants\" section in the config.");

        for (String key : requiredPlantsSection.getKeys(false)) {
            KeySet requiredPlants = KeySet.createKeySet();
            section.getStringList("required-plants." + key + ".types").forEach(requiredPlant ->
                    requiredPlants.add(Key.ofMaterialAndData(requiredPlant.toUpperCase(Locale.ENGLISH))));
            int requiredAmount = section.getInt("required-plants." + key + ".amount");
            this.requiredPlants.put(new KeyRequirements(requiredPlants), requiredAmount);
        }
    }

    @Override
    protected void registerListeners() {
        registerListener(this);
    }

    @Override
    protected void clearModuleData(KeyDataTracker data) {
        data.clear();
    }

    @Override
    public double getProgress(SuperiorPlayer superiorPlayer) {
        KeyDataTracker farmingTracker = get(superiorPlayer);

        if (farmingTracker == null)
            return 0.0;

        int requiredPlants = 0;
        int grewPlants = 0;

        for (Map.Entry<KeyRequirements, Integer> requiredPlant : this.requiredPlants.entrySet()) {
            requiredPlants += requiredPlant.getValue();
            grewPlants += Math.min(farmingTracker.getCounts(requiredPlant.getKey()), requiredPlant.getValue());
        }

        return (double) grewPlants / requiredPlants;
    }

    @Override
    public int getProgressValue(SuperiorPlayer superiorPlayer) {
        KeyDataTracker farmingTracker = get(superiorPlayer);

        if (farmingTracker == null)
            return 0;

        int kills = 0;

        for (Map.Entry<KeyRequirements, Integer> requiredPlant : this.requiredPlants.entrySet())
            kills += Math.min(farmingTracker.getCounts(requiredPlant.getKey()), requiredPlant.getValue());

        return kills;
    }

    @Override
    public void saveProgress(ConfigurationSection section) {
        for (Map.Entry<SuperiorPlayer, KeyDataTracker> entry : entrySet()) {
            String uuid = entry.getKey().getUniqueId().toString();
            entry.getValue().getCounts().forEach((plant, count) ->
                    section.set("grown-plants." + uuid + "." + plant, count.get()));
        }

        PlantsTracker.INSTANCE.save(section);
    }

    @Override
    public void loadProgress(ConfigurationSection section) {
        ConfigurationSection grownPlants = section.getConfigurationSection("grown-plants");
        if (grownPlants != null) {
            for (String uuid : grownPlants.getKeys(false)) {
                KeyDataTracker farmingTracker = new KeyDataTracker();
                UUID playerUUID = UUID.fromString(uuid);
                SuperiorPlayer superiorPlayer = this.plugin.getPlayers().getSuperiorPlayer(playerUUID);

                insertData(superiorPlayer, farmingTracker);

                for (String key : grownPlants.getConfigurationSection(uuid).getKeys(false)) {
                    Key typeKey = getMissionPlantKey(Key.ofMaterialAndData(key));
                    if (typeKey == null) continue;
                    farmingTracker.load(typeKey, grownPlants.getInt(uuid + "." + key));
                }
            }
        }

        ConfigurationSection placedPlants = section.getConfigurationSection("placed-plants");
        if (placedPlants != null) {
            int dataVersion = section.getInt("data-version", 0);

            if (dataVersion == 0) {
                loadLegacyData(placedPlants);
            } else {
                for (String placerUUID : placedPlants.getKeys(false)) {
                    UUID placer = UUID.fromString(placerUUID);
                    ConfigurationSection placerSection = placedPlants.getConfigurationSection(placerUUID);
                    if (placerSection == null)
                        continue;

                    for (String worldName : placerSection.getKeys(false)) {
                        ConfigurationSection worldSection = placerSection.getConfigurationSection(worldName);
                        if (worldSection == null)
                            continue;

                        for (String chunkKey : worldSection.getKeys(false)) {
                            PlantsTracker.INSTANCE.load(worldName, Long.parseLong(chunkKey),
                                    worldSection.getIntegerList(chunkKey), placer);
                        }
                    }
                }

                ConfigurationSection legacyPlacedPlants = section.getConfigurationSection("placed-plants-legacy");
                if (legacyPlacedPlants != null)
                    loadLegacyData(legacyPlacedPlants);
            }
        }
    }

    private void loadLegacyData(ConfigurationSection section) {
        for (String locationKey : section.getKeys(false)) {
            UUID placer = UUID.fromString(section.getString(locationKey));
            String[] sections = locationKey.split(";");
            if (sections.length == 4) {
                BlockPosition plantPosition = plugin.getFactory().createBlockPosition(Integer.parseInt(sections[1]),
                        Integer.parseInt(sections[2]), Integer.parseInt(sections[3]));
                PlantsTracker.INSTANCE.loadLegacy(sections[0], plantPosition, placer);
            }
        }
    }

    @Override
    public void formatItem(SuperiorPlayer superiorPlayer, ItemStack itemStack) {
        KeyDataTracker farmingTracker = getOrCreate(superiorPlayer, s -> new KeyDataTracker());

        if (farmingTracker == null)
            return;

        ItemMeta itemMeta = itemStack.getItemMeta();

        if (itemMeta == null)
            return;

        if (itemMeta.hasDisplayName())
            itemMeta.setDisplayName(Placeholders.parseKeyPlaceholders(this.requiredPlants, farmingTracker, itemMeta.getDisplayName(), true));

        if (itemMeta.hasLore()) {
            List<String> lore = new ArrayList<>();
            for (String line : Objects.requireNonNull(itemMeta.getLore()))
                lore.add(Placeholders.parseKeyPlaceholders(this.requiredPlants, farmingTracker, line, true));
            itemMeta.setLore(lore);
        }

        itemStack.setItemMeta(itemMeta);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent e) {
        PlantType plantType = PlantType.getBySaplingType(e.getBlock().getType());
        Key plantKey = plantType == PlantType.UNKNOWN ? Key.of(e.getBlock()) : plantType.getCachedKey();

        if (getMissionPlantKey(plantKey) == null)
            return;

        PlantsTracker.INSTANCE.track(e.getBlock(), e.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e) {
        PlantsTracker.INSTANCE.untrack(e.getBlock());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockExplode(EntityExplodeEvent e) {
        for (Block block : e.blockList())
            PlantsTracker.INSTANCE.untrack(block);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent e) {
        for (Block block : e.getBlocks())
            PlantsTracker.INSTANCE.untrack(block);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPistonRetract(BlockPistonExtendEvent e) {
        for (Block block : e.getBlocks())
            PlantsTracker.INSTANCE.untrack(block);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBambooGrow(BlockSpreadEvent e) {
        handlePlantGrow(e.getBlock(), e.getNewState());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlantGrow(StructureGrowEvent e) {
        Block block = e.getLocation().getBlock();
        handlePlantGrow(block, block.getState());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlantGrow(BlockGrowEvent e) {
        handlePlantGrow(e.getBlock(), e.getNewState());
    }

    private void handlePlantGrow(Block plantBlock, BlockState newState) {
        PlantType plantType = PlantType.getByType(newState.getType());
        Key plantKey = plantType == PlantType.UNKNOWN ? Key.of(newState) : plantType.getCachedKey();
        plantKey = getMissionPlantKey(plantKey);

        if (plantKey == null)
            return;

        int maxAge = plantType.getMaxAge();
        if (maxAge > 0 && getBlockAge(newState) < maxAge)
            return;

        Location placedBlockLocation = plantBlock.getLocation();

        switch (plantType) {
            case CACTUS:
            case SUGAR_CANE:
            case BAMBOO:
                placedBlockLocation = getRoot(plantBlock);
                break;
            case MELON:
            case PUMPKIN:
                placedBlockLocation = getStemRoot(plantType, plantBlock);
                break;
            case CHORUS_PLANT:
                placedBlockLocation = getChorusRoot(plantBlock);
                break;
        }

        UUID placerUUID = PlantsTracker.INSTANCE.getPlacer(placedBlockLocation);

        if (placerUUID == null)
            return;

        SuperiorPlayer playerTracked;

        SuperiorPlayer superiorPlayer = this.plugin.getPlayers().getSuperiorPlayer(placerUUID);
        if (getIslandMission()) {
            // In case this is an island mission, we want to get the uuid of the owner.
            Island island = superiorPlayer.getIsland();
            if (island == null)
                return;
            playerTracked = island.getOwner();
        } else {
            playerTracked = superiorPlayer;
        }

        if (!this.plugin.getMissions().canCompleteNoProgress(playerTracked, this))
            return;

        KeyDataTracker farmingTracker = getOrCreate(playerTracked, s -> new KeyDataTracker());

        if (farmingTracker == null)
            return;

        farmingTracker.track(plantKey, 1);

        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> playerTracked.runIfOnline(player -> {
            if (canComplete(playerTracked))
                this.plugin.getMissions().rewardMission(this, playerTracked, true);
        }), 2L);
    }

    private int getBlockAge(BlockState newState) {
        try {
            BlockData blockData = newState.getBlockData();
            return blockData instanceof Ageable ? ((Ageable) blockData).getAge() : 0;
        } catch (Throwable error) {
            // noinspection deprecation
            return newState.getRawData();
        }
    }

    private static Location getRoot(Block original) {
        Block lastSimilarBlock = original.getRelative(BlockFace.DOWN);

        Material originalType = lastSimilarBlock.getType();

        while (lastSimilarBlock.getType() == originalType) {
            lastSimilarBlock = lastSimilarBlock.getRelative(BlockFace.DOWN);
        }

        return lastSimilarBlock.getLocation().add(0, 1, 0);
    }

    private static Location getStemRoot(PlantType plantType, Block plantBlock) {
        Material stemType = plantType == PlantType.PUMPKIN ? Material.PUMPKIN_STEM : Material.MELON_STEM;

        for (BlockFace blockFace : STEM_NEARBY_BLOCKS) {
            Block nearbyBlock = plantBlock.getRelative(blockFace);
            if (nearbyBlock.getType() == stemType) {
                return nearbyBlock.getLocation();
            }
        }

        return plantBlock.getLocation();
    }

    private static Location getChorusRoot(Block plantBlock) {
        return findChorusRoot(plantBlock, BlockFace.SELF).orElseGet(plantBlock::getLocation);
    }

    private static Optional<Location> findChorusRoot(Block plantBlock, BlockFace ignoredFace) {
        Block downBlock = plantBlock.getRelative(BlockFace.DOWN);

        if (downBlock.getType() == Material.END_STONE)
            return Optional.of(plantBlock.getLocation());

        for (BlockFace blockFace : CHORUS_NEARBY_BLOCKS) {
            if (blockFace != ignoredFace) {
                Block nearbyBlock = plantBlock.getRelative(blockFace);
                if (PlantType.getByType(nearbyBlock.getType()) == PlantType.CHORUS_PLANT) {
                    Optional<Location> nearbyResult = findChorusRoot(nearbyBlock, blockFace.getOppositeFace());
                    if (nearbyResult.isPresent())
                        return nearbyResult;
                }
            }
        }

        return Optional.empty();
    }

    @Nullable
    private Key getMissionPlantKey(@Nullable Key blockKey) {
        if (blockKey == null)
            return null;

        for (KeyRequirements requirement : requiredPlants.keySet()) {
            if (requirement.contains(blockKey))
                return requirement.getKey(blockKey);
        }

        return null;
    }

}
