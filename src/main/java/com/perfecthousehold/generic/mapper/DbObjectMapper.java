package com.perfecthousehold.generic.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Mapper component dedicated exclusively to deserializing PostgreSQL JSON query results
 * ({@code row_to_json} and {@code json_agg}) directly into database entity models.
 */
@Component
@RequiredArgsConstructor
public class DbObjectMapper {

    private final ObjectMapper objectMapper;

    /**
     * Maps a PostgreSQL {@code row_to_json} string directly to an {@link Optional} target entity.
     *
     * @param json        JSON string from {@code row_to_json}, may be {@code null} or blank
     * @param targetClass Target DB entity class (e.g. {@code User.class})
     * @param <T>         Target entity type
     * @return {@link Optional} containing the mapped entity, or {@link Optional#empty()} when no row was returned
     */
    public <T> Optional<T> map(String json, Class<T> targetClass) {
        if (isNullOrBlankJson(json)) {
            return Optional.empty();
        }

        try {
            return Optional.ofNullable(objectMapper.readValue(json, targetClass));
        } catch (IOException e) {
            throw new DbMappingException("Failed to map DB JSON to " + targetClass.getSimpleName(), e);
        }
    }

    /**
     * Maps a PostgreSQL {@code json_agg} string directly into a {@link List} of target entities.
     *
     * @param json         JSON array string from {@code json_agg}, may be {@code null} or blank
     * @param elementClass Target entity class
     * @param <T>          Element type
     * @return List of mapped entities, or an empty list when no rows were returned
     */
    public <T> List<T> mapList(String json, Class<T> elementClass) {
        if (isNullOrBlankJson(json)) {
            return List.of();
        }

        try {
            var listType = objectMapper.getTypeFactory().constructCollectionType(List.class, elementClass);
            List<T> result = objectMapper.readValue(json, listType);
            return result != null ? result : List.of();
        } catch (IOException e) {
            throw new DbMappingException("Failed to map DB JSON to List<" + elementClass.getSimpleName() + ">", e);
        }
    }

    private boolean isNullOrBlankJson(String json) {
        return json == null || json.isBlank() || "null".equals(json.trim());
    }
}
