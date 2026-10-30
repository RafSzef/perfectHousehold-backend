package com.perfecthousehold.generic.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;

/**
 * Base generic service providing direct DTO-free mapping from incoming JSON
 * (Strings, Maps, JsonNodes) directly to database entity models.
 */
public abstract class GenericService {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    protected ObjectMapper objectMapper;

    /**
     * Maps arbitrary incoming JSON (String, JsonNode, Map, byte[]) directly to
     * the target DB entity object without creating intermediate DTO classes.
     *
     * @param source      JSON payload (String, Map, JsonNode)
     * @param targetClass Target DB entity class (e.g. User.class)
     * @param <T>         Target entity type
     * @return Populated target entity instance
     */
    @SuppressWarnings("unchecked")
    public <T> T mapDbObject(Object source, Class<T> targetClass) {
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
    public <T> List<T> mapDbList(Object source, Class<T> elementClass) {
        if (source == null) {
            return Collections.emptyList();
        }

        try {
            var typeFactory = objectMapper.getTypeFactory();
            var listType = typeFactory.constructCollectionType(List.class, elementClass);

            if (source instanceof String str) {
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
