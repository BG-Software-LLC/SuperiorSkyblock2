package com.bgsoftware.superiorskyblock.module.upgrades.type;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.commands.ISuperiorCommand;
import com.bgsoftware.superiorskyblock.core.ObjectsPools;
import com.bgsoftware.superiorskyblock.module.upgrades.commands.CmdAdminAddMobDrops;
import com.bgsoftware.superiorskyblock.module.upgrades.commands.CmdAdminSetMobDrops;
import com.bgsoftware.superiorskyblock.world.BukkitEntities;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class UpgradeTypeMobDrops implements IUpgradeType {

    private final SuperiorSkyblockPlugin plugin;
    private final boolean isWildStackerInstalled;

    public UpgradeTypeMobDrops(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
        isWildStackerInstalled = Bukkit.getPluginManager().isPluginEnabled("WildStacker");
    }

    @Override
    public List<Listener> getListeners() {
        return Collections.singletonList(new MobDropsListener());
    }

    @Override
    public List<ISuperiorCommand> getCommands() {
        return Arrays.asList(new CmdAdminAddMobDrops(), new CmdAdminSetMobDrops());
    }

    private static boolean canDupeDropsForEntity(Entity entity) {
        return entity instanceof LivingEntity && !(entity instanceof Player) && !(entity instanceof ArmorStand);
    }

    private class MobDropsListener implements Listener {

        // Priority is set to HIGH for fixing detection with WildStacker
        // https://github.com/BG-Software-LLC/SuperiorSkyblock2/issues/540
        @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
        public void onLastDamageEntity(EntityDamageEvent e) {
            if (!canDupeDropsForEntity(e.getEntity()))
                return;

            LivingEntity livingEntity = (LivingEntity) e.getEntity();

            if (livingEntity.getHealth() - e.getFinalDamage() > 0)
                return;

            Island island;
            try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
                island = plugin.getGrid().getIslandAt(livingEntity.getLocation(wrapper.getHandle()));
            }
            if (island == null)
                return;

            BukkitEntities.cacheEntityEquipment(livingEntity);
        }

        @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
        public void onEntityDeath(EntityDeathEvent e) {
            if (!canDupeDropsForEntity(e.getEntity()))
                return;

            Island island;
            try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
                island = plugin.getGrid().getIslandAt(e.getEntity().getLocation(wrapper.getHandle()));
            }
            if (island == null)
                return;

            if (plugin.getSettings().isDropsUpgradePlayersMultiply()) {
                EntityDamageEvent lastDamage = e.getEntity().getLastDamageCause();
                if (!(lastDamage instanceof EntityDamageByEntityEvent) ||
                        !BukkitEntities.getPlayerSource(((EntityDamageByEntityEvent) lastDamage).getDamager()).isPresent())
                    return;
            }

            double mobDropsMultiplier = island.getMobDropsMultiplier();
            if (mobDropsMultiplier > 1)
                modifyEventDrops(e.getDrops(), e.getEntity(), mobDropsMultiplier);

            BukkitEntities.clearEntityEquipment(e.getEntity());
        }

        private void modifyEventDrops(List<ItemStack> drops, LivingEntity livingEntity, double mobDropsMultiplier) {
            List<ItemStack> dropsToAdd = isWildStackerInstalled ? null : new LinkedList<>();

            for (ItemStack itemStack : drops) {
                if (itemStack != null && !BukkitEntities.isEquipment(livingEntity, itemStack)) {
                    int newAmount = (int) Math.floor(itemStack.getAmount() * mobDropsMultiplier);

                    if (isWildStackerInstalled) {
                        itemStack.setAmount(newAmount);
                    } else {
                        int stackAmounts = newAmount / itemStack.getMaxStackSize();
                        int leftOvers = newAmount % itemStack.getMaxStackSize();
                        boolean usedOriginal = false;

                        if (stackAmounts > 0) {
                            itemStack.setAmount(itemStack.getMaxStackSize());
                            usedOriginal = true;

                            ItemStack stackItem = itemStack.clone();
                            stackItem.setAmount(itemStack.getMaxStackSize());

                            for (int i = 0; i < stackAmounts - 1; i++)
                                dropsToAdd.add(itemStack.clone());
                        }

                        if (leftOvers > 0) {
                            if (usedOriginal) {
                                ItemStack leftOversItem = itemStack.clone();
                                leftOversItem.setAmount(leftOvers);
                                dropsToAdd.add(leftOversItem);
                            } else {
                                itemStack.setAmount(leftOvers);
                            }
                        }
                    }
                }
            }

            if (dropsToAdd != null)
                drops.addAll(dropsToAdd);
        }

    }

}
