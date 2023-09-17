package com.bgsoftware.superiorskyblock.core.menu.button.impl;

import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.menu.button.MenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.menu.button.PagedMenuTemplateButton;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.Materials;
import com.bgsoftware.superiorskyblock.core.ServerVersion;
import com.bgsoftware.superiorskyblock.core.formatting.Formatters;
import com.bgsoftware.superiorskyblock.core.itemstack.ItemBuilder;
import com.bgsoftware.superiorskyblock.core.itemstack.ItemSkulls;
import com.bgsoftware.superiorskyblock.core.key.Keys;
import com.bgsoftware.superiorskyblock.core.key.types.MaterialKey;
import com.bgsoftware.superiorskyblock.core.menu.button.AbstractPagedMenuButton;
import com.bgsoftware.superiorskyblock.core.menu.button.PagedMenuTemplateButtonImpl;
import com.bgsoftware.superiorskyblock.core.menu.impl.MenuCounts;
import com.google.common.collect.ImmutableMap;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.Optional;

public class CountsPagedObjectButton extends AbstractPagedMenuButton<MenuCounts.View, MenuCounts.BlockCount> {

    private static final BigInteger MAX_STACK = BigInteger.valueOf(64);

    private static final ImmutableMap<String, String> BLOCKS_TO_ITEMS = new ImmutableMap.Builder<String, String>()
            .put("ACACIA_DOOR", "ACACIA_DOOR_ITEM")
            .put("ACACIA_WALL_SIGN", "ACACIA_SIGN")
            .put("BAMBOO_SAPLING", "BAMBOO")
            .put("BED_BLOCK", "BED")
            .put("BEETROOT_BLOCK", "BEETROOT")
            .put("BEETROOTS", "BEETROOT")
            .put("BIRCH_DOOR", "BIRCH_DOOR_ITEM")
            .put("BIRCH_WALL_SIGN", "BIRCH_SIGN")
            .put("BLACK_WALL_BANNER", "BLACK_BANNER")
            .put("BLUE_WALL_BANNER", "BLUE_BANNER")
            .put("BRAIN_CORAL_WALL_FAN", "BRAIN_CORAL")
            .put("BURNING_FURNACE", "FURNACE")
            .put("BREWING_STAND", "BREWING_STAND_ITEM")
            .put("BROWN_WALL_BANNER", "BROWN_BANNER")
            .put("BUBBLE_CORAL_WALL_FAN", "BUBBLE_CORAL")
            .put("CAKE_BLOCK", "CAKE")
            .put("CARROT", "CARROT_ITEM")
            .put("CARROTS", "CARROT")
            .put("CAULDRON", "CAULDRON_ITEM")
            .put("COCOA", ServerVersion.isLegacy() ? "INK_SACK:3" : "COCOA_BEANS")
            .put("CREEPER_WALL_HEAD", "CREEPER_HEAD")
            .put("CROPS", "SEEDS")
            .put("CYAN_WALL_BANNER", "CYAN_BANNER")
            .put("DARK_OAK_DOOR", "DARK_OAK_DOOR_ITEM")
            .put("DARK_OAK_WALL_SIGN", "DARK_OAK_SIGN")
            .put("DAYLIGHT_DETECTOR_INVERTED", "DAYLIGHT_DETECTOR")
            .put("DEAD_BRAIN_CORAL_WALL_FAN", "DEAD_BRAIN_CORAL")
            .put("DEAD_BUBBLE_CORAL_WALL_FAN", "DEAD_BUBBLE_CORAL")
            .put("DEAD_FIRE_CORAL_WALL_FAN", "DEAD_FIRE_CORAL")
            .put("DEAD_HORN_CORAL_WALL_FAN", "DEAD_HORN_CORAL")
            .put("DEAD_TUBE_CORAL_WALL_FAN", "DEAD_TUBE_CORAL")
            .put("DIODE_BLOCK_OFF", "DIODE")
            .put("DIODE_BLOCK_ON", "DIODE")
            .put("DRAGON_WALL_HEAD", "DRAGON_HEAD")
            .put("END_PORTAL", "END_PORTAL_FRAME")
            .put("FIRE_CORAL_WALL_FAN", "FIRE_CORAL")
            .put("FLOWER_POT", "FLOWER_POT_ITEM")
            .put("GLOWING_REDSTONE_ORE", "REDSTONE_ORE")
            .put("GRAY_WALL_BANNER", "GRAY_BANNER")
            .put("GREEN_WALL_BANNER", "GREEN_BANNER")
            .put("HORN_CORAL_WALL_FAN", "HORN_CORAL")
            .put("IRON_DOOR_BLOCK", "IRON_DOOR")
            .put("JUNGLE_DOOR", "JUNGLE_DOOR_ITEM")
            .put("JUNGLE_WALL_SIGN", "JUNGLE_SIGN")
            .put("KELP_PLANT", "KELP")
            .put("LAVA", "LAVA_BUCKET")
            .put("LIGHT_BLUE_WALL_BANNER", "LIGHT_BLUE_BANNER")
            .put("LIGHT_GRAY_WALL_BANNER", "LIGHT_GRAY_BANNER")
            .put("LIME_WALL_BANNER", "LIME_BANNER")
            .put("MAGENTA_WALL_BANNER", "MAGENTA_BANNER")
            .put("MELON_STEM", "MELON_SEEDS")
            .put("MOVING_PISTON", "PISTON")
            .put("NETHER_WARTS", "NETHER_STALK")
            .put("OAK_WALL_SIGN", "OAK_SIGN")
            .put("ORANGE_WALL_BANNER", "ORANGE_BANNER")
            .put("PINK_WALL_BANNER", "PINK_BANNER")
            .put("PISTON_EXTENSION", "PISTON")
            .put("PISTON_HEAD", "PISTON")
            .put("PISTON_MOVING_PIECE", "PISTON")
            .put("PLAYER_WALL_HEAD", "PLAYER_HEAD")
            .put("POTATO", "POTATO_ITEM")
            .put("POTATOES", "POTATO")
            .put("POTTED_ACACIA_SAPLING", "FLOWER_POT")
            .put("POTTED_ALLIUM", "FLOWER_POT")
            .put("POTTED_AZURE_BLUET", "FLOWER_POT")
            .put("POTTED_BAMBOO", "FLOWER_POT")
            .put("POTTED_BIRCH_SAPLING", "FLOWER_POT")
            .put("POTTED_BLUE_ORCHID", "FLOWER_POT")
            .put("POTTED_BROWN_MUSHROOM", "FLOWER_POT")
            .put("POTTED_CACTUS", "FLOWER_POT")
            .put("POTTED_CORNFLOWER", "FLOWER_POT")
            .put("POTTED_DANDELION", "FLOWER_POT")
            .put("POTTED_DARK_OAK_SAPLING", "FLOWER_POT")
            .put("POTTED_DEAD_BUSH", "FLOWER_POT")
            .put("POTTED_FERN", "FLOWER_POT")
            .put("POTTED_JUNGLE_SAPLING", "FLOWER_POT")
            .put("POTTED_LILY_OF_THE_VALLEY", "FLOWER_POT")
            .put("POTTED_OAK_SAPLING", "FLOWER_POT")
            .put("POTTED_ORANGE_TULIP", "FLOWER_POT")
            .put("POTTED_OXEYE_DAISY", "FLOWER_POT")
            .put("POTTED_PINK_TULIP", "FLOWER_POT")
            .put("POTTED_POPPY", "FLOWER_POT")
            .put("POTTED_RED_MUSHROOM", "FLOWER_POT")
            .put("POTTED_RED_TULIP", "FLOWER_POT")
            .put("POTTED_SPRUCE_SAPLING", "FLOWER_POT")
            .put("POTTED_WHITE_TULIP", "FLOWER_POT")
            .put("POTTED_WITHER_ROSE", "FLOWER_POT")
            .put("PUMPKIN_STEM", "PUMPKIN_SEEDS")
            .put("PURPLE_WALL_BANNER", "PURPLE_BANNER")
            .put("REDSTONE_COMPARATOR_OFF", "REDSTONE_COMPARATOR")
            .put("REDSTONE_COMPARATOR_ON", "REDSTONE_COMPARATOR")
            .put("REDSTONE_LAMP_ON", "REDSTONE_LAMP")
            .put("REDSTONE_TORCH_OFF", "REDSTONE_TORCH")
            .put("REDSTONE_WALL_TORCH", "REDSTONE_TORCH")
            .put("REDSTONE_WIRE", "REDSTONE")
            .put("RED_WALL_BANNER", "RED_BANNER")
            .put("SIGN_POST", "SIGN")
            .put("STANDING_BANNER", "BANNER")
            .put("STATIONARY_LAVA", "LAVA_BUCKET")
            .put("STATIONARY_WATER", "WATER_BUCKET")
            .put("SKELETON_WALL_SKULL", "SKELETON_SKULL")
            .put("SKULL", "SKULL_ITEM")
            .put("SPRUCE_DOOR", "SPRUCE_DOOR_ITEM")
            .put("SPRUCE_WALL_SIGN", "SPRUCE_SIGN")
            .put("SUGAR_CANE_BLOCK", "SUGAR_CANE")
            .put("SWEET_BERRY_BUSH", "SWEET_BERRY")
            .put("TALL_SEAGRASS", "SEAGRASS")
            .put("TRIPWIRE", "TRIPWIRE_HOOK")
            .put("TUBE_CORAL_WALL_FAN", "TUBE_CORAL")
            .put("WALL_BANNER", "BANNER")
            .put("WALL_SIGN", "SIGN")
            .put("WALL_TORCH", "TORCH")
            .put("WATER", "WATER_BUCKET")
            .put("WHITE_WALL_BANNER", "WHITE_BANNER")
            .put("WITHER_SKELETON_WALL_SKULL", "WITHER_SKELETON_SKULL")
            .put("WOODEN_DOOR", "WOOD_DOOR")
            .put("YELLOW_WALL_BANNER", "YELLOW_BANNER")
            .put("ZOMBIE_WALL_HEAD", "ZOMBIE_HEAD")
            .build();

    private CountsPagedObjectButton(MenuTemplateButton<MenuCounts.View> templateButton, MenuCounts.View menuView) {
        super(templateButton, menuView);
    }

    @Override
    public void onButtonClick(InventoryClickEvent clickEvent) {
        // Dummy button
    }

    @Override
    public ItemStack modifyViewItem(ItemStack buttonItem) {
        Key rawKey = pagedObject.getBlockKey();
        Pair<Key, ItemStack> customKeyItem = plugin.getBlockValues().convertCustomKeyItem(rawKey);

        BigDecimal amount = new BigDecimal(pagedObject.getAmount());

        ItemMeta currentMeta = buttonItem.getItemMeta();
        ItemBuilder itemBuilder;
        String materialName = null;

        ItemStack customItem = customKeyItem.getValue();
        if (customItem != null) {
            itemBuilder = new ItemBuilder(customItem);
            materialName = Optional.ofNullable(rawKey.getSubKey()).orElseGet(rawKey::toString);
        } else {
            Key blockKey = customKeyItem.getKey();

            String convertedItem = BLOCKS_TO_ITEMS.get(blockKey.getGlobalKey());

            if (convertedItem != null) {
                Key tempBlockType = Keys.ofMaterialAndData(convertedItem);
                if (tempBlockType instanceof MaterialKey)
                    blockKey = tempBlockType;
            }

            Material blockMaterial;
            byte damage = 0;

            try {
                blockMaterial = Material.valueOf(blockKey.getGlobalKey());
                if (!blockKey.getSubKey().isEmpty()) {
                    try {
                        damage = Byte.parseByte(blockKey.getSubKey());
                    } catch (Throwable ignored) {
                    }
                }
            } catch (Exception ex) {
                blockMaterial = Material.BEDROCK;
                materialName = blockKey.getGlobalKey();
            }

            String texture;

            if (blockMaterial == Materials.SPAWNER.toBukkitType() && !blockKey.getSubKey().isEmpty() &&
                    !(texture = ItemSkulls.getTexture(blockKey.getSubKey())).isEmpty()) {
                itemBuilder = new ItemBuilder(ItemSkulls.getPlayerHead(Materials.PLAYER_HEAD.toBukkitItem(), texture));
                materialName = blockKey.getSubKey() + "_SPAWNER";
            } else {
                itemBuilder = new ItemBuilder(blockMaterial, damage);
                if (materialName == null)
                    materialName = rawKey.getGlobalKey();
            }
        }

        BigDecimal worthValue = plugin.getBlockValues().getBlockWorth(rawKey);
        BigDecimal levelValue = plugin.getBlockValues().getBlockLevel(rawKey);

        SuperiorPlayer inventoryViewer = menuView.getInventoryViewer();

        return itemBuilder
                .withName(currentMeta.hasDisplayName() ? currentMeta.getDisplayName() : "")
                .withLore(currentMeta.hasLore() ? currentMeta.getLore() : Collections.emptyList())
                .withAmount(BigInteger.ONE.max(MAX_STACK.min(amount.toBigInteger())).intValue())
                .replaceAll("{0}", Formatters.CAPITALIZED_FORMATTER.format(materialName))
                .replaceAll("{1}", amount + "")
                .replaceAll("{2}", Formatters.NUMBER_FORMATTER.format(worthValue.multiply(amount)))
                .replaceAll("{3}", Formatters.NUMBER_FORMATTER.format(levelValue.multiply(amount)))
                .replaceAll("{4}", Formatters.FANCY_NUMBER_FORMATTER.format(worthValue.multiply(amount), inventoryViewer.getUserLocale()))
                .replaceAll("{5}", Formatters.FANCY_NUMBER_FORMATTER.format(levelValue.multiply(amount), inventoryViewer.getUserLocale()))
                .build(inventoryViewer);
    }

    public static class Builder extends PagedMenuTemplateButtonImpl.AbstractBuilder<MenuCounts.View, MenuCounts.BlockCount> {

        @Override
        public PagedMenuTemplateButton<MenuCounts.View, MenuCounts.BlockCount> build() {
            return new PagedMenuTemplateButtonImpl<>(buttonItem, clickSound, commands, requiredPermission,
                    lackPermissionSound, nullItem, getButtonIndex(), CountsPagedObjectButton.class,
                    CountsPagedObjectButton::new);
        }

    }

}
