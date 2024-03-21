package org.mpm.server.conf;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

public class MyLocalDateTimeDeSerializer extends JsonDeserializer<Object> {

    private static final String dateTimeFormat = "yyyy-MM-dd HH:mm:ss";
    private static final String dateTimeFormat2 = "yyyy-MM-dd'T'HH:mm:ss:SSSZ";

    @Override
    public Object deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        try {
            LocalDateTimeDeserializer deserializer = new LocalDateTimeDeserializer(
                    DateTimeFormatter.ofPattern(dateTimeFormat));
            return deserializer.deserialize(jsonParser, deserializationContext);
        } catch (IOException e) {
            LocalDateTimeDeserializer deserializer = new LocalDateTimeDeserializer(
                    DateTimeFormatter.ofPattern(dateTimeFormat2));
            return deserializer.deserialize(jsonParser, deserializationContext);
        }
    }
}
