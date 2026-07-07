package com.bgsoftware.superiorskyblock.nms.v1_16_R3.world;

import com.bgsoftware.common.reflection.ClassInfo;
import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.platform.event.GameEvent;
import com.bgsoftware.superiorskyblock.platform.event.GameEventPriority;
import com.bgsoftware.superiorskyblock.platform.event.GameEventType;
import com.bgsoftware.superiorskyblock.platform.event.args.GameEventArgs;
import net.minecraft.server.v1_16_R3.Block;
import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.IBlockData;
import net.minecraft.server.v1_16_R3.NextTickListEntry;
import net.minecraft.server.v1_16_R3.TickListServer;
import net.minecraft.server.v1_16_R3.WorldServer;
import org.bukkit.craftbukkit.v1_16_R3.block.CraftBlock;
import org.bukkit.craftbukkit.v1_16_R3.block.CraftBlockState;

import java.lang.reflect.Modifier;
import java.util.List;
import java.util.function.Consumer;

public class BlockTickListServerTracker {

    private static final ReflectField<Consumer<NextTickListEntry<Block>>> TICK_FUNCTION_SPIGOT =
            new ReflectField<Consumer<NextTickListEntry<Block>>>(TickListServer.class,
                    Consumer.class, Modifier.PRIVATE | Modifier.FINAL, 1).removeFinal();
    private static final ReflectField<Consumer<NextTickListEntry<Block>>> TICK_FUNCTION_PAPER =
            new ReflectField<Consumer<NextTickListEntry<Block>>>(
                    new ClassInfo("com.destroystokyo.paper.server.ticklist.PaperTickList", ClassInfo.PackageType.UNKNOWN),
                    Consumer.class, Modifier.PRIVATE | Modifier.FINAL, 1).removeFinal();


    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private BlockTickListServerTracker() {

    }

    public static void listenForTicks(WorldServer worldServer) {
        TickListServer<Block> blockTicks = worldServer.getBlockTickList();
        if (TICK_FUNCTION_PAPER.isValid()) {
            TICK_FUNCTION_PAPER.set(blockTicks, nextTickData -> tick(worldServer, nextTickData));
        } else {
            TICK_FUNCTION_SPIGOT.set(blockTicks, nextTickData -> tick(worldServer, nextTickData));
        }
    }

    private static void tick(WorldServer worldServer, NextTickListEntry<Block> nextTickData) {
        BlockPosition blockPosition = nextTickData.getPosition();
        IBlockData blockState = worldServer.getType(blockPosition);
        if (blockState.a(nextTickData.b())) {
            IBlockData oldData = worldServer.getType(blockPosition);
            try {
                plugin.getGameEventsDispatcher().startCaptureEvents();
                blockState.a(worldServer, blockPosition, worldServer.random);
            } finally {
                List<GameEvent<?>> capturedEvents = plugin.getGameEventsDispatcher().stopCaptureEvents();
                // We don't want to fire the BlockUpdateShapeEvent if another event was fired in that tick.
                // This is to prevent blocks from being considered updated twice.
                if (!capturedEvents.isEmpty())
                    return;
            }
            IBlockData newData = worldServer.getType(blockPosition);
            if (oldData.getBlock() != newData.getBlock()) {
                // Block was changed, let's call an update
                GameEventArgs.BlockUpdateShapeEvent blockUpdateShapeEvent = new GameEventArgs.BlockUpdateShapeEvent();
                blockUpdateShapeEvent.block = CraftBlock.at(worldServer, blockPosition);
                CraftBlockState oldState = CraftBlockState.getBlockState(worldServer, blockPosition, 3);
                oldState.setData(oldData);
                blockUpdateShapeEvent.oldState = oldState;
                GameEvent<GameEventArgs.BlockUpdateShapeEvent> gameEvent = GameEventType.BLOCK_UPDATE_SHAPE_EVENT.createEvent(blockUpdateShapeEvent);
                plugin.getGameEventsDispatcher().onGameEvent(gameEvent, GameEventPriority.MONITOR);
            }
        }
    }

}
