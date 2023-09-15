package com.bgsoftware.superiorskyblock.core;

import com.bgsoftware.common.annotations.Nullable;
import org.bukkit.inventory.EquipmentSlot;

import java.util.function.Supplier;

public enum PlayerHand {

    MAIN_HAND {
        @Override
        public EquipmentSlot getEquipmentSlot() {
            return EquipmentSlot.HAND;
        }
    },
    OFF_HAND {
        @Override
        public EquipmentSlot getEquipmentSlot() {
            return EQUIPMENT_SLOT_OFF_HAND;
        }
    };

    @Nullable
    private static final EquipmentSlot EQUIPMENT_SLOT_OFF_HAND = ((Supplier<EquipmentSlot>) () -> {
        try {
            return EquipmentSlot.valueOf("OFF_HAND");
        } catch (IllegalArgumentException error) {
            return null;
        }
    }).get();

    public static PlayerHand of(EquipmentSlot equipmentSlot) {
        if (equipmentSlot == EquipmentSlot.HAND) {
            return PlayerHand.MAIN_HAND;
        } else if (equipmentSlot == EQUIPMENT_SLOT_OFF_HAND) {
            return PlayerHand.OFF_HAND;
        }

        throw new IllegalArgumentException("Cannot get PlayerHand from: " + equipmentSlot);
    }

    public abstract EquipmentSlot getEquipmentSlot();

}
