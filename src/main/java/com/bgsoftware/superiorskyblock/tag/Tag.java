/*
 * JNBT License

Copyright (c) 2010 Graham Edgecombe
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice,
      this list of conditions and the following disclaimer.
      
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
      
    * Neither the name of the JNBT team nor the names of its
      contributors may be used to endorse or promote products derived from
      this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE. 
 */
package com.bgsoftware.superiorskyblock.tag;

import com.bgsoftware.common.reflection.ReflectConstructor;
import com.bgsoftware.common.reflection.ReflectMethod;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.core.ServerVersion;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import org.bukkit.Bukkit;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Represents a single NBT tag.
 *
 * @author Graham Edgecombe
 */
public abstract class Tag<E> {

    protected static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    protected final ReflectMethod<Object> A;
    protected final ReflectConstructor<Object> CONSTRUCTOR;
    protected final E value;

    protected Tag(E value, Class<?> clazz, Class<?>... parameterTypes) {
        this.value = value;
        this.A = new ReflectMethod<>(clazz, clazz, "a", parameterTypes);
        this.CONSTRUCTOR = new ReflectConstructor<>(clazz, parameterTypes);
    }

    public static Tag<?> fromNBT(Object tag) {
        if (tag.getClass().equals(ByteArrayTag.CLASS))
            return ByteArrayTag.fromNBT(tag);
        else if (tag.getClass().equals(ByteTag.CLASS))
            return ByteTag.fromNBT(tag);
        else if (tag.getClass().equals(CompoundTag.CLASS))
            return CompoundTag.fromNBT(tag);
        else if (tag.getClass().equals(DoubleTag.CLASS))
            return DoubleTag.fromNBT(tag);
        else if (tag.getClass().equals(EndTag.CLASS))
            return new EndTag();
        else if (tag.getClass().equals(FloatTag.CLASS))
            return FloatTag.fromNBT(tag);
        else if (tag.getClass().equals(IntArrayTag.CLASS))
            return IntArrayTag.fromNBT(tag);
        else if (tag.getClass().equals(IntTag.CLASS))
            return IntTag.fromNBT(tag);
        else if (tag.getClass().equals(ListTag.CLASS))
            return ListTag.fromNBT(tag);
        else if (tag.getClass().equals(LongTag.CLASS))
            return LongTag.fromNBT(tag);
        else if (tag.getClass().equals(ShortTag.CLASS))
            return ShortTag.fromNBT(tag);
        else if (tag.getClass().equals(StringTag.CLASS))
            return StringTag.fromNBT(tag);

        throw new IllegalArgumentException("Cannot convert " + tag.getClass() + " to Tag!");
    }

    public static Tag<?> fromStream(DataInputStream is, int depth) throws IOException {
        int type = is.readByte() & 0xFF;
        return fromStream(is, depth, type);
    }

    protected static Tag<?> fromStream(DataInputStream is, int depth, int type) throws IOException {
        switch (type) {
            case NBTTags.TYPE_END:
                return EndTag.fromStream(is, depth);
            case NBTTags.TYPE_BYTE:
                return ByteTag.fromStream(is);
            case NBTTags.TYPE_SHORT:
                return ShortTag.fromStream(is);
            case NBTTags.TYPE_INT:
                return IntTag.fromStream(is);
            case NBTTags.TYPE_LONG:
                return LongTag.fromStream(is);
            case NBTTags.TYPE_FLOAT:
                return FloatTag.fromStream(is);
            case NBTTags.TYPE_DOUBLE:
                return DoubleTag.fromStream(is);
            case NBTTags.TYPE_BYTE_ARRAY:
                return ByteArrayTag.fromStream(is);
            case NBTTags.TYPE_STRING:
                return StringTag.fromStream(is);
            case NBTTags.TYPE_LIST:
                return ListTag.fromStream(is, depth);
            case NBTTags.TYPE_COMPOUND:
                return CompoundTag.fromStream(is, depth);
            case NBTTags.TYPE_INT_ARRAY:
                return IntArrayTag.fromStream(is);
            case NBTTags.TYPE_BIG_DECIMAL:
                return BigDecimalTag.fromStream(is);
            case NBTTags.TYPE_UUID:
                return UUIDTag.fromStream(is);
            case NBTTags.TYPE_PERSISTENT_DATA:
                return PersistentDataTagSerialized.fromStream(is);
        }

        throw new IllegalArgumentException("Invalid tag: " + type);
    }

    protected static Class<?> getNNTClass(String nbtType) {
        try {
            if (ServerVersion.isAtLeast(ServerVersion.v1_17)) {
                return Class.forName("net.minecraft.nbt." + nbtType);
            } else {
                String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
                return Class.forName("net.minecraft.server." + version + "." + nbtType);
            }
        } catch (Exception error) {
            Log.error(error, "An unexpected error while loading nbt class ", nbtType, ":");
            return null;
        }
    }

    public E getValue() {
        return value;
    }

    @Override
    public String toString() {
        return NBTUtils.getTypeName(this.getClass()) + ": " + value;
    }

    public void write(DataOutputStream os) throws IOException {
        int type = NBTUtils.getTypeCode(this.getClass());

        os.writeByte(type);

        if (type == NBTTags.TYPE_END) {
            throw new IOException("Named TAG_End not permitted.");
        }

        writeData(os);
    }

    protected abstract void writeData(DataOutputStream outputStream) throws IOException;

    public Object toNBT() {
        if (A.isValid()) {
            return A.invoke(null, value);
        } else {
            return CONSTRUCTOR.newInstance(value);
        }
    }

}