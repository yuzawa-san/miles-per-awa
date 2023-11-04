/*
 * Copyright (c) 2022-2023 James Yuzawa (https://www.jyuzawa.com/)
 * All rights reserved. Licensed under the MIT License.
 */
package com.jyuzawa.miles_per_awa.controller;

import com.jyuzawa.miles_per_awa.dto.LocationsRequest;
import com.jyuzawa.miles_per_awa.dto.LocationsResponse;
import com.jyuzawa.miles_per_awa.dto.LocationsResponse.PersonLocation;
import com.jyuzawa.miles_per_awa.dto.LocationsResponse.PersonLocation.PersonLocationBuilder;
import com.jyuzawa.miles_per_awa.dto.RouteResponse;
import com.jyuzawa.miles_per_awa.entity.CalculatedPosition;
import com.jyuzawa.miles_per_awa.service.RoutePointsService;
import com.jyuzawa.miles_per_awa.service.RouteService;
import com.jyuzawa.miles_per_awa.service.VelocityService;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class MainController {
    private final RoutePointsService routePointsService;
    private final RouteService routeService;
    private final VelocityService velocityService;

    @GetMapping("/route")
    public ResponseEntity<RouteResponse> route() {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(6, TimeUnit.HOURS))
                .lastModified(routePointsService.getLastModified())
                .body(RouteResponse.builder()
                        .intervalMeters(RouteService.INTERVAL_METERS)
                        .rawPath(routeService.getRawPath())
                        .build());
    }

    @PostMapping("/locations")
    public LocationsResponse locations(@RequestBody LocationsRequest in) {
        List<PersonLocation> personLocations = velocityService.getUsers(in.getPeople()).entrySet().stream()
                .map(entry -> {
                    CalculatedPosition calculatedPosition = entry.getValue();
                    PersonLocationBuilder person = PersonLocation.builder()
                            .name(entry.getKey())
                            .lat(calculatedPosition.getLatitude())
                            .lon(calculatedPosition.getLongitude())
                            .timestampMs(calculatedPosition.getPositionTimestampSeconds() * 1000L);
                    if (calculatedPosition.isHasVelocity()) {
                        person.index(calculatedPosition.getIndex())
                                .indexTimestampMs(calculatedPosition.getTimestampSeconds() * 1000L)
                                .velocity(calculatedPosition.getVelocity());
                    }
                    return person.build();
                })
                .toList();
        return LocationsResponse.builder().people(personLocations).build();
    }
}
