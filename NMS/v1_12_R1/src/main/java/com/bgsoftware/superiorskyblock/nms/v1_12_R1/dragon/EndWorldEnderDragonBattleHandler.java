package com.bgsoftware.superiorskyblock.nms.v1_12_R1.dragon;

import com.bgsoftware.common.annotations.Nullable;
import net.minecraft.server.v1_12_R1.EnderDragonBattle;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import net.minecraft.server.v1_12_R1.WorldServer;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EndWorldEnderDragonBattleHandler extends EnderDragonBattle {

    private final Map<UUID, IslandEnderDragonBattle> worldDragonBattlesMap = new HashMap<>();
    private final List<IslandEnderDragonBattle> worldDragonBattlesList = new LinkedList<>();

    public EndWorldEnderDragonBattleHandler(WorldServer worldServer) {
        super(worldServer, new NBTTagCompound());
    }

    @Override
    public void b() {
        this.worldDragonBattlesList.forEach(EnderDragonBattle::b);
    }

    public void addDragonBattle(UUID uuid, IslandEnderDragonBattle enderDragonBattle) {
        IslandEnderDragonBattle oldBattle = this.worldDragonBattlesMap.put(uuid, enderDragonBattle);
        if (oldBattle != null)
            this.worldDragonBattlesList.remove(oldBattle);
        this.worldDragonBattlesList.add(enderDragonBattle);
    }

    @Nullable
    public IslandEnderDragonBattle getDragonBattle(UUID uuid) {
        return this.worldDragonBattlesMap.get(uuid);
    }

    @Nullable
    public IslandEnderDragonBattle removeDragonBattle(UUID uuid) {
        IslandEnderDragonBattle enderDragonBattle = this.worldDragonBattlesMap.remove(uuid);
        if (enderDragonBattle != null)
            this.worldDragonBattlesList.remove(enderDragonBattle);
        return enderDragonBattle;
    }

}
