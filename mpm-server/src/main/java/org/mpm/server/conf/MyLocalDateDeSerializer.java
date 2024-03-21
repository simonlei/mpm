package org.mpm.server.conf;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MyLocalDateDeSerializer extends JsonDeserializer<Object> {

    private static final String dateFormat = "yyyy-MM-dd";
    private static final String dateTimeFormat = "yyyy-MM-dd HH:mm:ss";

    @Override
    public Object deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
            throws IOException, JsonProcessingException {
        try {
            LocalDateDeserializer deserializer = new LocalDateDeserializer(DateTimeFormatter.ofPattern(dateFormat));
            return deserializer.deserialize(jsonParser, deserializationContext);
        } catch (IOException e) {
            LocalDateTimeDeserializer deserializer = new LocalDateTimeDeserializer(
                    DateTimeFormatter.ofPattern(dateTimeFormat));
            LocalDateTime deserialize = deserializer.deserialize(jsonParser, deserializationContext);
            return deserialize.toLocalDate();
        }
    }
}
