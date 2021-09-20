package com.bgsoftware.superiorskyblock.utils.logic;

import com.bgsoftware.superiorskyblock.Locale;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPrivilege;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.ServerVersion;
import com.bgsoftware.superiorskyblock.utils.islands.IslandPrivileges;
import com.bgsoftware.superiorskyblock.key.Key;
import com.bgsoftware.superiorskyblock.utils.legacy.Materials;
import com.bgsoftware.superiorskyblock.utils.threads.Executor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Animals;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Donkey;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Item;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Mule;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public final class ProtectionLogic {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private ProtectionLogic(){
    }

    public static boolean handleBlockPlace(Block block, Player player, boolean sendMessages){
        Island island = plugin.getGrid().getIslandAt(block.getLocation());
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(player);

        if(island == null) {
            if(!superiorPlayer.hasBypassModeEnabled() && plugin.getGrid().isIslandsWorld(superiorPlayer.getWorld())){
                if(sendMessages)
                    Locale.BUILD_OUTSIDE_ISLAND.send(superiorPlayer);
                return false;
            }

            return true;
        }

        if(!island.hasPermission(superiorPlayer, IslandPrivileges.BUILD)){
            if(sendMessages)
                Locale.sendProtectionMessage(superiorPlayer);
            return false;
        }

        if(!island.isInsideRange(block.getLocation())){
            if(sendMessages)
                Locale.BUILD_OUTSIDE_ISLAND.send(superiorPlayer);
            return false;
        }

        return true;
    }

    public static boolean handleBlockBreak(Block block, Player player, boolean sendMessages){
        Island island = plugin.getGrid().getIslandAt(block.getLocation());
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(player);

        if(island == null) {
            if(!superiorPlayer.hasBypassModeEnabled() && plugin.getGrid().isIslandsWorld(player.getWorld())) {
                if(sendMessages)
                    Locale.DESTROY_OUTSIDE_ISLAND.send(superiorPlayer);
                return false;
            }

            return true;
        }

        Material blockType = block.getType();

        IslandPrivilege islandPermission = blockType == Materials.SPAWNER.toBukkitType() ?
                IslandPrivileges.SPAWNER_BREAK : IslandPrivileges.BREAK;

        if(!island.hasPermission(superiorPlayer, islandPermission)){
            if(sendMessages)
                Locale.sendProtectionMessage(player);
            return false;
        }

        if(plugin.getSettings().getValuableBlocks().contains(Key.of(block)) &&
                !island.hasPermission(superiorPlayer, IslandPrivileges.VALUABLE_BREAK)){
            if(sendMessages)
                Locale.sendProtectionMessage(player);
            return false;
        }

        if(!island.isInsideRange(block.getLocation())){
            if(sendMessages)
                Locale.DESTROY_OUTSIDE_ISLAND.send(superiorPlayer);
            return false;
        }

        return true;
    }

    public static void handleEntityInteract(PlayerInteractEntityEvent e){
        if(e.getRightClicked() instanceof Painting || e.getRightClicked() instanceof ItemFrame)
            return;

        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getPlayer());
        Island island = plugin.getGrid().getIslandAt(e.getRightClicked().getLocation());
        ItemStack usedItem = e.getPlayer().getItemInHand();

        boolean closeInventory = false;

        IslandPrivilege islandPrivilege;

        if(e.getRightClicked() instanceof ArmorStand){
            islandPrivilege = IslandPrivileges.INTERACT;
        }
        else if(usedItem != null && e.getRightClicked() instanceof Animals &&
                plugin.getNMSEntities().isAnimalFood(usedItem, (Animals) e.getRightClicked())){
            islandPrivilege = IslandPrivileges.ANIMAL_BREED;
        }
        else if(usedItem != null && usedItem.getType() == Material.NAME_TAG){
            islandPrivilege = IslandPrivileges.NAME_ENTITY;
        }
        else if(e.getRightClicked() instanceof Villager){
            islandPrivilege = IslandPrivileges.VILLAGER_TRADING;
            closeInventory = true;
        }
        else if(e.getRightClicked() instanceof Horse || (ServerVersion.isAtLeast(ServerVersion.v1_11) && (
                e.getRightClicked() instanceof Mule || e.getRightClicked() instanceof Donkey))){
            islandPrivilege = IslandPrivileges.HORSE_INTERACT;
            closeInventory = true;
        }
        else return;

        if(island != null && !island.hasPermission(superiorPlayer, islandPrivilege)){
            e.setCancelled(true);
            Locale.sendProtectionMessage(superiorPlayer);
            if(closeInventory) {
                Executor.sync(() -> {
                    Inventory openInventory = e.getPlayer().getOpenInventory().getTopInventory();
                    if(openInventory != null && (openInventory.getType() == InventoryType.MERCHANT ||
                            openInventory.getType() == InventoryType.CHEST))
                        e.getPlayer().closeInventory();
                }, 1L);
            }
        }
    }

    public static boolean handleItemFrameRotate(Player player, ItemFrame itemFrame){
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(player);
        Island island = plugin.getGrid().getIslandAt(itemFrame.getLocation());

        if(island == null) {
            if(!superiorPlayer.hasBypassModeEnabled() && plugin.getGrid().isIslandsWorld(player.getWorld())) {
                Locale.INTERACT_OUTSIDE_ISLAND.send(superiorPlayer);
                return false;
            }

            return true;
        }

        if(!island.hasPermission(superiorPlayer, IslandPrivileges.ITEM_FRAME)){
            Locale.sendProtectionMessage(player);
            return false;
        }

        if(!island.isInsideRange(itemFrame.getLocation())){
            Locale.INTERACT_OUTSIDE_ISLAND.send(superiorPlayer);
            return false;
        }

        return true;
    }

    public static boolean handleItemFrameBreak(SuperiorPlayer superiorPlayer, ItemFrame itemFrame){
        Island island = plugin.getGrid().getIslandAt(itemFrame.getLocation());

        if(island == null) {
            if(!superiorPlayer.hasBypassModeEnabled() && plugin.getGrid().isIslandsWorld(superiorPlayer.getWorld())) {
                Locale.INTERACT_OUTSIDE_ISLAND.send(superiorPlayer);
                return false;
            }

            return true;
        }

        if(!island.hasPermission(superiorPlayer, IslandPrivileges.ITEM_FRAME)){
            Locale.sendProtectionMessage(superiorPlayer);
            return false;
        }

        if(!island.isInsideRange(itemFrame.getLocation())){
            Locale.INTERACT_OUTSIDE_ISLAND.send(superiorPlayer);
            return false;
        }

        return true;
    }

    public static boolean handlePlayerPickupItem(Player player, Item item){
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(player);
        Island island = plugin.getGrid().getIslandAt(item.getLocation());

        if(island != null && !plugin.getNMSPlayers().wasThrownByPlayer(item, player) &&
                !island.hasPermission(superiorPlayer, IslandPrivileges.PICKUP_DROPS)){
            Locale.sendProtectionMessage(superiorPlayer);
            return false;
        }

        return true;
    }

}
