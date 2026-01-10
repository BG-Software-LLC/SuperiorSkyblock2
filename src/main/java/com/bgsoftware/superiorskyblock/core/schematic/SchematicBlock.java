package com.bgsoftware.superiorskyblock.core.schematic;

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.core.ServerVersion;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.tag.CompoundTag;
import com.bgsoftware.superiorskyblock.tag.ListTag;
import com.bgsoftware.superiorskyblock.tag.StringTag;
import com.bgsoftware.superiorskyblock.tag.Tag;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.inventory.InventoryType;

import java.util.Collections;

public class SchematicBlock {

    private static final ListTag EMPTY_LIST_TAG = ListTag.of(Collections.emptyList());
    private static final char LEGACY_COLOR_CHAR = '\u00A7';

    private static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

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

    public Location getLocation() {
        return location;
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

        if (isSignId(id)) {
            if (this.tileEntityData.containsKey("front_text")) {
                backFrontSignLinesReplace(this.tileEntityData, island);
            } else {
                legacySignLinesReplace(this.tileEntityData, island);
            }
        } else if (plugin.getSettings().getDefaultContainers().isEnabled() && isChestId(id)) {
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

    public boolean shouldPostPlace() {
        if (this.tileEntityData == null)
            return false;

        String id = this.tileEntityData.getString("id").orElse(null);
        return id != null && isSignId(id);
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
            return;
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
                line = getSignMessageLine(messages, realIndex);
            }

            line = line.replace("{player}", island.getOwner().getName())
                    .replace("{island}", island.getName().isEmpty() ? island.getOwner().getName() : island.getName());

            newMessages.addTag(StringTag.of(line));
        }

        frontText.setTag("messages", newFrontTextMessages);
        backText.setTag("messages", newBackTextMessages);
    }

    private static String getSignMessageLine(ListTag messages, int index) {
        if (index >= messages.getValue().size())
            return "";

        Tag<?> messageTag = messages.getValue().get(index);
        if (messageTag instanceof StringTag)
            return ((StringTag) messageTag).getValue();
        if (messageTag instanceof CompoundTag) {
            return toLegacyComponent((CompoundTag) messageTag);
        }

        return "";
    }

    private static String toLegacyComponent(CompoundTag compound) {
        StringBuilder builder = new StringBuilder();
        appendLegacyComponent(builder, compound);
        return builder.toString();
    }

    private static void appendLegacyComponent(StringBuilder builder, CompoundTag compound) {
        appendLegacyFormatting(builder, compound);

        String text = compound.getString("text").orElse("");
        builder.append(text);

        ListTag extra = compound.getList("extra").orElse(null);
        if (extra != null) {
            for (Tag<?> extraTag : extra.getValue()) {
                if (extraTag instanceof StringTag) {
                    builder.append(((StringTag) extraTag).getValue());
                } else if (extraTag instanceof CompoundTag) {
                    appendLegacyComponent(builder, (CompoundTag) extraTag);
                }
            }
        }
    }

    private static void appendLegacyFormatting(StringBuilder builder, CompoundTag compound) {
        String color = compound.getString("color").orElse(null);
        if (color != null) {
            String legacyColor = toLegacyColor(color);
            if (!legacyColor.isEmpty())
                builder.append(legacyColor);
        }

        if (isTrue(compound, "bold"))
            builder.append(LEGACY_COLOR_CHAR).append('l');
        if (isTrue(compound, "italic"))
            builder.append(LEGACY_COLOR_CHAR).append('o');
        if (isTrue(compound, "underlined"))
            builder.append(LEGACY_COLOR_CHAR).append('n');
        if (isTrue(compound, "strikethrough"))
            builder.append(LEGACY_COLOR_CHAR).append('m');
        if (isTrue(compound, "obfuscated"))
            builder.append(LEGACY_COLOR_CHAR).append('k');
    }

    private static boolean isTrue(CompoundTag compound, String key) {
        Number value = compound.getNumber(key).orElse(null);
        return value != null && value.intValue() != 0;
    }

    private static String toLegacyColor(String color) {
        if (color.startsWith("#") && color.length() == 7)
            return toLegacyHexColor(color);

        switch (color) {
            case "black":
                return "" + LEGACY_COLOR_CHAR + '0';
            case "dark_blue":
                return "" + LEGACY_COLOR_CHAR + '1';
            case "dark_green":
                return "" + LEGACY_COLOR_CHAR + '2';
            case "dark_aqua":
                return "" + LEGACY_COLOR_CHAR + '3';
            case "dark_red":
                return "" + LEGACY_COLOR_CHAR + '4';
            case "dark_purple":
                return "" + LEGACY_COLOR_CHAR + '5';
            case "gold":
                return "" + LEGACY_COLOR_CHAR + '6';
            case "gray":
                return "" + LEGACY_COLOR_CHAR + '7';
            case "dark_gray":
                return "" + LEGACY_COLOR_CHAR + '8';
            case "blue":
                return "" + LEGACY_COLOR_CHAR + '9';
            case "green":
                return "" + LEGACY_COLOR_CHAR + 'a';
            case "aqua":
                return "" + LEGACY_COLOR_CHAR + 'b';
            case "red":
                return "" + LEGACY_COLOR_CHAR + 'c';
            case "light_purple":
                return "" + LEGACY_COLOR_CHAR + 'd';
            case "yellow":
                return "" + LEGACY_COLOR_CHAR + 'e';
            case "white":
                return "" + LEGACY_COLOR_CHAR + 'f';
            case "reset":
                return "" + LEGACY_COLOR_CHAR + 'r';
            default:
                return "";
        }
    }

    private static String toLegacyHexColor(String color) {
        String hex = color.substring(1).toLowerCase();
        StringBuilder builder = new StringBuilder(14);
        builder.append(LEGACY_COLOR_CHAR).append('x');
        for (int i = 0; i < hex.length(); i++) {
            builder.append(LEGACY_COLOR_CHAR).append(hex.charAt(i));
        }
        return builder.toString();
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

    public SchematicBlock setLocation(Location newBlockLoc) {
        return new SchematicBlock(newBlockLoc, this.blockId, this.extra);
    }

    private static boolean isSignId(String id) {
        return id.equals("Sign") || id.equals("minecraft:sign");
    }

    private static boolean isChestId(String id) {
        return id.equals("Chest") || id.equals("minecraft:chest");
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
