package com.bgsoftware.superiorskyblock.external;

import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.events.IslandGenerateBlockEvent;
import com.bgsoftware.superiorskyblock.api.objects.Pair;
import io.th0rgal.oraxen.items.OraxenItems;
import io.th0rgal.oraxen.mechanics.MechanicFactory;
import io.th0rgal.oraxen.mechanics.MechanicsManager;
import io.th0rgal.oraxen.mechanics.provided.gameplay.block.BlockMechanicFactory;
import io.th0rgal.oraxen.mechanics.provided.gameplay.noteblock.NoteBlockMechanicFactory;
import io.th0rgal.oraxen.mechanics.provided.gameplay.stringblock.StringBlockMechanicFactory;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class OraxenHook {

    private static final List<Pair<MechanicFactory, SetBlockModelFunction>> AVAILABLE_MECHANICS;

    static {
        List<Pair<MechanicFactory, SetBlockModelFunction>> availableMechanics = new LinkedList<>();

        try {
            Class.forName("io.th0rgal.oraxen.mechanics.provided.gameplay.block.BlockMechanicFactory");
            availableMechanics.add(new Pair<>(MechanicsManager.getMechanicFactory("block"), BlockMechanicFactory::setBlockModel));
            availableMechanics.add(new Pair<>(MechanicsManager.getMechanicFactory("noteblock"), NoteBlockMechanicFactory::setBlockModel));
            availableMechanics.add(new Pair<>(MechanicsManager.getMechanicFactory("stringblock"), StringBlockMechanicFactory::setBlockModel));
        } catch (Throwable error) {
            availableMechanics.add(new Pair<>(MechanicsManager.getMechanicFactory("block"), (block, itemId) -> {
                ReflectMethod<Void> setBlockModel = new ReflectMethod<>(
                        "io.th0rgal.oraxen.mechanics.provided.block.BlockMechanicFactory",
                        "setBlockModel", Block.class, String.class);
                if (setBlockModel.isValid())
                    setBlockModel.invoke(null, block, itemId);
            }));
            availableMechanics.add(new Pair<>(MechanicsManager.getMechanicFactory("noteblock"), (block, itemId) -> {
                ReflectMethod<Void> setBlockModel = new ReflectMethod<>(
                        "io.th0rgal.oraxen.mechanics.provided.noteblock.NoteBlockMechanicFactory",
                        "setBlockModel", Block.class, String.class);
                if (setBlockModel.isValid())
                    setBlockModel.invoke(null, block, itemId);
            }));
        }

        AVAILABLE_MECHANICS = Collections.unmodifiableList(availableMechanics);
    }

    public static void register(SuperiorSkyblockPlugin plugin) {
        Bukkit.getPluginManager().registerEvents(new GeneratorListener(), plugin);
    }

    private static class GeneratorListener implements Listener {

        @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
        public void onIslandGenerateBlock(IslandGenerateBlockEvent event) {
            if (!event.getBlock().getGlobalKey().equals("ORAXEN"))
                return;

            String itemId = event.getBlock().getSubKey().toLowerCase(Locale.ENGLISH);

            if (!OraxenItems.exists(itemId)) {
                event.setCancelled(true);
                return;
            }

            Pair<MechanicFactory, SetBlockModelFunction> mechanic = AVAILABLE_MECHANICS.stream()
                    .filter(pair -> pair.getKey() != null && !pair.getKey().isNotImplementedIn(itemId))
                    .findFirst().orElse(null);

            if (mechanic == null) {
                event.setCancelled(true);
                return;
            }

            event.setPlaceBlock(false);
            mechanic.getValue().setBlockModel(event.getLocation().getBlock(), itemId);
        }

    }

    private interface SetBlockModelFunction {

        void setBlockModel(Block block, String itemId);

    }

}
