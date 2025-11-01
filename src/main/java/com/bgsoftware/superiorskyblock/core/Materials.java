package com.bgsoftware.superiorskyblock.core;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

public enum Materials {

    CLOCK("WATCH"),
    PLAYER_HEAD("SKULL_ITEM", 3),
    GOLDEN_AXE("GOLD_AXE"),
    SPAWNER("MOB_SPAWNER"),
    SUNFLOWER("DOUBLE_PLANT"),
    BLACK_STAINED_GLASS_PANE("STAINED_GLASS_PANE", 15),
    BONE_MEAL("INK_SACK", 15),
    NETHER_PORTAL("PORTAL"),
    END_PORTAL_FRAME("ENDER_PORTAL_FRAME");


    private static final EnumMap<Material, EnumSet<Tag>> MATERIAL_TAGS = setupMaterialTags();
    private static final EnumSet<Material> BLOCK_NON_LEGACY_MATERIALS = allOf(material -> material.isBlock() && !isLegacy(material));
    private static final EnumSet<Material> SOLID_MATERIALS = allOf(Material::isSolid);
    private static final Map<String, String> PATCHED_MATERIAL_NAMES = setupPatchedMaterialNames();

    private final String bukkitType;
    private final short bukkitData;

    Materials(String bukkitType) {
        this(bukkitType, 0);
    }

    Materials(String bukkitType, int bukkitData) {
        this.bukkitType = bukkitType;
        this.bukkitData = (short) bukkitData;
    }

    public Material toBukkitType() {
        try {
            return Material.valueOf(ServerVersion.isLegacy() ? bukkitType : name());
        } catch (Exception ex) {
            throw new IllegalArgumentException("Couldn't cast " + name() + " into a bukkit enum. Contact Ome_R!");
        }
    }

    public ItemStack toBukkitItem() {
        return toBukkitItem(1);
    }

    public ItemStack toBukkitItem(int amount) {
        return ServerVersion.isLegacy() ? new ItemStack(toBukkitType(), amount, bukkitData) : new ItemStack(toBukkitType(), amount);
    }

    public static boolean hasTag(Material material, Tag tag) {
        EnumSet<Tag> materialsTag = MATERIAL_TAGS.get(material);
        return materialsTag != null && materialsTag.contains(tag);
    }

    public static boolean isSlab(Material material) {
        return hasTag(material, Tag.SLAB);
    }

    public static boolean isWater(Material material) {
        return hasTag(material, Tag.WATER);
    }

    public static boolean isLegacy(Material material) {
        return hasTag(material, Tag.LEGACY);
    }

    public static boolean isRail(Material material) {
        return hasTag(material, Tag.RAIL);
    }

    public static boolean isMinecart(Material material) {
        return hasTag(material, Tag.MINECART);
    }

    public static boolean isChest(Material material) {
        return hasTag(material, Tag.CHEST);
    }

    public static boolean isBoat(Material material) {
        return hasTag(material, Tag.BOAT);
    }

    public static boolean isLava(Material material) {
        return hasTag(material, Tag.LAVA);
    }

    public static boolean isSign(Material material) {
        return hasTag(material, Tag.SIGN);
    }

    public static boolean isDye(Material material) {
        return hasTag(material, Tag.DYE);
    }

    public static boolean isSpawnEgg(Material material) {
        return hasTag(material, Tag.SPAWN_EGG);
    }

    public static boolean isCarpet(Material material) {
        return hasTag(material, Tag.CARPET);
    }

    public static boolean isHarness(Material material) {
        return hasTag(material, Tag.HARNESS);
    }

    public static boolean isBed(Material material) {
        return hasTag(material, Tag.BED);
    }

    public static Set<Material> getBlocksNonLegacy() {
        return Collections.unmodifiableSet(BLOCK_NON_LEGACY_MATERIALS);
    }

    public static Set<Material> getSolids() {
        return Collections.unmodifiableSet(SOLID_MATERIALS);
    }

    public static String patchOldMaterialName(String type) {
        return PATCHED_MATERIAL_NAMES.getOrDefault(type, type);
    }

    public static void init() {

    }

    private static EnumSet<Material> allOf(Predicate<Material> predicate) {
        EnumSet<Material> enumSet = EnumSet.noneOf(Material.class);
        Arrays.stream(Material.values()).filter(predicate).forEach(enumSet::add);
        return enumSet;
    }

    private static EnumMap<Material, EnumSet<Tag>> setupMaterialTags() {
        EnumMap<Material, EnumSet<Tag>> enumMap = new EnumMap<>(Material.class);

        for (Material material : Material.values()) {
            EnumSet<Tag> materialTags = EnumSet.noneOf(Tag.class);

            String materialName = material.name();
            if (materialName.startsWith("LEGACY_"))
                materialTags.add(Tag.LEGACY);
            if (materialName.contains("SLAB"))
                materialTags.add(Tag.SLAB);
            if (materialName.contains("WATER"))
                materialTags.add(Tag.WATER);
            if (materialName.contains("RAIL"))
                materialTags.add(Tag.RAIL);
            if (materialName.contains("MINECART"))
                materialTags.add(Tag.MINECART);
            if (material == Material.CHEST || material == Material.ENDER_CHEST ||
                    material == Material.TRAPPED_CHEST || materialName.contains("SHULKER_BOX") ||
                    materialName.equals("BARREL"))
                materialTags.add(Tag.CHEST);
            if (materialName.contains("BOAT"))
                materialTags.add(Tag.BOAT);
            if (materialName.contains("LAVA"))
                materialTags.add(Tag.LAVA);
            if (materialName.contains("SIGN"))
                materialTags.add(Tag.SIGN);
            if (ServerVersion.isLegacy() ? material == Material.INK_SACK : materialName.contains("_DYE"))
                materialTags.add(Tag.DYE);
            if (ServerVersion.isLegacy() ? material == Material.MONSTER_EGG : materialName.contains("_SPAWN_EGG"))
                materialTags.add(Tag.SPAWN_EGG);
            if (materialName.contains("CARPET"))
                materialTags.add(Tag.CARPET);
            if (materialName.contains("HARNESS"))
                materialTags.add(Tag.HARNESS);
            if (materialName.contains("BED"))
                materialTags.add(Tag.BED);

            if (!materialTags.isEmpty())
                enumMap.put(material, materialTags);
        }

        Arrays.stream(Material.values()).forEach(material -> {

        });
        return enumMap;
    }

    private static Map<String, String> setupPatchedMaterialNames() {
        Map<String, String> map = new HashMap<>();
        try {
            Material.valueOf("GRASS");
        } catch (IllegalArgumentException error) {
            map.put("GRASS", "GRASS_BLOCK");
        }
        return map.isEmpty() ? Collections.emptyMap() : map;
    }

    public enum Tag {

        SLAB,
        WATER,
        LEGACY,
        RAIL,
        MINECART,
        CHEST,
        BOAT,
        LAVA,
        SIGN,
        DYE,
        SPAWN_EGG,
        CARPET,
        BED,
        HARNESS

    }

}