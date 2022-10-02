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


import com.bgsoftware.superiorskyblock.core.logging.Log;
import com.google.common.base.Preconditions;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Locale;

/**
 * The <code>TAG_Byte_Array</code> tag.
 *
 * @author Graham Edgecombe
 */
@SuppressWarnings("WeakerAccess")
public class ByteArrayTag extends Tag<byte[]> {

    /*package*/ static final Class<?> CLASS = getNNTClass("NBTTagByteArray");

    /**
     * Creates the tag.
     *
     * @param value The value.
     */
    public ByteArrayTag(byte[] value) {
        super(value, CLASS, byte[].class);
    }

    public static ByteArrayTag fromNBT(Object tag) {
        Preconditions.checkArgument(tag.getClass().equals(CLASS), "Cannot convert " + tag.getClass() + " to ByteArrayTag!");

        try {
            byte[] value = plugin.getNMSTags().getNBTByteArrayValue(tag);
            return new ByteArrayTag(value);
        } catch (Exception error) {
            Log.error(error, "An unexpected error occurred while converting tag byte-array from NMS:");
            return null;
        }
    }

    public static ByteArrayTag fromStream(DataInputStream is) throws IOException {
        int length = is.readInt();
        byte[] bytes = new byte[length];
        is.readFully(bytes);
        return new ByteArrayTag(bytes);
    }

    @Override
    public String toString() {
        StringBuilder hex = new StringBuilder();
        for (byte b : value) {
            String hexDigits = Integer.toHexString(b).toUpperCase(Locale.ENGLISH);
            if (hexDigits.length() == 1) {
                hex.append("0");
            }
            hex.append(hexDigits).append(" ");
        }
        return "TAG_Byte_Array: " + hex;
    }

    @Override
    protected void writeData(DataOutputStream os) throws IOException {
        os.writeInt(value.length);
        os.write(value);
    }

}