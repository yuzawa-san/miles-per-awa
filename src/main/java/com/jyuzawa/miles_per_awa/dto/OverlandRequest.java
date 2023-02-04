/*
 * Copyright (c) 2022 James Yuzawa (https://www.jyuzawa.com/)
 * All rights reserved. Licensed under the MIT License.
 */
package com.jyuzawa.miles_per_awa.dto;

import com.jyuzawa.miles_per_awa.entity.Datapoint;
import com.jyuzawa.miles_per_awa.entity.LatLng;
import java.time.Instant;
import java.util.List;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@Builder
@Value
public final class OverlandRequest {

    private final List<OverlandLocation> locations;

    @Jacksonized
    @Builder
    @Value
    public static final class OverlandLocation {
        private final String type;
        private final OverlandGeometry geometry;
        private final OverlandProperties properties;

        public boolean isValid() {
            return "Feature".equals(type)
                    && geometry != null
                    && geometry.isValid()
                    && properties != null
                    && properties.isValid();
        }

        public Datapoint toPoint(OverlandLocation start) {
            Instant startTimestamp = start.getProperties().getTimestamp();
            LatLng startLatLng = start.getGeometry().getLatLng();
            Instant timestamp = properties.getTimestamp();
            LatLng latLng = geometry.getLatLng();
            double heading = startLatLng.heading(latLng);
            return Datapoint.builder()
                    .timestamp(timestamp)
                    .coords(latLng)
                    .heading(heading)
                    .velocity(startLatLng.distance(latLng)
                            / (timestamp.getEpochSecond() - startTimestamp.getEpochSecond()))
                    .build();
        }
    }

    @Jacksonized
    @Builder
    @Value
    public static final class OverlandGeometry {
        private final String type;
        private final List<Double> coordinates;

        public boolean isValid() {
            return "Point".equals(type) && coordinates != null && coordinates.size() == 2;
        }

        public LatLng getLatLng() {
            return new LatLng(coordinates.get(0), coordinates.get(0));
        }
    }

    @Jacksonized
    @Builder
    @Value
    public static final class OverlandProperties {
        private final String device_id;
        private final Integer horizontal_accuracy;
        private final Instant timestamp;

        public boolean isValid() {
            return device_id != null && timestamp != null && horizontal_accuracy != null && horizontal_accuracy < 100;
        }
    }
}
