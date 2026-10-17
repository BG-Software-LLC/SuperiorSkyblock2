package com.bgsoftware.superiorskyblock.external;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.key.CustomKeyParser;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeySet;
import com.bgsoftware.superiorskyblock.core.key.KeyIndicator;
import com.bgsoftware.superiorskyblock.core.key.Keys;
import com.bgsoftware.superiorskyblock.core.key.set.KeySets;
import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.MobExecutor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

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

            if (mythicMob != null && mobExecutor.getVanillaTypes().contains(mythicMob)) {
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
