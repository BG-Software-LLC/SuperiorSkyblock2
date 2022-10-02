//@formatter:off

/*
 * JNBT License
 *
 * Copyright (c) 2010 Graham Edgecombe
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *
 *     * Neither the name of the JNBT team nor the names of its
 *       contributors may be used to endorse or promote products derived from
 *       this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

//@formatter:on
package com.bgsoftware.superiorskyblock.tag;

import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.google.common.base.Preconditions;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;

/**
 * The <code>TAG_Byte_Array</code> tag.
 *
 * @author Jocopa3
 */
@SuppressWarnings("WeakerAccess")
public class IntArrayTag extends Tag<int[]> {

    /*package*/ static final Class<?> CLASS = getNNTClass("NBTTagIntArray");

    /**
     * Creates the tag.
     *
     * @param value The value.
     */
    public IntArrayTag(int[] value) {
        super(value, CLASS, int[].class);
    }

    public static IntArrayTag fromNBT(Object tag) {
        Preconditions.checkArgument(tag.getClass().equals(CLASS), "Cannot convert " + tag.getClass() + " to IntArrayTag!");

        try {
            int[] value = plugin.getNMSTags().getNBTIntArrayValue(tag);
            return new IntArrayTag(value);
        } catch (Exception error) {
            Log.error(error, "An unexpected error occurred while converting tag int-array from NMS:");
            return null;
        }
    }

    public static IntArrayTag fromUUID(UUID uuid) {
        long MSB = uuid.getMostSignificantBits();
        long LSB = uuid.getLeastSignificantBits();
        return new IntArrayTag(new int[]{(int) (MSB >> 32), (int) MSB, (int) (LSB >> 32), (int) LSB});
    }

    public static IntArrayTag fromStream(DataInputStream is) throws IOException {
        int length = is.readInt();
        int[] data = new int[length];
        for (int i = 0; i < length; i++) {
            data[i] = is.readInt();
        }
        return new IntArrayTag(data);
    }

    @Override
    public String toString() {
        StringBuilder integers = new StringBuilder();
        for (int b : value) {
            integers.append(b).append(" ");
        }
        return "TAG_Int_Array: " + integers.toString();
    }

    @Override
    protected void writeData(DataOutputStream os) throws IOException {
        os.writeInt(value.length);
        for (int i : value)
            os.writeInt(i);
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        int prime = 31;
        int result = super.hashCode();
        result = (prime * result) + Arrays.hashCode(value);
        return result;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object obj) {
        return this == obj || (obj instanceof IntArrayTag && Arrays.equals(value, ((IntArrayTag) obj).value));
    }

}