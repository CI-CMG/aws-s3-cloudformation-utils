package edu.colorado.cires.cmg.awsdatautils.e2e.framework;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class DoubleSerializer extends StdSerializer<Double> {
    public DoubleSerializer() {
        this((Class)null);
    }

    public DoubleSerializer(Class<Double> t) {
        super(t);
    }

    public void serialize(Double value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        if (value == null) {
            gen.writeNull();
        } else {
            DecimalFormat format = new DecimalFormat("0.#################", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
            gen.writeNumber(format.format(value));
        }

    }
}
