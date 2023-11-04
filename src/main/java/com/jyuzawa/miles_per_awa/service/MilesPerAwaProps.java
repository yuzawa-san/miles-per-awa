/*
 * Copyright (c) 2023 James Yuzawa (https://www.jyuzawa.com/)
 * All rights reserved. Licensed under the MIT License.
 */
package com.jyuzawa.miles_per_awa.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.TimeZone;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "route")
public class MilesPerAwaProps {
    @Getter
    private TimeZone timezone = TimeZone.getDefault();

    public void setTimezone(String timezone) {
        this.timezone = TimeZone.getTimeZone(timezone);
    }

    @Setter
    @Getter
    private List<Person> people = List.of();

    public record Person(String id, LocalDateTime startTime, Double speed, Duration pace, String paceUnit) {
        private static final double METERS_PER_KILOMETER = 1000d;
        private static final double METERS_PER_MILE = 1609.34d;
        private static final long SECONDS_PER_HOUR = Duration.ofHours(1).getSeconds();

        public double getVelocity() {
            if (paceUnit.startsWith("mi")) {
                return METERS_PER_MILE / pace.getSeconds();
            } else if (paceUnit.startsWith("kilo") || paceUnit.equalsIgnoreCase("km")) {
                return METERS_PER_KILOMETER / pace.getSeconds();
            } else if (paceUnit.equals("mph")) {
                return METERS_PER_MILE / SECONDS_PER_HOUR * speed;
            } else if (paceUnit.equals("kph")) {
                return METERS_PER_KILOMETER / SECONDS_PER_HOUR * speed;
            }
            throw new IllegalArgumentException("paceUnit " + paceUnit + " is not supported");
        }

        public String getPaceInfo() {
            if (paceUnit.endsWith("ph")) {
                return speed + " " + paceUnit;
            }
            return pace.toMinutes() + "m" + pace.toSecondsPart() + "s per " + paceUnit;
        }
    }
}
