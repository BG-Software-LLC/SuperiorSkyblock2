package com.bgsoftware.superiorskyblock.core.schematic;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.core.ServerVersion;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.tag.CompoundTag;
import com.bgsoftware.superiorskyblock.tag.ListTag;
import com.bgsoftware.superiorskyblock.tag.StringTag;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.inventory.InventoryType;

import java.util.Collections;

public class SchematicBlock {

    private static final ListTag EMPTY_LIST_TAG = ListTag.of(Collections.emptyList());

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    private static final String SIGN_ID = ServerVersion.isLegacy() ? "Sign" : "minecraft:sign";
    private static final String CHEST_ID = ServerVersion.isLegacy() ? "Chest" : "minecraft:chest";

    private final Location location;
    private final int blockId;
    @Nullable
    private final Extra extra;
    @Nullable
    private CompoundTag tileEntityData = null;

    public SchematicBlock(Location location, int blockId, @Nullable Extra extra) {
        this.location = location;
        this.blockId = blockId;
        this.extra = extra;
    }

    public int getX() {
        return location.getBlockX();
    }

    public int getY() {
        return location.getBlockY();
    }

    public int getZ() {
        return location.getBlockZ();
    }

    public World getWorld() {
        return location.getWorld();
    }

    public int getCombinedId() {
        return this.blockId;
    }

    @Nullable
    public CompoundTag getStatesTag() {
        return this.extra == null ? null : this.extra.statesTag;
    }

    @Nullable
    public CompoundTag getOriginalTileEntity() {
        CompoundTag tileEntity = this.extra == null ? null : this.extra.tileEntity;
        return tileEntity == null ? null : tileEntity.copy();
    }

    @Nullable
    public CompoundTag getTileEntityData() {
        return this.tileEntityData;
    }

    public void doPrePlace(Island island) {
        CompoundTag originalTileEntity = getOriginalTileEntity();

        if (originalTileEntity == null)
            return;


        this.tileEntityData = CompoundTag.fromNBT(originalTileEntity.toNBT());
        String id = this.tileEntityData.getString("id").orElse(null);

        if (id == null) {
            Log.warn("Weird tile-entity data detected: " + this.tileEntityData.getValue());
            throw new RuntimeException("Detected tile-entity data with no 'id' key.");
        }

        if (id.equalsIgnoreCase(SIGN_ID)) {
            if (this.tileEntityData.containsKey("front_text")) {
                backFrontSignLinesReplace(this.tileEntityData, island);
            } else {
                legacySignLinesReplace(this.tileEntityData, island);
            }
        } else if (id.equalsIgnoreCase(CHEST_ID)) {
            if (plugin.getSettings().getDefaultContainers().isEnabled()) {
                String inventoryType = this.tileEntityData.getString("inventoryType").orElse(null);
                if (inventoryType != null) {
                    try {
                        InventoryType containerType = InventoryType.valueOf(inventoryType);
                        ListTag items = plugin.getSettings().getDefaultContainers().getContents(containerType);
                        if (items != null)
                            this.tileEntityData.setTag("Items", items.copy());
                    } catch (Exception ignored) {
                    }
                }
            }
        }
    }

    public boolean shouldPostPlace() {
        if (this.tileEntityData == null)
            return false;

        String id = this.tileEntityData.getString("id").orElse(null);
        return id != null && id.equalsIgnoreCase(SIGN_ID);
    }

    public void doPostPlace(Island island) {
        try {
            plugin.getNMSWorld().placeSign(island, location);
        } finally {
            this.tileEntityData = null;
        }
    }

    private static void backFrontSignLinesReplace(CompoundTag tileEntityData, Island island) {
        CompoundTag frontText = tileEntityData.getCompound("front_text").orElse(null);
        CompoundTag backText = tileEntityData.getCompound("back_text").orElse(null);

        if (frontText == null || backText == null) {
            // This should never occur
            Log.error("Invalid sign tile entity data: ", tileEntityData);
        }

        ListTag frontTextMessages = frontText.getList("messages").orElse(EMPTY_LIST_TAG);
        ListTag backTextMessages = backText.getList("messages").orElse(EMPTY_LIST_TAG);
        ListTag newFrontTextMessages = ListTag.of(StringTag.class);
        ListTag newBackTextMessages = ListTag.of(StringTag.class);

        for (int i = 0; i < 8; ++i) {
            ListTag messages = i < 4 ? frontTextMessages : backTextMessages;
            ListTag newMessages = i < 4 ? newFrontTextMessages : newBackTextMessages;

            int realIndex = i % 4;

            String line;
            if (i < plugin.getSettings().getDefaultSign().size()) {
                line = plugin.getSettings().getDefaultSign().get(i);
            } else {
                line = ((StringTag) messages.getValue().get(realIndex)).getValue();
            }

            line = line.replace("{player}", island.getOwner().getName())
                    .replace("{island}", island.getName().isEmpty() ? island.getOwner().getName() : island.getName());

            newMessages.addTag(StringTag.of(line));
        }

        frontText.setTag("messages", newFrontTextMessages);
        backText.setTag("messages", newBackTextMessages);
    }

    private static void legacySignLinesReplace(CompoundTag tileEntityData, Island island) {
        boolean needSignFormat = false;

        for (int i = 1; i <= 4; i++) {
            boolean isDefaultSignLine = false;
            String line;

            if ((i - 1) >= plugin.getSettings().getDefaultSign().size()) {
                line = tileEntityData.getString("Text" + i).orElse(null);
            } else {
                line = plugin.getSettings().getDefaultSign().get(i - 1);
                if (ServerVersion.isAtLeast(ServerVersion.v1_17)) {
                    isDefaultSignLine = true;
                    needSignFormat = true;
                }
            }

            if (line != null) {
                tileEntityData.setString((isDefaultSignLine ? "SSB.Text" : "Text") + i, line
                        .replace("{player}", island.getOwner().getName())
                        .replace("{island}", island.getName().isEmpty() ? island.getOwner().getName() : island.getName())
                );
            }
        }

        if (needSignFormat)
            tileEntityData.setByte("SSB.HasSignLines", (byte) 1);
    }

    public static class Extra {

        @Nullable
        private final CompoundTag statesTag;
        @Nullable
        private final CompoundTag tileEntity;

        public Extra(@Nullable CompoundTag statesTag, @Nullable CompoundTag tileEntity) {
            this.statesTag = statesTag;
            this.tileEntity = tileEntity;
        }

        public CompoundTag getTileEntity() {
            return tileEntity;
        }

        public CompoundTag getStatesTag() {
            return statesTag;
        }

    }

}
