/*
 * Copyright (C) 2015, 2017 wysohn.  All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation,  version 3.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.generallib.binary.utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class LittleEndianReader {
    private ByteBuffer bytebuffer;

    public LittleEndianReader(byte[] data) {
        bytebuffer = ByteBuffer.wrap(data);
        bytebuffer.order(ByteOrder.LITTLE_ENDIAN);
    }

    public byte readByte() {
        return bytebuffer.get();
    }

    public short readShort() {
        return bytebuffer.getShort();
    }

    public int readInt() {
        return bytebuffer.getInt();
    }

    public long readLong() {
        return bytebuffer.getLong();
    }

    public void putByte(byte b) {
        bytebuffer.position(bytebuffer.position() - 1);
        bytebuffer.put(b);
    }

    public void putShort(short s) {
        bytebuffer.position(bytebuffer.position() - 2);
        bytebuffer.putShort(s);
    }

    public void putInt(int i) {
        bytebuffer.position(bytebuffer.position() - 4);
        bytebuffer.putInt(i);
    }

    public void putLong(long l) {
        bytebuffer.position(bytebuffer.position() - 8);
        bytebuffer.putLong(l);
    }

    public void back(int byteNums) {
        bytebuffer.position(bytebuffer.position() - byteNums);
    }

    public void reset() {
        bytebuffer.rewind();
    }

    public int getRemaining() {
        return bytebuffer.remaining();
    }

    public byte[] toByteArray() {
        return bytebuffer.array();
    }
}
