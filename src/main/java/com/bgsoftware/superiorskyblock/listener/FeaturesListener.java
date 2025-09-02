package com.bgsoftware.superiorskyblock.listener;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.common.reflection.ReflectField;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.island.IslandPreview;
import com.bgsoftware.superiorskyblock.api.missions.Mission;
import com.bgsoftware.superiorskyblock.api.service.region.InteractionResult;
import com.bgsoftware.superiorskyblock.api.service.region.RegionManagerService;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.core.EnumHelper;
import com.bgsoftware.superiorskyblock.core.LazyReference;
import com.bgsoftware.superiorskyblock.core.ObjectsPools;
import com.bgsoftware.superiorskyblock.core.PlayerHand;
import com.bgsoftware.superiorskyblock.core.collections.EnumerateMap;
import com.bgsoftware.superiorskyblock.core.events.args.PluginEventArgs;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEvent;
import com.bgsoftware.superiorskyblock.core.events.plugin.PluginEventType;
import com.bgsoftware.superiorskyblock.core.key.ConstantKeys;
import com.bgsoftware.superiorskyblock.core.messages.Message;
import com.bgsoftware.superiorskyblock.core.threads.BukkitExecutor;
import com.bgsoftware.superiorskyblock.island.privilege.IslandPrivileges;
import com.bgsoftware.superiorskyblock.platform.event.GameEvent;
import com.bgsoftware.superiorskyblock.platform.event.GameEventPriority;
import com.bgsoftware.superiorskyblock.platform.event.GameEventType;
import com.bgsoftware.superiorskyblock.platform.event.args.GameEventArgs;
import com.bgsoftware.superiorskyblock.service.region.ProtectionHelper;
import com.bgsoftware.superiorskyblock.world.BukkitItems;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public class FeaturesListener extends AbstractGameEventListener {

    private static final EnumerateMap<PluginEventType<?>, PlaceholdersPopulator<?>> POPULATORS = new EnumerateMap<>(PluginEventType.values());

    @Nullable
    private static final Material VAULT = EnumHelper.getEnum(Material.class, "VAULT");
    @Nullable
    private static final Material TRIAL_SPAWNER = EnumHelper.getEnum(Material.class, "TRIAL_SPAWNER");
    @Nullable
    private static final Material SCULK_SENSOR = EnumHelper.getEnum(Material.class, "SCULK_SENSOR");
    @Nullable
    private static final Material SCULK_SHRIEKER = EnumHelper.getEnum(Material.class, "SCULK_SHRIEKER");

    private final LazyReference<RegionManagerService> protectionManager = new LazyReference<RegionManagerService>() {
        @Override
        protected RegionManagerService create() {
            return plugin.getServices().getService(RegionManagerService.class);
        }
    };

    public FeaturesListener(SuperiorSkyblockPlugin plugin) {
        super(plugin);
        registerListeners();
    }

    /* EVENT COMMANDS */

    private void onGeneralPluginEvent(PluginEvent<?> event) {
        List<String> commands = plugin.getSettings().getEventCommands().get(event.getType().getBukkitEventName());

        if (commands == null)
            return;

        Map<String, String> placeholdersReplaces = new HashMap<>();

        PlaceholdersPopulator placeholdersPopulator = POPULATORS.computeIfAbsent(event.getType(),
                type -> createPlaceholdersPopulator(event.getArgs().getClass()));
        if (placeholdersPopulator != null)
            placeholdersPopulator.populate(event.getArgs(), placeholdersReplaces);

        BukkitExecutor.ensureMain(() -> {
            for (String command : commands) {
                for (Map.Entry<String, String> replaceEntry : placeholdersReplaces.entrySet())
                    command = command.replace(replaceEntry.getKey(), replaceEntry.getValue());
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            }
        });
    }

    /* OBSIDIAN TO LAVA */

    private void onObsidianClick(GameEvent<GameEventArgs.PlayerInteractEvent> e) {
        Player player = e.getArgs().player;
        ItemStack handItem = e.getArgs().usedItem;
        Block clickedBlock = e.getArgs().clickedBlock;

        if (handItem == null || clickedBlock == null || handItem.getType() != Material.BUCKET ||
                clickedBlock.getType() != Material.OBSIDIAN)
            return;

        if (!plugin.getGrid().isIslandsWorld(clickedBlock.getWorld()))
            return;

        if (plugin.getStackedBlocks().getStackedBlockAmount(clickedBlock) != 1)
            return;

        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(player);
        InteractionResult interactionResult = this.protectionManager.get().handleBlockBreak(superiorPlayer, clickedBlock);
        if (ProtectionHelper.shouldPreventInteraction(interactionResult, superiorPlayer, true))
            return;

        Island island;
        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            island = plugin.getGrid().getIslandAt(clickedBlock.getLocation(wrapper.getHandle()));
        }
        if (island == null)
            return;

        e.setCancelled();


        ItemStack inHandItem = handItem.clone();
        inHandItem.setAmount(inHandItem.getAmount() - 1);

        PlayerHand usedHand = e.getArgs().usedHand;
        BukkitItems.setHandItem(player, usedHand, inHandItem.getAmount() == 0 ? null : inHandItem);

        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            BukkitItems.addItem(new ItemStack(Material.LAVA_BUCKET), player.getInventory(),
                    player.getLocation(wrapper.getHandle()));
        }

        island.handleBlockBreak(ConstantKeys.OBSIDIAN, 1);

        clickedBlock.setType(Material.AIR);
    }

    /* VISITORS BLOCKED COMMANDS */

    private void onPlayerCommandAsVisitor(GameEvent<GameEventArgs.PlayerCommandEvent> e) {
        Player player = e.getArgs().player;
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(player);

        if (superiorPlayer.hasBypassModeEnabled())
            return;

        Island island;
        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            island = plugin.getGrid().getIslandAt(player.getLocation(wrapper.getHandle()));
        }

        if (island == null || island.isSpawn() || !island.isVisitor(superiorPlayer, true))
            return;

        String[] message = e.getArgs().command.toLowerCase(Locale.ENGLISH).split(" ");

        String commandLabel = message[0].toCharArray()[0] == '/' ? message[0].substring(1) : message[0];

        if (plugin.getSettings().getBlockedVisitorsCommands().stream().anyMatch(commandLabel::contains)) {
            e.setCancelled();
            Message.VISITOR_BLOCK_COMMAND.send(superiorPlayer);
        }
    }

    /* PREVIEW BLOCKED COMMANDS */

    private void onPlayerCommandWhilePreview(GameEvent<GameEventArgs.PlayerCommandEvent> e) {
        Player player = e.getArgs().player;
        SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(player);

        if (superiorPlayer.hasBypassModeEnabled())
            return;

        IslandPreview islandPreview = plugin.getGrid().getIslandPreview(superiorPlayer);
        if (islandPreview == null)
            return;

        String[] message = e.getArgs().command.toLowerCase(Locale.ENGLISH).split(" ");

        String commandLabel = message[0].toCharArray()[0] == '/' ? message[0].substring(1) : message[0];

        if (plugin.getSettings().getIslandPreviews().getBlockedCommands().stream().anyMatch(commandLabel::contains)) {
            e.setCancelled();
            Message.ISLAND_PREVIEW_BLOCK_COMMAND.send(superiorPlayer);
        }
    }

    /* BLOCKS TRACKING */

    private void onChunkLoad(GameEvent<GameEventArgs.ChunkLoadEvent> e) {
        List<Island> chunkIslands = plugin.getGrid().getIslandsAt(e.getArgs().chunk);
        chunkIslands.forEach(island -> handleIslandChunkLoad(island, e.getArgs().chunk));
    }

    private void handleIslandChunkLoad(Island island, Chunk chunk) {
        List<Location> blockEntities = plugin.getNMSChunks().getBlockEntities(chunk);

        if (blockEntities.isEmpty())
            return;

        blockEntities.forEach(blockEntity -> {
            plugin.getNMSWorld().replaceSculkSensorListener(island, blockEntity);
            plugin.getNMSWorld().replaceTrialBlockPlayerDetector(island, blockEntity);
        });
    }

    /* VAULTS & TRIAL SPAWNERS */

    private void onTrialBlockPlace(GameEvent<GameEventArgs.BlockPlaceEvent> e) {
        Block block = e.getArgs().block;
        Material blockType = block.getType();

        if (blockType != VAULT && blockType != TRIAL_SPAWNER)
            return;

        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            Location blockLocation = block.getLocation(wrapper.getHandle());

            Island island = plugin.getGrid().getIslandAt(blockLocation);

            if (island == null)
                return;

            plugin.getNMSWorld().replaceTrialBlockPlayerDetector(island, blockLocation);
        }
    }

    /* SCULK SENSORS */

    private void onSculkSensorPlace(GameEvent<GameEventArgs.BlockPlaceEvent> e) {
        Block block = e.getArgs().block;
        Material blockType = block.getType();

        if (blockType != SCULK_SENSOR)
            return;

        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            Location blockLocation = block.getLocation(wrapper.getHandle());

            Island island = plugin.getGrid().getIslandAt(blockLocation);

            if (island == null)
                return;

            plugin.getNMSWorld().replaceSculkSensorListener(island, blockLocation);
        }
    }

    private void onSculkShriekerInteract(GameEvent<GameEventArgs.PlayerInteractEvent> e) {
        Action action = e.getArgs().action;
        Block clickedBlock = e.getArgs().clickedBlock;

        if (action != Action.PHYSICAL || !plugin.getGrid().isIslandsWorld(clickedBlock.getWorld()) ||
                clickedBlock.getType() != SCULK_SHRIEKER)
            return;

        InteractionResult interactionResult;
        try (ObjectsPools.Wrapper<Location> wrapper = ObjectsPools.LOCATION.obtain()) {
            SuperiorPlayer superiorPlayer = plugin.getPlayers().getSuperiorPlayer(e.getArgs().player);
            Location blockLocation = clickedBlock.getLocation(wrapper.getHandle());
            interactionResult = protectionManager.get().handleCustomInteraction(superiorPlayer,
                    blockLocation, IslandPrivileges.SCULK_SENSOR);
        }
        if (ProtectionHelper.shouldPreventInteraction(interactionResult, null, false))
            e.setCancelled();
    }

    /* INTERNAL */

    private void registerListeners() {
        for (PluginEventType<?> eventType : PluginEventType.values()) {
            String bukkitEventName = eventType.getBukkitEventName();
            if (bukkitEventName != null && plugin.getSettings().getEventCommands().containsKey(bukkitEventName))
                plugin.getPluginEventsDispatcher().registerCallback(eventType, this::onGeneralPluginEvent);
        }

        if (plugin.getSettings().isObsidianToLava())
            registerCallback(GameEventType.PLAYER_INTERACT_EVENT, GameEventPriority.HIGHEST, this::onObsidianClick);
        if (!plugin.getSettings().getBlockedVisitorsCommands().isEmpty())
            registerCallback(GameEventType.PLAYER_COMMAND_EVENT, GameEventPriority.HIGHEST, this::onPlayerCommandAsVisitor);
        if (!plugin.getSettings().getIslandPreviews().getBlockedCommands().isEmpty())
            registerCallback(GameEventType.PLAYER_COMMAND_EVENT, GameEventPriority.HIGHEST, this::onPlayerCommandWhilePreview);

        registerCallback(GameEventType.CHUNK_LOAD_EVENT, GameEventPriority.MONITOR, this::onChunkLoad);

        if (VAULT != null && TRIAL_SPAWNER != null) {
            registerCallback(GameEventType.BLOCK_PLACE_EVENT, GameEventPriority.MONITOR, this::onTrialBlockPlace);
        }

        if (SCULK_SENSOR != null && SCULK_SHRIEKER != null) {
            registerCallback(GameEventType.BLOCK_PLACE_EVENT, GameEventPriority.MONITOR, this::onSculkSensorPlace);
            registerCallback(GameEventType.PLAYER_INTERACT_EVENT, GameEventPriority.MONITOR, this::onSculkShriekerInteract);
        }
    }

    private static <Args extends PluginEventArgs> PlaceholdersPopulator<Args> createPlaceholdersPopulator(Class<?> argsClass) {
        List<FieldData<?>> fieldsData = new LinkedList<>();

        collectAllFields(argsClass, field -> {
            ReflectField<?> reflectField = new ReflectField<>(field.getDeclaringClass(), field.getType(), field.getName());
            if (reflectField.isValid()) {
                String placeholder = "%" + field.getName() + "%";
                FieldData<?> fieldData = createFieldData(reflectField, field, placeholder);
                fieldsData.add(fieldData);
                if (field.getName().equals("superiorPlayer"))
                    fieldsData.add(new FieldData(fieldData.reflectField, "%player%", fieldData.toStringMethod));
                else if (field.getName().equals("targetPlayer"))
                    fieldsData.add(new FieldData(fieldData.reflectField, "%target%", fieldData.toStringMethod));
            }
        });

        return (args, placeholders) -> {
            for (FieldData fieldData : fieldsData) {
                Optional.ofNullable(fieldData.reflectField.get(args)).ifPresent(value ->
                        placeholders.put(fieldData.placeholder, (String) fieldData.toStringMethod.apply(value)));
            }
        };

    }

    private static FieldData<?> createFieldData(ReflectField<?> reflectField, Field field, String placeholder) {
        if (field.getType() == SuperiorPlayer.class)
            return new FieldData<>((ReflectField<SuperiorPlayer>) reflectField, placeholder, SuperiorPlayer::getName);
        else if (field.getType() == Island.class)
            return new FieldData<>((ReflectField<Island>) reflectField, placeholder, Island::getName);
        else if (field.getType() == Mission.class)
            return new FieldData<>((ReflectField<Mission>) reflectField, placeholder, Mission::getName);
        else if (field.getType() == String.class)
            return new FieldData<>((ReflectField<String>) reflectField, placeholder, s -> s);
        else
            return new FieldData<>(reflectField, placeholder, String::valueOf);
    }

    private static void collectAllFields(Class<?> clazz, Consumer<Field> consumer) {
        while (clazz != null && clazz != Object.class) {
            for (Field field : clazz.getFields())
                consumer.accept(field);
            clazz = clazz.getSuperclass();
        }
    }

    private interface PlaceholdersPopulator<T> {

        void populate(T args, Map<String, String> placeholders);

    }

    private static class FieldData<T> {

        private final ReflectField<T> reflectField;
        private final Function<T, String> toStringMethod;
        private final String placeholder;

        FieldData(ReflectField<T> reflectField, String placeholder, Function<T, String> toStringMethod) {
            this.reflectField = reflectField;
            this.toStringMethod = toStringMethod;
            this.placeholder = placeholder;
        }

    }

}
