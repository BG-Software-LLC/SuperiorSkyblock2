package com.bgsoftware.superiorskyblock.service.region;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.commands.SuperiorCommand;
import com.bgsoftware.superiorskyblock.api.config.SettingsManager;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandFlag;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeyCategory;
import com.bgsoftware.superiorskyblock.api.key.KeyMap;
import com.bgsoftware.superiorskyblock.api.key.KeySet;
import com.bgsoftware.superiorskyblock.api.service.region.InteractionResult;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.commands.CommandsHelper;
import com.bgsoftware.superiorskyblock.core.ObjectsPools;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEventType;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEventsDispatcher;
import com.bgsoftware.superiorskyblock.core.key.KeyIndicator;
import com.bgsoftware.superiorskyblock.core.key.Keys;
import com.bgsoftware.superiorskyblock.core.key.category.BuiltinKeyCategories;
import com.bgsoftware.superiorskyblock.core.key.category.FixedKeyCategory;
import com.bgsoftware.superiorskyblock.core.key.map.KeyMaps;
import com.bgsoftware.superiorskyblock.core.key.set.KeySets;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.core.threads.Synchronized;
import com.bgsoftware.superiorskyblock.player.PlayerLocales;
import com.bgsoftware.superiorskyblock.world.BukkitEntities;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;

public class ProtectionHelper {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private static final Synchronized<KeyMap<RequirementsCache>> BLOCK_PRIVILEGES_CACHE = Synchronized.of(KeyMaps.createHashMap(KeyIndicator.MATERIAL));
    private static final Synchronized<KeyMap<RequirementsCache>> ENTITY_PRIVILEGES_CACHE = Synchronized.of(KeyMaps.createHashMap(KeyIndicator.ENTITY_TYPE));

    private ProtectionHelper() {

    }

    public static void registerListeners(PluginEventsDispatcher dispatcher) {
        dispatcher.registerCallback(PluginEventType.SETTINGS_UPDATE_EVENT, ProtectionHelper::onSettingsUpdate);
    }

    public static boolean shouldPreventInteraction(InteractionResult interactionResult,
                                                   @Nullable SuperiorPlayer superiorPlayer, boolean sendMessages) {
        switch (interactionResult) {
            case ISLAND_RECALCULATE:
                if (sendMessages && superiorPlayer != null) {
                    Message.ISLAND_BEING_CALCULATED.send(superiorPlayer);
                }
                return true;
            case MISSING_PRIVILEGE:
                if (sendMessages && superiorPlayer != null) {
                    sendProtectionMessage(superiorPlayer.asPlayer());
                }
                return true;
            case OUTSIDE_ISLAND:
                if (sendMessages && superiorPlayer != null) {
                    Message.BUILD_OUTSIDE_ISLAND.send(superiorPlayer);
                }
                return true;
            case SUCCESS:
                return false;
        }

        throw new IllegalStateException("No handling for result " + interactionResult);
    }

    public static void sendProtectionMessage(CommandSender sender) {
        boolean isSpawnIsland;
        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            Island island = plugin.getGrid().getIslandAt(((Player) sender).getLocation(wrapper.getHandle()));
            isSpawnIsland = island != null && island.isSpawn();
        }

        Locale locale = PlayerLocales.getLocale(sender);

        if (!isSpawnIsland) {
            Message.ISLAND_PROTECTED.send(sender, locale);
        } else {
            Message.SPAWN_PROTECTED.send(sender, locale);
        }

        SuperiorCommand bypassCommand = plugin.getCommands().getAdminCommand("bypass");
        if (bypassCommand != null && CommandsHelper.hasCommandAccess(bypassCommand, sender)) {
            if (!isSpawnIsland) {
                Message.ISLAND_PROTECTED_OPPED.send(sender, locale);
            } else {
                Message.SPAWN_PROTECTED_OPPED.send(sender, locale);
            }
        }
    }

    public static List<IslandPrivilege> getBlockInteractPrivileges(Key key) {
        return getBlockRequirements(key).requiredPrivileges.getOrDefault(KeyCategory.Action.INTERACT, Collections.emptyList());
    }

    public static List<IslandPrivilege> getBlockBreakPrivileges(Key key) {
        return getBlockRequirements(key).requiredPrivileges.getOrDefault(KeyCategory.Action.BREAK, Collections.emptyList());
    }

    public static List<IslandPrivilege> getBlockPlacePrivileges(Key key) {
        return getBlockRequirements(key).requiredPrivileges.getOrDefault(KeyCategory.Action.PLACE, Collections.emptyList());
    }

    public static List<IslandPrivilege> getEntityInteractPrivileges(Key key) {
        return getEntityRequirements(key).requiredPrivileges.getOrDefault(KeyCategory.Action.INTERACT, Collections.emptyList());
    }

    public static List<IslandPrivilege> getEntityInteractPrivileges(Entity entity) {
        return getEntityRequirementsInternal(entity, ProtectionHelper::getEntityInteractPrivileges,
                category -> category.getRequiredPrivilege(KeyCategory.Action.INTERACT));
    }

    public static List<IslandPrivilege> getEntityDamagePrivileges(Key key) {
        return getEntityRequirements(key).requiredPrivileges.getOrDefault(KeyCategory.Action.DAMAGE, Collections.emptyList());
    }

    public static List<IslandPrivilege> getEntityDamagePrivileges(Entity entity) {
        return getEntityRequirementsInternal(entity, ProtectionHelper::getEntityDamagePrivileges,
                category -> category.getRequiredPrivilege(KeyCategory.Action.DAMAGE));
    }

    public static List<IslandPrivilege> getEntitySpawnPrivileges(Key key) {
        return getEntityRequirements(key).requiredPrivileges.getOrDefault(KeyCategory.Action.SPAWN, Collections.emptyList());
    }

    public static List<IslandPrivilege> getEntitySpawnPrivileges(Entity entity) {
        return getEntityRequirementsInternal(entity, ProtectionHelper::getEntitySpawnPrivileges,
                category -> category.getRequiredPrivilege(KeyCategory.Action.SPAWN));
    }

    public static List<IslandFlag> getEntityNaturalSpawningFlags(Key key) {
        return getEntityRequirements(key).requiredFlags.getOrDefault(KeyCategory.Action.NATURAL_SPAWN, Collections.emptyList());
    }

    public static List<IslandFlag> getEntityNaturalSpawningFlags(Entity entity) {
        return getEntityRequirementsInternal(entity, ProtectionHelper::getEntityNaturalSpawningFlags,
                category -> category.getRequiredFlag(KeyCategory.Action.NATURAL_SPAWN));
    }

    public static List<IslandFlag> getEntitySpawnerSpawningFlags(Key key) {
        return getEntityRequirements(key).requiredFlags.getOrDefault(KeyCategory.Action.SPAWNER_SPAWN, Collections.emptyList());
    }

    public static List<IslandFlag> getEntitySpawnerSpawningFlags(Entity entity) {
        return getEntityRequirementsInternal(entity, ProtectionHelper::getEntitySpawnerSpawningFlags,
                category -> category.getRequiredFlag(KeyCategory.Action.SPAWNER_SPAWN));
    }

    private static <T> List<T> getEntityRequirementsInternal(Entity entity,
                                                             Function<Key, List<T>> keyBasedCollector,
                                                             Function<KeyCategory, T> extraRequirementCollector) {
        KeyCategory tamedCategory = Objects.requireNonNull(plugin.getSettings().getEntityCategoriesMapping()
                .getCategoryByKey(BuiltinKeyCategories.TAMED));
        if (!BukkitEntities.isTamed(entity) || !tamedCategory.isListed(Keys.of(entity.getType()))) {
            return keyBasedCollector.apply(Keys.of(entity));
        }

        T extraRequirement = extraRequirementCollector.apply(tamedCategory);
        if (extraRequirement == null) {
            return keyBasedCollector.apply(Keys.of(entity));
        }

        List<T> requirements = new LinkedList<>(keyBasedCollector.apply(Keys.of(entity)));
        requirements.add(extraRequirement);

        return Collections.unmodifiableList(requirements);
    }

    private static void onSettingsUpdate() {
        KeyMap<RequirementsCache> blockPrivilegesCache = KeyMaps.createHashMap(KeyIndicator.MATERIAL);
        KeyMap<RequirementsCache> entityPrivilegesCache = KeyMaps.createHashMap(KeyIndicator.ENTITY_TYPE);

        KeySet allBlocks = KeySets.createHashSet(KeyIndicator.MATERIAL);
        KeySet allEntities = KeySets.createHashSet(KeyIndicator.ENTITY_TYPE);

        for (KeyCategory blockCategory : plugin.getSettings().getBlockCategoriesMapping().getCategories()) {
            if (blockCategory instanceof FixedKeyCategory) {
                allBlocks.addAll(((FixedKeyCategory) blockCategory).getKeys());
            }
        }
        for (KeyCategory entityCategory : plugin.getSettings().getEntityCategoriesMapping().getCategories()) {
            if (entityCategory instanceof FixedKeyCategory) {
                allEntities.addAll(((FixedKeyCategory) entityCategory).getKeys());
            }
        }

        for (Material material : Material.values()) {
            if (material.isBlock()) {
                allBlocks.add(Keys.of(material));
            }
        }
        for (EntityType entityType : EntityType.values()) {
            allEntities.add(Keys.of(entityType));
        }

        for (Key block : allBlocks) {
            blockPrivilegesCache.put(block, new RequirementsCache(block, plugin.getSettings().getBlockCategoriesMapping()));
        }
        for (Key entity : allEntities) {
            entityPrivilegesCache.put(entity, new RequirementsCache(entity, plugin.getSettings().getEntityCategoriesMapping()));
        }

        BLOCK_PRIVILEGES_CACHE.set(blockPrivilegesCache);
        ENTITY_PRIVILEGES_CACHE.set(entityPrivilegesCache);
    }

    private static RequirementsCache getBlockRequirements(Key key) {
        return getRequirements(key, true);
    }

    private static RequirementsCache getEntityRequirements(Key key) {
        return getRequirements(key, false);
    }

    private static RequirementsCache getRequirements(Key key, boolean isBlock) {
        Synchronized<KeyMap<RequirementsCache>> cachesMap = isBlock ? BLOCK_PRIVILEGES_CACHE : ENTITY_PRIVILEGES_CACHE;

        long[] stamp = new long[1];

        {
            RequirementsCache cache = cachesMap.readAndGet(map -> {
                stamp[0] = cachesMap.stamp();
                return map.get(key);
            });

            if (cache != null) {
                return cache;
            }
        }

        return cachesMap.writeAndGet(map -> {
            RequirementsCache cache = cachesMap.stamp() == stamp[0] ? null : map.get(key);

            if (cache == null) {
                cache = new RequirementsCache(key, isBlock ? plugin.getSettings().getBlockCategoriesMapping() :
                        plugin.getSettings().getEntityCategoriesMapping());
                map.put(key, cache);
            }

            return cache;
        });
    }

    private static class RequirementsCache {

        private final EnumMap<KeyCategory.Action, List<IslandPrivilege>> requiredPrivileges = new EnumMap<>(KeyCategory.Action.class);
        private final EnumMap<KeyCategory.Action, List<IslandFlag>> requiredFlags = new EnumMap<>(KeyCategory.Action.class);

        RequirementsCache(Key key, SettingsManager.CategoriesMapper mapper) {
            for (KeyCategory category : mapper.getCategories(key)) {
                for (KeyCategory.Action action : KeyCategory.Action.values()) {
                    IslandPrivilege islandPrivilege = category.getRequiredPrivilege(action);
                    if (islandPrivilege != null) {
                        requiredPrivileges.computeIfAbsent(action, a -> new LinkedList<>()).add(islandPrivilege);
                    }

                    IslandFlag islandFlag = category.getRequiredFlag(action);
                    if (islandFlag != null) {
                        requiredFlags.computeIfAbsent(action, a -> new LinkedList<>()).add(islandFlag);
                    }
                }
            }

            requiredPrivileges.replaceAll((action, privileges) -> Collections.unmodifiableList(privileges));
            requiredFlags.replaceAll((action, flags) -> Collections.unmodifiableList(flags));
        }

    }

}
