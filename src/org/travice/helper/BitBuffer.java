
package org.travice.helper;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class BitBuffer {

    private final ByteBuf buffer;

    private int writeByte;
    private int writeCount;

    private int readByte;
    private int readCount;

    public BitBuffer() {
        buffer = Unpooled.buffer();
    }

    public BitBuffer(ByteBuf buffer) {
        this.buffer = buffer;
    }

    public void writeEncoded(byte[] bytes) {
        for (byte b : bytes) {
            b -= 48;
            if (b > 40) {
                b -= 8;
            }
            write(b);
        }
    }

    public void write(int b) {
        if (writeCount == 0) {
            writeByte |= b;
            writeCount = 6;
        } else {
            int remaining = 8 - writeCount;
            writeByte <<= remaining;
            writeByte |= b >> (6 - remaining);
            buffer.writeByte(writeByte);
            writeByte = b & ((1 << (6 - remaining)) - 1);
            writeCount = 6 - remaining;
        }
    }

    public int readUnsigned(int length) {
        int result = 0;

        while (length > 0) {
            if (readCount == 0) {
                readByte = buffer.readUnsignedByte();
                readCount = 8;
            }
            if (readCount >= length) {
                result <<= length;
                result |= readByte >> (readCount - length);
                readByte &= (1 << (readCount - length)) - 1;
                readCount -= length;
                length = 0;
            } else {
                result <<= readCount;
                result |= readByte;
                length -= readCount;
                readByte = 0;
                readCount = 0;
            }
        }

        return result;
    }

    public int readSigned(int length) {
        int result = readUnsigned(length);
        int signBit = 1 << (length - 1);
        if ((result & signBit) == 0) {
            return result;
        } else {
            result &= signBit - 1;
            result += ~(signBit - 1);
            return result;
        }
    }

}
