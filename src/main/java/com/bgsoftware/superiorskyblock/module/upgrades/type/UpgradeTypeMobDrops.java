package com.bgsoftware.superiorskyblock.module.upgrades.type;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.commands.ISuperiorCommand;
import com.bgsoftware.superiorskyblock.module.upgrades.commands.CmdAdminAddMobDrops;
import com.bgsoftware.superiorskyblock.module.upgrades.commands.CmdAdminSetMobDrops;
import com.bgsoftware.superiorskyblock.utils.entities.EntityUtils;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class UpgradeTypeMobDrops implements IUpgradeType {

    private static final List<ISuperiorCommand> commands = Arrays.asList(new CmdAdminAddMobDrops(),
            new CmdAdminSetMobDrops());

    private final SuperiorSkyblockPlugin plugin;

    public UpgradeTypeMobDrops(SuperiorSkyblockPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public Listener getListener() {
        return new MobDropsListener();
    }

    @Override
    public List<ISuperiorCommand> getCommands() {
        return commands;
    }

    private final class MobDropsListener implements Listener {

        @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
        public void onLastDamageEntity(EntityDamageEvent e) {
            if (!(e.getEntity() instanceof LivingEntity))
                return;

            LivingEntity livingEntity = (LivingEntity) e.getEntity();

            if (!(livingEntity instanceof ArmorStand) && livingEntity.getHealth() - e.getFinalDamage() > 0)
                return;

            Island island = plugin.getGrid().getIslandAt(livingEntity.getLocation());

            if (island == null)
                return;

            EntityUtils.cacheEntityEquipment(livingEntity);
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
                        EntityUtils.getPlayerDamager((EntityDamageByEntityEvent) lastDamage) == null)
                    return;
            }

            double mobDropsMultiplier = island.getMobDropsMultiplier();

            if (mobDropsMultiplier > 1) {
                List<ItemStack> dropItems = new ArrayList<>(e.getDrops());
                for (ItemStack itemStack : dropItems) {
                    if (itemStack != null && !EntityUtils.isEquipment(e.getEntity(), itemStack) &&
                            !plugin.getNMSTags().getNBTTag(itemStack).getValue().containsKey("WildChests")) {
                        int newAmount = (int) (itemStack.getAmount() * mobDropsMultiplier);

                        if (Bukkit.getPluginManager().isPluginEnabled("WildStacker")) {
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
                                    e.getDrops().add(itemStack.clone());
                            }

                            if (leftOvers > 0) {
                                if (usedOriginal) {
                                    ItemStack leftOversItem = itemStack.clone();
                                    leftOversItem.setAmount(leftOvers);
                                    e.getDrops().add(leftOversItem);
                                } else {
                                    itemStack.setAmount(leftOvers);
                                }
                            }
                        }
                    }
                }
            }

            EntityUtils.clearEntityEquipment(e.getEntity());
        }

    }

}
