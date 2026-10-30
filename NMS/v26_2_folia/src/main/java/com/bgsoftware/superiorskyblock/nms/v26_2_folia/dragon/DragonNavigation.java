package com.bgsoftware.superiorskyblock.nms.v26_2_folia.dragon;

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.EndSpikeFeature;
import net.minecraft.world.level.pathfinder.Node;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;

import java.lang.reflect.Field;
import java.util.List;
import java.util.function.IntBinaryOperator;

public final class DragonNavigation {
    private static final Field NODES = field("nodes");
    private static final Field ADJACENCY = field("nodeAdjacency");
    private static final int[] CONNECTIONS = {
            6146, 8197, 8202, 16404, 32808, 32848, 65696, 131392, 131712, 263424, 526848, 525313,
            1581057, 3166214, 2138120, 6373424, 4358208, 12910976, 9044480, 9706496, 15216640,
            13688832, 11763712, 8257536
    };

    private DragonNavigation() {
    }

    public static void freeze(org.bukkit.entity.EnderDragon dragon, SuperiorSkyblockPlugin plugin) {
        NamespacedKey key = new NamespacedKey(plugin, "island_dragon_ai");
        if (!dragon.getPersistentDataContainer().has(key, PersistentDataType.BOOLEAN))
            dragon.getPersistentDataContainer().set(key, PersistentDataType.BOOLEAN, dragon.hasAI());
        dragon.setAI(false);
    }

    public static void restore(org.bukkit.entity.EnderDragon dragon, SuperiorSkyblockPlugin plugin) {
        NamespacedKey key = new NamespacedKey(plugin, "island_dragon_ai");
        Boolean original = dragon.getPersistentDataContainer().get(key, PersistentDataType.BOOLEAN);
        if (original != null) {
            dragon.setAI(original);
            dragon.getPersistentDataContainer().remove(key);
        }
    }

    public static Node[] create(ServerLevel level, BlockPos origin, List<EndSpikeFeature.EndSpike> spikes) {
        return create(origin, spikes, (x, z) ->
                level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new BlockPos(x, 0, z)).getY());
    }

    public static Node[] create(BlockPos origin, List<EndSpikeFeature.EndSpike> spikes, IntBinaryOperator height) {
        Node[] nodes = new Node[24];
        for (int i = 0; i < nodes.length; ++i) {
            int adjustment = i >= 12 && i < 20 ? 15 : 5;
            int count = i < 12 ? 12 : i < 20 ? 8 : 4;
            int index = i < 12 ? i : i < 20 ? i - 12 : i - 20;
            float radius = i < 12 ? 60.0F : i < 20 ? 40.0F : 20.0F;
            float angle = 2.0F * (-Mth.PI + (float) (Math.PI / count) * index);
            int x = origin.getX() + Mth.floor(radius * Mth.cos(angle));
            int z = origin.getZ() + Mth.floor(radius * Mth.sin(angle));
            int groundY = Math.max(origin.getY(), spikeGroundY(spikes, x, z, origin.getY()));
            groundY = Math.max(groundY, height.applyAsInt(x, z));
            nodes[i] = new Node(x, Math.max(origin.getY() + 9, groundY + adjustment), z);
        }
        return nodes;
    }

    private static int spikeGroundY(List<EndSpikeFeature.EndSpike> spikes, int x, int z, int islandY) {
        for (EndSpikeFeature.EndSpike spike : spikes) {
            int deltaX = x - spike.getCenterX();
            int deltaZ = z - spike.getCenterZ();
            if (deltaX * deltaX + deltaZ * deltaZ <= spike.getRadius() * spike.getRadius() + 1)
                return islandY + spike.getHeight() + 1 - 64;
        }
        return Integer.MIN_VALUE;
    }

    public static void install(EnderDragon dragon, Node[] prepared) {
        try {
            Node[] nodes = (Node[]) NODES.get(dragon);
            int[] adjacency = (int[]) ADJACENCY.get(dragon);
            for (int i = 0; i < nodes.length; ++i)
                nodes[i] = new Node(prepared[i].x, prepared[i].y, prepared[i].z);
            System.arraycopy(CONNECTIONS, 0, adjacency, 0, adjacency.length);
            for (var part : dragon.getSubEntities()) {
                part.setPos(dragon.position());
                part.xo = part.xOld = dragon.getX();
                part.yo = part.yOld = dragon.getY();
                part.zo = part.zOld = dragon.getZ();
            }
            dragon.getPhaseManager().getCurrentPhase().begin();
        } catch (IllegalAccessException error) {
            throw new IllegalStateException("Unable to initialize island dragon navigation", error);
        }
    }

    private static Field field(String name) {
        try {
            Field field = EnderDragon.class.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (ReflectiveOperationException error) {
            throw new ExceptionInInitializerError(error);
        }
    }
}
