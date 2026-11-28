package com.bgsoftware.superiorskyblock.external;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.events.IslandGenerateBlockEvent;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.CustomKeyParser;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.service.region.InteractionResult;
import com.bgsoftware.superiorskyblock.api.service.region.RegionManagerService;
import com.bgsoftware.superiorskyblock.api.service.world.WorldRecordFlags;
import com.bgsoftware.superiorskyblock.api.service.world.WorldRecordService;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.LazyReference;
import com.bgsoftware.superiorskyblock.core.ObjectsPools;
import com.bgsoftware.superiorskyblock.core.key.KeyIndicator;
import com.bgsoftware.superiorskyblock.core.key.Keys;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.service.region.ProtectionHelper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.xenondevs.nova.api.Nova;
import xyz.xenondevs.nova.api.block.NovaBlock;
import xyz.xenondevs.nova.api.block.NovaBlockState;
import xyz.xenondevs.nova.api.item.NovaItem;
import xyz.xenondevs.nova.api.protection.ProtectionIntegration;

import java.util.Locale;

public class NovaHook {

    private static final String NOVA_PREFIX = "NOVA";
    private static final Key BLOCK_ITEM_KEY = Keys.of(Material.SHULKER_SHELL);
    private static final Key BLOCK_KEY = Keys.of(Material.NOTE_BLOCK);

    private static final LazyReference<RegionManagerService> protectionManagerService = new LazyReference<>() {
        @Override
        protected RegionManagerService create() {
            return plugin.getServices().getService(RegionManagerService.class);
        }
    };

    private static final LazyReference<WorldRecordService> worldRecordService = new LazyReference<>() {
        @Override
        protected WorldRecordService create() {
            return plugin.getServices().getService(WorldRecordService.class);
        }
    };

    private static boolean registered = false;

    private static SuperiorSkyblockPlugin plugin;
    private static Nova nova;

    public static void register(SuperiorSkyblockPlugin plugin) {
        NovaHook.plugin = plugin;
        NovaHook.nova = Nova.getNova();

        Bukkit.getPluginManager().registerEvents(new ListenerImpl(), plugin);
        nova.registerProtectionIntegration(new NovaProtectionIntegration());

        if (!registered) {
            registered = true;
            plugin.getKeys().registerCustomMaterialKeyParser(new NovaKeyParser(), BLOCK_ITEM_KEY, BLOCK_KEY);
        }
    }

    private static class NovaProtectionIntegration implements ProtectionIntegration {

        @Override
        public boolean canBreak(@NotNull OfflinePlayer player, @Nullable ItemStack item, @NotNull Location location) {
            Island island = plugin.getGrid().getIslandAt(location);

            if (island == null) {
                return true;
            }

            SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(player.getUniqueId());

            if (superiorPlayer == null) {
                return true;
            }

            InteractionResult interactionResult = protectionManagerService.get().handleBlockBreak(superiorPlayer, location.getBlock());

            return !ProtectionHelper.shouldPreventInteraction(interactionResult, superiorPlayer, true);
        }

        @Override
        public boolean canPlace(@NotNull OfflinePlayer player, @NotNull ItemStack item, @NotNull Location location) {
            Island island = plugin.getGrid().getIslandAt(location);

            if (island == null) {
                return true;
            }

            SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(player.getUniqueId());

            if (superiorPlayer == null) {
                return true;
            }

            InteractionResult interactionResult = protectionManagerService.get().handleBlockPlace(superiorPlayer, location.getBlock());

            return !ProtectionHelper.shouldPreventInteraction(interactionResult, superiorPlayer, true);
        }

        @Override
        public boolean canUseBlock(@NotNull OfflinePlayer player, @Nullable ItemStack item, @NotNull Location location) {
            Island island = plugin.getGrid().getIslandAt(location);

            if (island == null) {
                return true;
            }

            SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(player.getUniqueId());

            if (superiorPlayer == null) {
                return true;
            }

            InteractionResult interactionResult = protectionManagerService.get().handleBlockInteract(superiorPlayer, location.getBlock(), Action.RIGHT_CLICK_BLOCK, item);

            return !ProtectionHelper.shouldPreventInteraction(interactionResult, superiorPlayer, true);
        }

        @Override
        public boolean canUseItem(@NotNull OfflinePlayer player, @NotNull ItemStack item, @NotNull Location location) {
            return true;
        }

        @Override
        public boolean canInteractWithEntity(@NotNull OfflinePlayer player, @NotNull Entity entity, @Nullable ItemStack item) {
            Island island = plugin.getGrid().getIslandAt(entity.getLocation());

            if (island == null) {
                return true;
            }

            SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(player.getUniqueId());

            if (superiorPlayer == null) {
                return true;
            }

            InteractionResult interactionResult = protectionManagerService.get().handleEntityInteract(superiorPlayer, entity, item);

            return !ProtectionHelper.shouldPreventInteraction(interactionResult, superiorPlayer, true);
        }

        @Override
        public boolean canHurtEntity(@NotNull OfflinePlayer player, @NotNull Entity entity, @Nullable ItemStack item) {
            Island island = plugin.getGrid().getIslandAt(entity.getLocation());

            if (island == null) {
                return true;
            }

            SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(player.getUniqueId());

            if (superiorPlayer == null) {
                return true;
            }

            InteractionResult interactionResult = protectionManagerService.get().handleEntityDamage(superiorPlayer.asPlayer(), entity);

            return !ProtectionHelper.shouldPreventInteraction(interactionResult, superiorPlayer, true);
        }

    }

    private static class ListenerImpl implements Listener {

        @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
        private void onBlockPlace(PlayerInteractEvent e) {
            if (e.getAction() != Action.RIGHT_CLICK_BLOCK || e.getItem() == null || e.getClickedBlock() == null) {
                return;
            }

            NovaItem novaItem = nova.getItemRegistry().getOrNull(e.getItem());

            if (novaItem == null || novaItem.getBlock() == null) {
                return;
            }

            NovaBlock novaBlock = novaItem.getBlock();
            Block block = e.getClickedBlock().getRelative(e.getBlockFace());

            BukkitExecutor.sync(() -> {
                if (block.getType() == Material.NOTE_BLOCK) {
                    Key key = Keys.of(NOVA_PREFIX, novaBlock.getId().toString().toUpperCase(Locale.ENGLISH), KeyIndicator.CUSTOM);

                    try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
                        worldRecordService.get().recordBlockPlace(key, block.getLocation(wrapper.getHandle()), 1,
                                null, WorldRecordFlags.SAVE_BLOCK_COUNT | WorldRecordFlags.DIRTY_CHUNKS);
                    }
                }
            }, 1L);
        }

        @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
        public void onIslandGenerateBlock(IslandGenerateBlockEvent e) {
            if (!e.getBlock().getGlobalKey().equals(NOVA_PREFIX)) {
                return;
            }

            NovaBlock block = nova.getBlockRegistry().getOrNull(e.getBlock().getSubKey());

            if (block == null) {
                e.setCancelled(true);
                return;
            }

            e.setPlaceBlock(false);
            nova.getBlockManager().placeBlock(e.getLocation(), block);
        }

    }

    private static class NovaKeyParser implements CustomKeyParser {

        @Override
        public Key getCustomKey(Location location) {
            NovaBlockState novaBlockState = nova.getBlockManager().getBlock(location);

            if (novaBlockState == null) {
                return null;
            }

            NovaBlock novaBlock = novaBlockState.getBlock();

            return Keys.of(NOVA_PREFIX, novaBlock.getId().toString().toUpperCase(Locale.ENGLISH), KeyIndicator.CUSTOM);
        }

        @Override
        public Key getCustomKey(ItemStack itemStack, Key def) {
            NovaItem novaItem = nova.getItemRegistry().getOrNull(itemStack);

            if (novaItem == null) {
                return def;
            }

            return Keys.of(NOVA_PREFIX, novaItem.getId().toString().toUpperCase(Locale.ENGLISH), KeyIndicator.CUSTOM);
        }

        @Override
        public boolean isCustomKey(Key key) {
            return key.getGlobalKey().equals(NOVA_PREFIX);
        }

        @Override
        public ItemStack getCustomKeyItem(Key key) {
            NovaItem novaItem = nova.getItemRegistry().getOrNull(key.getSubKey().toLowerCase(Locale.ENGLISH));

            if (novaItem == null) {
                return null;
            }

            return novaItem.createClientsideItemStack();
        }

    }

}
