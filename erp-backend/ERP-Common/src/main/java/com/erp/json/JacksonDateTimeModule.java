package com.erp.json;

import tools.jackson.databind.ext.javatime.deser.LocalDateDeserializer;
import tools.jackson.databind.ext.javatime.deser.LocalDateTimeDeserializer;
import tools.jackson.databind.ext.javatime.deser.LocalTimeDeserializer;
import tools.jackson.databind.ext.javatime.ser.LocalDateSerializer;
import tools.jackson.databind.ext.javatime.ser.LocalDateTimeSerializer;
import tools.jackson.databind.ext.javatime.ser.LocalTimeSerializer;
import tools.jackson.databind.module.SimpleModule;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Jackson 3 module with the date/time formats the front end uses.
 * Registered as a bean (see WebMvcConfiguration); Spring Boot adds every JacksonModule bean
 * to the JsonMapper it uses for request and response bodies.
 * (Replaces JacksonObjectMapper: in Jackson 3 an ObjectMapper is immutable and built with a
 * builder, so it can no longer be configured by subclassing.)
 */
public class JacksonDateTimeModule extends SimpleModule {

    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    public static final String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm";
    public static final String DEFAULT_TIME_FORMAT = "HH:mm:ss";

    public JacksonDateTimeModule() {
        super("erp-date-time");
        DateTimeFormatter date = DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT);
        DateTimeFormatter dateTime = DateTimeFormatter.ofPattern(DEFAULT_DATE_TIME_FORMAT);
        DateTimeFormatter time = DateTimeFormatter.ofPattern(DEFAULT_TIME_FORMAT);

        addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(dateTime));
        addDeserializer(LocalDate.class, new LocalDateDeserializer(date));
        addDeserializer(LocalTime.class, new LocalTimeDeserializer(time));
        addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(dateTime));
        addSerializer(LocalDate.class, new LocalDateSerializer(date));
        addSerializer(LocalTime.class, new LocalTimeSerializer(time));
    }
}
