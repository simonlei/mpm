package org.mpm.server.conf;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class LocalDateFormatConfiguration {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
        return builder -> {
            builder.deserializerByType(LocalDate.class, new MyLocalDateDeSerializer());
            builder.deserializerByType(LocalDateTime.class, new MyLocalDateTimeDeSerializer());
        };
    }
}