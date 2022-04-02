package com.bgsoftware.superiorskyblock.nms.v1_18_R1.dragon;

import com.bgsoftware.superiorskyblock.nms.v1_18_R1.mapping.level.WorldServer;
import net.minecraft.world.level.dimension.end.EnderDragonBattle;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class EndWorldEnderDragonBattleHandler extends EnderDragonBattle {

    private final Map<UUID, EnderDragonBattle> worldDragonBattlesMap = new HashMap<>();
    private final List<EnderDragonBattle> worldDragonBattlesList = new LinkedList<>();

    public EndWorldEnderDragonBattleHandler(WorldServer worldServer) {
        super(worldServer.getHandle(), worldServer.getSeed(), new net.minecraft.nbt.NBTTagCompound());
    }

    @Override
    public void b() {
        this.worldDragonBattlesList.forEach(EnderDragonBattle::b);
    }

    public void addDragonBattle(UUID uuid, EnderDragonBattle enderDragonBattle) {
        EnderDragonBattle oldBattle = this.worldDragonBattlesMap.put(uuid, enderDragonBattle);
        if (oldBattle != null)
            this.worldDragonBattlesList.remove(oldBattle);
        this.worldDragonBattlesList.add(enderDragonBattle);
    }

    @Nullable
    public EnderDragonBattle removeDragonBattle(UUID uuid) {
        EnderDragonBattle enderDragonBattle = this.worldDragonBattlesMap.remove(uuid);
        if (enderDragonBattle != null)
            this.worldDragonBattlesList.remove(enderDragonBattle);
        return enderDragonBattle;
    }

}
