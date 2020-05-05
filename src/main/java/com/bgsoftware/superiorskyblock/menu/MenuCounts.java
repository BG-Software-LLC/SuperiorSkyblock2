package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.config.CommentedConfiguration;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.ServerVersion;
import com.bgsoftware.superiorskyblock.utils.StringUtils;
import com.bgsoftware.superiorskyblock.utils.items.HeadUtils;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import com.bgsoftware.superiorskyblock.utils.legacy.Materials;
import com.bgsoftware.superiorskyblock.utils.registry.Registry;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public final class MenuCounts extends PagedSuperiorMenu<Pair<Key, Integer>> {

    private static final Map<String, String> blocksToItems = new HashMap<>();

    static {
        blocksToItems.put("SIGN_POST", "SIGN");
        blocksToItems.put("SUGAR_CANE_BLOCK", "SUGAR_CANE");
        blocksToItems.put("WALL_SIGN", "SIGN");
        blocksToItems.put("COCOA", ServerVersion.isLegacy() ? "INK_SACK:3" : "COCOA_BEANS");
    }

    private final Island island;

    private MenuCounts(SuperiorPlayer superiorPlayer, Island island){
        super("menuCounts", superiorPlayer);
        this.island = island;
    }

    @Override
    public void onPlayerClick(InventoryClickEvent event, Pair<Key, Integer> block) {
    }

    @Override
    protected void cloneAndOpen(SuperiorMenu previousMenu) {
        openInventory(superiorPlayer, previousMenu, island);
    }

    @Override
    protected ItemStack getObjectItem(ItemStack clickedItem, Pair<Key, Integer> block) {
        try {
            Key blockKey = block.getKey();
            int amount = block.getValue();

            String[] keySections = blockKey.toString().split(":");

            if (blocksToItems.containsKey(keySections[0])) {
                String[] item = blocksToItems.get(keySections[0]).split(":");
                String itemType = item[0];
                try {
                    //Checking if the material is valid
                    Material.valueOf(itemType);
                    if (item.length == 2)
                        keySections = item;
                    else
                        keySections[0] = itemType;
                }catch(Throwable ignored){}
            }

            Material blockMaterial;
            byte damage = 0;
            String materialName = null;

            try {
                blockMaterial = Material.valueOf(keySections[0]);
                if(keySections.length == 2) {
                    try {
                        damage = Byte.parseByte(keySections[1]);
                    }catch(Throwable ignored){}
                }
            } catch (Exception ex) {
                blockMaterial = Material.BEDROCK;
                materialName = keySections[0];
            }

            ItemMeta currentMeta = clickedItem.getItemMeta();
            ItemBuilder itemBuilder;

            if (blockMaterial == Materials.SPAWNER.toBukkitType() && keySections.length > 1) {
                itemBuilder = new ItemBuilder(HeadUtils.getPlayerHead(Materials.PLAYER_HEAD.toBukkitItem(),
                        HeadUtils.getTexture(keySections[1])));
                materialName = keySections[1] + "_SPAWNER";
            } else {
                itemBuilder = new ItemBuilder(blockMaterial, damage);
                if (materialName == null)
                    materialName = blockMaterial.name();
            }

            ItemStack itemStack = itemBuilder
                    .withName(currentMeta.hasDisplayName() ? currentMeta.getDisplayName() : "")
                    .withLore(currentMeta.hasLore() ? currentMeta.getLore() : new ArrayList<>())
                    .replaceAll("{0}", StringUtils.format(materialName))
                    .replaceAll("{1}", amount + "")
                    .replaceAll("{2}", StringUtils.format(plugin.getBlockValues().getBlockWorth(blockKey).multiply(BigDecimal.valueOf(amount))))
                    .replaceAll("{3}", StringUtils.format(plugin.getBlockValues().getBlockLevel(blockKey).multiply(BigDecimal.valueOf(amount))))
                    .replaceAll("{4}", StringUtils.fancyFormat(plugin.getBlockValues().getBlockWorth(blockKey).multiply(BigDecimal.valueOf(amount)), superiorPlayer.getUserLocale()))
                    .replaceAll("{5}", StringUtils.fancyFormat(plugin.getBlockValues().getBlockLevel(blockKey).multiply(BigDecimal.valueOf(amount)), superiorPlayer.getUserLocale()))
                    .build(superiorPlayer);

            itemStack.setAmount(Math.max(1, Math.min(64, amount)));

            return itemStack;
        }catch(Exception ex){
            SuperiorSkyblockPlugin.log("Failed to load menu because of block: " + block.getKey());
            throw ex;
        }
    }

    @Override
    protected List<Pair<Key, Integer>> requestObjects() {
        return island.getBlockCounts().entrySet().stream().sorted((o1, o2) -> {
            Material firstMaterial = Material.valueOf(o1.getKey().toString().split(":")[0]);
            Material secondMaterial = Material.valueOf(o2.getKey().toString().split(":")[0]);
            int compare = plugin.getNMSBlocks().compareMaterials(firstMaterial, secondMaterial);
            return compare != 0 ? compare : o1.getKey().toString().compareTo(o2.getKey().toString());
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
        MenuCounts menuCounts = new MenuCounts(superiorPlayer, island);
        menuCounts.open(previousMenu);
    }

    public static void refreshMenus(){
        refreshMenus(MenuCounts.class);
    }

}
