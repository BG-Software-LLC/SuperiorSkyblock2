package com.bgsoftware.superiorskyblock.nms.v26_2_folia.dragon;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.dimension.end.DragonRespawnStage;
import org.bukkit.event.entity.EntityRemoveEvent;

import java.util.List;

public final class IslandDragonRespawn {
    private IslandDragonRespawn() {
    }

    public static void tick(IslandDragonFight fight, List<EndCrystal> crystals, int time) {
        ServerLevel level = fight.level;
        BlockPos beam = fight.beamPosition();
        switch (fight.respawnStage) {
            case START -> {
                crystals.forEach(crystal -> crystal.setBeamTarget(beam));
                fight.setRespawnStage(DragonRespawnStage.PREPARING_TO_SUMMON_PILLARS);
            }
            case PREPARING_TO_SUMMON_PILLARS -> {
                if (time >= 100) {
                    fight.setRespawnStage(DragonRespawnStage.SUMMONING_PILLARS);
                } else if (time == 0 || time == 50 || time == 51 || time == 52 || time >= 95) {
                    level.levelEvent(LevelEvent.ANIMATION_DRAGON_SUMMON_ROAR, beam, 0);
                }
            }
            case SUMMONING_PILLARS -> {
                if (time % 40 == 0)
                    fight.setRespawnStage(DragonRespawnStage.SUMMONING_DRAGON);
            }
            case SUMMONING_DRAGON -> {
                if (time >= 100) {
                    fight.setRespawnStage(DragonRespawnStage.END);
                    fight.resetSpikeCrystals();
                    for (EndCrystal crystal : crystals) {
                        crystal.setBeamTarget(null);
                        level.explode(crystal, crystal.getX(), crystal.getY(), crystal.getZ(),
                                6.0F, Level.ExplosionInteraction.NONE);
                        crystal.discard(EntityRemoveEvent.Cause.EXPLODE);
                    }
                } else if (time == 0) {
                    crystals.forEach(crystal -> crystal.setBeamTarget(beam));
                } else if (time >= 80 || time < 5) {
                    level.levelEvent(LevelEvent.ANIMATION_DRAGON_SUMMON_ROAR, beam, 0);
                }
            }
            case END -> {
            }
        }
    }
}
