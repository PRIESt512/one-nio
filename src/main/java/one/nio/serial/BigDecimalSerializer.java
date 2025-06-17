package one.nio.serial;

import one.nio.serial.gen.NullObjectOutputStream;
import one.nio.util.JavaInternals;

import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;

public class BigDecimalSerializer extends Serializer<BigDecimal> {

    private static final MethodHandle INT_VAL_SET;
    private static final MethodHandle SCALE_SET;

    private static final MethodHandle INT_VAL_GET;
    private static final MethodHandle SCALE_GET;

    static {
        try {
            var look = MethodHandles.privateLookupIn(BigDecimal.class, MethodHandles.lookup());

            Field intVal = BigDecimal.class.getDeclaredField("intVal");
            intVal.setAccessible(true);
            INT_VAL_SET = look.unreflectSetter(intVal);
            INT_VAL_GET = look.unreflectGetter(intVal);

            Field scale = BigDecimal.class.getDeclaredField("scale");
            scale.setAccessible(true);
            SCALE_SET = look.unreflectSetter(scale);
            SCALE_GET = look.unreflectGetter(scale);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    protected BigDecimalSerializer() {
        super(BigDecimal.class);
    }

    @Override
    public void calcSize(BigDecimal value, CalcSizeStream css) throws IOException {
        try {
            css.writeObject(NullObjectOutputStream.INSTANCE);
            css.writeObject((BigInteger) INT_VAL_GET.invokeExact(value));
            css.count += 4;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void write(BigDecimal value, DataStream out) throws IOException {
        try {
//            out.writeObject(NullObjectOutputStream.INSTANCE);
            out.writeObject((BigInteger) INT_VAL_GET.invokeExact(value));
            out.writeInt((int) SCALE_GET.invokeExact(value));
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public BigDecimal read(DataStream in) throws IOException, ClassNotFoundException {
        try {
            BigDecimal value = (BigDecimal) JavaInternals.unsafe.allocateInstance(BigDecimal.class);
            INT_VAL_SET.invokeExact(value, (BigInteger) in.readObject());
            SCALE_SET.invokeExact(value, in.readInt());
            return value;
        } catch (InstantiationException e) {
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
    public void toJson(BigDecimal obj, StringBuilder builder) throws IOException {

    }

    @Override
    public BigDecimal fromJson(JsonReader in) throws IOException, ClassNotFoundException {
        return null;
    }
}
