package com.bgsoftware.superiorskyblock.nms.v26_2_folia.dragon;

import com.bgsoftware.superiorskyblock.nms.v26_2_folia.NMSDragonFightImpl;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.level.dimension.end.EnderDragonFight;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class WorldDragonFight extends EnderDragonFight {
    private final NMSDragonFightImpl provider;

    public WorldDragonFight(ServerLevel level, NMSDragonFightImpl provider) {
        super(false, true, true, Optional.empty(), 0, Optional.empty(), Optional.empty(), new ArrayList<>(), List.of());
        this.level = level;
        this.origin = BlockPos.ZERO;
        this.provider = provider;
        this.dragonEvent = new ServerBossEvent(UUID.randomUUID(), Component.translatable("entity.minecraft.ender_dragon"),
                BossEvent.BossBarColor.PINK, BossEvent.BossBarOverlay.PROGRESS);
        this.dragonEvent.setVisible(false);
    }

    @Override
    public void tick() {
    }

    @Override
    public boolean tryRespawn(BlockPos position) {
        if (position == null)
            return false;
        IslandDragonFight fight = provider.find(level.getWorld(), position);
        if (fight == null) {
            provider.restoreForCrystal(level.getWorld(), position);
            return false;
        }
        fight.requestRespawn(position);
        return true;
    }

    @Override
    public boolean tryRespawn() {
        return false;
    }

    @Override
    public void onCrystalDestroyed(EndCrystal crystal, DamageSource source) {
        IslandDragonFight fight = provider.find(level.getWorld(), crystal.blockPosition());
        if (fight != null)
            fight.crystalDestroyed(crystal, source);
    }
}
