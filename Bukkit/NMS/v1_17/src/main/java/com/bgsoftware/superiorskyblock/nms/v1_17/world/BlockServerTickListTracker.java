package com.bgsoftware.superiorskyblock.nms.v1_17.world;

import com.bgsoftware.common.reflection.ClassInfo;
import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.platform.event.GameEvent;
import com.bgsoftware.superiorskyblock.platform.event.GameEventFlags;
import com.bgsoftware.superiorskyblock.platform.event.GameEventPriority;
import com.bgsoftware.superiorskyblock.platform.event.GameEventType;
import com.bgsoftware.superiorskyblock.platform.event.args.GameEventArgs;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ServerTickList;
import net.minecraft.world.level.TickNextTickData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.craftbukkit.v1_17_R1.block.CraftBlock;
import org.bukkit.craftbukkit.v1_17_R1.block.CraftBlockStates;

import java.lang.reflect.Modifier;
import java.util.List;
import java.util.function.Consumer;

public class BlockServerTickListTracker {

    private static final ReflectField<Consumer<TickNextTickData<Block>>> TICK_FUNCTION_SPIGOT =
            new ReflectField<Consumer<TickNextTickData<Block>>>(ServerTickList.class,
                    Consumer.class, Modifier.PRIVATE | Modifier.FINAL, 1).removeFinal();
    private static final ReflectField<Consumer<TickNextTickData<Block>>> TICK_FUNCTION_PAPER =
            new ReflectField<Consumer<TickNextTickData<Block>>>(
                    new ClassInfo("com.destroystokyo.paper.server.ticklist.PaperTickList", ClassInfo.PackageType.UNKNOWN),
                    Consumer.class, Modifier.PRIVATE | Modifier.FINAL, 1).removeFinal();


    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private BlockServerTickListTracker() {

    }

    public static void listenForTicks(ServerLevel serverLevel) {
        ServerTickList<Block> blockTicks = serverLevel.getBlockTicks();
        if (TICK_FUNCTION_PAPER.isValid()) {
            TICK_FUNCTION_PAPER.set(blockTicks, nextTickData -> tick(serverLevel, nextTickData));
        } else {
            TICK_FUNCTION_SPIGOT.set(blockTicks, nextTickData -> tick(serverLevel, nextTickData));
        }
    }

    private static void tick(ServerLevel serverLevel, TickNextTickData<Block> nextTickData) {
        BlockState blockState = serverLevel.getBlockState(nextTickData.pos);
        if (blockState.is(nextTickData.getType())) {
            BlockState oldState = serverLevel.getBlockState(nextTickData.pos);
            try {
                // Only capture blocks related events
                plugin.getGameEventsDispatcher().startCaptureEvents(GameEventFlags.BLOCK_EVENT | GameEventFlags.MAYBE_BLOCK_EVENT);
                blockState.tick(serverLevel, nextTickData.pos, serverLevel.random);
            } finally {
                List<GameEvent<?>> capturedEvents = plugin.getGameEventsDispatcher().stopCaptureEvents();
                // Remove BlockPhysicsEvent which we don't listen to
                capturedEvents.removeIf(gameEvent -> gameEvent.getType() == GameEventType.BLOCK_PHYSICS_EVENT);
                // We don't want to fire the BlockUpdateShapeEvent if another event was fired in the tick method.
                // This is to prevent blocks from being considered updated twice.
                if (!capturedEvents.isEmpty())
                    return;
            }
            BlockState newState = serverLevel.getBlockState(nextTickData.pos);
            if (oldState.getBlock() != newState.getBlock()) {
                // Block was changed, let's call an update
                GameEventArgs.BlockUpdateShapeEvent blockUpdateShapeEvent = new GameEventArgs.BlockUpdateShapeEvent();
                blockUpdateShapeEvent.block = CraftBlock.at(serverLevel, nextTickData.pos);
                blockUpdateShapeEvent.oldState = CraftBlockStates.getBlockState(nextTickData.pos, oldState, null);
                GameEvent<GameEventArgs.BlockUpdateShapeEvent> gameEvent = GameEventType.BLOCK_UPDATE_SHAPE_EVENT.createEvent(blockUpdateShapeEvent);
                plugin.getGameEventsDispatcher().onGameEvent(gameEvent, GameEventPriority.MONITOR);
            }
        }
    }

}
