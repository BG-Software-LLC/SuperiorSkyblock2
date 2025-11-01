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

import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Objects;

/**
 * Represents a single NBT tag.
 *
 * @author Graham Edgecombe
 */
public abstract class Tag<E> {

    protected static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    protected final E value;

    protected Tag(E value) {
        this.value = value;
    }

    public static Tag<?> fromNBT(Object tag) {
        if (tag.getClass().equals(ByteArrayTag.TAG_CONVERTER.getNBTClass()))
            return ByteArrayTag.fromNBT(tag);
        else if (tag.getClass().equals(ByteTag.TAG_CONVERTER.getNBTClass()))
            return ByteTag.fromNBT(tag);
        else if (tag.getClass().equals(CompoundTag.TAG_CONVERTER.getNBTClass()))
            return CompoundTag.fromNBT(tag);
        else if (tag.getClass().equals(DoubleTag.TAG_CONVERTER.getNBTClass()))
            return DoubleTag.fromNBT(tag);
        else if (tag.getClass().equals(EndTag.TAG_CONVERTER.getNBTClass()))
            return EndTag.of();
        else if (tag.getClass().equals(FloatTag.TAG_CONVERTER.getNBTClass()))
            return FloatTag.fromNBT(tag);
        else if (tag.getClass().equals(IntArrayTag.TAG_CONVERTER.getNBTClass()))
            return IntArrayTag.fromNBT(tag);
        else if (tag.getClass().equals(IntTag.TAG_CONVERTER.getNBTClass()))
            return IntTag.fromNBT(tag);
        else if (tag.getClass().equals(ListTag.TAG_CONVERTER.getNBTClass()))
            return ListTag.fromNBT(tag);
        else if (tag.getClass().equals(LongTag.TAG_CONVERTER.getNBTClass()))
            return LongTag.fromNBT(tag);
        else if (tag.getClass().equals(ShortTag.TAG_CONVERTER.getNBTClass()))
            return ShortTag.fromNBT(tag);
        else if (tag.getClass().equals(StringTag.TAG_CONVERTER.getNBTClass()))
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

    @Nullable
    protected NMSTagConverter getNMSConverter() {
        return null;
    }

    public Object toNBT() {
        NMSTagConverter converter = getNMSConverter();
        if (converter == null)
            throw new UnsupportedOperationException();
        return Objects.requireNonNull(converter.toNBT(this.value));
    }

}