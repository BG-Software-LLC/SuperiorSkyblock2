package com.bgsoftware.superiorskyblock.handlers;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.key.Key;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.gui.GUIInventory;
import com.bgsoftware.superiorskyblock.utils.FileUtil;
import com.bgsoftware.superiorskyblock.utils.HeadUtil;
import com.bgsoftware.superiorskyblock.utils.ItemBuilder;
import com.bgsoftware.superiorskyblock.utils.StringUtil;
import com.bgsoftware.superiorskyblock.utils.key.KeyMap;
import com.bgsoftware.superiorskyblock.utils.key.SKey;
import com.bgsoftware.superiorskyblock.utils.legacy.Materials;

import com.bgsoftware.superiorskyblock.utils.threads.SuperiorThread;
import com.bgsoftware.superiorskyblock.wrappers.SBlockPosition;
import com.bgsoftware.superiorskyblock.wrappers.SSuperiorPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Biome;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("WeakerAccess")
public final class PanelHandler {

    private SuperiorSkyblockPlugin plugin;
    public GUIInventory mainPage, membersPage, visitorsPage, playerPage, rolePage,
            islandCreationPage, biomesPage, warpsPage, valuesPage;

    public Map<UUID, UUID> islands = new HashMap<>();

    public PanelHandler(SuperiorSkyblockPlugin plugin){
        this.plugin = plugin;
    }

    private Sound getSound(String name){
        try{
            return Sound.valueOf(name);
        }catch(Exception ex){
            return null;
        }
    }

    public void openPanel(SuperiorPlayer superiorPlayer){
        mainPage.openInventory(superiorPlayer, true);
    }

    public void openMembersPanel(SuperiorPlayer superiorPlayer, int page){
        if(Bukkit.isPrimaryThread()){
            new SuperiorThread(() -> openMembersPanel(superiorPlayer, page)).start();
            return;
        }

        Inventory inventory = membersPage.clonedInventory();
        List<UUID> members = new ArrayList<>();

        if(superiorPlayer.getIsland() != null)
            members.addAll(superiorPlayer.getIsland().getAllMembers());

        members.sort(Comparator.comparing(o -> SSuperiorPlayer.of(o).getName()));

        //noinspection unchecked
        List<Integer> slots = membersPage.get("slots", List.class);

        ItemStack memberItem = membersPage.get("memberItem", ItemStack.class);

        for(int i = 0; i < slots.size() && (i + (slots.size() * (page - 1))) < members.size(); i++){
            SuperiorPlayer _superiorPlayer = SSuperiorPlayer.of(members.get(i + (slots.size() * (page - 1))));
            inventory.setItem(slots.get(i), new ItemBuilder(memberItem)
                    .replaceAll("{0}", _superiorPlayer.getName())
                    .asSkullOf(_superiorPlayer).build());
        }

        int previousSlot = membersPage.get("previousSlot", Integer.class);
        ItemStack previousButton = membersPage.get("previousButton", ItemStack.class);
        inventory.setItem(previousSlot, new ItemBuilder(previousButton)
                .replaceName("{0}", (page == 1 ? "&c" : "&a")).build());

        int currentSlot = membersPage.get("currentSlot", Integer.class);
        ItemStack currentButton = membersPage.get("currentButton", ItemStack.class);
        inventory.setItem(currentSlot, new ItemBuilder(currentButton)
                .replaceLore("{0}", page + "").build());

        int nextSlot = membersPage.get("nextSlot", Integer.class);
        ItemStack nextButton = membersPage.get("nextButton", ItemStack.class);
        inventory.setItem(nextSlot, new ItemBuilder(nextButton)
                .replaceName("{0}", (members.size() > page * slots.size() ? "&a" : "&c")).build());

        membersPage.openInventory(superiorPlayer, inventory);
    }

    public void openVisitorsPanel(SuperiorPlayer superiorPlayer, int page){
        if(Bukkit.isPrimaryThread()){
            new SuperiorThread(() -> openVisitorsPanel(superiorPlayer, page)).start();
            return;
        }

        Inventory inventory = visitorsPage.clonedInventory();
        List<UUID> visitors = new ArrayList<>();

        if(superiorPlayer.getIsland() != null)
            visitors.addAll(superiorPlayer.getIsland().getVisitors());

        visitors.sort(Comparator.comparing(o -> SSuperiorPlayer.of(o).getName()));

        //noinspection unchecked
        List<Integer> slots = visitorsPage.get("slots", List.class);

        ItemStack visitorItem = visitorsPage.get("visitorItem", ItemStack.class);

        for(int i = 0; i < slots.size() && (i + (slots.size() * (page - 1))) < visitors.size(); i++){
            SuperiorPlayer _superiorPlayer = SSuperiorPlayer.of(visitors.get(i + (slots.size() * (page - 1))));
            String islandOwner = "None";
            if(_superiorPlayer.getIsland() != null)
                islandOwner = _superiorPlayer.getIsland().getOwner().getName();
            inventory.setItem(slots.get(i), new ItemBuilder(visitorItem)
                    .replaceAll("{0}", _superiorPlayer.getName())
                    .replaceAll("{1}", islandOwner)
                    .asSkullOf(_superiorPlayer).build());
        }

        int previousSlot = visitorsPage.get("previousSlot", Integer.class);
        ItemStack previousButton = visitorsPage.get("previousButton", ItemStack.class);
        inventory.setItem(previousSlot, new ItemBuilder(previousButton)
                .replaceName("{0}", (page == 1 ? "&c" : "&a")).build());

        int currentSlot = visitorsPage.get("currentSlot", Integer.class);
        ItemStack currentButton = visitorsPage.get("currentButton", ItemStack.class);
        inventory.setItem(currentSlot, new ItemBuilder(currentButton)
                .replaceLore("{0}", page + "").build());

        int nextSlot = visitorsPage.get("nextSlot", Integer.class);
        ItemStack nextButton = visitorsPage.get("nextButton", ItemStack.class);
        inventory.setItem(nextSlot, new ItemBuilder(nextButton)
                .replaceName("{0}", (visitors.size() > page * slots.size() ? "&a" : "&c")).build());

        visitorsPage.openInventory(superiorPlayer, inventory);
    }

    public void openPlayerPanel(SuperiorPlayer superiorPlayer, SuperiorPlayer targetPlayer){
        Inventory inventory = Bukkit.createInventory(null, playerPage.getSize(), ChatColor.BOLD + targetPlayer.getName());
        inventory.setContents(playerPage.getContents());
        playerPage.openInventory(superiorPlayer, inventory);
    }

    public void openRolePanel(SuperiorPlayer superiorPlayer, SuperiorPlayer targetPlayer){
        Inventory inventory = Bukkit.createInventory(null, rolePage.getSize(), ChatColor.BOLD + targetPlayer.getName());
        inventory.setContents(rolePage.getContents());
        rolePage.openInventory(superiorPlayer, inventory);
    }

    public void openIslandCreationPanel(SuperiorPlayer superiorPlayer){
        if(Bukkit.isPrimaryThread()){
            new SuperiorThread(() -> openIslandCreationPanel(superiorPlayer)).start();
            return;
        }

        Inventory inventory = islandCreationPage.clonedInventory();

        for(String schematic : plugin.getSchematics().getSchematics()){
            if(islandCreationPage.contains(schematic + "-has-access-item")) {
                ItemStack schematicItem = islandCreationPage.get(schematic + "-has-access-item", ItemStack.class);
                String permission = islandCreationPage.get(schematic + "-permission", String.class);
                int slot = islandCreationPage.get(schematic + "-slot", Integer.class);

                if(!superiorPlayer.hasPermission(permission))
                    schematicItem = islandCreationPage.get(schematic + "-no-access-item", ItemStack.class);

                inventory.setItem(slot, schematicItem);
            }
        }

        islandCreationPage.openInventory(superiorPlayer, inventory);
    }

    public void openBiomesPanel(SuperiorPlayer superiorPlayer){
        if(Bukkit.isPrimaryThread()){
            new SuperiorThread(() -> openBiomesPanel(superiorPlayer)).start();
            return;
        }

        Inventory inventory = biomesPage.clonedInventory();

        for(Biome biome : Biome.values()){
            String biomeName = biome.name().toLowerCase();
            if(biomesPage.contains(biomeName + "-has-access-item")) {
                ItemStack biomeItem = biomesPage.get(biomeName + "-has-access-item", ItemStack.class);
                String permission = biomesPage.get(biomeName + "-permission", String.class);
                int slot = biomesPage.get(biomeName + "-slot", Integer.class);

                if(!superiorPlayer.hasPermission(permission))
                    biomeItem = biomesPage.get(biomeName + "-no-access-item", ItemStack.class);

                inventory.setItem(slot, biomeItem);
            }
        }

        biomesPage.openInventory(superiorPlayer, inventory);
    }

    public void openWarpsPanel(SuperiorPlayer superiorPlayer, int page) {
        openWarpsPanel(superiorPlayer, getIsland(superiorPlayer), page);
    }

    public Island getIsland(SuperiorPlayer superiorPlayer){
        return plugin.getGrid().getIsland(SSuperiorPlayer.of(islands.get(superiorPlayer.getUniqueId())));
    }

    public void openWarpsPanel(SuperiorPlayer superiorPlayer, Island island, int page) {
        if (Bukkit.isPrimaryThread()) {
            new SuperiorThread(
                    () -> openWarpsPanel(superiorPlayer, island, page)).start();
            return;
        }

        Inventory inventory = warpsPage.clonedInventory();
        List<String> warps = new ArrayList<>(island.getAllWarps());

        warps.sort(String::compareTo);

        //noinspection unchecked
        List<Integer> slots = warpsPage.get("slots", List.class);

        ItemStack warpItem = warpsPage.get("warpItem", ItemStack.class);

        for(int i = 0; i < slots.size() && (i + (slots.size() * (page - 1))) < warps.size(); i++){
            String warpName = warps.get(i + (slots.size() * (page - 1)));
            inventory.setItem(slots.get(i), new ItemBuilder(warpItem)
                    .replaceAll("{0}", warpName)
                    .replaceAll("{1}", SBlockPosition.of(island.getWarpLocation(warpName)).toString()).build());
        }

        int previousSlot = warpsPage.get("previousSlot", Integer.class);
        ItemStack previousButton = warpsPage.get("previousButton", ItemStack.class);
        inventory.setItem(previousSlot, new ItemBuilder(previousButton)
                .replaceName("{0}", (page == 1 ? "&c" : "&a")).build());

        int currentSlot = warpsPage.get("currentSlot", Integer.class);
        ItemStack currentButton = warpsPage.get("currentButton", ItemStack.class);
        inventory.setItem(currentSlot, new ItemBuilder(currentButton)
                .replaceLore("{0}", page + "").build());

        int nextSlot = warpsPage.get("nextSlot", Integer.class);
        ItemStack nextButton = warpsPage.get("nextButton", ItemStack.class);
        inventory.setItem(nextSlot, new ItemBuilder(nextButton)
                .replaceName("{0}", (warps.size() > page * slots.size() ? "&a" : "&c")).build());

        warpsPage.openInventory(superiorPlayer, inventory);
        islands.put(superiorPlayer.getUniqueId(), island.getOwner().getUniqueId());
    }

    public void openValuesPanel(SuperiorPlayer superiorPlayer, Island island){
        Inventory valuesPageInventory = valuesPage.clonedInventory();
        Inventory inventory = Bukkit.createInventory(null, valuesPageInventory.getSize(),
                valuesPageInventory.getTitle().replace("{0}", island.getOwner().getName())
                        .replace("{1}", island.getWorthAsBigDecimal().toString()));
        inventory.setContents(valuesPageInventory.getContents());

        new SuperiorThread(() -> {
            //noinspection unchecked
            KeyMap<Integer> countedBlocks = (KeyMap<Integer>) valuesPage.get("countedBlocks", KeyMap.class);

            for(Key key : countedBlocks.keySet()){
                String[] sections = key.toString().split(":");
                ItemStack itemStack = new ItemStack(Material.valueOf(sections[0]));
                int slot = countedBlocks.get(key);

                String typeName = StringUtil.format(sections[0]);
                int amount = island.getBlockCount(SKey.of(itemStack));

                if(sections.length == 2) {
                    if(itemStack.getType() == Materials.SPAWNER.toBukkitType()) {
                        EntityType entityType = EntityType.valueOf(sections[1]);
                        amount = island.getBlockCount(SKey.of(Materials.SPAWNER.toBukkitType() + ":" + entityType));
                        itemStack = HeadUtil.getEntityHead(entityType);
                        typeName = StringUtil.format(sections[1]) + " Spawner";
                    }
                    else {
                        itemStack.setDurability(Short.valueOf(sections[1]));
                        amount = island.getBlockCount(SKey.of(itemStack));
                    }
                }

                String blockName = valuesPage.get("blockName", String.class);
                //noinspection unchecked
                List<String> blockLore = (List<String>) valuesPage.get("blockLore", List.class);

                itemStack = new ItemBuilder(itemStack).withName(blockName).withLore(blockLore)
                        .replaceAll("{0}", typeName).replaceAll("{1}", String.valueOf(amount)).build();

                if(amount == 0)
                    amount = 1;
                else if(amount > 64)
                    amount = 64;

                itemStack.setAmount(amount);

                inventory.setItem(slot, itemStack);
            }

            valuesPage.openInventory(superiorPlayer, inventory);
        }).start();
    }

    public void closeInventory(SuperiorPlayer superiorPlayer){
        GUIInventory.from(superiorPlayer).closeInventory(superiorPlayer);
        islands.remove(superiorPlayer.getUniqueId());
    }

}
