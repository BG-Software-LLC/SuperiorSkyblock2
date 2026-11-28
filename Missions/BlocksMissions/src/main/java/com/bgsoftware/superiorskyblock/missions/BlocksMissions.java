package com.bgsoftware.superiorskyblock.missions;

import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeySet;
import com.bgsoftware.superiorskyblock.api.missions.MissionLoadException;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.Counter;
import com.bgsoftware.superiorskyblock.missions.blocks.BlocksTracker;
import com.bgsoftware.superiorskyblock.missions.common.BuiltinMission;
import com.bgsoftware.superiorskyblock.missions.common.Placeholders;
import com.bgsoftware.superiorskyblock.missions.common.requirements.KeyRequirements;
import com.bgsoftware.superiorskyblock.missions.common.tracker.KeyDataTracker;
import com.bgsoftware.wildstacker.api.WildStackerAPI;
import com.bgsoftware.wildstacker.api.events.BarrelUnstackEvent;
import com.bgsoftware.wildtools.api.events.CuboidWandUseEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class BlocksMissions extends BuiltinMission<KeyDataTracker> implements Listener {

    private final Map<KeyRequirements, Integer> requiredBlocks = new LinkedHashMap<>();

    private boolean onlyNatural;
    private ProgressAction progressAction;

    private Predicate<Location> isBarrelCheck = block -> false;

    @Override
    protected void loadConfiguration(ConfigurationSection section) throws MissionLoadException {
        if (!section.contains("required-blocks"))
            throw new MissionLoadException("You must have the \"required-blocks\" section in the config.");

        for (String key : section.getConfigurationSection("required-blocks").getKeys(false)) {
            KeySet blocks = KeySet.createKeySet();
            section.getStringList("required-blocks." + key + ".types").forEach(requiredBlock ->
                    blocks.add(Key.ofMaterialAndData(requiredBlock.toUpperCase(Locale.ENGLISH))));
            int requiredAmount = section.getInt("required-blocks." + key + ".amount");
            this.requiredBlocks.put(new KeyRequirements(blocks), requiredAmount);
        }

        this.onlyNatural = section.getBoolean("only-natural-blocks", false);
        this.progressAction = section.getBoolean("blocks-placement", false) ?
                ProgressAction.PLACE : ProgressAction.BREAK;
    }

    @Override
    protected void registerListeners() {
        registerListener(this);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (Bukkit.getPluginManager().isPluginEnabled("WildStacker")) {
                registerListener(new WildStackerListener());
                this.isBarrelCheck = block -> WildStackerAPI.getWildStacker().getSystemManager().isStackedBarrel(block);
            }

            if (Bukkit.getPluginManager().isPluginEnabled("WildTools")) {
                registerListener(new WildToolsListener());
            }
        }, 1L);
    }

    @Override
    protected void clearModuleData(KeyDataTracker data) {
        data.clear();
    }

    @Override
    public double getProgress(SuperiorPlayer superiorPlayer) {
        KeyDataTracker blocksCounter = get(superiorPlayer);

        if (blocksCounter == null)
            return 0.0;

        int requiredBlocks = 0;
        int interactions = 0;

        for (Map.Entry<KeyRequirements, Integer> requiredBlock : this.requiredBlocks.entrySet()) {
            requiredBlocks += requiredBlock.getValue();
            interactions += Math.min(blocksCounter.getCounts(requiredBlock.getKey()), requiredBlock.getValue());
        }

        return (double) interactions / requiredBlocks;
    }

    @Override
    public int getProgressValue(SuperiorPlayer superiorPlayer) {
        KeyDataTracker blocksCounter = get(superiorPlayer);

        if (blocksCounter == null)
            return 0;

        int interactions = 0;

        for (Map.Entry<KeyRequirements, Integer> requiredBlock : this.requiredBlocks.entrySet())
            interactions += Math.min(blocksCounter.getCounts(requiredBlock.getKey()), requiredBlock.getValue());

        return interactions;
    }

    @Override
    public void saveProgress(ConfigurationSection section) {
        for (Map.Entry<SuperiorPlayer, KeyDataTracker> entry : entrySet()) {
            String uuid = entry.getKey().getUniqueId().toString();
            for (Map.Entry<Key, Counter> blockCountEntry : entry.getValue().getCounts().entrySet()) {
                section.set(uuid + ".counts." + blockCountEntry.getKey(), blockCountEntry.getValue().get());
            }
        }

        BlocksTracker.INSTANCE.save(section);
    }

    @Override
    public void loadProgress(ConfigurationSection section) {
        for (String uuid : section.getKeys(false)) {
            KeyDataTracker blocksCounter = new KeyDataTracker();
            UUID playerUUID;

            try {
                playerUUID = UUID.fromString(uuid);
            } catch (Exception error) {
                // tracked section probably, skipping.
                continue;
            }

            SuperiorPlayer superiorPlayer = this.plugin.getPlayers().getSuperiorPlayer(playerUUID);

            insertData(superiorPlayer, blocksCounter);

            if (section.contains(uuid + ".counts")) {
                ConfigurationSection countsSection = section.getConfigurationSection(uuid + ".counts");
                if (countsSection != null) {
                    for (String key : countsSection.getKeys(false)) {
                        Key typeKey = getMissionBlockKey(Key.ofMaterialAndData(key));
                        if (typeKey == null) continue;
                        blocksCounter.load(typeKey, countsSection.getInt(key));
                    }
                }
            } else {
                ConfigurationSection countsSection = section.getConfigurationSection(uuid);
                if (countsSection != null) {
                    for (String key : countsSection.getKeys(false)) {
                        Key typeKey = getMissionBlockKey(Key.ofMaterialAndData(key));
                        if (typeKey == null) continue;
                        blocksCounter.load(typeKey, section.getInt(uuid + "." + key));
                    }
                }
            }

            loadTrackedBlocks(section.getConfigurationSection(uuid + ".tracked.placed"), section.getConfigurationSection(uuid + ".tracked.broken"));
        }

        loadTrackedBlocks(section.getConfigurationSection("tracked.placed"), section.getConfigurationSection("tracked.broken"));
    }

    private static void loadTrackedBlocks(@Nullable ConfigurationSection trackedPlacedSection,
                                          @Nullable ConfigurationSection trackedBrokenSection) {
        if (trackedPlacedSection != null) {
            for (String worldName : trackedPlacedSection.getKeys(false)) {
                ConfigurationSection worldSection = trackedPlacedSection.getConfigurationSection(worldName);
                BlocksTracker.INSTANCE.loadTrackedBlocks(BlocksTracker.TrackingType.PLACED_BLOCKS, worldName, worldSection);
            }
        }

        if (trackedBrokenSection != null) {
            for (String worldName : trackedBrokenSection.getKeys(false)) {
                ConfigurationSection worldSection = trackedBrokenSection.getConfigurationSection(worldName);
                BlocksTracker.INSTANCE.loadTrackedBlocks(BlocksTracker.TrackingType.BROKEN_BLOCKS, worldName, worldSection);
            }
        }
    }

    @Override
    public void formatItem(SuperiorPlayer superiorPlayer, ItemStack itemStack) {
        KeyDataTracker blocksCounter = getOrCreate(superiorPlayer, s -> new KeyDataTracker());

        if (blocksCounter == null)
            return;

        ItemMeta itemMeta = itemStack.getItemMeta();

        if (itemMeta == null)
            return;

        if (itemMeta.hasDisplayName())
            itemMeta.setDisplayName(Placeholders.parseKeyPlaceholders(this.requiredBlocks, blocksCounter, itemMeta.getDisplayName(), true));

        if (itemMeta.hasLore()) {
            List<String> lore = new ArrayList<>();
            for (String line : Objects.requireNonNull(itemMeta.getLore()))
                lore.add(Placeholders.parseKeyPlaceholders(this.requiredBlocks, blocksCounter, line, true));
            itemMeta.setLore(lore);
        }

        itemStack.setItemMeta(itemMeta);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent e) {
        SuperiorPlayer superiorPlayer = this.plugin.getPlayers().getSuperiorPlayer(e.getPlayer());

        if (!this.plugin.getMissions().canCompleteNoProgress(superiorPlayer, this))
            return;

        Key blockKey = getMissionBlockKey(Key.of(e.getBlock()));
        if (blockKey == null)
            return;

        if (this.progressAction == ProgressAction.PLACE) {
            // We want to handle block place only if players gain progress by placing blocks.
            handleBlockAction(e.getPlayer(), e.getBlock().getLocation(), blockKey, superiorPlayer, false);
        }

        if (this.onlyNatural) {
            // We want to track block broken & placed only if this mission only progresses for natural blocks
            // We do that in a delayed tick so all other missions will check for their progress as well.
            Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
                BlocksTracker.INSTANCE.untrackBlock(BlocksTracker.TrackingType.BROKEN_BLOCKS, e.getBlock());
                BlocksTracker.INSTANCE.trackBlock(BlocksTracker.TrackingType.PLACED_BLOCKS, e.getBlock());
            }, 1L);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e) {
        SuperiorPlayer superiorPlayer = this.plugin.getPlayers().getSuperiorPlayer(e.getPlayer());

        if (!this.plugin.getMissions().canCompleteNoProgress(superiorPlayer, this))
            return;

        Key blockKey = getMissionBlockKey(Key.of(e.getBlock()));
        if (blockKey == null)
            return;

        if (this.progressAction == ProgressAction.BREAK) {
            // We want to handle block break only if players gain progress by breaking blocks.
            handleBlockAction(e.getPlayer(), e.getBlock().getLocation(), blockKey, superiorPlayer, true);
        }

        if (this.onlyNatural) {
            // We want to track block broken & placed only if this mission only progresses for natural blocks
            // We do that in a delayed tick so all other missions will check for their progress as well.
            Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
                BlocksTracker.INSTANCE.untrackBlock(BlocksTracker.TrackingType.PLACED_BLOCKS, e.getBlock());
                BlocksTracker.INSTANCE.trackBlock(BlocksTracker.TrackingType.BROKEN_BLOCKS, e.getBlock());
            }, 1L);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent e) {
        if (!e.getBlocks().isEmpty())
            handleBlockPistonMove(new LinkedList<>(e.getBlocks()), e.getDirection());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent e) {
        if (!e.getBlocks().isEmpty())
            handleBlockPistonMove(new LinkedList<>(e.getBlocks()), e.getDirection());
    }

    private class WildStackerListener implements Listener {

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onBarrelUnstack(BarrelUnstackEvent e) {
            if (onlyNatural || progressAction != ProgressAction.BREAK || !(e.getUnstackSource() instanceof Player))
                return;

            Player player = (Player) e.getUnstackSource();

            SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(player);

            if (!BlocksMissions.this.plugin.getMissions().canCompleteNoProgress(superiorPlayer, BlocksMissions.this))
                return;

            Key blockKey = getMissionBlockKey(Key.of(e.getBarrel().getType(), e.getBarrel().getData()));
            if (blockKey == null)
                return;

            handleBlockAction(player, e.getBarrel().getLocation(), blockKey, superiorPlayer, false);
        }

    }

    private class WildToolsListener implements Listener {

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onCuboidUse(CuboidWandUseEvent e) {
            if (progressAction != ProgressAction.BREAK)
                return;

            SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());

            if (!BlocksMissions.this.plugin.getMissions().canCompleteNoProgress(superiorPlayer, BlocksMissions.this))
                return;

            for (Location location : e.getBlocks()) {
                Block block = location.getBlock();

                Key blockKey = getMissionBlockKey(Key.of(block));
                if (blockKey == null)
                    return;

                handleBlockAction(e.getPlayer(), location, blockKey, superiorPlayer, true);

                if (onlyNatural) {
                    // We want to track block broken & placed only if this mission only progresses for natural blocks
                    // We do that in a delayed tick so all other missions will check for their progress as well.
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        BlocksTracker.INSTANCE.untrackBlock(BlocksTracker.TrackingType.PLACED_BLOCKS, block);
                        BlocksTracker.INSTANCE.trackBlock(BlocksTracker.TrackingType.BROKEN_BLOCKS, block);
                    }, 1L);
                }
            }
        }

    }

    private void handleBlockPistonMove(LinkedList<Block> blockList, BlockFace direction) {
        blockList.removeIf(block -> getMissionBlockKey(Key.of(block)) == null ||
                !BlocksTracker.INSTANCE.isTracked(BlocksTracker.TrackingType.PLACED_BLOCKS, block));

        if (blockList.isEmpty())
            return;

        List<Block> movedBlocks = blockList.stream()
                .map(block -> block.getRelative(direction))
                .collect(Collectors.toList());

        List<Block> addedBlocks = new LinkedList<>(movedBlocks);
        addedBlocks.removeAll(blockList);

        List<Block> removedBlocks = new LinkedList<>(blockList);
        removedBlocks.removeAll(movedBlocks);

        removedBlocks.forEach(block -> BlocksTracker.INSTANCE.untrackBlock(BlocksTracker.TrackingType.PLACED_BLOCKS, block));
        addedBlocks.forEach(block -> BlocksTracker.INSTANCE.trackBlock(BlocksTracker.TrackingType.PLACED_BLOCKS, block));
    }

    /**
     * Handle placing or breaking of a block and add count to mission progress.
     *
     * @param player         The player.
     * @param blockLocation  The location of the block.
     * @param blockKey       The {@link Key} of the block in @param blockLocation.
     * @param superiorPlayer The {@link SuperiorPlayer} wrapper of @param player.
     */
    private void handleBlockAction(Player player, Location blockLocation, Key blockKey,
                                   @Nullable SuperiorPlayer superiorPlayer, boolean checkForBarrels) {
        int blockAmount = getBlockAmount(player, blockLocation);

        if (superiorPlayer == null)
            superiorPlayer = plugin.getPlayers().getSuperiorPlayer(player);

        if (checkForBarrels && this.isBarrelCheck.test(blockLocation))
            return;

        // In case we progress for breaking blocks, we want to ensure the block is not tracked in case
        // the mission only progresses for natural blocks.
        if (this.onlyNatural && this.progressAction == ProgressAction.BREAK &&
                BlocksTracker.INSTANCE.isTracked(BlocksTracker.TrackingType.PLACED_BLOCKS, blockLocation)) {
            return;
        }

        handleBlockTrack(superiorPlayer, blockKey, blockAmount);
    }

    private void handleBlockTrack(SuperiorPlayer superiorPlayer, Key blockKey, int amount) {
        KeyDataTracker blocksCounter = getOrCreate(superiorPlayer, s -> new KeyDataTracker());

        blocksCounter.track(blockKey, amount);

        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> superiorPlayer.runIfOnline(_player -> {
            if (canComplete(superiorPlayer))
                this.plugin.getMissions().rewardMission(this, superiorPlayer, true);
        }), 2L);
    }

    private int getBlockAmount(Player player, Location blockLocation) {
        int blockAmount = this.plugin.getStackedBlocks().getStackedBlockAmount(blockLocation);

        // When sneaking, you'll break 64 from the stack. Otherwise, 1.
        int amount = !player.isSneaking() ? 1 : 64;

        // Fix amount so it won't be more than the stack's amount
        amount = Math.min(amount, blockAmount);

        return amount;
    }

    @Nullable
    private Key getMissionBlockKey(Key blockKey) {
        for (KeyRequirements requirementsList : requiredBlocks.keySet()) {
            if (requirementsList.contains(blockKey))
                return requirementsList.getKey(blockKey);
        }

        return null;
    }

    private enum ProgressAction {

        BREAK,
        PLACE

    }

}
