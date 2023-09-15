package com.bgsoftware.superiorskyblock.core.serialization.impl;

import com.bgsoftware.common.annotations.NotNull;
import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.serialization.ISerializer;
import com.bgsoftware.superiorskyblock.core.serialization.Serializers;
import com.bgsoftware.superiorskyblock.tag.CompoundTag;
import com.bgsoftware.superiorskyblock.tag.ListTag;
import com.bgsoftware.superiorskyblock.tag.Tag;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class InventorySerializer implements ISerializer<ItemStack[], byte[]> {

    private static final ItemStack[] EMPTY_CONTENTS = new ItemStack[0];
    private static final byte[] EMPTY_SERIALIZED_DATA = new byte[0];

    private static final InventorySerializer INSTANCE = new InventorySerializer();

    public static InventorySerializer getInstance() {
        return INSTANCE;
    }

    private InventorySerializer() {
    }

    @NotNull
    @Override
    public byte[] serialize(@Nullable ItemStack[] serializable) {
        if (serializable == null || serializable.length == 0)
            return EMPTY_SERIALIZED_DATA;

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutput = new DataOutputStream(outputStream);

        CompoundTag compoundTag = new CompoundTag();

        List<ItemStack> serializedItems = new ArrayList<>(serializable.length);
        byte[] slots = new byte[serializable.length * 2];

        for (int i = 0; i < serializable.length; ++i) {
            ItemStack itemStack = serializable[i];
            int itemIndex = i * 2;

            if (itemStack == null || itemStack.getType() == Material.AIR) {
                slots[itemIndex] = -1;
                slots[itemIndex + 1] = 0;
            } else {
                ItemStack serializedItem = getSerializedItem(itemStack);
                int similarItemIndex = serializedItems.indexOf(serializedItem);
                if (similarItemIndex == -1) {
                    slots[itemIndex] = (byte) serializedItems.size();
                    serializedItems.add(serializedItem);
                } else {
                    slots[itemIndex] = (byte) similarItemIndex;
                }

                slots[itemIndex + 1] = (byte) itemStack.getAmount();
            }
        }

        ListTag items = new ListTag(CompoundTag.class, Collections.emptyList());
        for (ItemStack itemStack : serializedItems)
            items.addTag(Serializers.ITEM_STACK_TO_TAG_SERIALIZER.serialize(itemStack));

        compoundTag.setTag("Items", items);
        compoundTag.setByteArray("Slots", slots);

        try {
            compoundTag.write(dataOutput);
        } catch (Exception error) {
            Log.entering("ENTER", Arrays.asList(serializable));
            Log.error(error, "An unexpected error occurred while serializing inventory:");
            return EMPTY_SERIALIZED_DATA;
        }

        return outputStream.toByteArray();
    }

    @Override
    public ItemStack[] deserialize(@Nullable byte[] deserializable) {
        if (deserializable == null || deserializable.length == 0)
            return EMPTY_CONTENTS;

        return deserialize(deserializable, true);
    }

    private static ItemStack[] deserialize(byte[] deserializable, boolean tryAgain) {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(deserializable);
        CompoundTag compoundTag;

        try {
            compoundTag = (CompoundTag) Tag.fromStream(new DataInputStream(inputStream), 0);
        } catch (Exception error) {
            if (tryAgain)
                return deserialize(new BigInteger(new String(deserializable), 32).toByteArray(), false);

            Log.entering("ENTER", new String(deserializable));
            Log.error(error, "An unexpected error occurred while deserializing inventory:");
            return EMPTY_CONTENTS;
        }

        ItemStack[] contents;

        if (compoundTag.containsKey("Length")) {
            contents = new ItemStack[compoundTag.getInt("Length")];

            for (int i = 0; i < contents.length; i++) {
                CompoundTag itemCompound = compoundTag.getCompound(i + "");
                if (itemCompound != null)
                    contents[i] = Serializers.ITEM_STACK_TO_TAG_SERIALIZER.deserialize(itemCompound);
            }
        } else {
            byte[] slots = compoundTag.getByteArray("Slots");
            ListTag items = compoundTag.getList("Items");
            ItemStack[] serializedItems = new ItemStack[items.size()];

            contents = new ItemStack[slots.length / 2];

            for (int i = 0; i < slots.length; i += 2) {
                int itemIndex = slots[i];

                if (itemIndex == -1 || itemIndex >= serializedItems.length)
                    continue;

                int itemAmount = slots[i + 1];

                if (serializedItems[itemIndex] == null) {
                    Tag<?> itemTag = items.getValue().get(itemIndex);
                    if (itemTag instanceof CompoundTag) {
                        serializedItems[itemIndex] = Serializers.ITEM_STACK_TO_TAG_SERIALIZER.deserialize((CompoundTag) itemTag);
                    } else {
                        serializedItems[itemIndex] = new ItemStack(Material.AIR);
                    }
                }

                ItemStack contentItem = serializedItems[itemIndex].clone();
                contentItem.setAmount(itemAmount);

                contents[i / 2] = contentItem;
            }

        }

        return contents;
    }

    private static ItemStack getSerializedItem(ItemStack itemStack) {
        ItemStack serializedItem = itemStack.clone();
        serializedItem.setAmount(1);
        return serializedItem;
    }

}
