/*
 * Copyright 2025 VK
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package one.nio.serial;

import one.nio.mem.DirectMemory;
import one.nio.util.Utf8;

import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static one.nio.util.JavaInternals.unsafe;

public class DataStream implements ObjectInput, ObjectOutput {
    private static final VarHandle B_HANDLE_V;
    private static final VarHandle SH_HANDLE_V;
    private static final VarHandle CH_HANDLE_V;
    private static final VarHandle I_HANDLE_V;
    private static final VarHandle L_HANDLE_V;

    static {
        B_HANDLE_V = MethodHandles.arrayElementVarHandle(byte[].class);
        SH_HANDLE_V = MethodHandles.byteArrayViewVarHandle(short[].class, ByteOrder.nativeOrder());
        CH_HANDLE_V = MethodHandles.byteArrayViewVarHandle(char[].class, ByteOrder.nativeOrder());
        I_HANDLE_V = MethodHandles.byteArrayViewVarHandle(int[].class, ByteOrder.nativeOrder());
        L_HANDLE_V = MethodHandles.byteArrayViewVarHandle(long[].class, ByteOrder.nativeOrder());
    }

    protected static final byte REF_NULL = -1;
    protected static final byte REF_RECURSIVE = -2;
    protected static final byte REF_RECURSIVE2 = -3;
    protected static final byte REF_EMBEDDED = -4;
    protected static final byte FIRST_BOOT_UID = -10;
    protected static final int INITIAL_ARRAY_CAPACITY = 400;

    protected byte[] array;
    protected long address;
    protected long limit;
    protected long offset;

    public DataStream(int capacity) {
        this(new byte[capacity], capacity);
    }

    public DataStream(byte[] array) {
        this(array, array.length);
    }

    public DataStream(long length) {
        this(null, length);
    }

    protected DataStream(byte[] array, long length) {
        this.array = array;
        this.limit = length;
        this.offset = 0;
    }

    public byte[] array() {
        return array;
    }

    public long address() {
        return address;
    }

    public int count() {
        return (int) (offset);
    }

    public void write(int b) throws IOException {
        long offset = alloc(1);
        B_HANDLE_V.set(array, (int) offset, (byte) b);
    }

    public void write(byte[] b) throws IOException {
        long offset = alloc(b.length);
        ByteBuffer buffer = ByteBuffer.wrap(array);
        buffer.put((int) offset, b, 0, b.length);
    }

    public void write(byte[] b, int off, int len) throws IOException {
        long offset = alloc(len);
        ByteBuffer buffer = ByteBuffer.wrap(array);
        buffer.put((int) offset, b, off, len);
    }

    public void writeBoolean(boolean v) throws IOException {
        long offset = alloc(1);
        B_HANDLE_V.set(array, (int) offset, v ? (byte) 1 : (byte) 0);
    }

    public void writeByte(int v) throws IOException {
        long offset = alloc(1);
        B_HANDLE_V.set(array, (int) offset, (byte) v);
    }

    public void writeShort(int v) throws IOException {
        long offset = alloc(2);
        SH_HANDLE_V.set(array, (int) offset, Short.reverseBytes((short) v));
    }

    public void writeChar(int v) throws IOException {
        long offset = alloc(2);
        CH_HANDLE_V.set(array, (int) offset, Character.reverseBytes((char) v));
    }

    public void writeInt(int v) throws IOException {
        long offset = alloc(4);
        I_HANDLE_V.set(array, (int) offset, Integer.reverseBytes(v));
    }

    public void writeLong(long v) throws IOException {
        long offset = alloc(8);
        L_HANDLE_V.set(array, (int) offset, Long.reverseBytes(v));
    }

    public void writeFloat(float v) throws IOException {
        writeInt(Float.floatToRawIntBits(v));
    }

    public void writeDouble(double v) throws IOException {
        writeLong(Double.doubleToRawLongBits(v));
    }

    public void writeBytes(String s) throws IOException {
        int length = s.length();
        long offset = alloc(length);
        for (int i = 0; i < length; i++) {
            B_HANDLE_V.set(array, offset, (byte) s.charAt(i));
        }
    }

    public void writeChars(String s) throws IOException {
        int length = s.length();
        long offset = alloc(length * 2);
        for (int i = 0; i < length; i++) {
            CH_HANDLE_V.set(array, offset, Character.reverseBytes(s.charAt(i)));
            offset += 2;
        }
    }

    public void writeUTF(String s) throws IOException {
        int utfLength = Utf8.length(s);
        if (utfLength <= 0x7fff) {
            writeShort(utfLength);
        } else {
            writeInt(utfLength | 0x80000000);
        }
        long offset = alloc(utfLength);
        Utf8.write(s, array, offset);
    }

    @SuppressWarnings("unchecked")
    public void writeObject(Object obj) throws IOException {
        if (obj == null) {
            writeByte(REF_NULL);
        } else {
            Serializer serializer = Repository.get(obj.getClass());
            if (serializer.uid < 0) {
                writeByte((byte) serializer.uid);
            } else {
                writeLong(serializer.uid);
            }
            serializer.write(obj, this);
        }
    }

    public void write(ByteBuffer src) throws IOException {
        int len = src.remaining();
        long offset = alloc(len);
//        MemorySegment scrSegment = MemorySegment.ofBuffer(src);
//        MemorySegment.copy(scrSegment, JAVA_BYTE, (src.arrayOffset() + src.position()), segment, JAVA_BYTE, offset, len);
        src.position(src.limit());
    }

    public void writeFrom(long address, int len) throws IOException {
        long offset = alloc(len);
        unsafe.copyMemory(null, address, array, offset, len);
    }

    public int read() throws IOException {
        long offset = alloc(1);
        return (int) B_HANDLE_V.get(array, offset);
    }

    public int read(byte[] b) throws IOException {
        readFully(b);
        return b.length;
    }

    public int read(byte[] b, int off, int len) throws IOException {
        readFully(b, off, len);
        return len;
    }

    public void readFully(byte[] b) throws IOException {
        long offset = alloc(b.length);
        ByteBuffer buffer = ByteBuffer.wrap(array);
        buffer.get((int) offset, b, 0, b.length);
    }

    public void readFully(byte[] b, int off, int len) throws IOException {
        long offset = alloc(len);
        ByteBuffer buffer = ByteBuffer.wrap(array);
        buffer.get((int) offset, b, off, len);
    }

    public long skip(long n) throws IOException {
        alloc((int) n);
        return n;
    }

    public int skipBytes(int n) throws IOException {
        alloc(n);
        return n;
    }

    public boolean readBoolean() throws IOException {
        long offset = alloc(1);
        return (byte) B_HANDLE_V.get(array, (int) offset) != 0;
    }

    public byte readByte() throws IOException {
        long offset = alloc(1);
        return (byte) B_HANDLE_V.get(array, (int) offset);
    }

    public int readUnsignedByte() throws IOException {
        long offset = alloc(1);
        return (byte) B_HANDLE_V.get(array, (int) offset) & 0xff;
    }

    public short readShort() throws IOException {
        long offset = alloc(2);
        return Short.reverseBytes((short) SH_HANDLE_V.get(array, (int) offset));
    }

    public int readUnsignedShort() throws IOException {
        long offset = alloc(2);
        return Short.reverseBytes((short) SH_HANDLE_V.get(array, (int) offset)) & 0xffff;
    }

    public char readChar() throws IOException {
        long offset = alloc(2);
        return Character.reverseBytes((char) CH_HANDLE_V.get(array, (int) offset));
    }

    public int readInt() throws IOException {
        long offset = alloc(4);
        return Integer.reverseBytes((int) I_HANDLE_V.get(array, (int) offset));
    }

    public long readLong() throws IOException {
        long offset = alloc(8);
        return Long.reverseBytes((long) L_HANDLE_V.get(array, (int) offset));
    }

    public float readFloat() throws IOException {
        return Float.intBitsToFloat(readInt());
    }

    public double readDouble() throws IOException {
        return Double.longBitsToDouble(readLong());
    }

    public String readLine() throws IOException {
//        for (long ptr = offset; ptr < limit; ptr++) {
//            if (unsafe.getByte(array, ptr) == '\n') {
//                int length = (int) (ptr - offset);
//                int cr = length > 0 && unsafe.getByte(array, ptr - 1) == '\r' ? 1 : 0;
//                return Utf8.read(array, alloc(length + 1), length - cr);
//            }
//        }
//
//        // The last line without CR
//        int length = (int) (limit - offset);
//        return length > 0 ? Utf8.read(array, alloc(length), length) : null;
        return null;
    }

    public String readUTF() throws IOException {
        int length = readUnsignedShort();
        if (length == 0) {
            return "";
        }
        if (length > 0x7fff) {
            length = (length & 0x7fff) << 16 | readUnsignedShort();
        }
        long offset = alloc(length);
        return Utf8.read(array, offset, length);
    }

    public Object readObject() throws IOException, ClassNotFoundException {
        Serializer serializer;
        byte b = readByte();
        if (b >= 0) {
            offset--;
            serializer = Repository.requestSerializer(readLong());
        } else if (b <= FIRST_BOOT_UID) {
            serializer = Repository.requestBootstrapSerializer(b);
        } else {
            return b == REF_NULL ? null : readRef(b);
        }
        return serializer.read(this);
    }

    protected Object readRef(byte tag) throws IOException, ClassNotFoundException {
        throw new IOException("Invalid reference tag: " + tag);
    }

    public void read(ByteBuffer dst) throws IOException {
        int len = dst.remaining();
        long offset = alloc(len);
        MemorySegment dstSegment = MemorySegment.ofBuffer(dst);
//        MemorySegment.copy(segment, JAVA_BYTE, offset, dstSegment, JAVA_BYTE, 0, len);
        dst.position(dst.limit());
    }

    public void readTo(long address, int len) throws IOException {
        long offset = alloc(len);
        unsafe.copyMemory(array, offset, null, address, len);
    }

    public ByteBuffer byteBuffer(int len) throws IOException {
        long offset = alloc(len);
        if (array != null) {
            return ByteBuffer.wrap(array, (int) (offset), len);
        } else {
            return DirectMemory.wrap(offset, len);
        }
    }

    public int available() {
        return (int) (limit - offset);
    }

    public void flush() throws IOException {
        // Nothing to do
    }

    public void close() throws IOException {
        // Nothing to do
    }

    public void register(Object obj) {
        // Nothing to do
    }

    public Closeable newScope() {
        return null;
    }

    protected long alloc(int size) throws IOException {
        long currentOffset = offset;
        if ((offset = currentOffset + size) > limit) {
            throw new IndexOutOfBoundsException();
        }
        return currentOffset;
    }
}
