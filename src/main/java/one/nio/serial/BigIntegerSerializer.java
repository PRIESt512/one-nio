package one.nio.serial;

import one.nio.util.JavaInternals;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.math.BigInteger;

public class BigIntegerSerializer extends Serializer<BigInteger> {

    private static final MethodHandle SIGNUM_SET;
    private static final MethodHandle MAG_SET;
    private static final MethodHandle BIT_COUNT_PLUS_ONE_SET;
    private static final MethodHandle BIT_LENGTH_PLUS_ONE_SET;
    private static final MethodHandle LOWEST_SET_BIT_PLUS_TWO_SET;
    private static final MethodHandle FIRST_NONZERO_INT_NUM_PLUS_TWO_SET;

    private static final MethodHandle SIGNUM_GET;
    private static final MethodHandle MAG_GET;
    private static final MethodHandle BIT_COUNT_PLUS_ONE_GET;
    private static final MethodHandle BIT_LENGTH_PLUS_ONE_GET;
    private static final MethodHandle LOWEST_SET_BIT_PLUS_TWO_GET;
    private static final MethodHandle FIRST_NONZERO_INT_NUM_PLUS_TWO_GET;

    static {
        try {
            MethodHandles.Lookup look = MethodHandles.lookup();

            Field signum = BigInteger.class.getDeclaredField("signum");
            signum.setAccessible(true);
            SIGNUM_SET = look.unreflectSetter(signum);
            SIGNUM_GET = look.unreflectGetter(signum);

            Field mag = BigInteger.class.getDeclaredField("mag");
            mag.setAccessible(true);
            MAG_SET = look.unreflectSetter(mag);
            MAG_GET = look.unreflectGetter(mag);

            Field bitCountPlusOne = BigInteger.class.getDeclaredField("bitCountPlusOne");
            bitCountPlusOne.setAccessible(true);
            BIT_COUNT_PLUS_ONE_SET = look.unreflectSetter(bitCountPlusOne);
            BIT_COUNT_PLUS_ONE_GET = look.unreflectGetter(bitCountPlusOne);

            Field bitLengthPlusOne = BigInteger.class.getDeclaredField("bitLengthPlusOne");
            bitLengthPlusOne.setAccessible(true);
            BIT_LENGTH_PLUS_ONE_SET = look.unreflectSetter(bitLengthPlusOne);
            BIT_LENGTH_PLUS_ONE_GET = look.unreflectGetter(bitLengthPlusOne);

            Field lowestSetBitPlusTwo = BigInteger.class.getDeclaredField("lowestSetBitPlusTwo");
            lowestSetBitPlusTwo.setAccessible(true);
            LOWEST_SET_BIT_PLUS_TWO_SET = look.unreflectSetter(lowestSetBitPlusTwo);
            LOWEST_SET_BIT_PLUS_TWO_GET = look.unreflectGetter(lowestSetBitPlusTwo);

            Field firstNonzeroIntNumPlusTwo = BigInteger.class.getDeclaredField("firstNonzeroIntNumPlusTwo");
            firstNonzeroIntNumPlusTwo.setAccessible(true);
            FIRST_NONZERO_INT_NUM_PLUS_TWO_SET = look.unreflectSetter(firstNonzeroIntNumPlusTwo);
            FIRST_NONZERO_INT_NUM_PLUS_TWO_GET = look.unreflectGetter(firstNonzeroIntNumPlusTwo);
        } catch (ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    protected BigIntegerSerializer() {
        super(BigInteger.class);
    }

    @Override
    public void calcSize(BigInteger value, CalcSizeStream css) throws IOException {
        try {
            css.writeObject((int[]) MAG_GET.invokeExact(value));
            css.count += 20;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void write(BigInteger value, DataStream out) throws IOException {
        try {
            out.writeInt((int) SIGNUM_GET.invokeExact(value));
            out.writeObject((int[]) MAG_GET.invokeExact(value));
            out.writeInt((int) BIT_COUNT_PLUS_ONE_GET.invokeExact(value));
            out.writeInt((int) BIT_LENGTH_PLUS_ONE_GET.invokeExact(value));
            out.writeInt((int) LOWEST_SET_BIT_PLUS_TWO_GET.invokeExact(value));
            out.writeInt((int) FIRST_NONZERO_INT_NUM_PLUS_TWO_GET.invokeExact(value));
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public BigInteger read(DataStream in) throws IOException, ClassNotFoundException {
        try {
            BigInteger value = (BigInteger) JavaInternals.unsafe.allocateInstance(BigInteger.class);
            SIGNUM_SET.invokeExact(value, in.readInt());
            MAG_SET.invokeExact(value, (int[]) in.readObject());
            BIT_COUNT_PLUS_ONE_SET.invokeExact(value, in.readInt());
            BIT_LENGTH_PLUS_ONE_SET.invokeExact(value, in.readInt());
            LOWEST_SET_BIT_PLUS_TWO_SET.invokeExact(value, in.readInt());
            FIRST_NONZERO_INT_NUM_PLUS_TWO_SET.invokeExact(value, in.readInt());
            return value;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void skip(DataStream in) throws IOException, ClassNotFoundException {
        in.skipBytes(in.readInt());
    }

    @Override
    public void toJson(BigInteger obj, StringBuilder builder) throws IOException {

    }

    @Override
    public BigInteger fromJson(JsonReader in) throws IOException, ClassNotFoundException {
        return null;
    }
}
