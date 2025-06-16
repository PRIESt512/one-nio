package one.nio.serial;

import one.nio.util.Utf8;

import java.io.IOException;
import java.math.BigDecimal;

public class BigDecimalSerializer extends Serializer<BigDecimal> {

    protected BigDecimalSerializer() {
        super(BigDecimal.class);
    }

    @Override
    public void calcSize(BigDecimal value, CalcSizeStream css) throws IOException {
        css.count += Utf8.length(value.toString());
    }

    @Override
    public void write(BigDecimal obj, DataStream out) throws IOException {
        out.writeUTF(obj.toString());
    }

    @Override
    public BigDecimal read(DataStream in) throws IOException, ClassNotFoundException {
        BigDecimal value = new BigDecimal(in.readUTF());
        return value;
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
