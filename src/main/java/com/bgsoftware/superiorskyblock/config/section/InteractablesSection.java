package com.bgsoftware.superiorskyblock.config.section;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.config.SettingsManager;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeyMap;
import com.bgsoftware.superiorskyblock.api.key.KeySet;
import com.bgsoftware.superiorskyblock.core.EnumHelper;
import com.bgsoftware.superiorskyblock.core.LazyReference;
import com.bgsoftware.superiorskyblock.core.Materials;
import com.bgsoftware.superiorskyblock.core.key.KeyIndicator;
import com.bgsoftware.superiorskyblock.core.key.map.KeyMaps;
import com.bgsoftware.superiorskyblock.core.key.set.KeySets;
import com.bgsoftware.superiorskyblock.core.key.types.MaterialKey;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class InteractablesSection implements SettingsManager.Interactables {

    private static final LazyReference<IslandPrivilege> PRIVILEGE_CHEST_ACCESS = lazyIslandPrivilege("CHEST_ACCESS");
    private static final LazyReference<IslandPrivilege> PRIVILEGE_FARM_TRAMPING = lazyIslandPrivilege("FARM_TRAMPING");
    private static final LazyReference<IslandPrivilege> PRIVILEGE_INTERACT = lazyIslandPrivilege("INTERACT");
    private static final LazyReference<IslandPrivilege> PRIVILEGE_PICKUP_LECTERN_BOOK = lazyIslandPrivilege("PICKUP_LECTERN_BOOK");
    private static final LazyReference<IslandPrivilege> PRIVILEGE_SIGN_INTERACT = lazyIslandPrivilege("SIGN_INTERACT");
    private static final LazyReference<IslandPrivilege> PRIVILEGE_SPAWNER_BREAK = lazyIslandPrivilege("SPAWNER_BREAK");
    private static final LazyReference<IslandPrivilege> PRIVILEGE_TURTLE_EGG_TRAMPING = lazyIslandPrivilege("TURTLE_EGG_TRAMPING");
    private static final LazyReference<IslandPrivilege> PRIVILEGE_USE = lazyIslandPrivilege("USE");

    private static final Material FARMLAND = EnumHelper.getEnum(Material.class, "FARMLAND", "SOIL");
    @Nullable
    private static final Material ROOTED_DIRT = EnumHelper.getEnum(Material.class, "ROOTED_DIRT");
    @Nullable
    private static final Material TURTLE_EGG = EnumHelper.getEnum(Material.class, "TURTLE_EGG");
    @Nullable
    private static final Material SWEET_BERRY_BUSH = EnumHelper.getEnum(Material.class, "SWEET_BERRY_BUSH");
    @Nullable
    private static final Material CAVE_VINES = EnumHelper.getEnum(Material.class, "CAVE_VINES");
    @Nullable
    private static final Material CAVE_VINES_PLANT = EnumHelper.getEnum(Material.class, "CAVE_VINES_PLANT");
    @Nullable
    private static final Material LECTERN = EnumHelper.getEnum(Material.class, "LECTERN");
    @Nullable
    private static final Material VAULT = EnumHelper.getEnum(Material.class, "VAULT");
    @Nullable
    private static final Material BLAST_FURNACE = EnumHelper.getEnum(Material.class, "BLAST_FURNACE");
    @Nullable
    private static final Material BURNING_FURNACE = EnumHelper.getEnum(Material.class, "BURNING_FURNACE");
    @Nullable
    private static final Material CHISELED_BOOKSHELF = EnumHelper.getEnum(Material.class, "CHISELED_BOOKSHELF");
    @Nullable
    private static final Material CRAFTER = EnumHelper.getEnum(Material.class, "CRAFTER");
    @Nullable
    private static final Material DECORATED_POT = EnumHelper.getEnum(Material.class, "DECORATED_POT");
    @Nullable
    private static final Material SMOKER = EnumHelper.getEnum(Material.class, "SMOKER");

    private final Map<IslandPrivilege, KeySet> privilegeToInteractables;
    private final KeyMap<IslandPrivilege> interactableToPrivilege;
    private final SuperiorSkyblockPlugin plugin;
    private final boolean legacy;

    public InteractablesSection(SuperiorSkyblockPlugin plugin, YamlConfiguration cfg) {
        this.plugin = plugin;
        this.legacy = cfg.getKeys(false).size() == 1 && cfg.isList("interactables");

        Map<IslandPrivilege, KeySet> privilegeToInteractables = new IdentityHashMap<>();
        if (this.legacy) {
            loadLegacyInternal(cfg, privilegeToInteractables);
        } else {
            loadInternal(cfg, privilegeToInteractables);
        }
        this.privilegeToInteractables = privilegeToInteractables.isEmpty() ? Collections.emptyMap() : Collections.unmodifiableMap(privilegeToInteractables);
        this.interactableToPrivilege = convertView(privilegeToInteractables);
    }

    private static KeyMap<IslandPrivilege> convertView(Map<IslandPrivilege, KeySet> base) {
        KeyMap<IslandPrivilege> converted = KeyMaps.createHashMap(KeyIndicator.MATERIAL);
        base.forEach((islandPrivilege, keySet) -> {
            for (Key key : keySet) {
                IslandPrivilege oldPrivilege = converted.put(key, islandPrivilege);
                if (oldPrivilege != null) {
                    Log.warnFromFile("interactables.yml", "The interactable block ", key, " had two privileges: ", oldPrivilege, ", ", islandPrivilege);
                }
            }
        });
        return KeyMaps.unmodifiableKeyMap(converted);
    }

    public boolean isLegacy() {
        return legacy;
    }

    @Override
    public Set<Key> getInteractables() {
        return Collections.unmodifiableSet(this.interactableToPrivilege.keySet());
    }

    @Override
    @Nullable
    public Set<Key> getInteractables(IslandPrivilege islandPrivilege) {
        Set<Key> interactables = this.privilegeToInteractables.get(islandPrivilege);
        return interactables == null || interactables.isEmpty() ? null : Collections.unmodifiableSet(interactables);
    }

    @Override
    @Nullable
    public IslandPrivilege getRequiredPrivilege(Key key) {
        return this.interactableToPrivilege.get(key);
    }

    public void saveToFile(File file) throws IOException {
        CommentedConfiguration cfg = CommentedConfiguration.loadConfiguration(plugin.getResource("interactables.yml"));

        Set<IslandPrivilege> islandPrivileges = new TreeSet<>(Comparator.comparing(IslandPrivilege::getName));
        islandPrivileges.addAll(this.privilegeToInteractables.keySet());

        boolean copyHeaderComment = true;
        for(IslandPrivilege islandPrivilege : islandPrivileges) {
            Set<String> keys = new TreeSet<>(String::compareTo);
            this.privilegeToInteractables.get(islandPrivilege).forEach(key -> keys.add(key.toString()));
            cfg.set(islandPrivilege.getName(), new LinkedList<>(keys));

            if (copyHeaderComment) {
                copyHeaderComment = false;
                cfg.setComment(islandPrivilege.getName(), cfg.getComment("interactables"));
            } else {
                cfg.setComment(islandPrivilege.getName(), "");
            }
        }

        // Remove the default interactables
        cfg.set("interactables", null);

        cfg.save(file);
    }

    private static void loadLegacyInternal(YamlConfiguration cfg, Map<IslandPrivilege, KeySet> interactablesMap) {
        List<String> interactables = cfg.getStringList("interactables");

        for (String blockType : interactables) {
            Key blockKey = Key.ofMaterialAndData(blockType);

            IslandPrivilege islandPrivilege;
            try {
                Material material = ((MaterialKey) blockKey).getMaterial();

                if (Materials.isChest(material)) {
                    islandPrivilege = PRIVILEGE_CHEST_ACCESS.get();
                } else if (material == LECTERN) {
                    islandPrivilege = PRIVILEGE_PICKUP_LECTERN_BOOK.get();
                } else if (isInventoryHolder(material)) {
                    islandPrivilege = PRIVILEGE_USE.get();
                } else if (Materials.isSign(material)) {
                    islandPrivilege = PRIVILEGE_SIGN_INTERACT.get();
                } else if (material == Materials.SPAWNER.toBukkitType()) {
                    islandPrivilege = PRIVILEGE_SPAWNER_BREAK.get();
                } else if (material == FARMLAND || material == ROOTED_DIRT || material == SWEET_BERRY_BUSH ||
                        material == CAVE_VINES || material == CAVE_VINES_PLANT) {
                    islandPrivilege = PRIVILEGE_FARM_TRAMPING.get();
                } else if (material == TURTLE_EGG) {
                    islandPrivilege = PRIVILEGE_TURTLE_EGG_TRAMPING.get();
                } else {
                    islandPrivilege = PRIVILEGE_INTERACT.get();
                }

            } catch (Throwable ignored) {
                // In the case this is a custom block or something, default to INTERACT privilege.
                islandPrivilege = PRIVILEGE_INTERACT.get();
            }

            interactablesMap.computeIfAbsent(islandPrivilege, p -> KeySets.createHashSet(KeyIndicator.MATERIAL))
                    .add(blockKey);
        }
    }

    private static boolean isInventoryHolder(Material material) {
        return Materials.isShelf(material) || material == BLAST_FURNACE || material == Material.BREWING_STAND ||
                material == BURNING_FURNACE || material == CHISELED_BOOKSHELF || material == CRAFTER ||
                material == DECORATED_POT || material == Material.DISPENSER || material == Material.DROPPER ||
                material == Material.FURNACE || material == Material.HOPPER || material == Material.JUKEBOX ||
                material == SMOKER || material == VAULT;
    }

    private static void loadInternal(YamlConfiguration cfg, Map<IslandPrivilege, KeySet> interactablesMap) {
        for (String islandPrivilegeName : cfg.getKeys(false)) {
            try {
                IslandPrivilege.register(islandPrivilegeName);
            } catch (IllegalStateException ignored) {
            }

            IslandPrivilege islandPrivilege = IslandPrivilege.getByName(islandPrivilegeName);
            KeySet keys = KeySets.createHashSet(KeyIndicator.MATERIAL, cfg.getStringList(islandPrivilegeName));
            interactablesMap.put(islandPrivilege, keys);
        }
    }

    private static LazyReference<IslandPrivilege> lazyIslandPrivilege(String name) {
        return new LazyReference<IslandPrivilege>() {
            @Override
            protected IslandPrivilege create() {
                try {
                    IslandPrivilege.register(name);
                } catch (IllegalStateException ignored) {
                }

                return IslandPrivilege.getByName(name);
            }
        };
    }

}
