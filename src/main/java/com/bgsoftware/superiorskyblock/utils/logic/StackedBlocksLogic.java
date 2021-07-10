package com.bgsoftware.superiorskyblock.utils.logic;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.hooks.CoreProtectHook;
import com.bgsoftware.superiorskyblock.utils.ServerVersion;
import com.bgsoftware.superiorskyblock.utils.events.EventsCaller;
import com.bgsoftware.superiorskyblock.utils.islands.IslandUtils;
import com.bgsoftware.superiorskyblock.utils.items.ItemUtils;
import com.bgsoftware.superiorskyblock.utils.key.Key;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;

import java.math.BigInteger;
import java.util.function.Consumer;

public final class StackedBlocksLogic {

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private StackedBlocksLogic(){
    }

    public static boolean canStackBlocks(Player player, ItemStack placeItem, Block againstBlock, BlockState replaceState){
        if(!plugin.getSettings().stackedBlocksEnabled)
            return false;

        if(plugin.getSettings().stackedBlocksDisabledWorlds.contains(againstBlock.getWorld().getName()))
            return false;

        if(placeItem.hasItemMeta() && (placeItem.getItemMeta().hasDisplayName() || placeItem.getItemMeta().hasLore()))
            return false;

        if(againstBlock.getType().name().equals("GLOWING_REDSTONE_ORE"))
            againstBlock.setType(Material.REDSTONE_ORE);

        //noinspection deprecation
        byte blockData = againstBlock.getData();
        Material blockType = againstBlock.getType();

        if(blockType.name().contains("PORTAL_FRAME")) {
            blockData = 0;
        }

        else if(blockType == Material.CAULDRON && placeItem.getType().name().equals("CAULDRON_ITEM")){
            blockType = Material.valueOf("CAULDRON_ITEM");
        }

        if(blockType != placeItem.getType() || blockData != placeItem.getDurability() ||
                (replaceState != null && replaceState.getType() != Material.AIR))
            return false;

        if(!plugin.getSettings().whitelistedStackedBlocks.contains(againstBlock))
            return false;

        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(player);

        if(!superiorPlayer.hasBlocksStackerEnabled() || (!superiorPlayer.hasPermission("superior.island.stacker.*") &&
                !superiorPlayer.hasPermission("superior.island.stacker." + placeItem.getType())))
            return false;

        return true;
    }

    public static boolean tryStack(Player player, ItemStack itemToDeposit, Location stackedBlock, Event event){
        return tryStack(plugin, player, !player.isSneaking() ? 1 : itemToDeposit.getAmount(), stackedBlock, depositedAmount -> {
            if(player.getGameMode() != GameMode.CREATIVE) {
                ItemStack inHand = itemToDeposit.clone();
                inHand.setAmount(depositedAmount);
                ItemUtils.removeItem(inHand, event, player);
            }
        });
    }

    public static boolean tryStack(SuperiorSkyblockPlugin plugin, Player player, int amount, Location stackedBlock, Consumer<Integer> depositedAmount){
        // When sneaking, you'll stack all the items in your hand. Otherwise, you'll stack only 1 block
        int blockAmount = plugin.getGrid().getBlockAmount(stackedBlock);
        Key blockKey = plugin.getGrid().getBlockKey(stackedBlock);
        int blockLimit = plugin.getSettings().stackedBlocksLimits.getOrDefault(blockKey, Integer.MAX_VALUE);

        if(amount + blockAmount > blockLimit){
            amount = blockLimit - blockAmount;
        }

        if(amount <= 0) {
            depositedAmount.accept(0);
            return false;
        }

        Block block = stackedBlock.getBlock();

        if(!EventsCaller.callBlockStackEvent(block, player, blockAmount, blockAmount + amount)) {
            depositedAmount.accept(0);
            return false;
        }

        Island island = plugin.getGrid().getIslandAt(stackedBlock);

        if(island != null){
            BigInteger islandBlockLimit = BigInteger.valueOf(island.getExactBlockLimit(blockKey));
            BigInteger islandBlockCount = island.getBlockCountAsBigInteger(blockKey);
            BigInteger bigAmount = BigInteger.valueOf(amount);

            //Checking for the specific provided key.
            if(islandBlockLimit.compareTo(BigInteger.valueOf(IslandUtils.NO_LIMIT.get())) > 0 &&
                    islandBlockCount.add(bigAmount).compareTo(islandBlockLimit) > 0) {
                amount = islandBlockLimit.subtract(islandBlockCount).intValue();
            }
            else{
                //Getting the global key values.
                Key globalKey = Key.of(blockKey.getGlobalKey());
                islandBlockLimit = BigInteger.valueOf(island.getExactBlockLimit(globalKey));
                islandBlockCount = island.getBlockCountAsBigInteger(globalKey);
                if(islandBlockLimit.compareTo(BigInteger.valueOf(IslandUtils.NO_LIMIT.get())) > 0 &&
                        islandBlockCount.add(bigAmount).compareTo(islandBlockLimit) > 0) {
                    amount = islandBlockLimit.subtract(islandBlockCount).intValue();
                }
            }
        }

        plugin.getGrid().setBlockAmount(block, blockAmount + amount);

        if(plugin.getGrid().hasBlockFailed()) {
            depositedAmount.accept(0);
            return false;
        }

        if(island != null){
            island.handleBlockPlace(block, amount);
        }

        CoreProtectHook.recordBlockChange(player, block, true);

        depositedAmount.accept(amount);

        return true;
    }

    public static boolean tryUnstack(Player player, Block block, SuperiorSkyblockPlugin plugin){
        int blockAmount = plugin.getGrid().getBlockAmount(block);

        if(blockAmount <= 1)
            return false;

        // When sneaking, you'll break 64 from the stack. Otherwise, 1.
        int amount = player == null || !player.isSneaking() ? 1 : 64, leftAmount;

        // Fix amount so it won't be more than the stack's amount
        amount = Math.min(amount, blockAmount);

        if(!EventsCaller.callBlockUnstackEvent(block, player, blockAmount, blockAmount - amount))
            return false;

        Island island = plugin.getGrid().getIslandAt(block.getLocation());

        plugin.getGrid().setBlockAmount(block, (leftAmount = blockAmount - amount));

        plugin.getNMSAdapter().playBreakAnimation(block);

        CoreProtectHook.recordBlockChange(player, block, false);

        if(plugin.getGrid().hasBlockFailed()) {
            if(island != null)
                island.handleBlockBreak(Key.of(block), blockAmount - 1);
            leftAmount = 0;
            amount = 1;
        }

        ItemStack blockItem = ServerVersion.isLegacy() ? block.getState().getData().toItemStack(amount) :
                new ItemStack(block.getType(), amount);

        if(blockItem.getType().name().equals("GLOWING_REDSTONE_ORE")) {
            blockItem.setType(Material.REDSTONE_ORE);
        }

        else if(ServerVersion.isLegacy() && blockItem.getType().name().equals("CAULDRON")) {
            blockItem.setType(Material.valueOf("CAULDRON_ITEM"));
        }

        if(island != null){
            island.handleBlockBreak(Key.of(blockItem), amount);
        }

        // If the amount of the stack is less than 0, it should be air.
        if(leftAmount <= 0){
            block.setType(Material.AIR);
        }

        // Dropping the item
        if(player != null && plugin.getSettings().stackedBlocksAutoPickup){
            ItemUtils.addItem(blockItem, player.getInventory(), block.getLocation());
        }
        else {
            block.getWorld().dropItemNaturally(block.getLocation(), blockItem);
        }

        return true;
    }

}
