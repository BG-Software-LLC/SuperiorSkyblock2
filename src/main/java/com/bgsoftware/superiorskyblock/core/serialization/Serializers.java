package com.bgsoftware.superiorskyblock.core.serialization;

import com.bgsoftware.superiorskyblock.api.wrappers.BlockOffset;
import com.bgsoftware.superiorskyblock.core.serialization.impl.InventorySerializer;
import com.bgsoftware.superiorskyblock.core.serialization.impl.ItemStack2TagSerializer;
import com.bgsoftware.superiorskyblock.core.serialization.impl.ItemStackSerializer;
import com.bgsoftware.superiorskyblock.core.serialization.impl.LocationSerializer;
import com.bgsoftware.superiorskyblock.core.serialization.impl.OffsetSerializer;
import com.bgsoftware.superiorskyblock.tag.CompoundTag;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

public class Serializers {

    public static final ISerializer<ItemStack[], byte[]> INVENTORY_SERIALIZER = InventorySerializer.getInstance();
    public static final ISerializer<ItemStack, String> ITEM_STACK_SERIALIZER = ItemStackSerializer.getInstance();
    public static final ISerializer<ItemStack, CompoundTag> ITEM_STACK_TO_TAG_SERIALIZER = ItemStack2TagSerializer.getInstance();
    public static final ISerializer<Location, String> LOCATION_SPACED_SERIALIZER = new LocationSerializer(", ");
    public static final ISerializer<Location, String> LOCATION_SERIALIZER = new LocationSerializer(",");
    public static final ISerializer<BlockOffset, String> OFFSET_SPACED_SERIALIZER = new OffsetSerializer(", ");
    public static final ISerializer<BlockOffset, String> OFFSET_SERIALIZER = new OffsetSerializer(",");

    private Serializers() {

    }

}
