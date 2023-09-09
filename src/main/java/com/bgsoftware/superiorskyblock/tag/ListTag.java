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

import com.bgsoftware.common.annotations.NotNull;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.google.common.base.Preconditions;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * The <code>TAG_List</code> tag.
 *
 * @author Graham Edgecombe
 */
@SuppressWarnings("rawtypes")
public class ListTag extends Tag<List<Tag<?>>> implements Iterable<Tag<?>> {

    /*package*/ static final Class<?> CLASS = getNNTClass("NBTTagList");

    /**
     * The type.
     */
    private final Class<? extends Tag> type;

    /**
     * Creates the tag.
     *
     * @param type  The type of item in the list.
     * @param value The value.
     */
    public ListTag(Class<? extends Tag> type, List<Tag<?>> value) {
        super(new ArrayList<>(value), null);
        this.type = type;
    }

    public ListTag copy() {
        return new ListTag(type, value);
    }

    public static ListTag fromNBT(Object tag) {
        Preconditions.checkArgument(tag.getClass().equals(CLASS), "Cannot convert " + tag.getClass() + " to ListTag!");

        List<Tag<?>> list = new ArrayList<>();

        try {
            int size = plugin.getNMSTags().getNBTTagListSize(tag);

            for (int i = 0; i < size; i++)
                list.add(Tag.fromNBT(plugin.getNMSTags().getNBTListIndexValue(tag, i)));

            return new ListTag(size == 0 ? EndTag.class : list.get(0).getClass(), list);
        } catch (Exception error) {
            Log.error(error, "An unexpected error occurred while converting tag list from NMS:");
            return null;
        }
    }

    public static ListTag fromStream(DataInputStream is, int depth) throws IOException {
        int childType = is.readByte();
        int length = is.readInt();
        List<Tag<?>> tagList = new ArrayList<>();

        for (int i = 0; i < length; i++) {
            Tag<?> tag = fromStream(is, depth + 1, childType);
            if (tag instanceof EndTag) {
                throw new IOException("TAG_End not permitted in a list.");
            }
            tagList.add(tag);
        }

        return new ListTag(NBTUtils.getTypeClass(childType), tagList);
    }

    @Override
    public List<Tag<?>> getValue() {
        return Collections.unmodifiableList(value);
    }

    @NotNull
    @Override
    public Iterator<Tag<?>> iterator() {
        return value.iterator();
    }

    public int size() {
        return this.value.size();
    }

    @Override
    public String toString() {
        StringBuilder bldr = new StringBuilder();
        bldr.append("TAG_List: ").append(value.size()).append(" entries of type ").append(NBTUtils.getTypeName(type)).append("\r\n{\r\n");
        for (Tag<?> t : value) {
            bldr.append("   ").append(t.toString().replaceAll("\r\n", "\r\n   ")).append("\r\n");
        }
        bldr.append("}");
        return bldr.toString();
    }

    @Override
    protected void writeData(DataOutputStream os) throws IOException {
        int size = value.size();
        os.writeByte(NBTUtils.getTypeCode(type));
        os.writeInt(size);
        for (Tag<?> _tag : value)
            _tag.writeData(os);
    }

    @Override
    public Object toNBT() {
        return plugin.getNMSTags().parseList(this);
    }

    /**
     * Gets the type of item in this list.
     *
     * @return The type of item in this list.
     */
    public Class<? extends Tag> getType() {
        return type;
    }

    public void addTag(Tag<?> tag) {
        value.add(tag);
    }

}