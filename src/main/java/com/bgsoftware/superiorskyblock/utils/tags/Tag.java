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

import com.bgsoftware.superiorskyblock.SuperiorSkyblockPlugin;
import com.bgsoftware.superiorskyblock.utils.reflections.ReflectionUtils;

/**
 * Represents a single NBT tag.
 *
 * @author Graham Edgecombe
 *
 */
public abstract class Tag<E> {

    protected static final SuperiorSkyblockPlugin plugin = SuperiorSkyblockPlugin.getPlugin();

    static final Class<?> CLASS;

    static {
        CLASS = ReflectionUtils.getClass("net.minecraft.server.VERSION.NBTBase");
    }

    /**
     * The value.
     */
    protected final E value;

    /**
     * Creates the tag.
     *
     * @param value The value.
     */
    public Tag(E value) {
        this.value = value;
    }

    /**
     * Gets the value of this tag.
     *
     * @return The value of this tag.
     */
    public E getValue(){
        return value;
    }

    public abstract Object toNBT();

    public static Tag<?> fromNBT(Object tag){
        if(tag.getClass().equals(ByteArrayTag.CLASS))
            return ByteArrayTag.fromNBT(tag);
        else if(tag.getClass().equals(ByteTag.CLASS))
            return ByteTag.fromNBT(tag);
        else if(tag.getClass().equals(CompoundTag.CLASS))
            return CompoundTag.fromNBT(tag);
        else if(tag.getClass().equals(DoubleTag.CLASS))
            return DoubleTag.fromNBT(tag);
        else if(tag.getClass().equals(EndTag.CLASS))
            return new EndTag();
        else if(tag.getClass().equals(FloatTag.CLASS))
            return FloatTag.fromNBT(tag);
        else if(tag.getClass().equals(IntArrayTag.CLASS))
            return IntArrayTag.fromNBT(tag);
        else if(tag.getClass().equals(IntTag.CLASS))
            return IntTag.fromNBT(tag);
        else if(tag.getClass().equals(ListTag.CLASS))
            return ListTag.fromNBT(tag);
        else if(tag.getClass().equals(LongTag.CLASS))
            return LongTag.fromNBT(tag);
        else if(tag.getClass().equals(ShortTag.CLASS))
            return ShortTag.fromNBT(tag);
        else if(tag.getClass().equals(StringTag.CLASS))
            return StringTag.fromNBT(tag);

        throw new IllegalArgumentException("Cannot convert " + tag.getClass() + " to Tag!");
    }

}