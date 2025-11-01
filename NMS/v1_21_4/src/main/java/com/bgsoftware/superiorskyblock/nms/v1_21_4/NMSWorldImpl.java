package com.bgsoftware.superiorskyblock.nms.v1_21_4;

import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.nms.v1_21_4.NMSUtils;
import com.bgsoftware.superiorskyblock.nms.v1_21_4.trial.IslandPlayerDetector;
import com.bgsoftware.superiorskyblock.nms.v1_21_4.vibration.IslandVibrationUser;
import com.bgsoftware.superiorskyblock.world.SignType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SculkSensorBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.TrialSpawnerBlockEntity;
import net.minecraft.world.level.block.entity.trialspawner.PlayerDetector;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawner;
import net.minecraft.world.level.block.entity.vault.VaultBlockEntity;
import net.minecraft.world.level.block.entity.vault.VaultConfig;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import org.bukkit.Location;
import org.bukkit.block.data.type.HangingSign;
import org.bukkit.block.data.type.WallHangingSign;

import java.lang.reflect.Modifier;

public class NMSWorldImpl extends com.bgsoftware.superiorskyblock.nms.v1_21_4.AbstractNMSWorld {

    private static final ReflectField<VibrationSystem.User> SCULK_SENSOR_BLOCK_ENTITY_VIBRATION_USER = new ReflectField<VibrationSystem.User>(
            SculkSensorBlockEntity.class, VibrationSystem.User.class, Modifier.PRIVATE | Modifier.FINAL, 1).removeFinal();

    public NMSWorldImpl(SuperiorSkyblockPlugin plugin) {
        super(plugin);
    }

    @Override
    protected Component[] getSignBlockEntityText(SignBlockEntity signBlockEntity) {
        return signBlockEntity.getFrontText().getMessages(false);
    }

    @Override
    public void replaceSculkSensorListener(Island island, Location location) {
        SculkSensorBlockEntity sculkSensorBlockEntity = NMSUtils.getBlockEntityAt(location, SculkSensorBlockEntity.class);
        if (sculkSensorBlockEntity == null || sculkSensorBlockEntity.getVibrationUser() instanceof IslandVibrationUser)
            return;

        SCULK_SENSOR_BLOCK_ENTITY_VIBRATION_USER.set(sculkSensorBlockEntity, new IslandVibrationUser(island, sculkSensorBlockEntity));
    }

    @Override
    public SignType getSignType(Object sign) {
        if (sign instanceof HangingSign)
            return SignType.HANGING_SIGN;
        else if (sign instanceof WallHangingSign)
            return SignType.HANGING_WALL_SIGN;
        else
            return super.getSignType(sign);
    }

    @Override
    public void replaceTrialBlockPlayerDetector(Island island, Location location) {
        BlockEntity blockEntity = NMSUtils.getBlockEntityAt(location, BlockEntity.class);
        if (blockEntity == null)
            return;

        if (blockEntity instanceof VaultBlockEntity vaultBlockEntity) {
            VaultConfig vaultConfig = vaultBlockEntity.getConfig();

            PlayerDetector playerDetector = vaultConfig.playerDetector();
            if (playerDetector instanceof IslandPlayerDetector)
                return;

            VaultConfig newConfig = new VaultConfig(
                    vaultConfig.lootTable(),
                    vaultConfig.activationRange(),
                    vaultConfig.deactivationRange(),
                    vaultConfig.keyItem(),
                    vaultConfig.overrideLootTableToDisplay(),
                    IslandPlayerDetector.trialVaultPlayerDetector(island, playerDetector),
                    vaultConfig.entitySelector()
            );

            vaultBlockEntity.setConfig(newConfig);
        } else if (blockEntity instanceof TrialSpawnerBlockEntity trialSpawnerBlockEntity) {
            TrialSpawner trialSpawner = trialSpawnerBlockEntity.getTrialSpawner();
            PlayerDetector playerDetector = trialSpawner.getPlayerDetector();

            if (playerDetector instanceof IslandPlayerDetector)
                return;

            trialSpawnerBlockEntity.trialSpawner = new TrialSpawner(
                    trialSpawner.normalConfig,
                    trialSpawner.ominousConfig,
                    trialSpawner.getData(),
                    trialSpawner.getTargetCooldownLength(),
                    trialSpawner.getRequiredPlayerRange(),
                    trialSpawner.stateAccessor,
                    IslandPlayerDetector.trialSpawnerPlayerDetector(island, playerDetector),
                    trialSpawner.getEntitySelector()
            );
        }
    }

}
