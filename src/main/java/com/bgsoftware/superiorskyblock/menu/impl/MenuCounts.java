package com.bgsoftware.superiorskyblock.menu.impl;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.menu.ISuperiorMenu;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.key.Key;
import com.bgsoftware.superiorskyblock.menu.PagedSuperiorMenu;
import com.bgsoftware.superiorskyblock.menu.file.MenuPatternSlots;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.ServerVersion;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.debug.PluginDebugger;
import com.bgsoftware.superiorskyblock.utils.items.HeadUtils;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.utils.legacy.Materials;
import com.google.common.collect.ImmutableMap;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class MenuCounts extends PagedSuperiorMenu<Pair<com.bgsoftware.superiorskyblock.api.key.Key, BigInteger>> {

    private static final BigInteger MAX_STACK = BigInteger.valueOf(64);
    private static final ImmutableMap<String, String> blocksToItems = new ImmutableMap.Builder<String, String>()
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

    private final Island island;

    private MenuCounts(SuperiorPlayer superiorPlayer, Island island) {
        super("menuCounts", superiorPlayer);
        this.island = island;
    }

    public static void init() {
        MenuCounts menuCounts = new MenuCounts(null, null);

        File file = new File(plugin.getDataFolder(), "menus/counts.yml");

        if (!file.exists())
            FileUtils.saveResource("menus/counts.yml");

        CommentedConfiguration cfg = CommentedConfiguration.loadConfiguration(file);

        MenuPatternSlots menuPatternSlots = FileUtils.loadGUI(menuCounts, "counts.yml", cfg);

        menuCounts.setPreviousSlot(getSlots(cfg, "previous-page", menuPatternSlots));
        menuCounts.setCurrentSlot(getSlots(cfg, "current-page", menuPatternSlots));
        menuCounts.setNextSlot(getSlots(cfg, "next-page", menuPatternSlots));
        menuCounts.setSlots(getSlots(cfg, "slots", menuPatternSlots));

        menuCounts.markCompleted();
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, ISuperiorMenu previousMenu, Island island) {
        new MenuCounts(superiorPlayer, island).open(previousMenu);
    }

    public static void refreshMenus(Island island) {
        refreshMenus(MenuCounts.class, superiorMenu -> superiorMenu.island.equals(island));
    }

    private static Material getSafeMaterial(String value) {
        try {
            return Material.valueOf(value);
        } catch (Exception ex) {
            return Material.BEDROCK;
        }
    }

    @Override
    public void onPlayerClick(InventoryClickEvent event, Pair<com.bgsoftware.superiorskyblock.api.key.Key, BigInteger> block) {
        // Do nothing.
    }

    @Override
    protected ItemStack getObjectItem(ItemStack clickedItem, Pair<com.bgsoftware.superiorskyblock.api.key.Key, BigInteger> block) {
        try {
            Key rawKey = (Key) block.getKey();
            Key blockKey = plugin.getBlockValues().convertKey(rawKey);

            BigDecimal amount = new BigDecimal(block.getValue());

            if (blocksToItems.containsKey(blockKey.getGlobalKey())) {
                String[] item = blocksToItems.get(blockKey.getGlobalKey()).split(":");
                String itemType = item[0];
                try {
                    //Checking if the material is valid
                    Material.valueOf(itemType);
                    String subKey = item.length == 2 ? item[1] : "";
                    blockKey = Key.of(item[0], subKey);
                } catch (Throwable ignored) {
                }
            }

            Material blockMaterial;
            byte damage = 0;
            String materialName = null;

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

            ItemMeta currentMeta = clickedItem.getItemMeta();
            ItemBuilder itemBuilder;
            String texture;

            if (blockMaterial == Materials.SPAWNER.toBukkitType() && !blockKey.getSubKey().isEmpty() &&
                    !(texture = HeadUtils.getTexture(blockKey.getSubKey())).isEmpty()) {
                itemBuilder = new ItemBuilder(HeadUtils.getPlayerHead(Materials.PLAYER_HEAD.toBukkitItem(), texture));
                materialName = blockKey.getSubKey() + "_SPAWNER";
            } else {
                itemBuilder = new ItemBuilder(blockMaterial, damage);
                if (materialName == null)
                    materialName = rawKey.getGlobalKey();
            }

            ItemStack itemStack = itemBuilder
                    .withName(currentMeta.hasDisplayName() ? currentMeta.getDisplayName() : "")
                    .withLore(currentMeta.hasLore() ? currentMeta.getLore() : new ArrayList<>())
                    .replaceAll("{0}", StringUtils.format(materialName))
                    .replaceAll("{1}", amount + "")
                    .replaceAll("{2}", StringUtils.format(plugin.getBlockValues().getBlockWorth(rawKey).multiply(amount)))
                    .replaceAll("{3}", StringUtils.format(plugin.getBlockValues().getBlockLevel(rawKey).multiply(amount)))
                    .replaceAll("{4}", StringUtils.fancyFormat(plugin.getBlockValues().getBlockWorth(rawKey).multiply(amount), superiorPlayer.getUserLocale()))
                    .replaceAll("{5}", StringUtils.fancyFormat(plugin.getBlockValues().getBlockLevel(rawKey).multiply(amount), superiorPlayer.getUserLocale()))
                    .build(superiorPlayer);

            itemStack.setAmount(BigInteger.ONE.max(MAX_STACK.min(amount.toBigInteger())).intValue());

            return itemStack;
        } catch (Exception ex) {
            SuperiorSkyblockPlugin.log("Failed to load menu because of block: " + block.getKey());
            PluginDebugger.debug(ex);
            throw ex;
        }
    }

    @Override
    protected List<Pair<com.bgsoftware.superiorskyblock.api.key.Key, BigInteger>> requestObjects() {
        return island.getBlockCountsAsBigInteger().entrySet().stream().sorted((o1, o2) -> {
            Material firstMaterial = getSafeMaterial(o1.getKey().getGlobalKey());
            Material secondMaterial = getSafeMaterial(o2.getKey().getGlobalKey());
            int compare = plugin.getNMSAlgorithms().compareMaterials(firstMaterial, secondMaterial);
            return compare != 0 ? compare : o1.getKey().compareTo(o2.getKey());
        }).map(Pair::new).collect(Collectors.toList());
    }

    @Override
    public void cloneAndOpen(ISuperiorMenu previousMenu) {
        openInventory(superiorPlayer, previousMenu, island);
    }

}
