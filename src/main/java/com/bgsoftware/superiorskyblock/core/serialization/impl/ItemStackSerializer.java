package com.bgsoftware.superiorskyblock.core.serialization.impl;

import com.bgsoftware.common.annotations.NotNull;
import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.core.Text;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.bgsoftware.superiorskyblock.core.serialization.ISerializer;
import com.bgsoftware.superiorskyblock.core.serialization.Serializers;
import com.bgsoftware.superiorskyblock.tag.CompoundTag;
import com.bgsoftware.superiorskyblock.tag.Tag;
import org.bukkit.inventory.ItemStack;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.math.BigInteger;

public class ItemStackSerializer implements ISerializer<ItemStack, String> {

    private static final ItemStackSerializer INSTANCE = new ItemStackSerializer();

    public static ItemStackSerializer getInstance() {
        return INSTANCE;
    }

    private ItemStackSerializer() {
    }

    @NotNull
    @Override
    public String serialize(@Nullable ItemStack serializable) {
        if (serializable == null)
            return "";

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutput = new DataOutputStream(outputStream);

        try {
            Serializers.ITEM_STACK_TO_TAG_SERIALIZER.serialize(serializable).write(dataOutput);
        } catch (Exception error) {
            Log.entering("ENTER", serializable);
            Log.error(error, "An unexpected error occurred while serializing item:");
            return "";
        }

        return new BigInteger(1, outputStream.toByteArray()).toString(32);
    }

    @Nullable
    @Override
    public ItemStack deserialize(@Nullable String deserializable) {
        if (Text.isBlank(deserializable))
            return null;

        ByteArrayInputStream inputStream = new ByteArrayInputStream(new BigInteger(deserializable, 32).toByteArray());

        try {
            CompoundTag compoundTag = (CompoundTag) Tag.fromStream(new DataInputStream(inputStream), 0);
            return Serializers.ITEM_STACK_TO_TAG_SERIALIZER.deserialize(compoundTag);
        } catch (Exception error) {
            Log.entering("ENTER", deserializable);
            Log.error(error, "An unexpected error occurred while deserializing item:");
            return null;
        }
    }
}
