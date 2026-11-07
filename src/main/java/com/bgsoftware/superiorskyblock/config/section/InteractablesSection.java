package com.bgsoftware.superiorskyblock.config.section;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.api.config.SettingsManager;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.key.KeyCategory;
import com.bgsoftware.superiorskyblock.api.key.KeySet;
import com.bgsoftware.superiorskyblock.config.SettingsContainerHolder;
import com.bgsoftware.superiorskyblock.core.EnumHelper;
import com.bgsoftware.superiorskyblock.core.Materials;
import com.bgsoftware.superiorskyblock.core.key.KeyIndicator;
import com.bgsoftware.superiorskyblock.core.key.category.FixedKeyCategory;
import com.bgsoftware.superiorskyblock.core.key.set.KeySets;
import com.bgsoftware.superiorskyblock.core.key.types.MaterialKey;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class InteractablesSection extends SettingsContainerHolder implements SettingsManager.Interactables {

    private static final Material FARMLAND = EnumHelper.getEnum(Material.class, "FARMLAND", "SOIL");
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

    public static void convertInteractables(YamlConfiguration cfg) {
        for (String blockType : cfg.getStringList("interactables")) {
            Key blockKey = Key.ofMaterialAndData(blockType);

            String islandPrivilege;
            try {
                Material material = ((MaterialKey) blockKey).getMaterial();

                if (Materials.isChest(material)) {
                    islandPrivilege = "CHEST_ACCESS";
                } else if (material == LECTERN) {
                    islandPrivilege = "PICKUP_LECTERN_BOOK";
                } else if (isInventoryHolder(material)) {
                    islandPrivilege = "USE";
                } else if (Materials.isSign(material)) {
                    islandPrivilege = "SIGN_INTERACT";
                } else if (material == Materials.SPAWNER.toBukkitType()) {
                    islandPrivilege = "SPAWNER_BREAK";
                } else if (material == FARMLAND || material == SWEET_BERRY_BUSH
                        || material == CAVE_VINES || material == CAVE_VINES_PLANT) {
                    islandPrivilege = "FARM_TRAMPING";
                } else if (material == TURTLE_EGG) {
                    islandPrivilege = "TURTLE_EGG_TRAMPING";
                } else {
                    islandPrivilege = "INTERACT";
                }

            } catch (Throwable ignored) {
                islandPrivilege = "INTERACT";
            }

            List<String> blocks = cfg.getStringList(islandPrivilege);
            blocks.add(blockKey.toString());
            cfg.set(islandPrivilege, blocks);
        }

        cfg.set("interactables", null);
    }

    private static boolean isInventoryHolder(Material material) {
        return Materials.isShelf(material) || material == BLAST_FURNACE || material == Material.BREWING_STAND
                || material == BURNING_FURNACE || material == CHISELED_BOOKSHELF || material == CRAFTER
                || material == DECORATED_POT || material == Material.DISPENSER || material == Material.DROPPER
                || material == Material.FURNACE || material == Material.HOPPER || material == Material.JUKEBOX
                || material == SMOKER || material == VAULT;
    }

    @Override
    @Deprecated
    public Set<Key> getInteractables() {
        KeySet keySet = KeySets.createHashSet(KeyIndicator.MATERIAL);

        for (KeyCategory blockCategory : getContainer().blockCategories.getCategories()) {
            if (blockCategory instanceof FixedKeyCategory &&
                    blockCategory.getRequiredPrivilege(KeyCategory.Action.INTERACT) != null) {
                keySet.addAll(((FixedKeyCategory) blockCategory).getKeys());
            }
        }

        return Collections.unmodifiableSet(keySet);
    }

    @Override
    @Nullable
    @Deprecated
    public Set<Key> getInteractables(IslandPrivilege islandPrivilege) {
        for (KeyCategory blockCategory : getContainer().blockCategories.getCategories()) {
            if (blockCategory instanceof FixedKeyCategory &&
                    blockCategory.getRequiredPrivilege(KeyCategory.Action.INTERACT) == islandPrivilege) {
                return ((FixedKeyCategory) blockCategory).getKeys();
            }
        }

        return null;
    }

    @Override
    @Nullable
    @Deprecated
    public IslandPrivilege getRequiredPrivilege(Key key) {
        for (KeyCategory blockCategory : getContainer().blockCategories.getCategories()) {
            IslandPrivilege requiredPrivilege = blockCategory.getRequiredPrivilege(KeyCategory.Action.INTERACT);
            if (requiredPrivilege != null && blockCategory.isListed(key)) {
                return requiredPrivilege;
            }
        }

        return null;
    }

}
