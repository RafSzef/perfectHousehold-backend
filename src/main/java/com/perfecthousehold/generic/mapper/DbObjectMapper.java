package com.perfecthousehold.generic.mapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;

/**
 * Reusable mapper component providing direct DTO-free mapping from incoming JSON
 * (Strings, Maps, JsonNodes, byte arrays, InputStreams) directly to database entity models.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DbObjectMapper {

    private final ObjectMapper objectMapper;

    /**
     * Maps arbitrary incoming JSON (String, JsonNode, Map, byte[], InputStream) directly to
     * the target DB entity object without creating intermediate DTO classes.
     *
     * @param source      JSON payload (String, Map, JsonNode, byte[], InputStream)
     * @param targetClass Target DB entity class (e.g. User.class)
     * @param <T>         Target entity type
     * @return Populated target entity instance, or {@code null} if {@code source} is {@code null}
     */
    @SuppressWarnings("unchecked")
    public <T> T map(Object source, Class<T> targetClass) {
        if (source == null) {
            return null;
        }

        if (targetClass.isInstance(source)) {
            return (T) source;
        }

        try {
            if (source instanceof String str) {
                return objectMapper.readValue(str, targetClass);
            } else if (source instanceof byte[] bytes) {
                return objectMapper.readValue(bytes, targetClass);
            } else if (source instanceof InputStream is) {
                return objectMapper.readValue(is, targetClass);
            } else if (source instanceof JsonNode node) {
                return objectMapper.treeToValue(node, targetClass);
            } else {
                return objectMapper.convertValue(source, targetClass);
            }
        } catch (Exception e) {
            log.error("Failed to map JSON payload to entity {}: {}", targetClass.getSimpleName(), e.getMessage());
            throw new RuntimeException("Failed to map JSON to " + targetClass.getSimpleName() + ": " + e.getMessage(), e);
        }
    }

    /**
     * Maps a JSON array or collection directly into a List of DB entity objects without DTOs.
     *
     * @param source       JSON array String or collection
     * @param elementClass Target entity class
     * @param <T>          Element type
     * @return List of DB entity objects
     */
    public <T> List<T> mapList(Object source, Class<T> elementClass) {
        if (source == null) {
            return Collections.emptyList();
        }

        try {
            var typeFactory = objectMapper.getTypeFactory();
            var listType = typeFactory.constructCollectionType(List.class, elementClass);

            if (source instanceof String str) {
                if (str.isBlank() || "null".equals(str.trim())) {
                    return Collections.emptyList();
                }
                return objectMapper.readValue(str, listType);
            } else {
                return objectMapper.convertValue(source, listType);
            }
        } catch (Exception e) {
            log.error("Failed to map JSON to List<{}>: {}", elementClass.getSimpleName(), e.getMessage());
            throw new RuntimeException("Failed to map JSON to List<" + elementClass.getSimpleName() + ">: " + e.getMessage(), e);
        }
    }
}
