package one.nio.serial;

import one.nio.util.Utf8;

import java.io.IOException;
import java.math.BigInteger;

public class BigIntegerSerializer extends Serializer<BigInteger> {

//    private static final VarHandle MODIFIERS;
//    private static final VarHandle SIGNUM;
//    private static final VarHandle MAG;
//    private static final VarHandle BIT_COUNT_PLUS_ONE;
//    private static final VarHandle BIT_LENGTH_PLUS_ONE;
//    private static final VarHandle LOWEST_SET_BIT_PLUS_TWO;
//    private static final VarHandle FIRST_NONZERO_INT_NUM_PLUS_TWO;
//
//    static {
//        try {
//            var look = MethodHandles.privateLookupIn(Field.class, MethodHandles.lookup());
////            MODIFIERS = look.findVarHandle(Field.class, "modifiers", int.class);
//            MethodHandles.Lookup lookup = MethodHandles.lookup();
////            Field modifiersField = Field.class.getDeclaredField("modifiers");
//
//            Field signum = BigInteger.class.getDeclaredField("signum");
////            var t = look.unreflectSetter(signum);
////            t.invoke(signum, signum.getModifiers() & ~FINAL);
//
//            signum.setAccessible(true);
////            signum.setInt(signum, signum.getModifiers() & ~FINAL);
////            MODIFIERS.set(signum, signum.getModifiers() & ~Modifier.FINAL);
//            SIGNUM = MethodHandles.privateLookupIn(BigInteger.class, MethodHandles.lookup()).unreflectVarHandle(signum);
//
//            Field mag = BigInteger.class.getDeclaredField("mag");
//            mag.setAccessible(true);
////            mag.setInt(signum, signum.getModifiers() & ~FINAL);
//            MAG = lookup.unreflectVarHandle(mag);
//
//            Field bitCountPlusOne = BigInteger.class.getDeclaredField("bitCountPlusOne");
//            bitCountPlusOne.setAccessible(true);
//            bitCountPlusOne.setInt(signum, signum.getModifiers() & ~FINAL);
//            BIT_COUNT_PLUS_ONE = lookup.unreflectVarHandle(mag);
//
//            Field bitLengthPlusOne = BigInteger.class.getDeclaredField("bitLengthPlusOne");
//            bitLengthPlusOne.setAccessible(true);
//            bitLengthPlusOne.setInt(signum, signum.getModifiers() & ~FINAL);
//            BIT_LENGTH_PLUS_ONE = lookup.unreflectVarHandle(mag);
//
//            Field lowestSetBitPlusTwo = BigInteger.class.getDeclaredField("lowestSetBitPlusTwo");
//            lowestSetBitPlusTwo.setAccessible(true);
//            lowestSetBitPlusTwo.setInt(signum, signum.getModifiers() & ~FINAL);
//            LOWEST_SET_BIT_PLUS_TWO = lookup.unreflectVarHandle(mag);
//
//            Field firstNonzeroIntNumPlusTwo = BigInteger.class.getDeclaredField("firstNonzeroIntNumPlusTwo");
//            firstNonzeroIntNumPlusTwo.setAccessible(true);
//            firstNonzeroIntNumPlusTwo.setInt(signum, signum.getModifiers() & ~FINAL);
//            FIRST_NONZERO_INT_NUM_PLUS_TWO = lookup.unreflectVarHandle(mag);
//

    /// /            SIGNUM = MethodHandles.privateLookupIn(BigInteger.class, MethodHandles.lookup())
    /// /                    .findVarHandle(BigInteger.class, "signum", int.class);
    /// /            MAG = MethodHandles.privateLookupIn(BigInteger.class, MethodHandles.lookup())
    /// /                    .findVarHandle(BigInteger.class, "mag", int[].class);
    /// /            BIT_COUNT_PLUS_ONE = MethodHandles.privateLookupIn(BigInteger.class, MethodHandles.lookup())
    /// /                    .findVarHandle(BigInteger.class, "bitCountPlusOne", int.class);
    /// /            BIT_LENGTH_PLUS_ONE = MethodHandles.privateLookupIn(BigInteger.class, MethodHandles.lookup())
    /// /                    .findVarHandle(BigInteger.class, "bitLengthPlusOne", int.class);
    /// /            LOWEST_SET_BIT_PLUS_TWO = MethodHandles.privateLookupIn(BigInteger.class, MethodHandles.lookup())
    /// /                    .findVarHandle(BigInteger.class, "lowestSetBitPlusTwo", int.class);
    /// /            FIRST_NONZERO_INT_NUM_PLUS_TWO = MethodHandles.privateLookupIn(BigInteger.class, MethodHandles.lookup())
    /// /                    .findVarHandle(BigInteger.class, "firstNonzeroIntNumPlusTwo", int.class);
    /// /            Field field = BigInteger.class.getDeclaredField("signum");
//        } catch (ReflectiveOperationException e) {
//            throw new ExceptionInInitializerError(e);
//        } catch (Throwable e) {
//            throw new RuntimeException(e);
//        }
//    }
    protected BigIntegerSerializer() {
        super(BigInteger.class);
    }

    @Override
    public void calcSize(BigInteger value, CalcSizeStream css) throws IOException {
        css.count += Utf8.length(value.toString());
    }

    @Override
    public void write(BigInteger value, DataStream out) throws IOException {
        out.writeUTF(value.toString());
    }

    @Override
    public BigInteger read(DataStream in) throws IOException, ClassNotFoundException {
        BigInteger value = new BigInteger((String) in.readUTF());
        return value;
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
