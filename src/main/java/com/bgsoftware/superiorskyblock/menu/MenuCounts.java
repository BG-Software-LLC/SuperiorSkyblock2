package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.common.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.ServerVersion;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.items.HeadUtils;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.utils.key.Key;
import com.bgsoftware.superiorskyblock.utils.legacy.Materials;
import com.bgsoftware.superiorskyblock.utils.registry.Registry;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class MenuCounts extends PagedSuperiorMenu<Pair<com.bgsoftware.superiorskyblock.api.key.Key, BigInteger>> {

    private static final BigInteger MAX_STACK = BigInteger.valueOf(64);
    private static final Map<String, String> blocksToItems = new HashMap<>();

    static {
        blocksToItems.put("ACACIA_DOOR", "ACACIA_DOOR_ITEM");
        blocksToItems.put("ACACIA_WALL_SIGN", "ACACIA_SIGN");
        blocksToItems.put("BAMBOO_SAPLING", "BAMBOO");
        blocksToItems.put("BED_BLOCK", "BED");
        blocksToItems.put("BEETROOT_BLOCK", "BEETROOT");
        blocksToItems.put("BEETROOTS", "BEETROOT");
        blocksToItems.put("BIRCH_DOOR", "BIRCH_DOOR_ITEM");
        blocksToItems.put("BIRCH_WALL_SIGN", "BIRCH_SIGN");
        blocksToItems.put("BLACK_WALL_BANNER", "BLACK_BANNER");
        blocksToItems.put("BLUE_WALL_BANNER", "BLUE_BANNER");
        blocksToItems.put("BRAIN_CORAL_WALL_FAN", "BRAIN_CORAL");
        blocksToItems.put("BURNING_FURNACE", "FURNACE");
        blocksToItems.put("BREWING_STAND", "BREWING_STAND_ITEM");
        blocksToItems.put("BROWN_WALL_BANNER", "BROWN_BANNER");
        blocksToItems.put("BUBBLE_CORAL_WALL_FAN", "BUBBLE_CORAL");
        blocksToItems.put("CAKE_BLOCK", "CAKE");
        blocksToItems.put("CARROT", "CARROT_ITEM");
        blocksToItems.put("CARROTS", "CARROT");
        blocksToItems.put("CAULDRON", "CAULDRON_ITEM");
        blocksToItems.put("COCOA", ServerVersion.isLegacy() ? "INK_SACK:3" : "COCOA_BEANS");
        blocksToItems.put("CREEPER_WALL_HEAD", "CREEPER_HEAD");
        blocksToItems.put("CROPS", "SEEDS");
        blocksToItems.put("CYAN_WALL_BANNER", "CYAN_BANNER");
        blocksToItems.put("DARK_OAK_DOOR", "DARK_OAK_DOOR_ITEM");
        blocksToItems.put("DARK_OAK_WALL_SIGN", "DARK_OAK_SIGN");
        blocksToItems.put("DAYLIGHT_DETECTOR_INVERTED", "DAYLIGHT_DETECTOR");
        blocksToItems.put("DEAD_BRAIN_CORAL_WALL_FAN", "DEAD_BRAIN_CORAL");
        blocksToItems.put("DEAD_BUBBLE_CORAL_WALL_FAN", "DEAD_BUBBLE_CORAL");
        blocksToItems.put("DEAD_FIRE_CORAL_WALL_FAN", "DEAD_FIRE_CORAL");
        blocksToItems.put("DEAD_HORN_CORAL_WALL_FAN", "DEAD_HORN_CORAL");
        blocksToItems.put("DEAD_TUBE_CORAL_WALL_FAN", "DEAD_TUBE_CORAL");
        blocksToItems.put("DIODE_BLOCK_OFF", "DIODE");
        blocksToItems.put("DIODE_BLOCK_ON", "DIODE");
        blocksToItems.put("DRAGON_WALL_HEAD", "DRAGON_HEAD");
        blocksToItems.put("END_PORTAL", "END_PORTAL_FRAME");
        blocksToItems.put("FIRE_CORAL_WALL_FAN", "FIRE_CORAL");
        blocksToItems.put("FLOWER_POT", "FLOWER_POT_ITEM");
        blocksToItems.put("GLOWING_REDSTONE_ORE", "REDSTONE_ORE");
        blocksToItems.put("GRAY_WALL_BANNER", "GRAY_BANNER");
        blocksToItems.put("GREEN_WALL_BANNER", "GREEN_BANNER");
        blocksToItems.put("HORN_CORAL_WALL_FAN", "HORN_CORAL");
        blocksToItems.put("IRON_DOOR_BLOCK", "IRON_DOOR");
        blocksToItems.put("JUNGLE_DOOR", "JUNGLE_DOOR_ITEM");
        blocksToItems.put("JUNGLE_WALL_SIGN", "JUNGLE_SIGN");
        blocksToItems.put("KELP_PLANT", "KELP");
        blocksToItems.put("LAVA", "LAVA_BUCKET");
        blocksToItems.put("LIGHT_BLUE_WALL_BANNER", "LIGHT_BLUE_BANNER");
        blocksToItems.put("LIGHT_GRAY_WALL_BANNER", "LIGHT_GRAY_BANNER");
        blocksToItems.put("LIME_WALL_BANNER", "LIME_BANNER");
        blocksToItems.put("MAGENTA_WALL_BANNER", "MAGENTA_BANNER");
        blocksToItems.put("MELON_STEM", "MELON_SEEDS");
        blocksToItems.put("MOVING_PISTON", "PISTON");
        blocksToItems.put("NETHER_WARTS", "NETHER_STALK");
        blocksToItems.put("OAK_WALL_SIGN", "OAK_SIGN");
        blocksToItems.put("ORANGE_WALL_BANNER", "ORANGE_BANNER");
        blocksToItems.put("PINK_WALL_BANNER", "PINK_BANNER");
        blocksToItems.put("PISTON_EXTENSION", "PISTON");
        blocksToItems.put("PISTON_HEAD", "PISTON");
        blocksToItems.put("PISTON_MOVING_PIECE", "PISTON");
        blocksToItems.put("PLAYER_WALL_HEAD", "PLAYER_HEAD");
        blocksToItems.put("POTATO", "POTATO_ITEM");
        blocksToItems.put("POTATOES", "POTATO");
        blocksToItems.put("POTTED_ACACIA_SAPLING", "FLOWER_POT");
        blocksToItems.put("POTTED_ALLIUM", "FLOWER_POT");
        blocksToItems.put("POTTED_AZURE_BLUET", "FLOWER_POT");
        blocksToItems.put("POTTED_BAMBOO", "FLOWER_POT");
        blocksToItems.put("POTTED_BIRCH_SAPLING", "FLOWER_POT");
        blocksToItems.put("POTTED_BLUE_ORCHID", "FLOWER_POT");
        blocksToItems.put("POTTED_BROWN_MUSHROOM", "FLOWER_POT");
        blocksToItems.put("POTTED_CACTUS", "FLOWER_POT");
        blocksToItems.put("POTTED_CORNFLOWER", "FLOWER_POT");
        blocksToItems.put("POTTED_DANDELION", "FLOWER_POT");
        blocksToItems.put("POTTED_DARK_OAK_SAPLING", "FLOWER_POT");
        blocksToItems.put("POTTED_DEAD_BUSH", "FLOWER_POT");
        blocksToItems.put("POTTED_FERN", "FLOWER_POT");
        blocksToItems.put("POTTED_JUNGLE_SAPLING", "FLOWER_POT");
        blocksToItems.put("POTTED_LILY_OF_THE_VALLEY", "FLOWER_POT");
        blocksToItems.put("POTTED_OAK_SAPLING", "FLOWER_POT");
        blocksToItems.put("POTTED_ORANGE_TULIP", "FLOWER_POT");
        blocksToItems.put("POTTED_OXEYE_DAISY", "FLOWER_POT");
        blocksToItems.put("POTTED_PINK_TULIP", "FLOWER_POT");
        blocksToItems.put("POTTED_POPPY", "FLOWER_POT");
        blocksToItems.put("POTTED_RED_MUSHROOM", "FLOWER_POT");
        blocksToItems.put("POTTED_RED_TULIP", "FLOWER_POT");
        blocksToItems.put("POTTED_SPRUCE_SAPLING", "FLOWER_POT");
        blocksToItems.put("POTTED_WHITE_TULIP", "FLOWER_POT");
        blocksToItems.put("POTTED_WITHER_ROSE", "FLOWER_POT");
        blocksToItems.put("PUMPKIN_STEM", "PUMPKIN_SEEDS");
        blocksToItems.put("PURPLE_WALL_BANNER", "PURPLE_BANNER");
        blocksToItems.put("REDSTONE_COMPARATOR_OFF", "REDSTONE_COMPARATOR");
        blocksToItems.put("REDSTONE_COMPARATOR_ON", "REDSTONE_COMPARATOR");
        blocksToItems.put("REDSTONE_LAMP_ON", "REDSTONE_LAMP");
        blocksToItems.put("REDSTONE_TORCH_OFF", "REDSTONE_TORCH");
        blocksToItems.put("REDSTONE_WALL_TORCH", "REDSTONE_TORCH");
        blocksToItems.put("REDSTONE_WIRE", "REDSTONE");
        blocksToItems.put("RED_WALL_BANNER", "RED_BANNER");
        blocksToItems.put("SIGN_POST", "SIGN");
        blocksToItems.put("STANDING_BANNER", "BANNER");
        blocksToItems.put("STATIONARY_LAVA", "LAVA_BUCKET");
        blocksToItems.put("STATIONARY_WATER", "WATER_BUCKET");
        blocksToItems.put("SKELETON_WALL_SKULL", "SKELETON_SKULL");
        blocksToItems.put("SKULL", "SKULL_ITEM");
        blocksToItems.put("SPRUCE_DOOR", "SPRUCE_DOOR_ITEM");
        blocksToItems.put("SPRUCE_WALL_SIGN", "SPRUCE_SIGN");
        blocksToItems.put("SUGAR_CANE_BLOCK", "SUGAR_CANE");
        blocksToItems.put("SWEET_BERRY_BUSH", "SWEET_BERRY");
        blocksToItems.put("TALL_SEAGRASS", "SEAGRASS");
        blocksToItems.put("TRIPWIRE", "TRIPWIRE_HOOK");
        blocksToItems.put("TUBE_CORAL_WALL_FAN", "TUBE_CORAL");
        blocksToItems.put("WALL_BANNER", "BANNER");
        blocksToItems.put("WALL_SIGN", "SIGN");
        blocksToItems.put("WALL_TORCH", "TORCH");
        blocksToItems.put("WATER", "WATER_BUCKET");
        blocksToItems.put("WHITE_WALL_BANNER", "WHITE_BANNER");
        blocksToItems.put("WITHER_SKELETON_WALL_SKULL", "WITHER_SKELETON_SKULL");
        blocksToItems.put("WOODEN_DOOR", "WOOD_DOOR");
        blocksToItems.put("YELLOW_WALL_BANNER", "YELLOW_BANNER");
        blocksToItems.put("ZOMBIE_WALL_HEAD", "ZOMBIE_HEAD");
    }

    private final Island island;

    private MenuCounts(SuperiorPlayer superiorPlayer, Island island){
        super("menuCounts", superiorPlayer);
        this.island = island;
    }

    @Override
    public void onPlayerClick(InventoryClickEvent event, Pair<com.bgsoftware.superiorskyblock.api.key.Key, BigInteger> block) {
    }

    @Override
    protected void cloneAndOpen(SuperiorMenu previousMenu) {
        openInventory(superiorPlayer, previousMenu, island);
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
                    if (item.length == 2)
                        blockKey = Key.of(item[0] + ":" + item[1]);
                    else
                        blockKey = Key.of(itemType);
                }catch(Throwable ignored){}
            }

            Material blockMaterial;
            byte damage = 0;
            String materialName = null;

            try {
                blockMaterial = Material.valueOf(blockKey.getGlobalKey());
                if(!blockKey.getSubKey().isEmpty()) {
                    try {
                        damage = Byte.parseByte(blockKey.getSubKey());
                    }catch(Throwable ignored){}
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
        }catch(Exception ex){
            SuperiorSkyblockPlugin.log("Failed to load menu because of block: " + block.getKey());
            throw ex;
        }
    }

    @Override
    protected List<Pair<com.bgsoftware.superiorskyblock.api.key.Key, BigInteger>> requestObjects() {
        return island.getBlockCountsAsBigInteger().entrySet().stream().sorted((o1, o2) -> {
            Material firstMaterial = getSafeMaterial(o1.getKey().getGlobalKey());
            Material secondMaterial = getSafeMaterial(o2.getKey().getGlobalKey());
            int compare = plugin.getNMSBlocks().compareMaterials(firstMaterial, secondMaterial);
            return compare != 0 ? compare : o1.getKey().compareTo(o2.getKey());
        }).map(Pair::new).collect(Collectors.toList());
    }

    public static void init(){
        MenuCounts menuCounts = new MenuCounts(null, null);

        File file = new File(plugin.getDataFolder(), "menus/counts.yml");

        if(!file.exists())
            FileUtils.saveResource("menus/counts.yml");

        CommentedConfiguration cfg = CommentedConfiguration.loadConfiguration(file);

        Registry<Character, List<Integer>> charSlots = FileUtils.loadGUI(menuCounts, "counts.yml", cfg);

        menuCounts.setPreviousSlot(getSlots(cfg, "previous-page", charSlots));
        menuCounts.setCurrentSlot(getSlots(cfg, "current-page", charSlots));
        menuCounts.setNextSlot(getSlots(cfg, "next-page", charSlots));
        menuCounts.setSlots(getSlots(cfg, "slots", charSlots));

        charSlots.delete();

        menuCounts.markCompleted();
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, SuperiorMenu previousMenu, Island island){
        new MenuCounts(superiorPlayer, island).open(previousMenu);
    }

    public static void refreshMenus(Island island){
        refreshMenus(MenuCounts.class, superiorMenu -> superiorMenu.island.equals(island));
    }

    private static Material getSafeMaterial(String value){
        try{
            return Material.valueOf(value);
        }catch(Exception ex){
            return Material.BEDROCK;
        }
    }

}
