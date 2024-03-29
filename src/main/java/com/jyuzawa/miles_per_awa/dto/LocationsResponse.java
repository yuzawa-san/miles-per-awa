/*
 * Copyright (c) 2022-2023 James Yuzawa (https://www.jyuzawa.com/)
 * All rights reserved. Licensed under the MIT License.
 */
package com.jyuzawa.miles_per_awa.dto;

import java.util.List;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Builder
@Value
public final class LocationsResponse {
    private final List<PersonLocation> people;

    @Jacksonized
    @Builder
    @Value
    public static final class PersonLocation {
        private final String name;
        private final long timestampMs;
        private final double lat;
        private final double lon;
        private final long indexTimestampMs;
        private final int index;
        private final double velocity;
    }
}
