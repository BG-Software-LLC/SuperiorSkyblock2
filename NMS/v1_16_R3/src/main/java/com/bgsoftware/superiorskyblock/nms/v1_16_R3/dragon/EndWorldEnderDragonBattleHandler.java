package com.bgsoftware.superiorskyblock.nms.v1_16_R3.dragon;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.common.collections.Lists;
import com.bgsoftware.common.collections.Maps;
import net.minecraft.server.v1_16_R3.EnderDragonBattle;
import net.minecraft.server.v1_16_R3.NBTTagCompound;
import net.minecraft.server.v1_16_R3.WorldServer;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EndWorldEnderDragonBattleHandler extends EnderDragonBattle {

    private final Map<UUID, IslandEnderDragonBattle> worldDragonBattlesMap = Maps.newHashMap();
    private final List<IslandEnderDragonBattle> worldDragonBattlesList = Lists.newLinkedList();

    public EndWorldEnderDragonBattleHandler(WorldServer worldServer) {
        super(worldServer, worldServer.getSeed(), new NBTTagCompound());
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
