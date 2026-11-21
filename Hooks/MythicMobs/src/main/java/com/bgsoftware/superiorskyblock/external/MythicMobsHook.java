package com.bgsoftware.superiorskyblock.external;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.key.CustomKeyParser;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeyCategory;
import com.bgsoftware.superiorskyblock.api.key.KeySet;
import com.bgsoftware.superiorskyblock.core.key.KeyIndicator;
import com.bgsoftware.superiorskyblock.core.key.Keys;
import com.bgsoftware.superiorskyblock.core.key.set.KeySets;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.world.BukkitEntities;
import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.api.mobs.entities.MythicEntityType;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.bukkit.adapters.BukkitEntityType;
import io.lumine.mythic.core.mobs.MobExecutor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

@SuppressWarnings("unused")
public class MythicMobsHook {

    private static final String MYTHIC_MOBS_PREFIX = "MYTHIC_MOBS";
    private static final KeySet MYTHIC_MOBS_KEYS = collectMythicMobsKeys();

    private static boolean registered = false;

    public static void register(SuperiorSkyblockPlugin plugin) {
        if (!registered) {
            registered = true;
            plugin.getKeys().registerCustomEntityTypeKeyParser(new MythicMobsKeyParser(), collectCustomKeys());
        }

        // Outside the check above, so "/is admin reload" checks the reloaded entity-categories.yml again.
        warnAboutMobWithoutDamagePrivilege(plugin);
    }

    private static void warnAboutMobWithoutDamagePrivilege(SuperiorSkyblockPlugin plugin) {
        MobExecutor mobExecutor = MythicBukkit.inst().getMobManager();
        Collection<MythicMob> vanillaTypes = mobExecutor.getVanillaTypes();

        for (MythicMob mythicMob : mobExecutor.getMobTypes()) {
            // Vanilla overrides keep their vanilla key, and therefore the categories of their vanilla type.
            if (vanillaTypes.contains(mythicMob)) {
                continue;
            }

            BukkitEntityType mythicEntity = mythicMob.getMythicEntity();
            EntityType entityType = mythicEntity == null ? null : mythicEntity.getBukkitType();
            // Skip entities the plugin doesn't track or protect (displays, markers, ...).
            if (entityType == null || !BukkitEntities.canHaveLimit(entityType)) {
                continue;
            }

            String mythicType = mythicMob.getInternalName();
            Key mythicKey = Keys.of(MYTHIC_MOBS_PREFIX, mythicType.toUpperCase(Locale.ENGLISH), KeyIndicator.CUSTOM);

            // Same lookup as BukkitEntities#getCategories(Entity), so the warning matches the real protection.
            if (!hasDamagePrivilege(plugin.getSettings().getEntityCategoriesMapping().getCategories(mythicKey))) {
                Log.warnFromFile("entity-categories.yml", "The MythicMobs mob MYTHIC_MOBS:", mythicType,
                        " is not in an entity category with a DAMAGE action, so visitors can damage it on islands");
            }
        }
    }

    private static boolean hasDamagePrivilege(List<KeyCategory> entityCategories) {
        for (KeyCategory entityCategory : entityCategories) {
            if (entityCategory.getRequiredPrivilege(KeyCategory.Action.DAMAGE) != null) {
                return true;
            }
        }

        return false;
    }

    private static KeySet collectMythicMobsKeys() {
        KeySet mythicMobsKeys = KeySets.createHashSet(KeyIndicator.ENTITY_TYPE);

        for (MythicMob mythicMob : MythicBukkit.inst().getMobManager().getMobTypes()) {
            EntityType entityType = mythicMob.getMythicEntity().getBukkitType();
            mythicMobsKeys.add(Keys.of(entityType));
        }

        return mythicMobsKeys;
    }

    private static Key[] collectCustomKeys() {
        KeySet customKeys = KeySets.createHashSet(KeyIndicator.ENTITY_TYPE);
        customKeys.addAll(MYTHIC_MOBS_KEYS);
        return customKeys.toArray(new Key[0]);
    }

    private static class MythicMobsKeyParser implements CustomKeyParser {

        @Override
        public Key getCustomKey(Entity entity) {
            MobExecutor mobExecutor = MythicBukkit.inst().getMobManager();

            String mythicType = mobExecutor.getMythicType(entity).orElse(null);

            if (mythicType == null) {
                return null;
            }

            MythicMob mythicMob = mobExecutor.getMythicMob(mythicType).orElse(null);

            if (mythicMob == null) {
                // MythicMobs registers a vanilla override (a mob named after an entity type or one of its
                // aliases, in any case: "zombie", "CaveSpider") under the type's own name, and drops an
                // override that was removed from the config, so the lookup above misses both.
                // Such mobs keep their vanilla key.
                if (MythicEntityType.get(mythicType) != null) {
                    return null;
                }
            } else if (mobExecutor.getVanillaTypes().contains(mythicMob)) {
                return null;
            }

            return Keys.of(MYTHIC_MOBS_PREFIX, mythicType.toUpperCase(Locale.ENGLISH), KeyIndicator.CUSTOM);
        }

        @Override
        public boolean isCustomKey(Key key) {
            return key.getGlobalKey().equals(MYTHIC_MOBS_PREFIX);
        }

    }

}
