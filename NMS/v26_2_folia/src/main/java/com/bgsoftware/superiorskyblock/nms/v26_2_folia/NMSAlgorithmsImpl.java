package com.bgsoftware.superiorskyblock.nms.v26_2_folia;

import com.bgsoftware.superiorskyblock.api.objects.Pair;
import io.papermc.paper.threadedregions.RegionizedServer;
import io.papermc.paper.threadedregions.TickRegionScheduler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BundleItem;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class NMSAlgorithmsImpl extends com.bgsoftware.superiorskyblock.nms.v26_2.NMSAlgorithmsImpl {

    @Override
    public double getCurrentTps() {
        var task = TickRegionScheduler.getCurrentTickingTask();
        var handle = task instanceof TickRegionScheduler.RegionScheduleHandle region ?
                region : RegionizedServer.getGlobalTickData();
        return handle.getTickReport1m(System.nanoTime()).tpsData().segmentAll().average();
    }

    @Override
    public Pair<Boolean, ItemStack[]> processBundleClick(Player player, ItemStack slotItem, ItemStack cursorItem, boolean rightClick) {
        if (!Bukkit.isOwnedByCurrentRegion(player))
            throw new IllegalStateException("Bundle interactions require the player's region");
        net.minecraft.world.item.ItemStack stored = CraftItemStack.asNMSCopy(slotItem);
        net.minecraft.world.item.ItemStack[] carried = {CraftItemStack.asNMSCopy(cursorItem)};
        if (!(stored.getItem() instanceof BundleItem) && !(carried[0].getItem() instanceof BundleItem))
            return null;
        ServerPlayer handle = ((CraftPlayer) player).getHandle();
        SimpleContainer container = new SimpleContainer(stored);
        Slot slot = new Slot(container, 0, 0, 0);
        SlotAccess cursor = SlotAccess.of(() -> carried[0], value -> carried[0] = value);
        ClickAction action = rightClick ? ClickAction.SECONDARY : ClickAction.PRIMARY;
        FeatureFlagSet features = handle.level().enabledFeatures();
        boolean handled = carried[0].isItemEnabled(features) && carried[0].overrideStackedOnOther(slot, action, handle);
        if (!handled && stored.isItemEnabled(features))
            handled = stored.overrideOtherStackedOnMe(carried[0], slot, action, handle, cursor);
        return new Pair<>(handled, new ItemStack[]{
                CraftItemStack.asBukkitCopy(container.getItem(0)), CraftItemStack.asBukkitCopy(carried[0])});
    }

}
