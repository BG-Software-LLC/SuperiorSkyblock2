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
package com.ome_r.superiorskyblock.utils.jnbt;

import java.io.Closeable;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * <p>
 * This class reads <strong>NBT</strong>, or <strong>Named Binary Tag</strong>
 * streams, and produces an object graph of subclasses of the <code>Tag</code>
 * object.
 * </p>
 *
 * <p>
 * The NBT format was created by Markus Persson, and the specification may be
 * found at <a href="http://www.minecraft.net/docs/NBT.txt">
 * http://www.minecraft.net/docs/NBT.txt</a>.
 * </p>
 *
 * @author Graham Edgecombe
 *
 */
public final class NBTInputStream implements Closeable {

	/**
	 * The data input stream.
	 */
	private final DataInputStream is;

	/**
	 * Creates a new <code>NBTInputStream</code>, which will source its data
	 * from the specified input stream.
	 *
	 * @param is
	 *            The input stream.
	 * @throws IOException
	 *             if an I/O error occurs.
	 */
	public NBTInputStream(InputStream is) throws IOException {
		this.is = new DataInputStream(new GZIPInputStream(is));
	}

	/**
	 * Reads an NBT tag from the stream.
	 *
	 * @return The tag that was read.
	 * @throws IOException
	 *             if an I/O error occurs.
	 */
	public Tag readTag() throws IOException {
		return readTag(0);
	}

	/**
	 * Reads an NBT from the stream.
	 *
	 * @param depth
	 *            The depth of this tag.
	 * @return The tag that was read.
	 * @throws IOException
	 *             if an I/O error occurs.
	 */
	private Tag readTag(int depth) throws IOException {
		int type = is.readByte() & 0xFF;
		return readTagPayload(type, depth);
	}

	/**
	 * Reads the payload of a tag, given the name and type.
	 *
	 * @param type
	 *            The type.
	 * @param depth
	 *            The depth.
	 * @return The tag.
	 * @throws IOException
	 *             if an I/O error occurs.
	 */
	private Tag readTagPayload(int type, int depth) throws IOException {
		switch (type) {
			case NBTConstants.TYPE_END:
				if (depth == 0) {
					throw new IOException("TAG_End found without a TAG_Compound/TAG_List tag preceding it.");
				} else {
					return new EndTag();
				}
			case NBTConstants.TYPE_BYTE:
				return new ByteTag(is.readByte());
			case NBTConstants.TYPE_SHORT:
				return new ShortTag(is.readShort());
			case NBTConstants.TYPE_INT:
				return new IntTag(is.readInt());
			case NBTConstants.TYPE_LONG:
				return new LongTag(is.readLong());
			case NBTConstants.TYPE_FLOAT:
				return new FloatTag(is.readFloat());
			case NBTConstants.TYPE_DOUBLE:
				return new DoubleTag(is.readDouble());
			case NBTConstants.TYPE_BYTE_ARRAY:
				int length = is.readInt();
				byte[] bytes = new byte[length];
				is.readFully(bytes);
				return new ByteArrayTag(bytes);
			case NBTConstants.TYPE_STRING:
				length = is.readShort();
				bytes = new byte[length];
				is.readFully(bytes);
				return new StringTag(new String(bytes, NBTConstants.CHARSET));
			case NBTConstants.TYPE_LIST:
				int childType = is.readByte();
				length = is.readInt();

				List<Tag> tagList = new ArrayList<>();
				for (int i = 0; i < length; i++) {
					Tag tag = readTagPayload(childType, depth + 1);
					if (tag instanceof EndTag) {
						throw new IOException("TAG_End not permitted in a list.");
					}
					tagList.add(tag);
				}

				return new ListTag(NBTUtils.getTypeClass(childType), tagList);
			case NBTConstants.TYPE_COMPOUND:
				Map<String, Tag> tagMap = new HashMap<>();
				while (true) {
					Tag tag = readTag(depth + 1);
					if (tag instanceof EndTag) {
						break;
					} else {
						//Get key
						int keyLength = is.readShort() & 0xFFFF;
						byte[] keyBytes = new byte[keyLength];
						is.readFully(keyBytes);
						String key = new String(keyBytes, NBTConstants.CHARSET);
						tagMap.put(key, tag);
					}
				}

				return new CompoundTag(tagMap);
			case NBTConstants.TYPE_INT_ARRAY:
				length = is.readInt();
				int[] data = new int[length];
				for (int i = 0; i < length; i++) {
					data[i] = is.readInt();
				}
				return new IntArrayTag(data);
			default:
				throw new IOException("Invalid tag type: " + type + ".");
		}
	}

	@Override
	public void close() throws IOException {
		is.close();
	}

}