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
import com.bgsoftware.common.annotations.Nullable;
import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.google.common.base.Preconditions;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.RandomAccess;

/**
 * The <code>TAG_List</code> tag.
 *
 * @author Graham Edgecombe
 */
@SuppressWarnings("rawtypes")
public class ListTag extends Tag<List<Tag<?>>> implements Iterable<Tag<?>> {

    private static final byte DYNAMIC_LIST_INDICATOR = (byte) 0xFF;
    /*package*/ static final Class<?> CLASS = getNNTClass("NBTTagList");

    /**
     * Hint to the type of elements in the list.
     */
    @Nullable
    private Class<? extends Tag> hintType;

    /**
     * Creates the tag.
     *
     * @param hintType Hint for the type of all items in the list.
     * @param value    The value.
     */
    public ListTag(@Nullable Class<? extends Tag> hintType, List<Tag<?>> value) {
        this(value, hintType, true);
    }

    private ListTag(List<Tag<?>> value, @Nullable Class<? extends Tag> hintType, boolean cloneList) {
        super(cloneListIfNeeded(value, cloneList), null);
        this.hintType = hintType;
    }

    public ListTag copy() {
        return new ListTag(this.hintType, this.value);
    }

    public void addTag(Tag<?> tag) {
        this.value.add(tag);
        // In case the type of the tag is not similar to the hintType, we reset it.
        if (this.hintType != null && tag.getClass() != this.hintType) {
            this.hintType = null;
        }
    }

    public static ListTag fromNBT(Object tag) {
        Preconditions.checkArgument(tag.getClass().equals(CLASS), "Cannot convert " + tag.getClass() + " to ListTag!");

        List<Tag<?>> list = new LinkedList<>();

        try {
            int size = plugin.getNMSTags().getNBTTagListSize(tag);
            Class<? extends Tag> lastTagType = null;
            boolean isSimilarType = true;

            for (int i = 0; i < size; i++) {
                Tag<?> currTag = Tag.fromNBT(plugin.getNMSTags().getNBTListIndexValue(tag, i));
                if (isSimilarType) {
                    if (lastTagType != null && currTag.getClass() != lastTagType) {
                        isSimilarType = false;
                    } else {
                        lastTagType = currTag.getClass();
                    }
                }
                list.add(currTag);
            }

            return new ListTag(list, !isSimilarType || size == 0 ? null : lastTagType, false);
        } catch (Exception error) {
            Log.error(error, "An unexpected error occurred while converting tag list from NMS:");
            return null;
        }
    }

    public static ListTag fromStream(DataInputStream is, int depth) throws IOException {
        int childType = is.readByte();
        return childType == DYNAMIC_LIST_INDICATOR ?
                fromStreamWithoutHint(is, depth) : fromStreamWithHint(is, depth, childType);
    }

    private static ListTag fromStreamWithHint(DataInputStream is, int depth, int childType) throws IOException {
        int length = is.readInt();

        List<Tag<?>> tagList = new LinkedList<>();

        for (int i = 0; i < length; i++) {
            Tag<?> tag = Tag.fromStream(is, depth + 1, childType);
            if (tag instanceof EndTag) {
                throw new IOException("TAG_End not permitted in a list.");
            }
            tagList.add(tag);
        }

        return new ListTag(tagList, NBTUtils.getTypeClass(childType), false);
    }

    private static ListTag fromStreamWithoutHint(DataInputStream is, int depth) throws IOException {
        int length = is.readInt();

        List<Tag<?>> tagList = new LinkedList<>();

        for (int i = 0; i < length; i++) {
            Tag<?> tag = Tag.fromStream(is, depth + 1);
            if (tag instanceof EndTag) {
                throw new IOException("TAG_End not permitted in a list.");
            }
            tagList.add(tag);
        }

        return new ListTag(tagList, null, false);
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
        bldr.append("TAG_List: ").append(value.size()).append(" entries");
        if (this.hintType != null) {
            bldr.append(" of type ").append(NBTUtils.getTypeName(hintType));
        }
        bldr.append("\r\n{\r\n");
        for (Tag<?> t : value) {
            bldr.append("   ").append(t.toString().replaceAll("\r\n", "\r\n   ")).append("\r\n");
        }
        bldr.append("}");
        return bldr.toString();
    }

    @Override
    protected void writeData(DataOutputStream os) throws IOException {
        if (this.hintType != null) {
            writeDataWithHint(os);
        } else {
            writeDataWithoutHint(os);
        }
    }

    private void writeDataWithHint(DataOutputStream os) throws IOException {
        int size = value.size();
        os.writeByte(NBTUtils.getTypeCode(this.hintType));
        os.writeInt(size);
        for (Tag<?> _tag : value)
            _tag.writeData(os);
    }

    private void writeDataWithoutHint(DataOutputStream os) throws IOException {
        int size = value.size();
        os.writeByte(DYNAMIC_LIST_INDICATOR);
        os.writeInt(size);
        for (Tag<?> _tag : value)
            _tag.write(os);
    }

    @Override
    public Object toNBT() {
        return plugin.getNMSTags().parseList(this);
    }

    private static <T> List<T> cloneListIfNeeded(List<T> list, boolean cloneList) {
        return cloneList || list instanceof RandomAccess ? new LinkedList<>(list) : list;
    }

}