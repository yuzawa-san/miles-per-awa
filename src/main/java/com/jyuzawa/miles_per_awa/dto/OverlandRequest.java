/*
 * Copyright (c) 2022 James Yuzawa (https://www.jyuzawa.com/)
 * All rights reserved. Licensed under the MIT License.
 */
package com.jyuzawa.miles_per_awa.dto;

import com.jyuzawa.miles_per_awa.controller.LatLng;
import com.jyuzawa.miles_per_awa.model.Datapoint;
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
            double heading = OverlandGeometry.bearingInDegrees(start.getGeometry(), geometry);
            return Datapoint.builder()
                    .timestamp(properties.getTimestamp())
                    .coords(new LatLng(geometry.getLatitude(), geometry.getLongitude()))
                    .heading(heading)
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

        static double bearingInRadians(OverlandGeometry src, OverlandGeometry dst) {
            double srcLat = Math.toRadians(src.getLatitude());
            double dstLat = Math.toRadians(dst.getLatitude());
            double dLng = Math.toRadians(dst.getLongitude() - src.getLongitude());

            return Math.atan2(
                    Math.sin(dLng) * Math.cos(dstLat),
                    Math.cos(srcLat) * Math.sin(dstLat) - Math.sin(srcLat) * Math.cos(dstLat) * Math.cos(dLng));
        }

        static double bearingInDegrees(OverlandGeometry src, OverlandGeometry dst) {
            return Math.toDegrees((bearingInRadians(src, dst) + Math.PI) % Math.PI);
        }

        public double getLatitude() {
            return coordinates.get(0);
        }

        public double getLongitude() {
            return coordinates.get(1);
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
