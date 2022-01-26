package edu.colorado.cires.cmg.s3cfutils.framework;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Custom implementation of ObjectMapperCreator. Serializes Double values with {@link DoubleSerializer}
 */
public final class ObjectMapperCreator {
    public static ObjectMapper create() {
        SimpleModule module = new SimpleModule();
        module.addSerializer(Double.class, new DoubleSerializer());
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.registerModule(module);
        objectMapper.disable(new MapperFeature[]{MapperFeature.DEFAULT_VIEW_INCLUSION});
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        objectMapper.setSerializationInclusion(Include.NON_NULL);
        return objectMapper;
    }

    private ObjectMapperCreator() {
    }
}
