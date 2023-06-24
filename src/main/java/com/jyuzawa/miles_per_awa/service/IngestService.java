/*
 * Copyright (c) 2023 James Yuzawa (https://www.jyuzawa.com/)
 * All rights reserved. Licensed under the MIT License.
 */
package com.jyuzawa.miles_per_awa.service;

import com.jyuzawa.miles_per_awa.entity.CalculatedPosition;
import com.jyuzawa.miles_per_awa.entity.Datapoint;
import com.jyuzawa.miles_per_awa.entity.RoutePoint;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class IngestService {

    private final RouteService routePointService;
    private final VelocityService velocityService;

    public CalculatedPosition ingest(String user, Datapoint datapoint) {
        Optional<RoutePoint> closest = routePointService.getClosest(datapoint);
        return velocityService.calculate(user, datapoint, closest);
    }
}
