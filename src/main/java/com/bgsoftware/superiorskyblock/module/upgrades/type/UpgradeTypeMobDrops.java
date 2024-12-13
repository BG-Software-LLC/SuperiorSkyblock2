package com.bgsoftware.superiorskyblock.module.upgrades.type;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.commands.ISuperiorCommand;
import com.bgsoftware.superiorskyblock.module.upgrades.commands.CmdAdminAddMobDrops;
import com.bgsoftware.superiorskyblock.module.upgrades.commands.CmdAdminSetMobDrops;
import com.bgsoftware.superiorskyblock.world.BukkitEntities;
import org.bukkit.Bukkit;
import org.bukkit.entity.ArmorStand;
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

    private static final List<ISuperiorCommand> commands = Arrays.asList(new CmdAdminAddMobDrops(),
            new CmdAdminSetMobDrops());

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
        return commands;
    }

    private class MobDropsListener implements Listener {

        // Priority is set to HIGH for fixing detection with WildStacker
        // https://github.com/BG-Software-LLC/SuperiorSkyblock2/issues/540
        @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
        public void onLastDamageEntity(EntityDamageEvent e) {
            if (!(e.getEntity() instanceof LivingEntity))
                return;

            LivingEntity livingEntity = (LivingEntity) e.getEntity();

            if (!(livingEntity instanceof ArmorStand) && livingEntity.getHealth() - e.getFinalDamage() > 0)
                return;

            Island island = plugin.getGrid().getIslandAt(livingEntity.getLocation());

            if (island == null)
                return;

            BukkitEntities.cacheEntityEquipment(livingEntity);
        }

        @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
        public void onEntityDeath(EntityDeathEvent e) {
            Island island = plugin.getGrid().getIslandAt(e.getEntity().getLocation());

            if (island == null)
                return;

            if (e.getEntity() instanceof Player)
                return;

            if (plugin.getSettings().isDropsUpgradePlayersMultiply()) {
                EntityDamageEvent lastDamage = e.getEntity().getLastDamageCause();
                if (!(lastDamage instanceof EntityDamageByEntityEvent) ||
                        !BukkitEntities.getPlayerSource(((EntityDamageByEntityEvent) lastDamage).getDamager()).isPresent())
                    return;
            }

            BukkitEntities.clearEntityEquipment(e.getEntity());

            double mobDropsMultiplier = island.getMobDropsMultiplier();
            if (mobDropsMultiplier <= 1)
                return;

            List<ItemStack> dropsToAdd = isWildStackerInstalled ? null : new LinkedList<>();

            for (ItemStack itemStack : e.getDrops()) {
                if (itemStack != null && !BukkitEntities.isEquipment(e.getEntity(), itemStack)) {
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
                e.getDrops().addAll(dropsToAdd);
        }

    }

}
