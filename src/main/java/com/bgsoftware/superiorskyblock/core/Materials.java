package com.bgsoftware.superiorskyblock.core;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
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


    private static final EnumMap<Material, MaterialTag> MATERIAL_TAGS = setupMaterialTags();
    private static final EnumSet<Material> BLOCK_NON_LEGACY_MATERIALS = allOf(material -> material.isBlock() && !isLegacy(material));
    private static final EnumSet<Material> SOLID_MATERIALS = allOf(Material::isSolid);

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

    public static boolean isSlab(Material material) {
        return MATERIAL_TAGS.get(material) instanceof SlabMaterialTag;
    }

    public static boolean isWater(Material material) {
        return MATERIAL_TAGS.get(material) instanceof WaterMaterialTag;
    }

    public static boolean isLegacy(Material material) {
        return MATERIAL_TAGS.get(material) instanceof LegacyMaterialTag;
    }

    public static boolean isRail(Material material) {
        return MATERIAL_TAGS.get(material) instanceof RailMaterialTag;
    }

    public static boolean isMinecart(Material material) {
        return MATERIAL_TAGS.get(material) instanceof MinecartMaterialTag;
    }

    public static boolean isChest(Material material) {
        return MATERIAL_TAGS.get(material) instanceof ChestMaterialTag;
    }

    public static boolean isBoat(Material material) {
        return MATERIAL_TAGS.get(material) instanceof BoatMaterialTag;
    }

    public static boolean isLava(Material material) {
        return MATERIAL_TAGS.get(material) instanceof LavaMaterialTag;
    }

    public static boolean isSign(Material material) {
        return MATERIAL_TAGS.get(material) instanceof SignMaterialTag;
    }

    public static boolean isDye(Material material) {
        return MATERIAL_TAGS.get(material) instanceof DyeMaterialTag;
    }

    public static Set<Material> getBlocksNonLegacy() {
        return Collections.unmodifiableSet(BLOCK_NON_LEGACY_MATERIALS);
    }

    public static Set<Material> getSolids() {
        return Collections.unmodifiableSet(SOLID_MATERIALS);
    }

    public static void init() {

    }

    private static EnumSet<Material> allOf(Predicate<Material> predicate) {
        EnumSet<Material> enumSet = EnumSet.noneOf(Material.class);
        Arrays.stream(Material.values()).filter(predicate).forEach(enumSet::add);
        return enumSet;
    }

    private static EnumMap<Material, MaterialTag> setupMaterialTags() {
        EnumMap<Material, MaterialTag> enumMap = new EnumMap<>(Material.class);
        Arrays.stream(Material.values()).forEach(material -> {
            String materialName = material.name();
            if (materialName.startsWith("LEGACY_"))
                enumMap.put(material, LegacyMaterialTag.INSTANCE);
            else if (materialName.contains("SLAB"))
                enumMap.put(material, SlabMaterialTag.INSTANCE);
            else if (materialName.contains("WATER"))
                enumMap.put(material, WaterMaterialTag.INSTANCE);
            else if (materialName.contains("RAIL"))
                enumMap.put(material, RailMaterialTag.INSTANCE);
            else if (materialName.contains("MINECART"))
                enumMap.put(material, MinecartMaterialTag.INSTANCE);
            else if (material == Material.CHEST || material == Material.ENDER_CHEST ||
                    material == Material.TRAPPED_CHEST || materialName.contains("SHULKER_BOX") ||
                    materialName.equals("BARREL"))
                enumMap.put(material, ChestMaterialTag.INSTANCE);
            else if (materialName.contains("BOAT"))
                enumMap.put(material, BoatMaterialTag.INSTANCE);
            else if (materialName.contains("LAVA"))
                enumMap.put(material, LavaMaterialTag.INSTANCE);
            else if (materialName.contains("SIGN"))
                enumMap.put(material, SignMaterialTag.INSTANCE);
            else if (ServerVersion.isLegacy() ? material == Material.INK_SACK : materialName.contains("_DYE"))
                enumMap.put(material, DyeMaterialTag.INSTANCE);
        });
        return enumMap;
    }

    private interface MaterialTag {

    }

    private static class SlabMaterialTag implements MaterialTag {

        private static final SlabMaterialTag INSTANCE = new SlabMaterialTag();

    }

    private static class WaterMaterialTag implements MaterialTag {

        private static final WaterMaterialTag INSTANCE = new WaterMaterialTag();

    }

    private static class LegacyMaterialTag implements MaterialTag {

        private static final LegacyMaterialTag INSTANCE = new LegacyMaterialTag();

    }

    private static class RailMaterialTag implements MaterialTag {

        private static final RailMaterialTag INSTANCE = new RailMaterialTag();

    }

    private static class MinecartMaterialTag implements MaterialTag {

        private static final MinecartMaterialTag INSTANCE = new MinecartMaterialTag();

    }

    private static class ChestMaterialTag implements MaterialTag {

        private static final ChestMaterialTag INSTANCE = new ChestMaterialTag();

    }

    private static class BoatMaterialTag implements MaterialTag {

        private static final BoatMaterialTag INSTANCE = new BoatMaterialTag();

    }

    private static class LavaMaterialTag implements MaterialTag {

        private static final LavaMaterialTag INSTANCE = new LavaMaterialTag();

    }

    private static class SignMaterialTag implements MaterialTag {

        private static final SignMaterialTag INSTANCE = new SignMaterialTag();

    }

    private static class DyeMaterialTag implements MaterialTag {

        private static final DyeMaterialTag INSTANCE = new DyeMaterialTag();

    }

}