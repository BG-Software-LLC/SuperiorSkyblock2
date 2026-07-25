package com.bgsoftware.superiorskyblock.listener;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.service.stackedblocks.InteractionResult;
import com.bgsoftware.superiorskyblock.api.service.stackedblocks.StackedBlocksInteractionService;
import com.bgsoftware.superiorskyblock.api.wrappers.BlockOffset;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.EnumHelper;
import com.bgsoftware.superiorskyblock.core.LazyReference;
import com.bgsoftware.superiorskyblock.core.ObjectsPools;
import com.bgsoftware.superiorskyblock.core.PlayerHand;
import com.bgsoftware.superiorskyblock.core.SBlockOffset;
import com.bgsoftware.superiorskyblock.core.key.Keys;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.menu.impl.internal.StackedBlocksDepositMenu;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.platform.event.GameEvent;
import com.bgsoftware.superiorskyblock.platform.event.GameEventPriority;
import com.bgsoftware.superiorskyblock.platform.event.GameEventType;
import com.bgsoftware.superiorskyblock.platform.event.args.GameEventArgs;
import com.bgsoftware.superiorskyblock.service.stackedblocks.StackedBlocksServiceHelper;
import com.bgsoftware.superiorskyblock.world.BukkitItems;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class StackedBlocksListener extends AbstractGameEventListener {

    @Nullable
    private static final Material COPPER_BLOCK = EnumHelper.getEnum(Material.class, "COPPER_BLOCK");
    private static final Material HONEYCOMB = EnumHelper.getEnum(Material.class, "HONEYCOMB");
    @Nullable
    private static final CreatureSpawnEvent.SpawnReason BUILD_COPPERGOLEM = EnumHelper.getEnum(CreatureSpawnEvent.SpawnReason.class, "BUILD_COPPERGOLEM");
    @Nullable
    private static final Material COPPER_CHEST = EnumHelper.getEnum(Material.class, "COPPER_CHEST");

    private final Map<CreatureSpawnEvent.SpawnReason, List<BlockOffset>> ENTITY_TEMPLATE_OFFSETS = buildEntityTemplateOffsetsMap();

    private final LazyReference<StackedBlocksInteractionService> stackedBlocksInteractionService = new LazyReference<StackedBlocksInteractionService>() {
        @Override
        protected StackedBlocksInteractionService create() {
            return plugin.getServices().getService(StackedBlocksInteractionService.class);
        }
    };

    public StackedBlocksListener(SuperiorSkyblockPlugin plugin) {
        super(plugin);
        this.registerListeners();
    }

    private void onStackedBlockInteract(GameEvent<GameEventArgs.PlayerInteractEvent> e) {
        if (e.getArgs().action != Action.RIGHT_CLICK_BLOCK)
            return;

        if (handleStackedBlockPlace(e)) return;
        if (handleStackedBlockUnstack(e)) return;
    }

    /* STACK LISTENERS */

    private void onStackedBlockPlace(GameEvent<GameEventArgs.BlockPlaceEvent> e) {
        Block block = e.getArgs().block;

        // We do not care about spawn island, and therefore only island worlds are relevant.
        if (!plugin.getGrid().isIslandsWorld(block.getWorld()))
            return;

        Block againstBlock = e.getArgs().againstBlock;

        if (againstBlock.equals(block))
            return;

        if (plugin.getStackedBlocks().getStackedBlockAmount(block) > 1)
            plugin.getStackedBlocks().setStackedBlock(block, 1);

        Player player = e.getArgs().player;
        PlayerHand usedHand = e.getArgs().usedHand;
        ItemStack inHand = e.getArgs().usedItem;

        if (inHand == null) {
            Log.error("BlockPlaceEvent by player ", player.getName(), " of block ", block.getType().name(), " with null hand item: ", usedHand);
            throw new RuntimeException("BlockPlaceEvent with null hand item");
        }

        // We do not stack blocks when the hand items has a name or a lore.
        if (inHand.hasItemMeta() && (inHand.getItemMeta().hasDisplayName() || inHand.getItemMeta().hasLore()))
            return;

        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(player);

        InteractionResult interactionResult = this.stackedBlocksInteractionService.get().handleStackedBlockPlace(
                superiorPlayer, againstBlock, usedHand.getEquipmentSlot());
        if (StackedBlocksServiceHelper.shouldCancelOriginalEvent(interactionResult))
            e.setCancelled();
    }

    private boolean handleStackedBlockPlace(GameEvent<GameEventArgs.PlayerInteractEvent> e) {
        if (!plugin.getSettings().getStackedBlocks().isEnabled())
            return false;

        boolean cancelled = false;

        Player player = e.getArgs().player;
        Block clickedBlock = e.getArgs().clickedBlock;
        PlayerHand usedHand = e.getArgs().usedHand;
        ItemStack usedItem = e.getArgs().usedItem;

        Material clickedBlockType = clickedBlock.getType();

        if (clickedBlockType == Material.DRAGON_EGG) {
            SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(player);

            if (plugin.getStackedBlocks().getStackedBlockAmount(clickedBlock) > 1) {
                e.setCancelled();
                if (usedItem == null)
                    this.stackedBlocksInteractionService.get().handleStackedBlockBreak(clickedBlock, superiorPlayer);
                cancelled = true;
            }

            if (usedItem != null) {
                InteractionResult interactionResult = this.stackedBlocksInteractionService.get()
                        .handleStackedBlockPlace(superiorPlayer, clickedBlock, usedHand.getEquipmentSlot());
                if (StackedBlocksServiceHelper.shouldCancelOriginalEvent(interactionResult)) {
                    e.setCancelled();
                    cancelled = true;
                }
            }
        } else if (clickedBlockType == COPPER_BLOCK && usedItem != null && usedItem.getType() == HONEYCOMB &&
                plugin.getStackedBlocks().getStackedBlockAmount(clickedBlock) > 1) {
            e.setCancelled();
            cancelled = true;
        }

        return cancelled;
    }

    /* UNSTACK LISTENERS */

    private void onStackedBlockBreak(GameEvent<GameEventArgs.BlockBreakEvent> e) {
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getArgs().player);
        InteractionResult interactionResult = this.stackedBlocksInteractionService.get()
                .handleStackedBlockBreak(e.getArgs().block, superiorPlayer);
        if (StackedBlocksServiceHelper.shouldCancelOriginalEvent(interactionResult))
            e.setCancelled();
    }

    private boolean handleStackedBlockUnstack(GameEvent<GameEventArgs.PlayerInteractEvent> e) {
        if (e.getArgs().usedItem != null || e.getArgs().usedHand != PlayerHand.MAIN_HAND)
            return false;

        Block clickedBlock = e.getArgs().clickedBlock;

        // We do not care about spawn island, and therefore only island worlds are relevant.
        if (!plugin.getGrid().isIslandsWorld(clickedBlock.getWorld()))
            return false;

        if (plugin.getStackedBlocks().getStackedBlockAmount(clickedBlock) <= 1)
            return false;

        Player player = e.getArgs().player;

        if (plugin.getSettings().getStackedBlocks().getDepositMenu().isEnabled() && player.isSneaking()) {
            StackedBlocksDepositMenu depositMenu = new StackedBlocksDepositMenu(clickedBlock.getLocation());
            player.openInventory(depositMenu.getInventory());
        } else {
            ItemStack offHandItem = BukkitItems.getHandItem(player, PlayerHand.OFF_HAND);
            if (offHandItem != null && offHandItem.getType() == clickedBlock.getType())
                return false;

            SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(player);
            InteractionResult interactionResult = this.stackedBlocksInteractionService.get()
                    .handleStackedBlockBreak(clickedBlock, superiorPlayer);
            if (StackedBlocksServiceHelper.shouldCancelOriginalEvent(interactionResult)) {
                e.setCancelled();
                return true;
            }
        }

        return false;
    }

    private void onStackedBlockBreakByEntity(GameEvent<GameEventArgs.EntityChangeBlockEvent> e) {
        InteractionResult interactionResult = this.stackedBlocksInteractionService.get()
                .handleStackedBlockBreak(e.getArgs().block, null);
        if (StackedBlocksServiceHelper.shouldCancelOriginalEvent(interactionResult))
            e.setCancelled();
    }

    private void onStackedBlockExplode(GameEvent<GameEventArgs.EntityExplodeEvent> e) {
        if (e.getArgs().isSoftExplosion)
            return;

        // We do not care about spawn island, and therefore only island worlds are relevant.
        if (!plugin.getGrid().isIslandsWorld(e.getArgs().entity.getWorld()))
            return;

        Iterator<Block> blockIterator = e.getArgs().blocks.iterator();
        while (blockIterator.hasNext()) {
            Block block = blockIterator.next();

            // Check if block is stackable
            if (!plugin.getSettings().getStackedBlocks().getWhitelisted().contains(Keys.of(block)))
                continue;

            int amount = plugin.getStackedBlocks().getStackedBlockAmount(block);

            if (amount <= 1)
                continue;

            // All checks are done. We can remove the block from the list.
            blockIterator.remove();

            ItemStack blockItem = block.getState().getData().toItemStack(amount);

            try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
                Location location = block.getLocation(wrapper.getHandle());

                Island island = plugin.getGrid().getIslandAt(location);
                if (island != null)
                    island.handleBlockBreak(block, amount);

                plugin.getStackedBlocks().removeStackedBlock(location);
                block.setType(Material.AIR);

                // Dropping the item
                block.getWorld().dropItemNaturally(location, blockItem);
            }

        }
    }

    /* STACKED-BLOCKS PROTECTION */

    private void onPistonExtend(GameEvent<GameEventArgs.PistonExtendEvent> e) {
        // We do not care about spawn island, and therefore only island worlds are relevant.
        if (!plugin.getGrid().isIslandsWorld(e.getArgs().block.getWorld()))
            return;

        for (Block block : e.getArgs().blocks) {
            if (plugin.getStackedBlocks().getStackedBlockAmount(block) > 1) {
                e.setCancelled();
                break;
            }
        }
    }

    public void onPistonRetract(GameEvent<GameEventArgs.PistonRetractEvent> e) {
        // We do not care about spawn island, and therefore only island worlds are relevant.
        if (!plugin.getGrid().isIslandsWorld(e.getArgs().block.getWorld()))
            return;

        for (Block block : e.getArgs().blocks) {
            if (plugin.getStackedBlocks().getStackedBlockAmount(block) > 1) {
                e.setCancelled();
                break;
            }
        }
    }

    private void onBlockChangeState(GameEvent<GameEventArgs.BlockFormEvent> e) {
        // We do not care about spawn island, and therefore only island worlds are relevant.
        if (!plugin.getGrid().isIslandsWorld(e.getArgs().block.getWorld()))
            return;

        if (plugin.getStackedBlocks().getStackedBlockAmount(e.getArgs().block) > 1)
            e.setCancelled();
    }

    private void onGolemCreate(GameEvent<GameEventArgs.EntitySpawnEvent> e) {
        List<BlockOffset> entityTemplateOffsets = ENTITY_TEMPLATE_OFFSETS.get(e.getArgs().spawnReason);
        if (entityTemplateOffsets == null)
            return;

        Entity entity = e.getArgs().entity;

        // We do not care about spawn island, and therefore only island worlds are relevant.
        if (!plugin.getGrid().isIslandsWorld(entity.getWorld()))
            return;

        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            Location entityLocation = entity.getLocation(wrapper.getHandle());

            if (plugin.getStackedBlocks().getStackedBlockAmount(entityLocation) > 1) {
                e.setCancelled();
                return;
            }

            for (BlockOffset blockOffset : entityTemplateOffsets) {
                if (plugin.getStackedBlocks().getStackedBlockAmount(blockOffset.applyToLocation(entityLocation)) > 1) {
                    e.setCancelled();
                    if (e.getArgs().spawnReason == BUILD_COPPERGOLEM)
                        onCopperGolemCancel(entityLocation);
                    return;
                }
            }
        }
    }

    private void onCopperGolemCancel(Location entityLocation) {
        Block copperChestBlock = entityLocation.getBlock().getRelative(BlockFace.DOWN);
        BukkitExecutor.sync(() -> {
            if (copperChestBlock.getType() == COPPER_CHEST) {
                copperChestBlock.setType(COPPER_BLOCK);
            }
        }, 1L);
    }

    private void onSpongeAbsorb(GameEvent<GameEventArgs.SpongeAbsorbEvent> e) {
        // We do not care about spawn island, and therefore only island worlds are relevant.
        if (!plugin.getGrid().isIslandsWorld(e.getArgs().block.getWorld()))
            return;

        if (plugin.getStackedBlocks().getStackedBlockAmount(e.getArgs().block) > 1)
            e.setCancelled();
    }

    private void onStackedBlockPhysics(GameEvent<GameEventArgs.BlockPhysicsEvent> e) {
        // We do not care about spawn island, and therefore only island worlds are relevant.
        if (!plugin.getGrid().isIslandsWorld(e.getArgs().block.getWorld()))
            return;

        if (plugin.getStackedBlocks().getStackedBlockAmount(e.getArgs().block) > 1)
            e.setCancelled();
    }

    /* INTERNAL */

    private void registerListeners() {
        if (!plugin.getSettings().getStackedBlocks().isEnabled()) {
            // Even if stacked blocks are disabled, we might want to keep the break or change related listeners.
            // We only want to keep them if there are any stacked blocks registered.
            if (!plugin.getStackedBlocks().hasStackedBlocks())
                return;
        } else {
            registerCallback(GameEventType.BLOCK_PLACE_EVENT, GameEventPriority.HIGHEST, this::onStackedBlockPlace);
        }

        registerCallback(GameEventType.BLOCK_BREAK_EVENT, GameEventPriority.HIGHEST, this::onStackedBlockBreak);
        registerCallback(GameEventType.PLAYER_INTERACT_EVENT, GameEventPriority.HIGHEST, this::onStackedBlockInteract);
        registerCallback(GameEventType.ENTITY_CHANGE_BLOCK_EVENT, GameEventPriority.HIGHEST, this::onStackedBlockBreakByEntity);
        registerCallback(GameEventType.ENTITY_EXPLODE_EVENT, GameEventPriority.HIGHEST, this::onStackedBlockExplode);
        registerCallback(GameEventType.PISTON_EXTEND_EVENT, GameEventPriority.LOWEST, this::onPistonExtend);
        registerCallback(GameEventType.PISTON_RETRACT_EVENT, GameEventPriority.LOWEST, this::onPistonRetract);
        registerCallback(GameEventType.BLOCK_FORM_EVENT, GameEventPriority.LOWEST, this::onBlockChangeState);
        registerCallback(GameEventType.ENTITY_SPAWN_EVENT, GameEventPriority.LOWEST, this::onGolemCreate);
        registerCallback(GameEventType.SPONGE_ABSORB_EVENT, GameEventPriority.LOWEST, this::onSpongeAbsorb);

        if (plugin.getSettings().isPhysicsListener())
            registerCallback(GameEventType.BLOCK_PHYSICS_EVENT, GameEventPriority.LOWEST, this::onStackedBlockPhysics);
    }

    private static Map<CreatureSpawnEvent.SpawnReason, List<BlockOffset>> buildEntityTemplateOffsetsMap() {
        EnumMap<CreatureSpawnEvent.SpawnReason, List<BlockOffset>> offsetsMap = new EnumMap<>(CreatureSpawnEvent.SpawnReason.class);

        List<BlockOffset> blockOffsets = new LinkedList<>();
        blockOffsets.add(SBlockOffset.fromOffsets(0, 1, 0));
        blockOffsets.add(SBlockOffset.fromOffsets(1, 1, 0));
        blockOffsets.add(SBlockOffset.fromOffsets(1, 1, 1));
        blockOffsets.add(SBlockOffset.fromOffsets(-1, 1, 0));
        blockOffsets.add(SBlockOffset.fromOffsets(-1, 1, -1));
        blockOffsets.add(SBlockOffset.fromOffsets(0, 2, 0));
        offsetsMap.put(CreatureSpawnEvent.SpawnReason.BUILD_IRONGOLEM, blockOffsets);

        blockOffsets = new LinkedList<>();
        blockOffsets.add(SBlockOffset.fromOffsets(0, 1, 0));
        blockOffsets.add(SBlockOffset.fromOffsets(0, 2, 0));
        offsetsMap.put(CreatureSpawnEvent.SpawnReason.BUILD_SNOWMAN, blockOffsets);

        blockOffsets = new LinkedList<>();
        blockOffsets.add(SBlockOffset.fromOffsets(0, 1, 0));
        blockOffsets.add(SBlockOffset.fromOffsets(1, 1, 0));
        blockOffsets.add(SBlockOffset.fromOffsets(1, 1, 1));
        blockOffsets.add(SBlockOffset.fromOffsets(-1, 1, 0));
        blockOffsets.add(SBlockOffset.fromOffsets(-1, 1, -1));
        blockOffsets.add(SBlockOffset.fromOffsets(0, 2, 0));
        blockOffsets.add(SBlockOffset.fromOffsets(1, 2, 0));
        blockOffsets.add(SBlockOffset.fromOffsets(1, 2, 1));
        blockOffsets.add(SBlockOffset.fromOffsets(-1, 2, 0));
        blockOffsets.add(SBlockOffset.fromOffsets(-1, 2, -1));
        offsetsMap.put(CreatureSpawnEvent.SpawnReason.BUILD_WITHER, blockOffsets);

        if (BUILD_COPPERGOLEM != null) {
            blockOffsets = new LinkedList<>();
            blockOffsets.add(SBlockOffset.fromOffsets(0, -1, 0));
            offsetsMap.put(BUILD_COPPERGOLEM, blockOffsets);
        }

        return Collections.unmodifiableMap(offsetsMap);
    }

}
