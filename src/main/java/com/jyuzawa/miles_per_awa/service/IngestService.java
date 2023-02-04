/*
 * Copyright (c) 2023 James Yuzawa (https://www.jyuzawa.com/)
 * All rights reserved. Licensed under the MIT License.
 */
package com.jyuzawa.miles_per_awa.service;

import com.jyuzawa.miles_per_awa.entity.Datapoint;
import com.jyuzawa.miles_per_awa.entity.Velocity;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class IngestService {

    private final RouteService routePointService;
    private final VelocityService velocityService;

    public Optional<Velocity> ingest(String user, Datapoint datapoint) {
        return routePointService
                .getClosest(datapoint)
                .map(closest -> velocityService.calculate(user, datapoint, closest.offset()));
    }
}
