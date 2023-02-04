/*
 * Copyright (c) 2022 James Yuzawa (https://www.jyuzawa.com/)
 * All rights reserved. Licensed under the MIT License.
 */
package com.jyuzawa.miles_per_awa.dto;

import java.time.Instant;
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
        private final int offsetMeters;
        private final float velocity;
        private final Instant timestamp;
    }
}
