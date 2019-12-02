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
package com.bgsoftware.superiorskyblock.utils.tags;

import com.bgsoftware.superiorskyblock.utils.reflections.ReflectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The <code>TAG_List</code> tag.
 *
 * @author Graham Edgecombe
 */
public final class ListTag extends Tag<List<Tag>> {

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
    public ListTag(Class<? extends Tag> type, List<Tag> value) {
        super(new ArrayList<>(value));
        this.type = type;
    }

    /**
     * Gets the type of item in this list.
     *
     * @return The type of item in this list.
     */
    public Class<? extends Tag> getType() {
        return type;
    }

    public void addTag(Tag tag){
        value.add(tag);
    }

    @Override
    public List<Tag> getValue() {
        return Collections.unmodifiableList(value);
    }

    @Override
    public String toString() {
        StringBuilder bldr = new StringBuilder();
        bldr.append("TAG_List: ").append(value.size()).append(" entries of type ").append(NBTUtils.getTypeName(type)).append("\r\n{\r\n");
        for (Tag t : value) {
            bldr.append("   ").append(t.toString().replaceAll("\r\n", "\r\n   ")).append("\r\n");
        }
        bldr.append("}");
        return bldr.toString();
    }

    @Override
    public Object toNBT() {
        return plugin.getNMSAdapter().parseList(this);
    }

    public static ListTag fromNBT(Object tag){
        Class nbtTagClass = ReflectionUtils.getClass("net.minecraft.server.VERSION.NBTTagList");
        if(!tag.getClass().equals(nbtTagClass))
            throw new IllegalArgumentException("Cannot convert " + tag.getClass() + " to ListTag!");

        List<Tag> list = new ArrayList<>();

        try {
            //noinspection unchecked
            int size = (int) nbtTagClass.getMethod("size").invoke(tag);

            for(int i = 0; i < size; i++)
                list.add(Tag.fromNBT(plugin.getNMSAdapter().getNBTListIndexValue(tag, i)));

            return new ListTag(size == 0 ? EndTag.class : list.get(0).getClass(), list);
        }catch(Exception ex){
            ex.printStackTrace();
            return null;
        }
    }

}