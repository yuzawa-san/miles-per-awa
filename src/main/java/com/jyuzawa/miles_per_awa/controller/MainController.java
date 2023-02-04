/*
 * Copyright (c) 2022 James Yuzawa (https://www.jyuzawa.com/)
 * All rights reserved. Licensed under the MIT License.
 */
package com.jyuzawa.miles_per_awa.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.jyuzawa.miles_per_awa.dto.LocationsRequest;
import com.jyuzawa.miles_per_awa.dto.LocationsResponse;
import com.jyuzawa.miles_per_awa.dto.LocationsResponse.PersonLocation;
import com.jyuzawa.miles_per_awa.dto.RouteResponse;
import com.jyuzawa.miles_per_awa.entity.Velocity;
import com.jyuzawa.miles_per_awa.service.RouteService;
import com.jyuzawa.miles_per_awa.service.VelocityService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
public class MainController {

    private final RouteService routeService;
    private final VelocityService velocityService;

    @GetMapping("/route")
    public RouteResponse route() {
        return RouteResponse.builder()
                .name(routeService.getName())
                .imperialUnits(routeService.isImperialUnits())
                .intervalMeters(routeService.getIntervalMeters())
                .rawPath(routeService.getRawPath())
                .build();
    }

    @PostMapping("/locations")
    public LocationsResponse locations(@RequestBody LocationsRequest in) {
        List<PersonLocation> personLocations = velocityService.getUsers(in.getPeople()).entrySet().stream()
                .map(entry -> {
                    Velocity velocity = entry.getValue();
                    return PersonLocation.builder()
                            .name(entry.getKey())
                            .index(velocity.index())
                            .timestampMs(velocity.timestamp().toEpochMilli())
                            .build();
                })
                .toList();
        return LocationsResponse.builder().people(personLocations).build();
    }
}
