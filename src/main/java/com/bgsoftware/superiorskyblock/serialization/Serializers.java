package com.bgsoftware.superiorskyblock.serialization;

import com.bgsoftware.superiorskyblock.api.wrappers.BlockOffset;
import com.bgsoftware.superiorskyblock.serialization.impl.InventorySerializer;
import com.bgsoftware.superiorskyblock.serialization.impl.ItemStackSerializer;
import com.bgsoftware.superiorskyblock.serialization.impl.LocationSerializer;
import com.bgsoftware.superiorskyblock.serialization.impl.OffsetSerializer;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

public final class Serializers {

    public static final ISerializer<ItemStack[], String> INVENTORY_SERIALIZER = InventorySerializer.getInstance();
    public static final ISerializer<ItemStack, String> ITEM_STACK_SERIALIZER = ItemStackSerializer.getInstance();
    public static final ISerializer<Location, String> LOCATION_SPACED_SERIALIZER = new LocationSerializer(", ");
    public static final ISerializer<Location, String> LOCATION_SERIALIZER = new LocationSerializer(",");
    public static final ISerializer<BlockOffset, String> OFFSET_SPACED_SERIALIZER = new OffsetSerializer(", ");
    public static final ISerializer<BlockOffset, String> OFFSET_SERIALIZER = new OffsetSerializer(",");

    private Serializers() {

    }

}
