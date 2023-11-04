/*
 * Copyright (c) 2023 James Yuzawa (https://www.jyuzawa.com/)
 * All rights reserved. Licensed under the MIT License.
 */
package com.jyuzawa.miles_per_awa.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.List;

@Converter(autoApply = true)
public class HistoryConverter implements AttributeConverter<List<RouteTuple>, String> {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final TypeReference<List<RouteTuple>> TYPE = new TypeReference<List<RouteTuple>>() {};

    @Override
    public String convertToDatabaseColumn(List<RouteTuple> entityValue) {
        if (entityValue == null) {
            return null;
        }
        try {
            return MAPPER.writeValueAsString(entityValue);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("failed to convert", e);
        }
    }

    @Override
    public List<RouteTuple> convertToEntityAttribute(String databaseValue) {
        if (databaseValue == null) {
            return null;
        }
        try {
            return MAPPER.readValue(databaseValue, TYPE);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("failed to convert", e);
        }
    }
}
